package com.google.adk.agents;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateInstance;
import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.callbacks.OutputParserAfterAgentCallback;
import com.alibaba.agentic.dynamic.callbacks.OutputParserAfterModelCallback;
import com.alibaba.agentic.dynamic.flows.DynamicFlow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.adk.SchemaUtils;
import com.google.adk.agents.*;
import com.google.adk.codeexecutors.BaseCodeExecutor;
import com.google.adk.events.Event;
import com.google.adk.examples.BaseExampleProvider;
import com.google.adk.examples.Example;
import com.google.adk.flows.llmflows.*;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.Model;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import com.google.adk.agents.Callbacks.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.joining;

public class DynamicLLMAgent extends LlmAgent {

    private final BaseLlmFlow llmFlow;

    protected static final ImmutableList<ResponseProcessor> RESPONSE_PROCESSORS =
            ImmutableList.of(CodeExecution.responseProcessor);

    private final AtomicPromptTemplateGetter atomicPromptTemplateGetter;

    protected DynamicLLMAgent(Builder builder) {
        super(builder
                .afterAgentCallback(
                    Collections.singletonList(new OutputParserAfterAgentCallback(builder.atomicPromptTemplateGetter)))
                .afterModelCallback(
                    Collections.singletonList(new OutputParserAfterModelCallback(builder.atomicPromptTemplateGetter))));
        this.atomicPromptTemplateGetter = builder.atomicPromptTemplateGetter;
        llmFlow = determineLlmFlow();
    }
    private volatile Model resolvedModel;

    @Override
    public Model resolvedModel() {
        if (resolvedModel == null) {
            synchronized (this) {
                if (resolvedModel == null) {
                    resolvedModel = (atomicPromptTemplateGetter.get().getModel());
                }
                if(resolvedModel == null) {
                    resolvedModel = super.resolvedModel();
                }
            }
        }
        return resolvedModel;
    }

