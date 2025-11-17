package com.google.adk.agents;

import com.alibaba.agentic.agent.dflow.llmflows.AutoDFlowFlow;
import com.alibaba.agentic.agent.dflow.llmflows.BaseDFlowLlmFlow;
import com.alibaba.agentic.agent.dflow.llmflows.SingleDFlowFlow;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.a2a.converters.PartConverter;
import com.google.adk.agents.*;
import com.google.adk.codeexecutors.BaseCodeExecutor;
import com.google.adk.examples.BaseExampleProvider;
import com.google.adk.examples.Example;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.LlmRegistry;
import com.google.adk.models.Model;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.BaseToolset;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Message;
import io.a2a.spec.TextPart;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
public class DFlowLlmAgent extends DFlowAgent<String> {
    private final Optional<Model> model;
    private final Instruction instruction;
    private final Instruction globalInstruction;
    private final List<Object> toolsUnion;
    private final ImmutableList<BaseToolset> toolsets;
    private final Optional<GenerateContentConfig> generateContentConfig;
    private final Optional<BaseExampleProvider> exampleProvider;
    private final DFlowLlmAgent.IncludeContents includeContents;
    private final boolean planning;
    private final Optional<Integer> maxSteps;
    private final boolean disallowTransferToParent;
    private final boolean disallowTransferToPeers;
    private final Optional<List<Callbacks.BeforeModelCallback>> beforeModelCallback;
    private final Optional<List<Callbacks.AfterModelCallback>> afterModelCallback;
    private final Optional<List<Callbacks.BeforeToolCallback>> beforeToolCallback;
    private final Optional<List<Callbacks.AfterToolCallback>> afterToolCallback;
    private final Optional<Schema> inputSchema;
    private final Optional<Schema> outputSchema;
    private final Optional<Executor> executor;
    private final Optional<String> outputKey;
    private final Optional<BaseCodeExecutor> codeExecutor;
    private volatile Model resolvedModel;
    private final BaseDFlowLlmFlow llmFlow;

    protected DFlowLlmAgent(Builder builder) {
        super(builder.name, builder.description, builder.subAgents, builder.agentCard);
        this.model = Optional.ofNullable(builder.model);
        this.instruction = (Instruction)(builder.instruction == null ? new Instruction.Static("") : builder.instruction);
        this.globalInstruction = (Instruction)(builder.globalInstruction == null ? new Instruction.Static("") : builder.globalInstruction);
        this.generateContentConfig = Optional.ofNullable(builder.generateContentConfig);
        this.exampleProvider = Optional.ofNullable(builder.exampleProvider);
        this.includeContents = builder.includeContents != null ? builder.includeContents : DFlowLlmAgent.IncludeContents.DEFAULT;
        this.planning = builder.planning != null && builder.planning;
        this.maxSteps = Optional.ofNullable(builder.maxSteps);
        this.disallowTransferToParent = builder.disallowTransferToParent == null ? true : builder.disallowTransferToParent;
        this.disallowTransferToPeers = builder.disallowTransferToPeers == null ? true : builder.disallowTransferToPeers;
        this.beforeModelCallback = Optional.ofNullable(builder.beforeModelCallback);
        this.afterModelCallback = Optional.ofNullable(builder.afterModelCallback);
        this.beforeToolCallback = Optional.ofNullable(builder.beforeToolCallback);
        this.afterToolCallback = Optional.ofNullable(builder.afterToolCallback);
        this.inputSchema = Optional.ofNullable(builder.inputSchema);
        this.outputSchema = Optional.ofNullable(builder.outputSchema);
        this.executor = Optional.ofNullable(builder.executor);
        this.outputKey = Optional.ofNullable(builder.outputKey);
        this.toolsUnion = builder.toolsUnion != null ? builder.toolsUnion : ImmutableList.of();
        this.toolsets = extractToolsets(this.toolsUnion);
        this.codeExecutor = Optional.ofNullable(builder.codeExecutor);
        this.llmFlow = this.determineLlmFlow();
        Preconditions.checkArgument(!this.name().isEmpty(), "Agent name cannot be empty.");
    }

