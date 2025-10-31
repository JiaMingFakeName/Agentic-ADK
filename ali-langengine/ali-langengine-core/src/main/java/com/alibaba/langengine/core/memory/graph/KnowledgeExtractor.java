/**
 * Copyright (C) 2025 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.memory.graph;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import lombok.Data;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识抽取器
 *
 * 从对话文本中自动抽取实体和关系，构建知识图谱
 *
 * 支持两种模式：
 * 1. LLM模式：使用大语言模型进行智能抽取（更准确）
 * 2. 规则模式：使用正则表达式和规则匹配（更快速）
 *
 * 抽取内容：
 * - 实体：人物、组织、地点、概念、产品等
 * - 关系：工作于、位于、包含、属于、喜欢等
 *
 * @author xiaoxuan.lp
 */
@Data
public class KnowledgeExtractor {

    /**
     * 语言模型（用于智能抽取）
     */
    private BaseLanguageModel llm;

    /**
     * 是否启用LLM抽取
     */
    private boolean useLLM = true;

    /**
     * 抽取结果
     */
    @Data
    public static class ExtractionResult {
        private List<Entity> entities = new ArrayList<>();
        private List<Relation> relations = new ArrayList<>();
    }

    /**
     * LLM抽取提示模板
     */
    private static final String EXTRACTION_PROMPT =
        "从以下对话中抽取知识三元组（实体-关系-实体）。\n\n" +
        "对话内容：\n{text}\n\n" +
        "请识别对话中的实体和它们之间的关系，按以下格式输出：\n" +
        "实体：[名称|类型|描述]\n" +
        "关系：[源实体|关系类型|目标实体]\n\n" +
        "实体类型包括：Person（人物）、Organization（组织）、Location（地点）、Concept（概念）、Product（产品）、Event（事件）等。\n" +
        "关系类型包括：工作于、属于、位于、包含、是一种、喜欢、参与、使用等。\n\n" +
        "示例：\n" +
        "实体：[张三|Person|软件工程师]\n" +
        "实体：[阿里巴巴|Organization|科技公司]\n" +
        "关系：[张三|工作于|阿里巴巴]\n\n" +
        "请开始抽取：";

    public KnowledgeExtractor(BaseLanguageModel llm) {
        this.llm = llm;
        this.useLLM = (llm != null);
    }

    public KnowledgeExtractor() {
        this.useLLM = false;
    }

    /**
     * 从对话消息中抽取知识
     */
    public ExtractionResult extractFromMessages(List<BaseMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ExtractionResult();
        }

        // 将消息转换为文本
        StringBuilder text = new StringBuilder();
        for (BaseMessage message : messages) {
            text.append(message.getContent()).append("\n");
        }

