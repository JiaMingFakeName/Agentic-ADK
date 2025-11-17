package com.alibaba.agentic.dynamic.flows;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.AtomicPromptTemplateInstance;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 动态基础请求处理器
 * 用于处理带有动态配置的LLM请求
 */

@Slf4j
public class DynamicContents implements RequestProcessor {

    /**
     * Content
     */
    RequestProcessor contents;

    /**
     * 原子级提示词模板实例
     */
    AtomicPromptTemplateGetter templateInstance;

    public DynamicContents(RequestProcessor contents, AtomicPromptTemplateGetter templateInstance) {
        this.contents = contents;
        this.templateInstance = templateInstance;
    }

    @Override
    public Single<RequestProcessingResult> processRequest(InvocationContext context, LlmRequest request) {

        Single<RequestProcessingResult> result = contents.processRequest(context, request);
        if (!templateInstance.get().needContents()) {
            return result;
        }

        return result.map(requestProcessingResult -> {
            LlmRequest.Builder builder = requestProcessingResult.updatedRequest().toBuilder();
            List<Content> currentContents = new ArrayList<>(requestProcessingResult.updatedRequest().contents());

            Content lastContent = null;
            // 区分历史和用户输入（如果context.最后一个）
            if(context.userContent().isPresent() && !currentContents.isEmpty()){
                lastContent = context.userContent().get();

                //再次确定是同一个
                if(!lastContent.equals(currentContents.get(currentContents.size() - 1))){
                    log.error("lastContent is not context.userContent, runner is not standard implements");
                }else {
                    //去除最后一个用户输入单独处理
                    currentContents.remove(currentContents.size() - 1);
                }
            }

            List<Content> finalResults = new ArrayList<>();
            if(templateInstance.get().needPreHistoryContent()){
                List<Content> preHistory = templateInstance.get().generatePreHistoryContents(context, Collections.emptyList());
                finalResults.addAll(preHistory);
            }

            if(templateInstance.get().needHistory()){
                List<Content> history = templateInstance.get().generateHistory(context, currentContents);
                finalResults.addAll(history);
            }else{
//                不设置则保持默认逻辑不动
                finalResults.addAll(currentContents);
            }

            if(templateInstance.get().needUserContent()){
                List<Content> userInput = templateInstance.get().generateUserInput(context);
                finalResults.addAll(userInput);
            }else{
                //不设置则保持默认逻辑不动
                if(lastContent != null){
                    finalResults.add(lastContent);
                }
            }
            return RequestProcessor.RequestProcessingResult.create(builder
                            .contents(finalResults)
                    .build(), requestProcessingResult.events());
        });
    }
}
