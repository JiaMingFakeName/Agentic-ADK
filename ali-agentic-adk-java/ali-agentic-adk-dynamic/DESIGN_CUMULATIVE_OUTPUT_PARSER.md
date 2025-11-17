# 累积式输出解析器设计文档

## 1. 概述

### 1.1 问题描述
当前流式输出场景中，LLM 返回的是增量数据（incremental），例如：
- 第1次返回: "1"
- 第2次返回: "2"
- 第3次返回: "3"
- 第4次返回: "4"

需要实现一个支持流式处理的 OutputParser，将增量数据累积后返回：
- 第1次返回: "1"
- 第2次返回: "12"
- 第3次返回: "123"
- 第4次返回: "1234"

### 1.2 核心要求
- 支持流式处理 (`supportStream = true`)
- 内部维护状态，累积所有增量输出
- 当 `llmResponse.partial()` 不再为 `true` 时，清空内部状态，准备处理下一轮

---

## 2. 设计方案

### 2.1 架构设计

#### 2.1.1 新增累积式解析器
创建 `CumulativeOutputParser` 类，使用 `ThreadLocal` 管理内部状态：

```java
public class CumulativeOutputParser implements OutputParser<String> {
    private static final ThreadLocal<State> STATE = 
        ThreadLocal.withInitial(State::new);
    
    private static class State {
        StringBuilder buffer = new StringBuilder();
        boolean lastWasPartial = true;
    }
    
    @Override
    public String parse(String string) {
        return parse(null, string);
    }
    
    @Override
    public String parse(LlmResponse llmResponse, String string) {
        State state = STATE.get();
        
        boolean isPartial = llmResponse != null && Boolean.TRUE.equals(llmResponse.partial());
        
        if (!state.lastWasPartial) {
            state.buffer.setLength(0);
        }
        
        state.buffer.append(string);
        state.lastWasPartial = isPartial;
        
        if (!isPartial) {
            STATE.remove();
        }
        
        return state.buffer.toString();
    }
}
```

**设计要点**：
- 通过 `OutputParser` 接口新增的 `parse(LlmResponse, String)` 方法获取 `partial` 状态
- 内部通过 `lastWasPartial` 标记判断是否需要重置 buffer
- 当 `partial=false` 时，下次 parse 会自动清空累积状态
- **当 `partial=false` 时，立即清理 `ThreadLocal`，完全内部消化状态管理**
- 无需外部调用任何清理方法

### 2.2 状态管理机制

#### 2.2.1 ThreadLocal 生命周期
```
[线程开始] -> [parse累积] -> [partial=true继续] -> [partial=false标记] -> [自动清理ThreadLocal]
              ↓                                    ↓                      ↓
           buffer.append()                    lastWasPartial=false    STATE.remove()
```

#### 2.2.2 状态重置时机
- 每次调用 `parse()` 时累积到 `ThreadLocal` 中的 `StringBuilder`
- 检测到 `llmResponse.partial() == false` 时，标记 `lastWasPartial = false`
- 同时立即调用 `STATE.remove()` 清理 `ThreadLocal`，防止内存泄漏
- **完全自动化管理，无需外部干预**

### 2.3 集成方案

#### 2.3.1 OutputParser 接口扩展
在 `OutputParser` 接口中新增方法：

```java
public interface OutputParser<T> {
    T parse(String string);

    default T parse(LlmResponse llmResponse, String string) {
        return parse(string);
    }
}
```

#### 2.3.2 OutputParserExecutor 修改
在 `OutputParserExecutor.parseOutput()` 方法中调用新接口：

```java
switch (outputParser.getType()) {
    case "bean":
        OutputParser bean = SpringContextHolder.getBean(outputParser.getContent(), OutputParser.class);
        result = bean.parse(llmResponse, original);
        break;
    case "class":
        Class<?> clazz = Class.forName(outputParser.getContent());
        Object obj = clazz.newInstance();
        if(obj instanceof OutputParser){
            result = ((OutputParser)obj).parse(llmResponse, original);
        }
        break;
    // ...
}
```

**优势**：
- 不修改 `OutputParserExecutor` 核心逻辑，仅传递 `llmResponse` 参数
- `CumulativeOutputParser` 内部通过 `llmResponse.partial()` 自动判断并管理状态
- 当 `partial=false` 时自动清理 `ThreadLocal`，无需外部调用
- 其他 `OutputParser` 实现无需修改，使用默认方法兼容

#### 2.3.3 生命周期管理
- `ThreadLocal` 的创建、使用、清理全部由 `CumulativeOutputParser` 内部自动管理
- 无需外部关心任何清理逻辑

