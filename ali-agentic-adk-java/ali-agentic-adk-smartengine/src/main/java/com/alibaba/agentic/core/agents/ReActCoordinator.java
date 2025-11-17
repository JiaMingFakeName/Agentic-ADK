package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.AgentContext;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.sessions.BaseSessionService;
import com.alibaba.agentic.core.sessions.Session;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/27 16:08
 */
@Data
public class ReActCoordinator implements Coordinator {

    private BaseSessionService sessionService;

    @Override
    public Flowable<Map<String, Object>> step(SystemContext systemContext) {
        AgentContext agentContext = systemContext.getCurrentAgentContext();
        agentContext.setCurrentStep(agentContext.getCurrentStep() + 1);
        if (agentContext.getMaxStep() <= agentContext.getCurrentStep()) {
            return stop(agentContext);
        }
        ReactEvent reactEvent = (ReactEvent) agentContext.getState();
        if (reactEvent == null) {
            /**
             * CALL: THINKING()
             */
            sessionService.appendMessage(agentContext.getSessionId(), new Message().setRole(Message.Role.system).setContent(agentContext.getSystemPrompt()));
            Session session = sessionService.appendMessage(agentContext.getSessionId(), new Message().setRole(Message.Role.user)
                    .setContent(String.valueOf(systemContext.getRequestParameter())));
            return thinking(agentContext, new LlmRequest()
                    .setMessages(session.getMessageList())
                    .setTools(agentContext.getTools()));
        }
        if (Action.thinking.equals(reactEvent.getAction())) {
            LlmResponse llmResponse = DelegationUtils.getResultOfLastNode(systemContext, LlmResponse.class);
            if (llmResponse != null) {
                if (CollectionUtils.isEmpty(llmResponse.getChoices())) {
                    /**
                     * CALL: STOP()
                     */
                    return stop(agentContext);
                }
                if (llmResponse.getChoices() != null) {
                    /**
                     * CALL: FUNCTION_CALL()
                     */
                    llmResponse.getChoices().forEach(choice -> {
                        sessionService.appendMessage(agentContext.getSessionId(), new Message().setRole(Message.Role.assistant)
                                .setContent(choice.getMessage().getContent())
                                .setFunctionCalls(choice.getMessage().getFunctionCalls()));
                    });
                    return act(agentContext, llmResponse.getChoices().get(0).getMessage().getFunctionCalls());
                } else {
                    /**
                     * CALL: STOP()
                     */
                    return stop(agentContext);
                }
            }
        } else if (Action.act.equals(reactEvent.getAction())) {
            List<FunctionCallRequest> functionCallRequests = reactEvent.getFunctionCallRequests();
            List<Map<String, Object>> toolResultList = DelegationUtils.getResultOfLoopNode(systemContext.getLastActivityId(), systemContext);
            Session session = sessionService.getSession(agentContext.getSessionId());
            for (int i = 0; i < functionCallRequests.size(); i++) {
                session.getMessageList().add(new Message().setRole(Message.Role.tool)
                        .setToolCallId(functionCallRequests.get(i).getId())
                        .setContent(JSON.toJSONString(Collections.singletonMap(functionCallRequests.get(i).getName(),
                                toolResultList.get(i)))));
            }
            sessionService.updateSession(session);
            /**
             * CALL: THINKING()
             */
            LlmRequest llmRequest = new LlmRequest();
            llmRequest.setMessages(session.getMessageList());
            return thinking(agentContext, llmRequest);
        }
        return stop(agentContext);
    }


    /**
     * 思考
     *
     * @param agentContext
     * @param llmRequest
     * @return
     */
    public Flowable<Map<String, Object>> thinking(AgentContext agentContext, LlmRequest llmRequest) {
        ReactEvent reactEvent = ReactEvent.builder().action(Action.thinking).llmRequest(llmRequest).build();
        agentContext.setState(reactEvent);
        return Flowable.just(new ObjectMapper().convertValue(reactEvent, Map.class));
    };

    /**
     *  执行
     *
     * @param agentContext
     * @param functionCallRequests
     * @return
     */
    public Flowable<Map<String, Object>> act(AgentContext agentContext, List<FunctionCallRequest> functionCallRequests) {
        ReactEvent reactEvent = ReactEvent.builder().action(Action.act).functionCallRequests(functionCallRequests).build();
        agentContext.setState(reactEvent);
        return Flowable.just(new ObjectMapper().convertValue(reactEvent, Map.class));
    };

    /**
     * 停止
     *
     * @param agentContext
     * @return
     */
    public Flowable<Map<String, Object>> stop(AgentContext agentContext) {
        ReactEvent reactEvent = ReactEvent.builder().action(Action.stop).build();
        agentContext.setState(reactEvent);
        return Flowable.just(new ObjectMapper().convertValue(reactEvent, Map.class));
    };

    public enum Action {
        /**
         * 思考
         */
        thinking,
        /**
         * 执行
         */
        act,
        /**
         * 停止
         */
        stop;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactEvent implements Serializable {

        private static final long serialVersionUID = 1127957356450973203L;

        Action action;
        LlmRequest llmRequest;
        List<FunctionCallRequest> functionCallRequests;
    }

}