    @Override
    /**
     * 父类初始化太早，没办法只能覆盖实现
     */
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return llmFlow.run(invocationContext).doOnNext(this::maybeSaveOutputToState);
    }

    /**
     * 父类初始化太早，没办法只能覆盖实现
     * @param event
     */
    private void maybeSaveOutputToState(Event event) {
        if (outputKey().isPresent() && event.finalResponse() && event.content().isPresent()) {
            // Concatenate text from all parts.
            Object output;
            String rawResult =
                    event.content().flatMap(Content::parts).orElse(ImmutableList.of()).stream()
                            .map(part -> part.text().orElse(""))
                            .collect(joining());

            Optional<Schema> outputSchema = outputSchema();
            if (outputSchema.isPresent()) {
                try {
                    Map<String, Object> validatedMap =
                            SchemaUtils.validateOutputSchema(rawResult, outputSchema.get());
                    output = validatedMap;
                } catch (JsonProcessingException e) {
                    System.err.println(
                            "Error: LlmAgent output for outputKey '"
                                    + outputKey().get()
                                    + "' was not valid JSON, despite an outputSchema being present."
                                    + " Saving raw output to state. Error: "
                                    + e.getMessage());
                    output = rawResult;
                } catch (IllegalArgumentException e) {
                    System.err.println(
                            "Error: LlmAgent output for outputKey '"
                                    + outputKey().get()
                                    + "' did not match the outputSchema."
                                    + " Saving raw output to state. Error: "
                                    + e.getMessage());
                    output = rawResult;
                }
            } else {
                output = rawResult;
            }
            event.actions().stateDelta().put(outputKey().get(), output);
        }
    }

    protected BaseLlmFlow determineLlmFlow()
    {
       return new DynamicFlow(super.maxSteps(), atomicPromptTemplateGetter);
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LlmAgent.Builder {
        private AtomicPromptTemplateGetter atomicPromptTemplateGetter;

        public Builder() {
            super();
            super.disallowTransferToParent(false);
            super.disallowTransferToPeers(false);
        }

        public Builder atomicPromptTemplateGetter(AtomicPromptTemplateGetter getter) {
            this.atomicPromptTemplateGetter = getter;
            return this;
        }

        @Override
        public Builder name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder description(String description) {
            super.description(description);
            return this;
        }

        @Override
        public Builder model(String model) {
            super.model(model);
            return this;
        }

        @Override
        public Builder model(BaseLlm model) {
            super.model(model);
            return this;
        }

        @Override
        public Builder instruction(Instruction instruction) {
            super.instruction(instruction);
            return this;
        }

        @Override
        public Builder instruction(String instruction) {
            super.instruction(instruction);
            return this;
        }

        @Override
        public Builder globalInstruction(Instruction globalInstruction) {
            super.globalInstruction(globalInstruction);
            return this;
        }

        @Override
        public Builder globalInstruction(String globalInstruction) {
            super.globalInstruction(globalInstruction);
            return this;
        }

        @Override
        public Builder subAgents(List<? extends BaseAgent> subAgents) {
            super.subAgents(subAgents);
            return this;
        }

        @Override
        public Builder subAgents(BaseAgent... subAgents) {
            super.subAgents(subAgents);
            return this;
        }

        @Override
        public Builder tools(List<?> tools) {
            super.tools(tools);
            return this;
        }

        @Override
        public Builder tools(Object... tools) {
            super.tools(tools);
            return this;
        }

        @Override
        public Builder generateContentConfig(GenerateContentConfig generateContentConfig) {
            super.generateContentConfig(generateContentConfig);
            return this;
        }

        @Override
        public Builder exampleProvider(BaseExampleProvider exampleProvider) {
            super.exampleProvider(exampleProvider);
            return this;
        }

        @Override
        public Builder exampleProvider(List<Example> examples) {
            super.exampleProvider(examples);
            return this;
        }

        @Override
        public Builder exampleProvider(Example... examples) {
            super.exampleProvider(examples);
            return this;
        }

        @Override
        public Builder includeContents(IncludeContents includeContents) {
            super.includeContents(includeContents);
            return this;
        }

        @Override
        public Builder planning(boolean planning) {
            super.planning(planning);
            return this;
        }

        @Override
        public Builder maxSteps(int maxSteps) {
            super.maxSteps(maxSteps);
            return this;
        }

        @Override
        public Builder disallowTransferToParent(boolean disallowTransferToParent) {
            super.disallowTransferToParent(disallowTransferToParent);
            return this;
        }

        @Override
        public Builder disallowTransferToPeers(boolean disallowTransferToPeers) {
            super.disallowTransferToPeers(disallowTransferToPeers);
            return this;
        }

        @Override
        public Builder beforeModelCallback(BeforeModelCallback beforeModelCallback) {
            super.beforeModelCallback(beforeModelCallback);
            return this;
        }

        @Override
        public Builder beforeModelCallback(List<BeforeModelCallbackBase> beforeModelCallback) {
            super.beforeModelCallback(beforeModelCallback);
            return this;
        }

        @Override
        public Builder beforeModelCallbackSync(BeforeModelCallbackSync beforeModelCallbackSync) {
            super.beforeModelCallbackSync(beforeModelCallbackSync);
            return this;
        }

        @Override
        public Builder afterModelCallback(AfterModelCallback afterModelCallback) {
            super.afterModelCallback(afterModelCallback);
            return this;
        }

        @Override
        public Builder afterModelCallback(List<AfterModelCallbackBase> afterModelCallback) {
            super.afterModelCallback(afterModelCallback);
            return this;
        }

        @Override
        public Builder afterModelCallbackSync(AfterModelCallbackSync afterModelCallbackSync) {
            super.afterModelCallbackSync(afterModelCallbackSync);
            return this;
        }

        @Override
        public Builder beforeAgentCallback(BeforeAgentCallback beforeAgentCallback) {
            super.beforeAgentCallback(beforeAgentCallback);
            return this;
        }

        @Override
        public Builder beforeAgentCallback(List<BeforeAgentCallbackBase> beforeAgentCallback) {
            super.beforeAgentCallback(beforeAgentCallback);
            return this;
        }

        @Override
        public Builder beforeAgentCallbackSync(BeforeAgentCallbackSync beforeAgentCallbackSync) {
            super.beforeAgentCallbackSync(beforeAgentCallbackSync);
            return this;
        }

        @Override
        public Builder afterAgentCallback(AfterAgentCallback afterAgentCallback) {
            super.afterAgentCallback(afterAgentCallback);
            return this;
        }

        @Override
        public Builder afterAgentCallback(List<AfterAgentCallbackBase> afterAgentCallback) {
            super.afterAgentCallback(afterAgentCallback);
            return this;
        }

        @Override
        public Builder afterAgentCallbackSync(AfterAgentCallbackSync afterAgentCallbackSync) {
            super.afterAgentCallbackSync(afterAgentCallbackSync);
            return this;
        }

        @Override
        public Builder beforeToolCallback(BeforeToolCallback beforeToolCallback) {
            super.beforeToolCallback(beforeToolCallback);
            return this;
        }

        @Override
        public Builder beforeToolCallback(List<BeforeToolCallbackBase> beforeToolCallbacks) {
            super.beforeToolCallback(beforeToolCallbacks);
            return this;
        }

        @Override
        public Builder beforeToolCallbackSync(BeforeToolCallbackSync beforeToolCallbackSync) {
            super.beforeToolCallbackSync(beforeToolCallbackSync);
            return this;
        }

        @Override
        public Builder afterToolCallback(AfterToolCallback afterToolCallback) {
            super.afterToolCallback(afterToolCallback);
            return this;
        }

        @Override
        public Builder afterToolCallback(List<AfterToolCallbackBase> afterToolCallbacks) {
            super.afterToolCallback(afterToolCallbacks);
            return this;
        }

        @Override
        public Builder afterToolCallbackSync(AfterToolCallbackSync afterToolCallbackSync) {
            super.afterToolCallbackSync(afterToolCallbackSync);
            return this;
        }

        @Override
        public Builder inputSchema(Schema inputSchema) {
            super.inputSchema(inputSchema);
            return this;
        }

        @Override
        public Builder outputSchema(Schema outputSchema) {
            super.outputSchema(outputSchema);
            return this;
        }

        @Override
        public Builder executor(Executor executor) {
            super.executor(executor);
            return this;
        }

        @Override
        public Builder outputKey(String outputKey) {
            super.outputKey(outputKey);
            return this;
        }

        @Override
        public Builder codeExecutor(BaseCodeExecutor codeExecutor) {
            super.codeExecutor(codeExecutor);
            return this;
        }

        @Override
        public DynamicLLMAgent build() {
            return new DynamicLLMAgent(this);
        }
    }
}
