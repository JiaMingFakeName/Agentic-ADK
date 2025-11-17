package com.alibaba.agentic.agent.dflow;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.config.StepHandler;
import com.alibaba.dflow.config.ThrowingConsumer;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.Telemetry;
import com.google.adk.agents.RunConfig;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.events.QueueManager;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DFlowA2AExecutorFactory {
    public static final String A2A_WRITE_BACK_QUEUE = "_A2A_writeback_queue";
    public static final String A2A_REQUEST = "_A2A_context";
    public static final String INVOCATION_CONTEXT = "_Adk_context";
    public static final String SESSION_ID = "_A2A_session_id";
    public static final String TASK_ID = "_A2A_task_id";
    public static final String INVOCATION_ID = "_A2A_invocation_id";
    public static final String APP_NAME = "_A2A_app_name";
    public static final String USER_ID = "_A2A_user_id";

    private final QueueManager queueManager;
    private static final ConcurrentHashMap<String, DFlowA2AExecutor<?>> executors = new ConcurrentHashMap<>();


    public DFlowA2AExecutorFactory(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void init(){
        DFlow.addHandler(new StepHandler(){

            @Override
            public int order() {
                return 0;
            }

            @Override
            public void onStepBefore(ContextStack contextStack) {

            }

            @Override
            public void onStepAfter(ContextStack contextStack) {
            }

            /**
             *  准备每次执行，以及每次执行中不可持久化的部分
             * @param contextStack
             * @param c
             * @throws UserException
             */
            @Override
            public void wrap(ContextStack contextStack, ThrowingConsumer<ContextStack> c) throws UserException {
                Object sessionId = contextStack.getGlobal().get(SESSION_ID);
                Object userId = contextStack.getGlobal().get(USER_ID);
                Object appName = contextStack.getGlobal().get(APP_NAME);
                Object invocationId = contextStack.getGlobal().get(INVOCATION_ID);
                Object taskId = contextStack.getGlobal().get(TASK_ID);
                if(sessionId == null || userId == null || appName == null || invocationId == null) {
                    //未初始化好或者不是DFlowA2A任务
                    c.accept(contextStack);
                    return;
                }


                Span span = Telemetry.getTracer().spanBuilder("dflowStep").startSpan();

                try (Scope scope = span.makeCurrent()) {
                    DFlowA2AExecutor<?> executor = executors.get(appName.toString());
                    if(executor != null) {
                        executor.prepareUnSerializableEachStep(contextStack, sessionId.toString(), userId.toString(), invocationId.toString(), span);
                        c.accept(contextStack);
                        executor.serializeToContext(contextStack, sessionId.toString(), userId.toString(), invocationId.toString(), span);
                        executor.clearUnSerializableEachStep(contextStack);
                    }else {
                        c.accept(contextStack);
                    }
                }
            }
        });
    }

    public <T> AgentExecutor create(DFlowRunner<T> runner, RunConfig runConfig){
        DFlowA2AExecutor<T> executor = new DFlowA2AExecutor<T>(runner, runConfig, queueManager);
        executors.put(runner.getAgent().name(), executor);
        executor.init();
        return executor;
    }
}
