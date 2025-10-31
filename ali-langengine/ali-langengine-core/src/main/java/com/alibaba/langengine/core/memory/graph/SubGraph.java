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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 子图
 *
 * 表示知识图谱的一个子集，包含相关的实体和关系
 *
 * @author xiaoxuan.lp
 */
@Data
public class SubGraph {

    /**
     * 中心实体（子图的起点）
     */
    private Entity centerEntity;

    /**
     * 子图包含的所有实体
     */
    private List<Entity> entities;

    /**
     * 子图包含的所有关系
     */
    private List<Relation> relations;

    /**
     * 子图的深度（从中心实体的跳数）
     */
    private int depth;

    public SubGraph() {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public SubGraph(Entity centerEntity, int depth) {
        this();
        this.centerEntity = centerEntity;
        this.depth = depth;
    }

    /**
     * 添加实体到子图
     */
    public void addEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    /**
     * 添加关系到子图
     */
    public void addRelation(Relation relation) {
        if (relation != null && !relations.contains(relation)) {
            relations.add(relation);
        }
    }

    /**
     * 获取子图的自然语言描述
     */
    public String toNaturalLanguage() {
        if (centerEntity == null) {
            return "Empty subgraph";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("关于 '%s' 的知识:\n", centerEntity.getName()));

        // 按关系类型分组
        relations.stream()
            .filter(r -> r.involves(centerEntity))
            .forEach(r -> {
                sb.append("- ").append(r.toNaturalLanguage()).append("\n");
            });

        return sb.toString();
    }

    /**
     * 获取子图的摘要统计
     */
    public String getSummary() {
        return String.format("SubGraph{center='%s', entities=%d, relations=%d, depth=%d}",
            centerEntity != null ? centerEntity.getName() : "null",
            entities.size(),
            relations.size(),
            depth);
    }

    /**
     * 检查子图是否为空
     */
    public boolean isEmpty() {
        return entities.isEmpty() && relations.isEmpty();
    }

    /**
     * 合并另一个子图
     */
    public void merge(SubGraph other) {
        if (other == null) {
            return;
        }

        for (Entity entity : other.getEntities()) {
            addEntity(entity);
        }

        for (Relation relation : other.getRelations()) {
            addRelation(relation);
        }
    }
}