    public Optional<List<Callbacks.BeforeModelCallback>> beforeModelCallback() {
        return this.beforeModelCallback;
    }
    private static ImmutableList<BaseToolset> extractToolsets(List<Object> toolsUnion) {
        return (ImmutableList)toolsUnion.stream().filter((obj) -> obj instanceof BaseToolset).map((obj) -> (BaseToolset)obj).collect(ImmutableList.toImmutableList());
    }

    protected BaseDFlowLlmFlow determineLlmFlow() {
        return (BaseDFlowLlmFlow)(this.disallowTransferToParent && this.disallowTransferToPeers && this.subAgents().isEmpty() ? new SingleDFlowFlow(this.maxSteps) : new AutoDFlowFlow(this.maxSteps));
    }

    public DFlow<String> start(ContextStack contextStack, Message p) {
        return llmFlow.run(contextStack, getInvocationContext(contextStack));
    }

    public static String convertMessageToString(Message context){
        return  context.getParts().stream()
                .filter(part -> part instanceof TextPart)
                .map(part -> ((TextPart) part).getText())
                .collect(Collectors.joining());
    }

    public static Content convertMessageToContent(Message message){
        Content newMessage =
                Content.builder()
                        .role("user")
                        .parts(
                                message.getParts().stream()
                                        .map(PartConverter::convertA2APartToGenaiPart)
                                        .collect(Collectors.toList()))
                        .build();
        return newMessage;
    }
    public Flowable<BaseTool> canonicalTools(Optional<ReadonlyContext> context) {
        List<Flowable<BaseTool>> toolFlowables = new ArrayList<>();
        for (Object toolOrToolset : toolsUnion) {
            if (toolOrToolset instanceof BaseTool) {
                BaseTool baseTool = (BaseTool) toolOrToolset;
                toolFlowables.add(Flowable.just(baseTool));
            } else if (toolOrToolset instanceof BaseToolset) {
                BaseToolset baseToolset = (BaseToolset) toolOrToolset;
                toolFlowables.add(baseToolset.getTools(context.orElse(null)));
            } else {
                throw new IllegalArgumentException(
                        "Object in tools list is not of a supported type: "
                                + toolOrToolset.getClass().getName());
            }
        }
        return Flowable.concat(toolFlowables);
    }
    public IncludeContents includeContents() {
        return includeContents;
    }
    public Flowable<BaseTool> canonicalTools(ReadonlyContext readonlyContext) {
        return canonicalTools(Optional.ofNullable(readonlyContext));
    }

    public Optional<List<Callbacks.BeforeToolCallback>> beforeToolCallback() {
        return beforeToolCallback;
    }

    public Optional<List<Callbacks.AfterToolCallback>> afterToolCallback() {
        return afterToolCallback;
    }

    public Optional<List<Callbacks.AfterModelCallback>> afterModelCallback() {
        return afterModelCallback;
    }
    public Model resolvedModel() {
        if (resolvedModel == null) {
            synchronized (this) {
                if (resolvedModel == null) {
                    resolvedModel = resolveModelInternal();
                }
            }
        }
        return resolvedModel;
    }

    public Optional<BaseExampleProvider> exampleProvider() {
        return exampleProvider;
    }

    /**
     * Resolves the model for this agent, checking first if it is defined locally, then searching
     * through ancestors.
     *
     * <p>This method is only for use by Agent Development Kit.
     *
     * @return The resolved {@link Model} for this agent.
     * @throws IllegalStateException if no model is found for this agent or its ancestors.
     */
    private Model resolveModelInternal() {
        if (this.model.isPresent()) {
            Model currentModel = this.model.get();

            if (currentModel.model().isPresent()) {
                return currentModel;
            }

            if (currentModel.modelName().isPresent()) {
                String modelName = currentModel.modelName().get();
                BaseLlm resolvedLlm = LlmRegistry.getLlm(modelName);

                return Model.builder().modelName(modelName).model(resolvedLlm).build();
            }
        }
        BaseAgent current = this.parentAgent();
        while (current != null) {
            if (current instanceof LlmAgent) {
                return ((LlmAgent) current).resolvedModel();
            }
            current = current.parentAgent();
        }
        throw new IllegalStateException("No model found for agent " + name() + " or its ancestors.");
    }