---

## 3. 详细实现计划

### 3.1 实现文件清单

| 文件 | 类型 | 说明 |
|------|------|------|
| `OutputParser.java` | 修改 | 新增 `parse(LlmResponse, String)` 方法 |
| `CumulativeOutputParser.java` | 新建 | 累积式解析器实现，使用 ThreadLocal 管理状态 |
| `OutputParserExecutor.java` | 修改 | 调用 `parse(llmResponse, original)` 传递完整上下文 |

### 3.2 ThreadLocal 管理方案

```java
public class CumulativeOutputParser implements OutputParser<String> {
    private static final ThreadLocal<State> STATE = 
        ThreadLocal.withInitial(State::new);
    
    private static class State {
        StringBuilder buffer = new StringBuilder();
        boolean lastWasPartial = true;
    }
    
    @Override
    public String parse(String string) {
        return parse(null, string);
    }
    
    @Override
    public String parse(LlmResponse llmResponse, String string) {
        State state = STATE.get();
        
        boolean isPartial = llmResponse != null && Boolean.TRUE.equals(llmResponse.partial());
        
        if (!state.lastWasPartial) {
            state.buffer.setLength(0);
        }
        
        state.buffer.append(string);
        state.lastWasPartial = isPartial;
        
        if (!isPartial) {
            STATE.remove();
        }
        
        return state.buffer.toString();
    }
}
```

**优势**：
- 利用 `OutputParser` 新增的 `parse(LlmResponse, String)` 方法获取 `partial` 状态
- 无需修改 `OutputParserExecutor` 添加特殊判断逻辑
- 线程隔离自动保证并发安全
- **状态管理和 ThreadLocal 清理完全内部化，无需外部调用任何方法**

### 3.3 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| 首次接收增量数据 | 初始化累积器，直接添加 |
| 中间增量数据 | 追加到已有累积结果 |
| 最后一次数据 (`partial=false`) | 返回完整累积结果后清空状态 |
| 连续两次非 partial 响应 | 每次都作为独立完整响应处理 |
| 空字符串增量 | 正常累积，保持状态 |
| 异常中断 | 需超时机制或显式清理 |

---

## 4. 示例用法

### 4.1 配置示例
```java
OutputParserDefine define = OutputParserDefine.builder()
    .type("class")
    .content("com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser")
    .supportStream(true)
    .build();
```

### 4.2 使用流程
```
LLM 流式输出: "1" -> "2" -> "3" -> "4"
              ↓      ↓      ↓      ↓
              partial=true  ...    partial=false
              ↓      ↓      ↓      ↓
累积解析器:   "1" -> "12" -> "123" -> "1234"
              ↓      ↓      ↓      ↓
                                   [清空状态]
```

---

## 5. 测试计划

### 5.1 单元测试用例
- [ ] 测试基本累积功能
- [ ] 测试 partial=false 时下次 parse 自动重置
- [ ] 测试连续多轮流式输出
- [ ] 测试空字符串累积
- [ ] 测试并发场景（多线程隔离）

### 5.2 集成测试
- [ ] 与实际 LLM 流式输出集成
- [ ] 验证 CallbackContext 状态管理
- [ ] 性能测试（大量累积数据）

---

## 6. 风险与注意事项

### 6.1 内存管理
- 累积大量数据可能导致内存占用过高
- 建议：
  - 增加最大累积长度限制
  - 实现自动清理机制（超时未收到 partial=false）

### 6.2 并发安全
- `ThreadLocal` 自动保证线程隔离
- 单个线程内串行处理流式响应

### 6.3 状态清理
- `ThreadLocal` 在检测到 `partial=false` 时自动清理（调用 `STATE.remove()`）
- **完全内部自动化，无需外部调用任何清理方法**
- 避免了线程池场景下的内存泄漏风险

---

## 7. 总结

本设计通过以下方式实现流式输出的累积解析：

1. **接口扩展**：在 `OutputParser` 接口新增 `parse(LlmResponse, String)` 方法，允许解析器获取 `partial` 状态
2. **内部状态管理**：`CumulativeOutputParser` 使用 `ThreadLocal<State>` 管理累积缓冲区和 `lastWasPartial` 标记
3. **自动清理机制**：通过 `llmResponse.partial()` 自动判断，当 `partial=false` 时立即清理 `ThreadLocal`
4. **无侵入集成**：`OutputParserExecutor` 仅需传递 `llmResponse` 参数，无需添加特殊判断逻辑

**核心优势**：状态管理和 `ThreadLocal` 清理完全封装在 `CumulativeOutputParser` 内部，外部调用者无需关心任何清理逻辑。

---

