# 阿里智能体开发工具包-Dynamic (Ali-Agent ADK Dynamic)

[English Version](README-CN.md)

## 项目介绍

阿里智能体开发工具包-Dynamic (Ali-Agent ADK Dynamic) 提供了最小粒度的单次LLM调用场景下，需要原子化共同变更的配置集合。
使得高频的迭代实验可以通过不同于稳定业务基础代码的发布节奏 变更迭代。 它是比普通Prompt管理系统更完整的升级。

具体来说：需要一同变更的内容除了prompt模版以外， 还有模型配置，输出变量的解析器，输入变量的格式化器。 我们称之为四元组。如果不能原子化变更，线上系统会出现不匹配错配时的bug。
    模型配置：因为prompt和模型是息息相关的，不同的模型适配的prompt大有不同，尤其是大模型切小模型的场景。
    输出变量的解析器：同样的，输出解析器也与prompt关系很大。json格式遵守得好的模型和其它靠 让模型锁定前几个token输出的套路解析就很不一样。
    输入变量的格式化器：系统中的变量如何变成文本，也有可能和模型/prompt息息相关。如在裸模型流行时代，每个模型的history都需要format成不同格式，FunctionCall的结果格式也各有不同。还有对输入变量的细节控制（如商品函数返回值的对象，如何针对不同的prompt裁剪）

为此，我们设计了对这四项内容的配置定义，以及执行器的实现。
基于Google-ADK LLMAgent，我们继承了DynamicLLMAgent，增加了这个AtomicPromptTemplateDefine定义。里面的动态配置是可选的（多数prompt配置后台没有完整的能力）。
每一个动态配置都会覆盖LLMAgent对应的原始配置。当配置了prompt模版时，会废弃原LLMAgent的多agent transfer能力（因为它是基于prompt实现的）



## 项目核心概览

### 核心概念：四元组配置

ADK-Dynamic 的核心设计思想是"原子化四元组"，即**单次LLM调用所需的四个必须同步变更的配置**：

1. **Prompt模板** (ChatPromptTemplateDefine / RawPromptTemplateDefine)
2. **模型配置** (ModelDefine)
3. **输出解析器** (OutputParserDefine)
4. **输入格式化器** (InputFormatterDefine)

这四项配置通过 `AtomicPromptTemplateDefine` 进行原子化管理，确保线上系统不会因配置不匹配而出错。

AtomicPromptTemplateDefine通过 AtomicPromptTemplateGetter 每次动态获取，运行时时时可改变。

DymanicLlmAgent继承自LlmAgent,支持了该参数的设置。

### 核心领域对象层次结构

```
AtomicPromptTemplateDefine (顶层容器)
├── ChatPromptTemplateDefine (Messages API模板) 【二选一】
│   ├── instructionTemplate: List<String>
│   ├── preHistoryTemplate: List<MessageTemplateDefine>
│   │   └── MessageTemplateDefine (消息模板)
│   │       ├── type: text/partial_text/blob/file
│   │       ├── mineType: MIME类型
│   │       ├── template: 模板内容
│   │       └── role: system/user/assistant
│   ├── historyFormatter: HistoryFormatterDefine
│   │   ├── type: 格式化器类型
│   │   ├── content: 格式化器内容
│   │   └── params: 格式化参数
│   └── userTemplate: List<MessageTemplateDefine>
│
├── RawPromptTemplateDefine (原始模板) 【二选一】
│   ├── totalPromptTemplate: String
│   └── historyFormatter: HistoryFormatterDefine
│
├── syntaxType: SyntaxType (语法类型枚举)
│   └── SIMPLE / HANDLEBARS / STRICT / COMMENT
│
├── model: ModelDefine (模型配置)
│   ├── name: 模型名称
│   ├── identifier: 模型标识符
│   └── extraConfigs: 扩展配置
│
├── outputParser: OutputParserDefine (输出解析器)
│   ├── type: 解析器类型
│   ├── content: 解析器内容
│   ├── mergeIncremental: 是否合并增量
│   └── params: 解析器参数
│
├── inputFormatter: List<InputFormatterDefine> (输入格式化器)
│   └── InputFormatterDefine
│       ├── type: 格式化器类型
│       ├── content: 格式化器内容
│       ├── inputVariableName: 输入变量名
│       └── params: 格式化参数
│
└── extraConfigs: Map<String, Object> (业务扩展配置)
```

### 核心对象详解

#### 第一层：顶层容器

##### AtomicPromptTemplateDefine（顶层容器）
**作用：** 整个动态配置的原子单元，包含四元组的所有配置。

