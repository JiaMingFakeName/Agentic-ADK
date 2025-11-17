package com.alibaba.agentic.agent.dflow;

import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.Telemetry;
import com.google.adk.agents.DFlowAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.BaseArtifactService;
import com.google.adk.events.Event;
import com.google.adk.memory.BaseMemoryService;
import com.google.adk.plugins.BasePlugin;
import com.google.adk.plugins.PluginManager;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.GetSessionConfig;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.adk.agents.DFlowAgent.getInvocationContext;


/**
 * 预处理后后处理逻辑可以基于此类扩展
 * @param <T>
 */
public class DFlowRunner<T> extends Runner implements ValidClosure {

    private final DFlowAgent<T> agent;
    private final String appName;
    private final BaseArtifactService artifactService;
    private final BaseSessionService sessionService;
    private final BaseMemoryService memoryService;
    private final PluginManager pluginManager;

    /**
     * 注意务必和DFlowA2AExecutorFactory中的sessionService保持一致
     *
     * @param agent
     * @param appName
     * @param artifactService
     * @param sessionService
     */
    public DFlowRunner(DFlowAgent<T> agent, String appName,
                       BaseSessionService sessionService,
                       BaseArtifactService artifactService,
                       BaseMemoryService memoryService
    ) {
        this(agent, appName, sessionService, artifactService, memoryService, null);
    }
    /**
     * 注意务必和DFlowA2AExecutorFactory中的sessionService保持一致
     *
     * @param agent
     * @param appName
     * @param artifactService
     * @param sessionService
     */
    public DFlowRunner(DFlowAgent<T> agent, String appName,
                       BaseSessionService sessionService,
                       BaseArtifactService artifactService,
                       BaseMemoryService memoryService,
                       List<BasePlugin> plugins
    ) {
        super(agent, appName, artifactService, sessionService);
        this.appName = appName;
        this.artifactService = artifactService;
        this.sessionService = sessionService;
        this.memoryService = memoryService;
        this.pluginManager = new PluginManager(plugins);
        this.agent = agent;
    }

    public DFlowAgent<T> getAgent() {
        return agent;
    }

    /**
     *  核心运行靠agent的a2a服务本体，a2a服务直接由executor提供。该方法仅供非A2A协议场景使用
     * @param userId The ID of the user for the session.
     * @param sessionId The ID of the session to run the agent in.
     * @param newMessage The new message from the user to process.
     * @param runConfig Configuration for the agent run.
     * @return
     */
    public Flowable<Event> runAsync(
            String userId, String sessionId, Content newMessage, RunConfig runConfig) {

        Span span = Telemetry.getTracer().spanBuilder("invocation").startSpan();

        try (Scope scope = span.makeCurrent()) {
            // 设置invocationContext
            InvocationContext invocationContext =
                    new InvocationContext(
                            sessionService,
                            artifactService,
                            memoryService,
                            pluginManager,
                            Optional.empty(),
                            Optional.empty(),
                            // [AliCustom] 将traceId作为invocationId生成 invocation
                            span.getSpanContext().getTraceId(),
                            getAgent(),
                            sessionService.getSession(appName, userId, sessionId, Optional.empty()).blockingGet(),
                            Optional.of(newMessage),
                            runConfig,
                            false);

            return agent.runAsync(invocationContext);
        }
    }


    /**
     * [AliCustom] 接到消息后的持久化，最终消息的忽略等再处理
     * @param event
     * @return
     */
    public List<Event> afterFilter(ContextStack contextStack, Event event) {

        if(event == null){
            return List.of();
        }

        // [AliCustom] 接到消息后的持久化
        InvocationContext invocationContext = getInvocationContext(contextStack);
        invocationContext.sessionService().appendEvent(invocationContext.session(),event);

        return List.of(event);
    }

    /**
     * [AliCustom] 获取session
     * @param s
     * @param userId
     * @param sessionId
     * @param empty
     * @return
     */
    public Session getSession(String s, String userId, String sessionId, Optional<GetSessionConfig> empty) {
        Session session =  sessionService.getSession(s, userId, sessionId, empty).blockingGet();
        if (session == null) {
            Single<Session> sessionSingle = sessionService().createSession(appName(), userId, new ConcurrentHashMap(), sessionId);
            session = (Session) sessionSingle.blockingGet();
        }
        return session;
    }
}
