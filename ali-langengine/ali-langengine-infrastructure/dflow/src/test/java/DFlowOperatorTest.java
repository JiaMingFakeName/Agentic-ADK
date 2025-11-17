import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * DFlow操作符使用示例测试类（使用directRun方式）
 * 包含各种算子的使用方法和场景示例，采用DFlow.directRun方式直接运行流程
 */
public class DFlowOperatorTest {
    
    public static void main(String[] args) throws Exception {
        // 初始化DFlow测试环境
        DFlow.globalInitForTest();
        
        // 示例1: 基本的Map操作符使用（directRun方式）
        testBasicOperators();
        
        // 示例2: FlatMap操作符使用（directRun方式）
        testFlatMapOperator();
        
        // 示例3: Zip并发操作符使用（directRun方式）
        testZipOperator();
        
        // 示例4: Any操作符使用（directRun方式）
        testAnyOperator();
        
        // 示例5: Delay延迟操作符使用（directRun方式）
        testDelayOperator();
        
        // 示例6: 异常处理（directRun方式）
        testExceptionHandling();
        
        System.out.println("所有DFlow操作符测试示例执行完成");
    }
    
    /**
     * 示例1: 基本的Map操作符使用（directRun方式）
     */
    private static void testBasicOperators() throws Exception {
        System.out.println("=== 示例1: 基本的Map操作符使用（directRun方式） ===");
        
        // 使用DFlow.directRun直接运行流程
        DFlow.directRun("初始参数", (param) -> 
            DFlow.just(param.getParam())
                .map((ctx, p) -> {
                    System.out.println("接收到参数: " + p);
                    ctx.put("processedData", "处理后的数据: " + p);
                    return "map处理结果";
                })
                .id("mapNode")
                .map((ctx, p) -> {
                    String data = ctx.get("processedData");
                    System.out.println("上下文数据: " + data);
                    System.out.println("上一个节点结果: " + p);
                    return "最终结果";
                })
                .id("endNode")
        , "testBasicOperators");
        
        // 等待执行完成
        Thread.sleep(1000);
        System.out.println();
    }
    
    /**
     * 示例2: FlatMap操作符使用（directRun方式）
     */
    private static void testFlatMapOperator() throws Exception {
        System.out.println("=== 示例2: FlatMap操作符使用（directRun方式） ===");
        
        DFlow.directRun("A", (param) -> 
            DFlow.just(param.getParam())
                .map((ctx, p) -> {
                    System.out.println("开始处理: " + p);
                    ctx.put("input", p);
                    return p;
                })
                .id("prepareData")
                // 使用flatMap根据条件返回不同的流程
                .flatMap((ctx, p) -> {
                    System.out.println("在flatMap中处理: " + p);
                    if ("A".equals(p)) {
                        // 返回一个简单流程
                        return DFlow.just("路径A的结果")
                            .map((c, param1) -> {
                                System.out.println("执行路径A");
                                return "A流程完成";
                            })
                            .id("pathA");
                    } else {
                        // 返回另一个流程
                        return DFlow.just("路径B的结果")
                            .map((c, param1) -> {
                                System.out.println("执行路径B");
                                return "B流程完成";
                            })
                            .id("pathB");
                    }
                })
                .id("branchLogic")
                .map((ctx, p) -> {
                    System.out.println("分支处理结果: " + p);
                    return "流程结束";
                })
                .id("finalNode")
        ,"testFlatMapOperator");
            
        // 等待执行完成
        Thread.sleep(1000);
        System.out.println();
    }
    
    /**
     * 示例3: Zip并发操作符使用（directRun方式）
     */
    private static void testZipOperator() throws Exception {
        System.out.println("=== 示例3: Zip并发操作符使用（directRun方式） ===");
        
        DFlow.directRun("开始并发任务", (param) -> 
            DFlow.just(param.getParam())
                .flatMap((ctx, p) -> {
                    // 创建多个并行任务
                    DFlow<String>[] tasks = new DFlow[3];
                    
                    // 任务1
                    tasks[0] = DFlow.just("task1")
                        .map((c, param1) -> {
                            System.out.println("执行任务1");
                            Thread.sleep(500); // 模拟耗时操作
                            return "任务1结果";
                        })
                        .id("task1");
                    
                    // 任务2
                    tasks[1] = DFlow.just("task2")
                        .map((c, param1) -> {
                            System.out.println("执行任务2");
                            Thread.sleep(300); // 模拟耗时操作
                            return "任务2结果";
                        })
                        .id("task2");
                    
                    // 任务3
                    tasks[2] = DFlow.just("task3")
                        .map((c, param1) -> {
                            System.out.println("执行任务3");
                            Thread.sleep(200); // 模拟耗时操作
                            return "任务3结果";
                        })
                        .id("task3");
                    
                    // 使用zip并发执行所有任务，等待全部完成
                    return DFlow.zip(tasks, null,
                        (ctx2, results) -> {
                            // 合并所有任务的结果
                            StringBuilder sb = new StringBuilder("合并结果: ");
                            for (int i = 0; i < results.length; i++) {
                                sb.append(results[i]);
                                if (i < results.length - 1) {
                                    sb.append(", ");
                                }
                            }
                            return sb.toString();
                        },
                        new TypeReference<String>(){})
                        .id("mergeResults");
                })
                .id("concurrentTasks")
                .map((ctx, p) -> {
                    System.out.println("所有任务完成，结果: " + p);
                    return "zip流程完成";
                })
                .id("zipEnd")
        , "testZipOperator");
        
        // 等待执行完成
        Thread.sleep(2000);
        System.out.println();
    }
    