**核心字段：**
- `chatPromptTemplate` / `rawPromptTemplate`: **二选一**的模板定义方式（chatPromptTemplate优先级更高）
- `syntaxType`: 模板变量的语法类型
- `model`: 模型配置（与prompt强关联）
- `outputParser`: 输出解析器（与模型输出能力强关联）
- `inputFormatter`: 输入格式化器列表（与prompt输入格式强关联）
- `extraConfigs`: 业务扩展配置


---

#### 第二层：Prompt模板（二选一）

##### 2.1 ChatPromptTemplateDefine（Message模板）
**作用：** OpenAI风格的多轮对话模板，支持角色分离。

**适用场景：** 对于新流行的OpenAI风格的Message API。
该类API预定义了List<Message>的接口，无法在模版中表达History的处理。因而需把历史消息的处理方式单独拎出来。
引申带来的问题就是history前后也需要两个列表的提示词模版。当没有history时，这两个模版倒是没什么区别了。

**结构组成：**
- `instructionTemplate`: 系统指令模版（system角色）
- `preHistoryTemplate`: 历史消息之前的提示词模版
  - 类型：`List<MessageTemplateDefine>` → 详见第三层
- `historyFormatter`: 历史消息格式化器
  - 类型：`HistoryFormatterDefine` → 详见第三层
- `userTemplate`: 用户消息提示词模版
  - 类型：`List<MessageTemplateDefine>` → 详见第三层

##### 2.2 RawPromptTemplateDefine（原始模板）
**作用：** ChatML格式的裸提示词模板。

**适用场景：** 极简User提示词，或者单框配置后台

**结构组成：**
- `totalPromptTemplate`: 完整的提示词字符串（支持`{history}`变量分割历史前后）
其格式如下：（若不符合格式，则简单填入一条user prompt）
```
<|im_start|>system
SystemTemplate here,You are {role}
<|im_end|>
<|im_start|>user
Prehistory Template here
<|im_end|>
<|im_start|>assistant
Prehistory Template here
<|im_end|>
{history}
<|im_start|>user
user template here
{var}
<|im_end|>
<|im_start|>image_url
http://mypicurl
<|im_end|>
<|im_start|>video
http://myvideourl
<|im_end|>
<|im_start|>audio
http://myaudiourl
<|im_end|>
<|im_start|>assistant
prefill if support

```
- `historyFormatter`: 历史消息格式化器
  - 类型：`HistoryFormatterDefine` → 详见第三层

---

#### 第三层：Prompt模板子组件

##### 3.1 MessageTemplateDefine（消息模板）
**位置：** 被 `ChatPromptTemplateDefine` 的 `preHistoryTemplate` 和 `userTemplate` 引用

**作用：** 定义单条消息的内容和类型，支持多模态。

**核心字段：**
- `type`: 消息类型
  - `TYPE_TEXT`: 完整文本
  - `TYPE_PARTIAL_TEXT`: 前缀文本（后续可拼接）
  - `TYPE_BLOB`: 二进制对象（图片、音频等）
  - `TYPE_FILE`: 文件
- `mineType`: MIME类型（如"image/png"）
- `template`: 模板内容（文本类型支持变量替换，非文本类型不支持）
- `role`: 消息角色（system/user/model）

**使用场景：** 构建多模态消息、区分角色的对话内容。

##### 3.2 HistoryFormatterDefine（历史格式化器）
**位置：** 被 `ChatPromptTemplateDefine` 和 `RawPromptTemplateDefine` 引用

**作用：** 控制历史消息如何处理。

**核心字段：**
- `type`: 格式化器类型（class/groovy等）
- `content`: 格式化器内容（类名或脚本）
- `params`: 格式化参数（如`maxCount`限制条数）

**典型实现：** `LimitCountHistoryFormatter`（限制历史消息条数）

**使用场景：** 控制上下文窗口大小、压缩历史消息。

---

#### 第二层：模板语法

##### SyntaxType（语法类型枚举）
**作用：** 定义模板中变量替换的语法规则。

**可选值：**
- `SIMPLE`: `{variable}` - 简单语法
- `HANDLEBARS`: `{{variable}}` - Handlebars语法
- `STRICT`: `${!variable}` - 严格语法
- `COMMENT`: `##variable##` - 注释语法

---

#### 第二层：模型配置

##### ModelDefine（模型配置）
**作用：** 定义AI模型及其参数。

