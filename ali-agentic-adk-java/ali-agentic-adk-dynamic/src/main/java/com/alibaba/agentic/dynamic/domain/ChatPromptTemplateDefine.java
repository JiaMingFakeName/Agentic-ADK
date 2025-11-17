package com.alibaba.agentic.dynamic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 聊天提示词模板定义
 * 用于定义聊天格式的提示词模板配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatPromptTemplateDefine {
    /**
     * 指令模板
     * 仅用于聊天格式(chat)的提示词
     */
    private List<String> instructionTemplate;

    /**
     * 短期记忆之前的prompt模版
     */
    private List<MessageTemplateDefine> preHistoryTemplate;
    
    /**
     * 历史消息格式化器
     * 用于格式化历史对话记录
     */
    private HistoryFormatterDefine historyFormatter;
    
    /**
     * 用户消息模板列表
     * 定义用户输入的消息模板
     */
    private List<MessageTemplateDefine> userTemplate;

    public static ChatPromptTemplateDefineBuilder builder() {
        return new ChatPromptTemplateDefineBuilder();
    }

    public static class ChatPromptTemplateDefineBuilder {
        private List<String> instructionTemplate;
        private HistoryFormatterDefine historyFormatter;
        private List<MessageTemplateDefine> userTemplate;
        private List<MessageTemplateDefine> preHistoryTemplate;

        ChatPromptTemplateDefineBuilder() {
        }

        public ChatPromptTemplateDefineBuilder instructionTemplate(List<String> instructionTemplate) {
            this.instructionTemplate = instructionTemplate;
            return this;
        }

        public ChatPromptTemplateDefineBuilder instructionTemplate(String template) {
            this.instructionTemplate = Collections.singletonList(template);
            return this;
        }


        public ChatPromptTemplateDefineBuilder preHistoryTemplate(List<MessageTemplateDefine> preHistoryTemplate) {
            this.preHistoryTemplate = preHistoryTemplate;
            return this;
        }

        public ChatPromptTemplateDefineBuilder historyFormatter(HistoryFormatterDefine historyFormatter) {
            this.historyFormatter = historyFormatter;
            return this;
        }

        public ChatPromptTemplateDefineBuilder userTemplate(List<MessageTemplateDefine> userTemplate) {
            this.userTemplate = userTemplate;
            return this;
        }

        public ChatPromptTemplateDefineBuilder userTemplate(String template) {
            this.userTemplate = Collections.singletonList(
                MessageTemplateDefine.builder()
                    .type(MessageTemplateDefine.TYPE_TEXT)
                    .template(template)
                    .build()
            );
            return this;
        }

        public ChatPromptTemplateDefine build() {
            return new ChatPromptTemplateDefine(instructionTemplate, preHistoryTemplate, historyFormatter, userTemplate);
        }

        @Override
        public String toString() {
            return "ChatPromptTemplateDefine.ChatPromptTemplateDefineBuilder(instructionTemplate=" + this.instructionTemplate + ", historyFormatter=" + this.historyFormatter + ", userTemplate=" + this.userTemplate + ")";
        }
    }

}