    /**
     * 示例4: Any操作符使用（directRun方式）
     */
    private static void testAnyOperator() throws Exception {
        System.out.println("=== 示例4: Any操作符使用（directRun方式） ===");
        
        DFlow.directRun("开始any任务", (param) -> 
            DFlow.just(param.getParam())
                .flatMap((ctx, p) -> {
                    // 创建多个任务，使用any操作符等待任意一个完成
                    DFlow<String>[] tasks = new DFlow[2];

                    // 快速完成的任务
                    tasks[0] = DFlow.just("fastTask")
                        .map((c, param1) -> {
                            System.out.println("快速任务开始");
                            Thread.sleep(200);
                            System.out.println("快速任务完成");
                            return "快速任务结果";
                        })
                        .id("fastTask");
                    
                    // 慢速任务
                    tasks[1] = DFlow.just("slowTask")
                        .map((c, param1) -> {
                            System.out.println("慢速任务开始");
                            Thread.sleep(1000);
                            System.out.println("慢速任务完成");
                            return "慢速任务结果";
                        })
                        .id("slowTask");
                    
                    // 使用any操作符，任意一个任务完成就继续
                    return DFlow.any(tasks, null,
                        (ctx2, result) -> "首个完成的任务结果: " + result.getData(),
                        new TypeReference<String>(){})
                        .id("anyResult");
                })
                .id("waitForAny")
                .map((ctx, p) -> {
                    System.out.println("收到any操作符结果: " + p);
                    return "any流程完成";
                })
                .id("anyEnd")
        , "testAnyOperator");
        
        // 等待执行完成
        Thread.sleep(1500);
        System.out.println();
    }
    
    /**
     * 示例5: Delay延迟操作符使用（directRun方式）
     */
    private static void testDelayOperator() throws Exception {
        System.out.println("=== 示例5: Delay延迟操作符使用（directRun方式） ===");
        
        DFlow.directRun("延迟测试", (param) -> 
            DFlow.just(param.getParam())
                .map((ctx, p) -> {
                    System.out.println("开始时间: " + System.currentTimeMillis());
                    return "准备延迟";
                })
                .id("beforeDelay")
                // 使用delay操作符延迟2秒
                .flatMap((ctx, p) -> DFlow.delay(p, 2000).id("delay2sec"))
                .id("afterDelayCall")
                .map((ctx, p) -> {
                    System.out.println("延迟后时间: " + System.currentTimeMillis());
                    System.out.println("延迟完成，参数: " + p);
                    return "延迟流程完成";
                })
                .id("delayEnd")
        , "testDelayOperator");
        
        // 等待执行完成
        Thread.sleep(3000);
        System.out.println();
    }
    
    /**
     * 示例6: 异常处理（directRun方式）
     */
    private static void testExceptionHandling() throws Exception {
        System.out.println("=== 示例6: 异常处理（directRun方式） ===");
        
        DFlow.directRun("异常处理测试", (param) -> 
            DFlow.just(param.getParam())
                .map((ctx, p) -> {
                    System.out.println("正常处理: " + p);
                    return "正常数据";
                })
                .id("normalProcess")
                // 可能抛出异常的节点
                .map((ctx, p) -> {
                    System.out.println("可能出错的处理");
                    // 模拟异常
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("模拟随机异常");
                    }
                    return "成功处理";
                })
                .id("riskyProcess")
                // 异常处理节点
                .onErrorReturn(ctx -> {
                    System.out.println("捕获到异常，进行处理");
                    ctx.put("errorHandled", "true");
                    return "异常处理结果";
                })
                .id("errorHandler")
                .map((ctx, p) -> {
                    System.out.println("最终结果: " + p);
                    String errorHandled = ctx.get("errorHandled");
                    if (errorHandled != null && "true".equals(errorHandled)) {
                        System.out.println("确认异常已被处理");
                    }
                    return "异常处理流程完成";
                })
                .id("exceptionEnd")
        , "testExceptionHandling");
        
        // 等待执行完成
        Thread.sleep(1000);
        System.out.println();
    }
}