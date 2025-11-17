package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.DashScopeLlm;
import com.alibaba.agentic.core.utils.AssertUtils;
import io.reactivex.rxjava3.core.Flowable;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReActAgentLlm extends DashScopeLlm {

    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        ReActCoordinator.ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, ReActCoordinator.ReactEvent.class);
        AssertUtils.assertNotNull(reactEvent);
        LlmRequest coordinatorRequest = reactEvent.getLlmRequest();
//        if (CollectionUtils.isEmpty(llmRequest.getMessages())) {
//            llmRequest.setMessages(coordinatorRequest.getMessages());
//        } else if (Objects.nonNull(coordinatorRequest) && CollectionUtils.isNotEmpty(coordinatorRequest.getMessages())) {
//            CollectionUtils.addAll(llmRequest.getMessages(), coordinatorRequest.getMessages().iterator());
//        }
        Flowable<LlmResponse> flowable = super.invoke(coordinatorRequest, systemContext);
        return flowable;

//        List<FunctionCallRequest> functionCallRequests = new ArrayList<>();
//        FunctionCallRequest functionCallRequest = new FunctionCallRequest();
//        functionCallRequest.setId("testMethod");
//        functionCallRequest.setName("testMethod");
//        functionCallRequest.setToolParameter(Map.of("a", "北京", "city", "北京"));
//        functionCallRequests.add(functionCallRequest);
//        Message message = new Message();
//        message.setFunctionCalls(functionCallRequests);
//        LlmResponse.Choice choice = new LlmResponse.Choice();
//        choice.setMessage(message);
//        return flowable.map(response -> {
//            response.setChoices(List.of(choice));
//            return response;
//        });
    }
}