    public Optional<GenerateContentConfig> generateContentConfig() {
        return generateContentConfig;
    }

    public Optional<Schema> inputSchema() {
        return inputSchema;
    }

    public Optional<Schema> outputSchema() {
        return outputSchema;
    }
    public Single<Map.Entry<String, Boolean>> canonicalGlobalInstruction(ReadonlyContext context) {
        if (globalInstruction instanceof Instruction.Static) {
            Instruction.Static staticInstr = (Instruction.Static) globalInstruction;
            return Single.just(new AbstractMap.SimpleEntry<>(staticInstr.instruction(), false));
        } else if (globalInstruction instanceof Instruction.Provider) {
            Instruction.Provider provider = (Instruction.Provider) globalInstruction;
            return provider.getInstruction().apply(context).map(instr -> new AbstractMap.SimpleEntry<>(instr, true));
        }
        throw new IllegalStateException("Unknown Instruction subtype: " + globalInstruction.getClass());
    }


    public Single<Map.Entry<String, Boolean>> canonicalInstruction(ReadonlyContext context) {
        if (instruction instanceof Instruction.Static) {
            Instruction.Static staticInstr = (Instruction.Static) instruction;
            return Single.just(new AbstractMap.SimpleEntry<>(staticInstr.instruction(), false));
        } else if (instruction instanceof Instruction.Provider) {
            Instruction.Provider provider = (Instruction.Provider) instruction;
            return provider.getInstruction().apply(context).map(instr -> new AbstractMap.SimpleEntry<>(instr, true));
        }
        throw new IllegalStateException("Unknown Instruction subtype: " + instruction.getClass());
    }
    public static enum IncludeContents {
        DEFAULT,
        NONE;
    }


