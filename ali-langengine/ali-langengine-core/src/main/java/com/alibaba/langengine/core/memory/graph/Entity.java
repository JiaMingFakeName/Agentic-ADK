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
 * 知识图谱中的实体
 *
 * 实体是知识图谱的节点，代表现实世界中的对象、概念或事件
 *
 * 示例：
 * - 人物：张三、李四
 * - 组织：阿里巴巴、Google
 * - 地点：杭州、北京
 * - 概念：机器学习、Java
 * - 事件：产品发布会、会议
 *
 * @author xiaoxuan.lp
 */
@Data
public class Entity {

    /**
     * 实体唯一标识符
     */
    private String id;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体类型（如：Person, Organization, Location, Concept, Event等）
     */
    private String type;

    /**
     * 实体描述
     */
    private String description;

    /**
     * 实体属性（键值对）
     * 例如：年龄、职位、成立时间等
     */
    private Map<String, Object> properties;

    /**
     * 创建时间戳
     */
    private Long createdAt;

    /**
     * 最后更新时间戳
     */
    private Long updatedAt;

    /**
     * 实体重要性评分（0-1之间）
     */
    private Double importance;

    /**
     * 被引用次数（用于评估重要性）
     */
    private Integer referenceCount;

    public Entity() {
        this.properties = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.importance = 0.5;
        this.referenceCount = 0;
    }

    public Entity(String name, String type) {
        this();
        this.id = generateId(name, type);
        this.name = name;
        this.type = type;
    }

    public Entity(String name, String type, String description) {
        this(name, type);
        this.description = description;
    }

    /**
     * 生成实体ID（基于名称和类型）
     */
    private String generateId(String name, String type) {
        return type + ":" + name.toLowerCase().replaceAll("\\s+", "_");
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
     * 增加引用计数
     */
    public void incrementReferenceCount() {
        this.referenceCount++;
        this.updatedAt = System.currentTimeMillis();

        // 根据引用次数调整重要性
        updateImportance();
    }

    /**
     * 更新重要性评分
     * 基于引用次数和时间衰减
     */
    private void updateImportance() {
        // 基础分：引用次数的对数归一化
        double baseScore = Math.log(referenceCount + 1) / Math.log(100);
        baseScore = Math.min(0.9, baseScore);

        // 时间衰减：新实体略微加分
        long daysPassed = (System.currentTimeMillis() - createdAt) / (24 * 60 * 60 * 1000);
        double timeBonus = Math.max(0, 0.1 * Math.exp(-daysPassed / 30.0));

        this.importance = Math.min(1.0, baseScore + timeBonus);
    }

    /**
     * 合并另一个实体的信息
     * 用于处理同一实体的多次提及
     */
    public void merge(Entity other) {
        if (other == null || !this.id.equals(other.id)) {
            return;
        }

        // 合并描述
        if (other.description != null && !other.description.isEmpty()) {
            if (this.description == null || this.description.isEmpty()) {
                this.description = other.description;
            } else {
                this.description += "; " + other.description;
            }
        }

        // 合并属性
        if (other.properties != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.putAll(other.properties);
        }

        // 累加引用次数
        this.referenceCount += other.referenceCount;

        // 更新时间戳
        this.updatedAt = System.currentTimeMillis();

        // 重新计算重要性
        updateImportance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Entity{name='%s', type='%s', importance=%.2f, refs=%d}",
            name, type, importance, referenceCount);
    }
}
