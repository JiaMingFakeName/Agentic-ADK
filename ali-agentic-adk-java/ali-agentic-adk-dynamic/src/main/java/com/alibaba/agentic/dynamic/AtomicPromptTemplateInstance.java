package com.alibaba.agentic.dynamic;

import com.alibaba.agentic.dynamic.domain.AtomicPromptTemplateDefine;
import com.alibaba.agentic.dynamic.domain.InputFormatterDefine;
import com.alibaba.agentic.dynamic.domain.MessageTemplateDefine;
import com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser;
import com.alibaba.agentic.dynamic.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.*;
import com.google.adk.models.Model;
import com.google.genai.types.*;
import io.reactivex.rxjava3.core.Maybe;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AtomicPromptTemplateInstance {

    public static final String STRINGFIED_STATE = "_StringifiedState";

    //    @Getter
    private final AtomicPromptTemplateDefine template;

    private final MultiSyntaxTemplateEngine engine;

    private boolean disableSetConfigToModel = false;

    private final boolean isChatModel;

    private final Set<String> templateVars;

    private String historyVarName;

    public AtomicPromptTemplateInstance(AtomicPromptTemplateDefine template) {
        this.template = template;
        engine = new MultiSyntaxTemplateEngine(template.getSyntaxType());
        isChatModel = template.getChatPromptTemplate() != null;

        Set<String> varsTodo = new HashSet<>();
        if (!isChatModel) {
            //把raw转成chat
            template.setChatPromptTemplate(new RawTemplateParser(template.getRawPromptTemplate()).transferToChatPrompt());
            historyVarName = (RawTemplateParser.getHistoryVarName(template.getRawPromptTemplate().getTotalPromptTemplate()));
        }
        if (template.getChatPromptTemplate().getInstructionTemplate() != null) {
            template.getChatPromptTemplate().getInstructionTemplate().forEach(t -> varsTodo.addAll(engine.extractVariables(t)));
        }
        if (template.getChatPromptTemplate().getUserTemplate() != null) {
            template.getChatPromptTemplate().getUserTemplate().stream().map(MessageTemplateDefine::getTemplate).forEach(
                    t -> varsTodo.addAll(engine.extractVariables(t))
            );
        }
        templateVars = varsTodo;
    }

    public boolean needBasic() {
        return template.getModel() != null;
    }

    private String getModelName() {
        if (template.getModel().getName() != null) {
            return template.getModel().getIdentifier() == null ?
                    template.getModel().getName() :
                    template.getModel().getIdentifier();
        }
        return null;
    }

    public LlmRequest.Builder patchBasicBuilder(RequestProcessor.RequestProcessingResult r) {
        LlmRequest.Builder builder = r.updatedRequest().toBuilder();
        if (template.getModel().getName() != null) {
            builder = builder.model(getModelName());
        }
        if (template.getModel().getExtraConfigs() != null) {
            Map<String, Object> extraConfigs = template.getModel().getExtraConfigs();
            GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();


            if(extraConfigs.containsKey("max_tokens")){
                extraConfigs.put("maxOutputTokens", extraConfigs.get("max_tokens"));
            }

            // 使用配置映射工具动态设置所有extraConfigs中的参数
            ConfigMapperUtil.applyConfigs(configBuilder, extraConfigs);

            //其他字段特殊处理
            String thinkingBudget = null;
            if(extraConfigs.containsKey("thinking_budget")) {
                thinkingBudget = extraConfigs.get("thinking_budget").toString();
            }
            if(extraConfigs.containsKey("thinkingBudget")) {
                thinkingBudget = extraConfigs.get("thinkingBudget").toString();
            }
            if(thinkingBudget != null) {
                configBuilder.thinkingConfig(ThinkingConfig.builder()
                        .includeThoughts(true)
                        .thinkingBudget(Integer.valueOf(thinkingBudget)).build());
            }

            JSONArray stopSequences = null;
            if(extraConfigs.containsKey("stop_sequences")) {
                stopSequences = JSON.parseArray(extraConfigs.get("stop_sequences").toString());
            }
            if(extraConfigs.containsKey("stopSequences")) {
                stopSequences = JSON.parseArray("stopSequences");
            }
            if(stopSequences != null) {
                List<String> stops = stopSequences.stream().map(String::valueOf).collect(Collectors.toList());
                configBuilder.stopSequences(stops);
            }
            builder = builder.config(configBuilder.build());
        }
        return builder;
    }

    public List<String> generateInstruction(InvocationContext invocationContext) {

        Map<String, Object> vars = (Map<String, Object>) invocationContext.session().state().get(STRINGFIED_STATE);

        return template.getChatPromptTemplate().getInstructionTemplate().stream()
                .map(t -> engine.render(t, vars)).collect(Collectors.toList());
    }

    public List<Content> generatePreHistoryContents(InvocationContext invocationContext, List<Object> objects) {
        Map<String, Object> vars = (Map<String, Object>) invocationContext.session().state().get(STRINGFIED_STATE);

//        if (isChatModel) {
            return template.getChatPromptTemplate().getPreHistoryTemplate().stream().map(
                    messageTemplateDefine -> {
                        return generateContentByTemplate(invocationContext, messageTemplateDefine, vars);
                    }
            ).collect(Collectors.toList());
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }

    public List<Content> generateHistory(InvocationContext invocationContext, List<Content> history) {

        if (isChatModel) {
            return HistoryFormatterExecutor.formatHistory(invocationContext, template.getChatPromptTemplate().getHistoryFormatter(), history);
        } else {
//            InputFormatterExecutor.formatInput(invocationContext, )
            //TODO
            return Collections.emptyList();
        }
    }

    public List<Content> generateUserInput(InvocationContext invocationContext) {
        Map<String, Object> vars = (Map<String, Object>) invocationContext.session().state().get(STRINGFIED_STATE);

//        if (isChatModel) {

            return template.getChatPromptTemplate().getUserTemplate().stream().map(
                    messageTemplateDefine -> {
                        return generateContentByTemplate(invocationContext, messageTemplateDefine, vars);
                    }
            ).collect(Collectors.toList());
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }

    private Content generateContentByTemplate(InvocationContext invocationContext, MessageTemplateDefine messageTemplateDefine, Map<String, Object> vars) {
        String template = messageTemplateDefine.getTemplate();

        String value = engine.render(template, vars);
        if(messageTemplateDefine.getType() == null){
            messageTemplateDefine.setType(MessageTemplateDefine.TYPE_TEXT);
        }
        switch (messageTemplateDefine.getType()) {
            case MessageTemplateDefine.TYPE_TEXT:
                return Content.builder()
                        .role(messageTemplateDefine.getRole() == null ? "user" : messageTemplateDefine.getRole())
                        .parts(Part.builder().text(value).build()).build();
            case MessageTemplateDefine.TYPE_BLOB:
            case MessageTemplateDefine.TYPE_FILE:
                return invocationContext.userContent().orElse(Content.builder()
                        .role("user")
                        .parts(Part.builder().text("no file").build()).build());
            case MessageTemplateDefine.TYPE_PARTIAL_TEXT:
                //TODO
                return Content.builder().
                        role("model")
                        .parts(Part.builder().text(value).build()).build();
            default:
                throw new NotImplementedException();
        }
    }

    public boolean needInstruction() {
//        if (isChatModel) {
            return template.getChatPromptTemplate().getInstructionTemplate() != null;
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }

    public boolean needContents() {
        return needHistory() || needUserContent();
    }

    public boolean needHistory() {
//        if (isChatModel) {
            return template.getChatPromptTemplate().getHistoryFormatter() != null;
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }

    public boolean needPreHistoryContent() {
//        if (isChatModel) {
            return template.getChatPromptTemplate().getPreHistoryTemplate() != null;
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }


    public boolean needUserContent() {
//        if (isChatModel) {
            return template.getChatPromptTemplate().getUserTemplate() != null;
//        } else {
//            TODO
//            throw new NotImplementedException();
//        }
    }

    public void formatStates(InvocationContext context) {
        HashMap<String, Object> state = new HashMap<String, Object>();
        state.putAll(context.session().state());
        HashMap<String, Object> stringValues = new HashMap<String, Object>();

        if (context.userContent().isPresent()) {
            setUserInput(context.userContent().get(), state);
        }

        if (template.getInputFormatter() != null) {
            for (InputFormatterDefine inputFormatter : template.getInputFormatter()) {
                Object original = state.get(inputFormatter.getInputVariableName());
                if (original == null) {
                    continue;
                }
                stringValues.put(inputFormatter.getInputVariableName(),
                        formatInput(context, inputFormatter, original, state));
            }
        }
        for (String key : templateVars) {
            if (stringValues.containsKey(key)) {
                continue;
            }
            if (state.containsKey(key)) {
                stringValues.put(key, JSON.toJSONString(state.get(key)));
            }
        }
        context.session().state().put(STRINGFIED_STATE, stringValues);
    }

    private static void setUserInput(Content content, HashMap<String, Object> state) {
        /**
         * 原始userContent
         */
        if (!state.containsKey("userContent")) {
            state.put("userContent", content);
        }
        /**
         * 简化input
         */
        if (!state.containsKey("input")) {
            state.put("input", content.text());
        }
    }

    private String formatInput(InvocationContext invocationContext, InputFormatterDefine inputFormatter, Object original, Map<String, Object> context) {
        return InputFormatterExecutor.formatInput(invocationContext, inputFormatter, original, context);
    }

    public boolean needOutputParser() {
        return template.getOutputParser() != null;
    }

    public Maybe<LlmResponse> parseOutput(CallbackContext callbackContext, LlmResponse llmResponse) {
        if (template.getOutputParser() == null) {
            return Maybe.empty();
        }
        Maybe<LlmResponse> res = OutputParserExecutor.parseOutput(callbackContext, template.getOutputParser(), llmResponse);
        return res;
    }


    public Model getModel() {
        if (getModelName() != null) {
            BaseLlm resolvedLlm = LlmRegistry.getLlm(getModelName());
            // try to set extraConfigs with llm properties
            if(!disableSetConfigToModel) {
                ConfigMapperUtil.applyConfigs(resolvedLlm, template.getModel().getExtraConfigs());
            }
            return Model.builder().modelName(template.getModel().getName()).model(resolvedLlm).build();
        }
        return null;
    }

    public void clearAgent() {
        CumulativeOutputParser.reset();
    }

    public boolean isDisableSetConfigToModel() {
        return disableSetConfigToModel;
    }

    public void setDisableSetConfigToModel(boolean disableSetConfigToModel) {
        this.disableSetConfigToModel = disableSetConfigToModel;
    }
}
