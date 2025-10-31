package com.alibaba.agentic.example;

import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.runner.Runner;
import com.alibaba.agentic.core.tools.BaseTool;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Error handling and retry mechanism examples
 *
 * @author Libres-coder
 * @date 2025/10/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class TestErrorHandling {

    @Test
    public void testGracefulErrorHandling() {
        System.out.println("========== testGracefulErrorHandling ==========");

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode validationNode = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "validation";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                String input = (String) args.get("input");
                System.out.println("Validating input: " + input);
                
                if (input == null || input.isEmpty()) {
                    System.out.println(" Validation failed: empty input");
                    return Flowable.just(Map.of("valid", false, "error", "Input cannot be empty"));
                }
                
                System.out.println(" Validation passed");
                return Flowable.just(Map.of("valid", true, "data", input));
            }
        });
        validationNode.setId("validation");

        ToolFlowNode processingNode = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "processing";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Map<String, Object> validationResult = DelegationUtils.getResultOfNode(systemContext, "validation", Map.class);
                
                if (!(Boolean) validationResult.get("valid")) {
                    System.out.println("Skipping processing due to validation failure");
                    return Flowable.just(Map.of(
                        "status", "skipped",
                        "reason", validationResult.get("error")
                    ));
                }
                
                System.out.println("Processing valid data: " + validationResult.get("data"));
                return Flowable.just(Map.of(
                    "status", "success",
                    "result", "Processed: " + validationResult.get("data")
                ));
            }
        });
        processingNode.setId("processing");

        validationNode.next(processingNode);
        flowCanvas.setRoot(validationNode);

        // Test with invalid input
        System.out.println("\n--- Test Case 1: Empty Input ---");
        Flowable<Result> flowable1 = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("input", "")));

        flowable1.blockingIterable().forEach(result ->
            System.out.println("Result: " + result.getData())
        );

        // Test with valid input
        System.out.println("\n--- Test Case 2: Valid Input ---");
        FlowCanvas flowCanvas2 = new FlowCanvas();
        ToolFlowNode validationNode2 = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "validation";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                String input = (String) args.get("input");
                System.out.println("Validating input: " + input);
                
                if (input == null || input.isEmpty()) {
                    System.out.println(" Validation failed: empty input");
                    return Flowable.just(Map.of("valid", false, "error", "Input cannot be empty"));
                }
                
                System.out.println(" Validation passed");
                return Flowable.just(Map.of("valid", true, "data", input));
            }
        });
        validationNode2.setId("validation");

        ToolFlowNode processingNode2 = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "processing";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Map<String, Object> validationResult = DelegationUtils.getResultOfNode(systemContext, "validation", Map.class);
                
                if (!(Boolean) validationResult.get("valid")) {
                    System.out.println("Skipping processing due to validation failure");
                    return Flowable.just(Map.of(
                        "status", "skipped",
                        "reason", validationResult.get("error")
                    ));
                }
                
                System.out.println("Processing valid data: " + validationResult.get("data"));
                return Flowable.just(Map.of(
                    "status", "success",
                    "result", "Processed: " + validationResult.get("data")
                ));
            }
        });
        processingNode2.setId("processing");

        validationNode2.next(processingNode2);
        flowCanvas2.setRoot(validationNode2);

        Flowable<Result> flowable2 = new Runner().run(flowCanvas2, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("input", "valid_data")));

        flowable2.blockingIterable().forEach(result ->
            System.out.println("Result: " + result.getData())
        );

        System.out.println(" Graceful error handling completed\n");
    }

    @Test
    public void testRetryMechanism() {
        System.out.println("========== testRetryMechanism ==========");

        FlowCanvas flowCanvas = new FlowCanvas();
        AtomicInteger attemptCount = new AtomicInteger(0);

        ToolFlowNode retryableTask = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "retryableTask";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                int attempt = attemptCount.incrementAndGet();
                System.out.println("Attempt #" + attempt);
                
                // Simulate failure on first 2 attempts
                if (attempt < 3) {
                    System.out.println(" Task failed, will retry...");
                    return Flowable.just(Map.of(
                        "success", false,
                        "attempt", attempt,
                        "message", "Temporary failure"
                    ));
                }
                
                System.out.println(" Task succeeded on attempt " + attempt);
                return Flowable.just(Map.of(
                    "success", true,
                    "attempt", attempt,
                    "result", "Success after retries"
                ));
            }
        });
        retryableTask.setId("retryableTask");

        ToolFlowNode retryController = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "retryController";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Map<String, Object> taskResult = DelegationUtils.getResultOfNode(systemContext, "retryableTask", Map.class);
                
                Boolean success = (Boolean) taskResult.get("success");
                Integer attempt = (Integer) taskResult.get("attempt");
                
                if (success) {
                    System.out.println("Final result: SUCCESS after " + attempt + " attempts");
                    return Flowable.just(Map.of(
                        "status", "completed",
                        "attempts", attempt,
                        "result", taskResult.get("result")
                    ));
                } else {
                    System.out.println("Task failed on attempt " + attempt);
                    return Flowable.just(Map.of(
                        "status", "retry_needed",
                        "attempts", attempt
                    ));
                }
            }
        });
        retryController.setId("retryController");

        retryableTask.next(retryController);
        flowCanvas.setRoot(retryableTask);

        // Simulate multiple retries by running the workflow multiple times
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n--- Workflow Execution #" + i + " ---");
            
            FlowCanvas canvas = new FlowCanvas();
            ToolFlowNode task = new ToolFlowNode(null, new BaseTool() {
                @Override
                public String name() {
                    return "retryableTask";
                }

                @Override
                public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                    int attempt = attemptCount.get();
                    System.out.println("Current attempt count: " + attempt);
                    
                    if (attempt < 3) {
                        System.out.println(" Task failed");
                        return Flowable.just(Map.of(
                            "success", false,
                            "attempt", attempt,
                            "message", "Temporary failure"
                        ));
                    }
                    
                    System.out.println(" Task succeeded");
                    return Flowable.just(Map.of(
                        "success", true,
                        "attempt", attempt,
                        "result", "Success after retries"
                    ));
                }
            });
            task.setId("retryableTask");

            ToolFlowNode controller = new ToolFlowNode(null, new BaseTool() {
                @Override
                public String name() {
                    return "retryController";
                }

                @Override
                public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                    Map<String, Object> taskResult = DelegationUtils.getResultOfNode(systemContext, "retryableTask", Map.class);
                    
                    Boolean success = (Boolean) taskResult.get("success");
                    Integer attempt = (Integer) taskResult.get("attempt");
                    
                    if (success) {
                        System.out.println("Final result: SUCCESS");
                        return Flowable.just(Map.of(
                            "status", "completed",
                            "attempts", attempt,
                            "result", taskResult.get("result")
                        ));
                    } else {
                        System.out.println("Retry needed");
                        return Flowable.just(Map.of(
                            "status", "retry_needed",
                            "attempts", attempt
                        ));
                    }
                }
            });
            controller.setId("retryController");

            task.next(controller);
            canvas.setRoot(task);

            Flowable<Result> flowable = new Runner().run(canvas, new Request()
                    .setInvokeMode(InvokeMode.SYNC)
                    .setParam(Map.of("execution", i)));

            flowable.blockingIterable().forEach(result ->
                System.out.println("Result: " + result.getData())
            );

            // Check if succeeded
            if (attemptCount.get() >= 3) {
                System.out.println("\n[Success] Task finally succeeded after " + attemptCount.get() + " total attempts");
                break;
            }
        }

        System.out.println(" Retry mechanism completed\n");
    }

    @Test
    public void testFallbackStrategy() {
        System.out.println("========== testFallbackStrategy ==========");

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode primaryService = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "primaryService";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                System.out.println("Attempting primary service...");
                
                // Simulate service failure
                boolean serviceAvailable = false;
                
                if (!serviceAvailable) {
                    System.out.println(" Primary service unavailable");
                    return Flowable.just(Map.of(
                        "available", false,
                        "error", "Service timeout"
                    ));
                }
                
                return Flowable.just(Map.of(
                    "available", true,
                    "data", "Primary data"
                ));
            }
        });
        primaryService.setId("primaryService");

        ToolFlowNode fallbackHandler = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "fallbackHandler";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Map<String, Object> primaryResult = DelegationUtils.getResultOfNode(systemContext, "primaryService", Map.class);
                
                Boolean available = (Boolean) primaryResult.get("available");
                
                if (!available) {
                    System.out.println("  Using fallback strategy...");
                    System.out.println("Fallback: Using cached data");
                    return Flowable.just(Map.of(
                        "source", "fallback",
                        "data", "Cached data",
                        "warning", "Primary service unavailable: " + primaryResult.get("error")
                    ));
                }
                
                System.out.println(" Using primary service data");
                return Flowable.just(Map.of(
                    "source", "primary",
                    "data", primaryResult.get("data")
                ));
            }
        });
        fallbackHandler.setId("fallbackHandler");

        primaryService.next(fallbackHandler);
        flowCanvas.setRoot(primaryService);

        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("request", "fetch_data")));

        flowable.blockingIterable().forEach(result ->
            System.out.println("Final result: " + result.getData())
        );

        System.out.println(" Fallback strategy completed\n");
    }
}

