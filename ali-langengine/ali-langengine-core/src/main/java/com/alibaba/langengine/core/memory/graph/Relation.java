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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 知识图谱中的关系（边）
 *
 * 关系连接两个实体，表示它们之间的语义联系
 *
 * 示例：
 * - (张三, 工作于, 阿里巴巴)
 * - (Java, 是一种, 编程语言)
 * - (杭州, 位于, 中国)
 * - (机器学习, 包含, 深度学习)
 *
 * @author xiaoxuan.lp
 */
@Data
public class Relation {

    /**
     * 关系唯一标识符
     */
    private String id;

    /**
     * 源实体（关系的起点）
     */
    private Entity source;

    /**
     * 目标实体（关系的终点）
     */
    private Entity target;

    /**
     * 关系类型/谓词
     * 例如：工作于、属于、位于、包含、是一种、喜欢等
     */
    private String relationType;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 关系属性（键值对）
     * 例如：开始时间、结束时间、强度等
     */
    private Map<String, Object> properties;

    /**
     * 关系权重/强度（0-1之间）
     * 表示关系的可信度或重要性
     */
    private Double weight;

    /**
     * 创建时间戳
     */
    private Long createdAt;

    /**
     * 最后更新时间戳
     */
    private Long updatedAt;

    /**
     * 是否为双向关系
     */
    private Boolean bidirectional;

    public Relation() {
        this.properties = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.weight = 1.0;
        this.bidirectional = false;
    }

    public Relation(Entity source, String relationType, Entity target) {
        this();
        this.source = source;
        this.target = target;
        this.relationType = relationType;
        this.id = generateId();

        // 增加实体的引用计数
        if (source != null) {
            source.incrementReferenceCount();
        }
        if (target != null) {
            target.incrementReferenceCount();
        }
    }

    public Relation(Entity source, String relationType, Entity target, Double weight) {
        this(source, relationType, target);
        this.weight = weight;
    }

    /**
     * 生成关系ID
     */
    private String generateId() {
        String sourceId = source != null ? source.getId() : "null";
        String targetId = target != null ? target.getId() : "null";
        String type = relationType != null ? relationType : "unknown";

        return String.format("%s-[%s]->%s", sourceId, type, targetId);
    }

    /**
     * 添加属性
     */
    public void addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 获取属性
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    /**
     * 增强关系权重（多次观察到相同关系）
     */
    public void strengthen(double amount) {
        this.weight = Math.min(1.0, this.weight + amount);
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 减弱关系权重（时间衰减或负面证据）
     */
    public void weaken(double amount) {
        this.weight = Math.max(0.0, this.weight - amount);
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 应用时间衰减
     * 长时间未被访问的关系权重逐渐降低
     */
    public void applyTimeDecay(double decayFactor) {
        long daysPassed = (System.currentTimeMillis() - updatedAt) / (24 * 60 * 60 * 1000);
        if (daysPassed > 0) {
            double decayWeight = Math.pow(decayFactor, daysPassed);
            this.weight *= decayWeight;
            this.weight = Math.max(0.1, this.weight); // 最低保留0.1
        }
    }

    /**
     * 刷新关系（重新观察到此关系）
     */
    public void refresh() {
        this.updatedAt = System.currentTimeMillis();
        // 轻微增强权重
        strengthen(0.05);
    }

    /**
     * 检查关系是否涉及指定实体
     */
    public boolean involves(Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity.equals(source) || entity.equals(target);
    }

    /**
     * 获取关系的另一端实体
     */
    public Entity getOtherEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.equals(source)) {
            return target;
        } else if (entity.equals(target)) {
            return source;
        }
        return null;
    }

    /**
     * 获取关系的自然语言描述
     */
    public String toNaturalLanguage() {
        String sourceName = source != null ? source.getName() : "?";
        String targetName = target != null ? target.getName() : "?";
        String type = relationType != null ? relationType : "relates to";

        return String.format("%s %s %s", sourceName, type, targetName);
    }

    /**
     * 检查是否为强关系（权重高于阈值）
     */
    public boolean isStrong(double threshold) {
        return weight != null && weight >= threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(id, relation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("(%s)-[%s:%.2f]->(%s)",
            source != null ? source.getName() : "?",
            relationType,
            weight,
            target != null ? target.getName() : "?");
    }
}
