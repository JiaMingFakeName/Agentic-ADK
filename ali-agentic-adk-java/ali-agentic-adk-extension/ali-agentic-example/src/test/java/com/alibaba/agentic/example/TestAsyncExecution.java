package com.alibaba.agentic.example;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Async execution mode examples
 *
 * @author Libres-coder
 * @date 2025/10/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class TestAsyncExecution {

    @Test
    public void testAsyncMode() throws InterruptedException {
        System.out.println("========== testAsyncMode ==========");

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode asyncTask = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "asyncTask";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                System.out.println("[Async Task] Starting long-running task...");
                try {
                    Thread.sleep(2000); // Simulate long-running task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Async Task] Task completed");
                return Flowable.just(Map.of("status", "completed", "result", "async_data"));
            }
        });
        asyncTask.setId("asyncTask");

        flowCanvas.setRoot(asyncTask);

        System.out.println("Submitting async request...");
        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger resultCount = new AtomicInteger(0);

        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.ASYNC)
                .setParam(Map.of("input", "async_test")));

        flowable.subscribe(
            result -> {
                System.out.println("Received result: " + result.getData());
                resultCount.incrementAndGet();
            },
            error -> {
                System.err.println("Error: " + error.getMessage());
                latch.countDown();
            },
            latch::countDown
        );

        System.out.println("Request submitted, not blocking main thread");
        long submitTime = System.currentTimeMillis();
        System.out.println("Submit time: " + (submitTime - startTime) + "ms (should be < 100ms)");

        latch.await(5, TimeUnit.SECONDS);
        System.out.println("Total results received: " + resultCount.get());
        System.out.println(" Async mode completed\n");
    }

    @Test
    public void testSyncVsAsyncComparison() throws InterruptedException {
        System.out.println("========== testSyncVsAsyncComparison ==========");

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode task = new ToolFlowNode(null, new BaseTool() {
            @Override
            public String name() {
                return "task";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return Flowable.just(Map.of("mode", args.get("mode"), "status", "done"));
            }
        });
        task.setId("task");

        flowCanvas.setRoot(task);

        // Test SYNC mode
        System.out.println("--- Testing SYNC mode ---");
        long syncStart = System.currentTimeMillis();
        
        Flowable<Result> syncFlowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("mode", "SYNC")));

        syncFlowable.blockingSubscribe(
            result -> System.out.println("SYNC result: " + result.getData())
        );
        
        long syncTime = System.currentTimeMillis() - syncStart;
        System.out.println("SYNC execution time: " + syncTime + "ms");

        // Test ASYNC mode
        System.out.println("\n--- Testing ASYNC mode ---");
        long asyncStart = System.currentTimeMillis();
        CountDownLatch asyncLatch = new CountDownLatch(1);

        Flowable<Result> asyncFlowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.ASYNC)
                .setParam(Map.of("mode", "ASYNC")));

        asyncFlowable.subscribe(
            result -> System.out.println("ASYNC result: " + result.getData()),
            error -> asyncLatch.countDown(),
            asyncLatch::countDown
        );

        long asyncSubmitTime = System.currentTimeMillis() - asyncStart;
        System.out.println("ASYNC submit time: " + asyncSubmitTime + "ms (non-blocking)");

        asyncLatch.await(3, TimeUnit.SECONDS);
        long asyncTotalTime = System.currentTimeMillis() - asyncStart;
        System.out.println("ASYNC total time: " + asyncTotalTime + "ms");

        System.out.println("\n SYNC vs ASYNC comparison completed");
        System.out.println("Key difference: ASYNC submit time << SYNC execution time\n");
    }
}