    public static class Builder {
        private String name;
        private String description;
        private Model model;
        private Instruction instruction;
        private Instruction globalInstruction;
        private ImmutableList<BaseAgent> subAgents;
        private ImmutableList<Object> toolsUnion;
        private GenerateContentConfig generateContentConfig;
        private BaseExampleProvider exampleProvider;
        private DFlowLlmAgent.IncludeContents includeContents;
        private Boolean planning;
        private Integer maxSteps;
        private Boolean disallowTransferToParent;
        private Boolean disallowTransferToPeers;
        private ImmutableList<Callbacks.BeforeModelCallback> beforeModelCallback;
        private ImmutableList<Callbacks.AfterModelCallback> afterModelCallback;
        private ImmutableList<Callbacks.BeforeAgentCallback> beforeAgentCallback;
        private ImmutableList<Callbacks.AfterAgentCallback> afterAgentCallback;
        private ImmutableList<Callbacks.BeforeToolCallback> beforeToolCallback;
        private ImmutableList<Callbacks.AfterToolCallback> afterToolCallback;
        private Schema inputSchema;
        private Schema outputSchema;
        private Executor executor;
        private String outputKey;
        private BaseCodeExecutor codeExecutor;
        private AgentCard agentCard;

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder name(String name) {
            this.name = name;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder description(String description) {
            this.description = description;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder model(String model) {
            this.model = Model.builder().modelName(model).build();
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder model(BaseLlm model) {
            this.model = Model.builder().model(model).build();
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder instruction(Instruction instruction) {
            this.instruction = instruction;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder instruction(String instruction) {
            this.instruction = instruction == null ? null : new Instruction.Static(instruction);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder globalInstruction(Instruction globalInstruction) {
            this.globalInstruction = globalInstruction;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder globalInstruction(String globalInstruction) {
            this.globalInstruction = globalInstruction == null ? null : new Instruction.Static(globalInstruction);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder subAgents(List<? extends BaseAgent> subAgents) {
            this.subAgents = ImmutableList.copyOf(subAgents);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder subAgents(BaseAgent... subAgents) {
            this.subAgents = ImmutableList.copyOf(subAgents);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder tools(List<?> tools) {
            this.toolsUnion = ImmutableList.copyOf(tools);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder tools(Object... tools) {
            this.toolsUnion = ImmutableList.copyOf(tools);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder generateContentConfig(GenerateContentConfig generateContentConfig) {
            this.generateContentConfig = generateContentConfig;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder exampleProvider(BaseExampleProvider exampleProvider) {
            this.exampleProvider = exampleProvider;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder exampleProvider(final List<Example> examples) {
            this.exampleProvider = new BaseExampleProvider() {
                public List<Example> getExamples(String query) {
                    return examples;
                }
            };
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder exampleProvider(final Example... examples) {
            this.exampleProvider = new BaseExampleProvider() {
                public ImmutableList<Example> getExamples(String query) {
                    return ImmutableList.copyOf(examples);
                }
            };
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder includeContents(DFlowLlmAgent.IncludeContents includeContents) {
            this.includeContents = includeContents;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder planning(boolean planning) {
            this.planning = planning;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder maxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder disallowTransferToParent(boolean disallowTransferToParent) {
            this.disallowTransferToParent = disallowTransferToParent;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder disallowTransferToPeers(boolean disallowTransferToPeers) {
            this.disallowTransferToPeers = disallowTransferToPeers;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeModelCallback(Callbacks.BeforeModelCallback beforeModelCallback) {
            this.beforeModelCallback = ImmutableList.of(beforeModelCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeModelCallback(List<Callbacks.BeforeModelCallback> beforeModelCallback) {
            if (beforeModelCallback == null) {
                this.beforeModelCallback = null;
            } else if (beforeModelCallback.isEmpty()) {
                this.beforeModelCallback = ImmutableList.of();
            } else {
                ImmutableList.Builder<Callbacks.BeforeModelCallback> builder = ImmutableList.builder();

                for(Callbacks.BeforeModelCallback callback : beforeModelCallback) {
                    if (callback instanceof Callbacks.BeforeModelCallback) {
                        Callbacks.BeforeModelCallback beforeModelCallbackInstance = (Callbacks.BeforeModelCallback)callback;
                        builder.add(beforeModelCallbackInstance);
                    } else if (callback instanceof Callbacks.BeforeModelCallbackSync) {
                        Callbacks.BeforeModelCallbackSync beforeModelCallbackSyncInstance = (Callbacks.BeforeModelCallbackSync)callback;
                        builder.add((Callbacks.BeforeModelCallback)(callbackContext, llmRequestBuilder) -> Maybe.fromOptional(beforeModelCallbackSyncInstance.call(callbackContext, llmRequestBuilder)));
                    } else {
                        DFlowLlmAgent.log.warn("Invalid beforeModelCallback callback type: %s. Ignoring this callback.", callback.getClass().getName());
                    }
                }

                this.beforeModelCallback = builder.build();
            }

            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeModelCallbackSync(Callbacks.BeforeModelCallbackSync beforeModelCallbackSync) {
            this.beforeModelCallback = ImmutableList.of((Callbacks.BeforeModelCallback)(callbackContext, llmRequestBuilder) -> Maybe.fromOptional(beforeModelCallbackSync.call(callbackContext, llmRequestBuilder)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterModelCallback(Callbacks.AfterModelCallback afterModelCallback) {
            this.afterModelCallback = ImmutableList.of(afterModelCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterModelCallback(List<Callbacks.AfterModelCallback> afterModelCallback) {
            if (afterModelCallback == null) {
                this.afterModelCallback = null;
            } else if (afterModelCallback.isEmpty()) {
                this.afterModelCallback = ImmutableList.of();
            } else {
                ImmutableList.Builder<Callbacks.AfterModelCallback> builder = ImmutableList.builder();

                for(Callbacks.AfterModelCallback callback : afterModelCallback) {
                    if (callback instanceof Callbacks.AfterModelCallback) {
                        Callbacks.AfterModelCallback afterModelCallbackInstance = (Callbacks.AfterModelCallback)callback;
                        builder.add(afterModelCallbackInstance);
                    } else if (callback instanceof Callbacks.AfterModelCallbackSync) {
                        Callbacks.AfterModelCallbackSync afterModelCallbackSyncInstance = (Callbacks.AfterModelCallbackSync)callback;
                        builder.add((Callbacks.AfterModelCallback)(callbackContext, llmResponse) -> Maybe.fromOptional(afterModelCallbackSyncInstance.call(callbackContext, llmResponse)));
                    } else {
                        log.warn("Invalid afterModelCallback callback type: %s. Ignoring this callback.", callback.getClass().getName());
                    }
                }

                this.afterModelCallback = builder.build();
            }

            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterModelCallbackSync(Callbacks.AfterModelCallbackSync afterModelCallbackSync) {
            this.afterModelCallback = ImmutableList.of((Callbacks.AfterModelCallback)(callbackContext, llmResponse) -> Maybe.fromOptional(afterModelCallbackSync.call(callbackContext, llmResponse)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeAgentCallback(Callbacks.BeforeAgentCallback beforeAgentCallback) {
            this.beforeAgentCallback = ImmutableList.of(beforeAgentCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeAgentCallback(List<Callbacks.BeforeAgentCallbackBase> beforeAgentCallback) {
            this.beforeAgentCallback = CallbackUtil.getBeforeAgentCallbacks(beforeAgentCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeAgentCallbackSync(Callbacks.BeforeAgentCallbackSync beforeAgentCallbackSync) {
            this.beforeAgentCallback = ImmutableList.of((Callbacks.BeforeAgentCallback)(callbackContext) -> Maybe.fromOptional(beforeAgentCallbackSync.call(callbackContext)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterAgentCallback(Callbacks.AfterAgentCallback afterAgentCallback) {
            this.afterAgentCallback = ImmutableList.of(afterAgentCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterAgentCallback(List<Callbacks.AfterAgentCallbackBase> afterAgentCallback) {
            this.afterAgentCallback = CallbackUtil.getAfterAgentCallbacks(afterAgentCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterAgentCallbackSync(Callbacks.AfterAgentCallbackSync afterAgentCallbackSync) {
            this.afterAgentCallback = ImmutableList.of((Callbacks.AfterAgentCallback)(callbackContext) -> Maybe.fromOptional(afterAgentCallbackSync.call(callbackContext)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeToolCallback(Callbacks.BeforeToolCallback beforeToolCallback) {
            this.beforeToolCallback = ImmutableList.of(beforeToolCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeToolCallback(List<Callbacks.BeforeToolCallback> beforeToolCallbacks) {
            if (beforeToolCallbacks == null) {
                this.beforeToolCallback = null;
            } else if (beforeToolCallbacks.isEmpty()) {
                this.beforeToolCallback = ImmutableList.of();
            } else {
                ImmutableList.Builder<Callbacks.BeforeToolCallback> builder = ImmutableList.builder();

                for(Callbacks.BeforeToolCallback callback : beforeToolCallbacks) {
                    if (callback instanceof Callbacks.BeforeToolCallback) {
                        Callbacks.BeforeToolCallback beforeToolCallbackInstance = (Callbacks.BeforeToolCallback)callback;
                        builder.add(beforeToolCallbackInstance);
                    } else if (callback instanceof Callbacks.BeforeToolCallbackSync) {
                        Callbacks.BeforeToolCallbackSync beforeToolCallbackSyncInstance = (Callbacks.BeforeToolCallbackSync)callback;
                        builder.add((Callbacks.BeforeToolCallback)(invocationContext, baseTool, input, toolContext) -> Maybe.fromOptional(beforeToolCallbackSyncInstance.call(invocationContext, baseTool, input, toolContext)));
                    } else {
                        log.warn("Invalid beforeToolCallback callback type: {}. Ignoring this callback.", callback.getClass().getName());
                    }
                }

                this.beforeToolCallback = builder.build();
            }

            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder beforeToolCallbackSync(Callbacks.BeforeToolCallbackSync beforeToolCallbackSync) {
            this.beforeToolCallback = ImmutableList.of((Callbacks.BeforeToolCallback)(invocationContext, baseTool, input, toolContext) -> Maybe.fromOptional(beforeToolCallbackSync.call(invocationContext, baseTool, input, toolContext)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterToolCallback(Callbacks.AfterToolCallback afterToolCallback) {
            this.afterToolCallback = ImmutableList.of(afterToolCallback);
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterToolCallback(List<Callbacks.AfterToolCallback> afterToolCallbacks) {
            if (afterToolCallbacks == null) {
                this.afterToolCallback = null;
            } else if (afterToolCallbacks.isEmpty()) {
                this.afterToolCallback = ImmutableList.of();
            } else {
                ImmutableList.Builder<Callbacks.AfterToolCallback> builder = ImmutableList.builder();

                for(Callbacks.AfterToolCallback callback : afterToolCallbacks) {
                    if (callback instanceof Callbacks.AfterToolCallback) {
                        Callbacks.AfterToolCallback afterToolCallbackInstance = (Callbacks.AfterToolCallback)callback;
                        builder.add(afterToolCallbackInstance);
                    } else if (callback instanceof Callbacks.AfterToolCallbackSync) {
                        Callbacks.AfterToolCallbackSync afterToolCallbackSyncInstance = (Callbacks.AfterToolCallbackSync)callback;
                        builder.add((Callbacks.AfterToolCallback)(invocationContext, baseTool, input, toolContext, response) -> Maybe.fromOptional(afterToolCallbackSyncInstance.call(invocationContext, baseTool, input, toolContext, response)));
                    } else {
                        log.warn("Invalid afterToolCallback callback type: {}. Ignoring this callback.", callback.getClass().getName());
                    }
                }

                this.afterToolCallback = builder.build();
            }

            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder afterToolCallbackSync(Callbacks.AfterToolCallbackSync afterToolCallbackSync) {
            this.afterToolCallback = ImmutableList.of((Callbacks.AfterToolCallback)(invocationContext, baseTool, input, toolContext, response) -> Maybe.fromOptional(afterToolCallbackSync.call(invocationContext, baseTool, input, toolContext, response)));
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder inputSchema(Schema inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder outputSchema(Schema outputSchema) {
            this.outputSchema = outputSchema;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder codeExecutor(BaseCodeExecutor codeExecutor) {
            this.codeExecutor = codeExecutor;
            return this;
        }

        @CanIgnoreReturnValue
        public DFlowLlmAgent.Builder agentCard(AgentCard agentCard) {
            this.agentCard = agentCard;
            return this;
        }

        protected void validate() {
            this.disallowTransferToParent = this.disallowTransferToParent != null && this.disallowTransferToParent;
            this.disallowTransferToPeers = this.disallowTransferToPeers != null && this.disallowTransferToPeers;
            if (this.outputSchema != null) {
                if (!this.disallowTransferToParent || !this.disallowTransferToPeers) {
                    System.err.println("Warning: Invalid config for agent " + this.name + ": outputSchema cannot co-exist with agent transfer configurations. Setting disallowTransferToParent=true and disallowTransferToPeers=true.");
                    this.disallowTransferToParent = true;
                    this.disallowTransferToPeers = true;
                }

                if (this.subAgents != null && !this.subAgents.isEmpty()) {
                    throw new IllegalArgumentException("Invalid config for agent " + this.name + ": if outputSchema is set, subAgents must be empty to disable agent transfer.");
                }

                if (this.toolsUnion != null && !this.toolsUnion.isEmpty()) {
                    throw new IllegalArgumentException("Invalid config for agent " + this.name + ": if outputSchema is set, tools must be empty.");
                }
            }

        }

        public DFlowLlmAgent build() {
            this.validate();
            return new DFlowLlmAgent(this);
        }
    }
}