        return extractFromText(text.toString());
    }

    /**
     * 从文本中抽取知识
     */
    public ExtractionResult extractFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ExtractionResult();
        }

        if (useLLM && llm != null) {
            try {
                return extractWithLLM(text);
            } catch (Exception e) {
                // LLM失败时降级到规则抽取
                return extractWithRules(text);
            }
        } else {
            return extractWithRules(text);
        }
    }

    /**
     * 使用LLM进行知识抽取
     */
    private ExtractionResult extractWithLLM(String text) {
        ExtractionResult result = new ExtractionResult();

        // 构建提示
        Map<String, Object> variables = new HashMap<>();
        variables.put("text", text);

        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(EXTRACTION_PROMPT);
        String prompt = promptTemplate.format(variables);

        // 调用LLM
        String response = llm.predict(prompt);

        // 解析LLM响应
        parseExtractionResponse(response, result);

        return result;
    }

    /**
     * 解析LLM的抽取结果
     */
    private void parseExtractionResponse(String response, ExtractionResult result) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Map<String, Entity> entityMap = new HashMap<>();

        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();

            // 解析实体：[名称|类型|描述]
            if (line.startsWith("实体：") || line.startsWith("Entity:")) {
                Entity entity = parseEntityLine(line);
                if (entity != null) {
                    entityMap.put(entity.getName(), entity);
                    result.getEntities().add(entity);
                }
            }
            // 解析关系：[源实体|关系类型|目标实体]
            else if (line.startsWith("关系：") || line.startsWith("Relation:")) {
                Relation relation = parseRelationLine(line, entityMap);
                if (relation != null) {
                    result.getRelations().add(relation);
                }
            }
        }
    }

    /**
     * 解析实体行
     */
    private Entity parseEntityLine(String line) {
        // 提取 [名称|类型|描述]
        Pattern pattern = Pattern.compile("\\[([^|]+)\\|([^|]+)\\|([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String name = matcher.group(1).trim();
            String type = matcher.group(2).trim();
            String description = matcher.group(3).trim();

            return new Entity(name, type, description);
        }

        return null;
    }

    /**
     * 解析关系行
     */
    private Relation parseRelationLine(String line, Map<String, Entity> entityMap) {
        // 提取 [源实体|关系类型|目标实体]
        Pattern pattern = Pattern.compile("\\[([^|]+)\\|([^|]+)\\|([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String sourceName = matcher.group(1).trim();
            String relationType = matcher.group(2).trim();
            String targetName = matcher.group(3).trim();

            Entity source = entityMap.get(sourceName);
            Entity target = entityMap.get(targetName);

            if (source != null && target != null) {
                return new Relation(source, relationType, target);
            }
        }

        return null;
    }

    /**
     * 使用规则进行知识抽取（简单版本）
     */
    private ExtractionResult extractWithRules(String text) {
        ExtractionResult result = new ExtractionResult();
        Map<String, Entity> entityMap = new HashMap<>();

        // 规则1: 提取"X是Y"模式 -> (X, 是一种, Y)
        extractIsAPattern(text, result, entityMap);

        // 规则2: 提取"X在Y"模式 -> (X, 位于, Y)
        extractLocationPattern(text, result, entityMap);

        // 规则3: 提取"X属于Y"模式
        extractBelongsToPattern(text, result, entityMap);

        // 规则4: 提取"X工作于Y"模式
        extractWorksAtPattern(text, result, entityMap);

        // 规则5: 提取"X包含Y"模式
        extractContainsPattern(text, result, entityMap);

        return result;
    }

    /**
     * 提取"是"关系模式
     */
    private void extractIsAPattern(String text, ExtractionResult result, Map<String, Entity> entityMap) {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z]+)是([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String source = matcher.group(1).trim();
            String target = matcher.group(2).trim();

            Entity sourceEntity = getOrCreateEntity(source, "Concept", entityMap, result);
            Entity targetEntity = getOrCreateEntity(target, "Concept", entityMap, result);

            Relation relation = new Relation(sourceEntity, "是一种", targetEntity);
            result.getRelations().add(relation);
        }
    }

    /**
     * 提取"在/位于"关系模式
     */
    private void extractLocationPattern(String text, ExtractionResult result, Map<String, Entity> entityMap) {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z]+)(在|位于)([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String source = matcher.group(1).trim();
            String target = matcher.group(3).trim();

            Entity sourceEntity = getOrCreateEntity(source, "Entity", entityMap, result);
            Entity targetEntity = getOrCreateEntity(target, "Location", entityMap, result);

            Relation relation = new Relation(sourceEntity, "位于", targetEntity);
            result.getRelations().add(relation);
        }
    }

    /**
     * 提取"属于"关系模式
     */
    private void extractBelongsToPattern(String text, ExtractionResult result, Map<String, Entity> entityMap) {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z]+)属于([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String source = matcher.group(1).trim();
            String target = matcher.group(2).trim();

            Entity sourceEntity = getOrCreateEntity(source, "Entity", entityMap, result);
            Entity targetEntity = getOrCreateEntity(target, "Organization", entityMap, result);

            Relation relation = new Relation(sourceEntity, "属于", targetEntity);
            result.getRelations().add(relation);
        }
    }

    /**
     * 提取"工作于"关系模式
     */
    private void extractWorksAtPattern(String text, ExtractionResult result, Map<String, Entity> entityMap) {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z]+)工作于([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String source = matcher.group(1).trim();
            String target = matcher.group(2).trim();

            Entity sourceEntity = getOrCreateEntity(source, "Person", entityMap, result);
            Entity targetEntity = getOrCreateEntity(target, "Organization", entityMap, result);

            Relation relation = new Relation(sourceEntity, "工作于", targetEntity);
            result.getRelations().add(relation);
        }
    }

    /**
     * 提取"包含"关系模式
     */
    private void extractContainsPattern(String text, ExtractionResult result, Map<String, Entity> entityMap) {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z]+)包含([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String source = matcher.group(1).trim();
            String target = matcher.group(2).trim();

            Entity sourceEntity = getOrCreateEntity(source, "Concept", entityMap, result);
            Entity targetEntity = getOrCreateEntity(target, "Concept", entityMap, result);

            Relation relation = new Relation(sourceEntity, "包含", targetEntity);
            result.getRelations().add(relation);
        }
    }

    /**
     * 获取或创建实体
     */
    private Entity getOrCreateEntity(String name, String type,
                                    Map<String, Entity> entityMap,
                                    ExtractionResult result) {
        Entity entity = entityMap.get(name);
        if (entity == null) {
            entity = new Entity(name, type);
            entityMap.put(name, entity);
            result.getEntities().add(entity);
        }
        return entity;
    }

    /**
     * 从结构化数据中抽取知识
     * 用于将已知的结构化信息转换为知识图谱格式
     */
    public ExtractionResult extractFromStructuredData(Map<String, Object> data) {
        ExtractionResult result = new ExtractionResult();

        // 这里可以根据特定的数据结构进行抽取
        // 示例实现留给具体应用场景定制

        return result;
    }

    /**
     * 合并多个抽取结果
     */
    public static ExtractionResult merge(List<ExtractionResult> results) {
        ExtractionResult merged = new ExtractionResult();
        Map<String, Entity> entityMap = new HashMap<>();

        for (ExtractionResult result : results) {
            // 合并实体
            for (Entity entity : result.getEntities()) {
                Entity existing = entityMap.get(entity.getId());
                if (existing != null) {
                    existing.merge(entity);
                } else {
                    entityMap.put(entity.getId(), entity);
                    merged.getEntities().add(entity);
                }
            }

            // 添加关系
            merged.getRelations().addAll(result.getRelations());
        }

        return merged;
    }
}