**核心字段：**
- `name`: 模型名称（如"gpt-4"、"qwen-max"），可能影响模型参数配置
- `identifier`: 模型唯一标识符，用于在具体的系统中取到，如：dashscope_qwen-turbo-0930
- `extraConfigs`: 模型扩展参数（如temperature、top_p、max_tokens等）
标准内容应当是 GenerateContentConfig 的配置。python和java的语法转化会自动进行。 此外，也会这其中的属性设置到BaseLlm对应的属性中（若有），以适配更自定义的参数。
---

#### 第二层：输入格式化

##### InputFormatterDefine（输入格式化器）
**作用：** 定义如何将系统变量格式化为模型可理解的文本。

**核心字段：**
- `type`: 格式化器类型（bean实例、class、groovy、handlebars等）
- `content`: 格式化器具体内容
- `inputVariableName`: 指定要格式化的变量名
- `params`: InputFormatter配置参数

**作用时机：**
会在最开始把state中的变量转为文本（若有inputFormatter，则使用inputFormatter）放到一个特殊的state中给模版format时备用。
脚本可以访问state中的所有变量。 替换模版中对应 inputVariableName 位置的字符串。

**脚本模版(class & bean)：**
继承 com.alibaba.agentic.dynamic.domain.inputformatter.InputFormatter
class模式直接new。bean模式从spring容器中获取content名的bean
InputFormatter的第三个参数是配置的params参数

**脚本模版(groovy)：**
```
varname + "s"
```
**脚本模版(handlebars)：**
```
{{varname}}+ "s"
```

---

#### 第二层：输出解析

##### OutputParserDefine（输出解析器）
**作用：** 定义如何解析模型的输出结果。

**核心字段：**
- `type`: 解析器类型（class/groovy等）
- `content`: 解析器具体内容（类名、脚本代码等）
- `mergeIncremental`: 是否先合并流式输出的增量结果,变成从头到目前为止完成的内容
- `params`: 解析器参数（对Java类则为setter值）


对于脚本类型（groovy）之类的，注意其可用变量名为：output
且只有文本类型可支持。

对于bean和class类型，继承com.alibaba.agentic.dynamic.domain.outputparser
覆盖parse(LlmResponse llmResponse, String string)方法可以处理 llmResponse，更原始的数据类型。

**作用时机： **

输出解析器会每次返回都调用，如果是流式内容，多数情况可能需要先合并成渐增的格式然后再处理。因此有个内部实现（当然可以自行实现这个原来一个个字蹦的形式）

*注意*，如果只是想修改格式从incremental变成渐增的。可以选择配置CumulativeOutputParser，但是不要再配置mergeIncremental=true了。
```
{
  "type": "class",
  "content": "com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser",
  "mergeIncremental": false
}
```

---





## 使用指南

### 快速开始

要开始使用 Ali-Agent ADK Dynamic，请按照以下步骤操作：

1. 添加 Maven 依赖：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-dynamic</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
```

2. 创建 Spring Boot 应用并添加组件扫描：
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 基本用法示例

#### 参考AgentTest中
```
        AtomicPromptTemplateDefine def = AtomicPromptTemplateDefine.builder()
                .model(ModelDefine.builder()
                        .name("test-model")
                        .identifier("test-model").build())
                .chatPromptTemplate(ChatPromptTemplateDefine.builder()
                        .instructionTemplate("you are a helpful assistant,your name is {var}")
                        .preHistoryTemplate(Collections.singletonList(MessageTemplateDefine.builder()
                                .type(MessageTemplateDefine.TYPE_TEXT)
                                .template("please print {var} at the beginning, and answer hello")
                                .build()))
                        .historyFormatter(HistoryFormatterDefine.builder()
                                .type("class")
                                .content("com.alibaba.agentic.TestHistoryFormatter")
                                .build())
                        .userTemplate("{input}")
                        .build())
                .inputFormatter(Collections.singletonList(InputFormatterDefine.builder()
                                .inputVariableName("var")
                                .type("groovy")
                                .content("var + 's'")
                        .build()
                ))
                .outputParser(OutputParserDefine.builder()
                        .type("groovy")
                        .mergeIncremental(true)
                        .content("return ['a':  input]")
                        .build())
                .build();



        DynamicLLMAgent agent = DynamicLLMAgent.builder()
                .name("test-agent")
                .atomicPromptTemplateGetter(new AtomicPromptTemplateGetter() {
                            @Override
                            public AtomicPromptTemplateInstance get() {
                                return new AtomicPromptTemplateInstance(def);
                            }
                        }).build();

```
