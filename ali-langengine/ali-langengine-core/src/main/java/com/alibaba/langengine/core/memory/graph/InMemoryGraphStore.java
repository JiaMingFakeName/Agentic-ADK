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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存图谱存储实现
 *
 * 基于内存的知识图谱存储，适合小到中等规模的知识图谱
 *
 * 特点：
 * - 快速读写，无需外部依赖
 * - 线程安全（使用ConcurrentHashMap）
 * - 适合原型开发和测试
 * - 数据不持久化（进程结束后丢失）
 *
 * 数据结构：
 * - entities: Map<entityId, Entity>
 * - relations: Map<relationId, Relation>
 * - entityRelations: Map<entityId, Set<relationId>> - 实体关系索引
 * - typeIndex: Map<entityType, Set<entityId>> - 类型索引
 * - relationTypeIndex: Map<relationType, Set<relationId>> - 关系类型索引
 *
 * @author xiaoxuan.lp
 */
public class InMemoryGraphStore implements GraphStore {

    /**
     * 实体存储
     */
    private final Map<String, Entity> entities;

    /**
     * 关系存储
     */
    private final Map<String, Relation> relations;

    /**
     * 实体关系索引（实体ID -> 关系ID集合）
     */
    private final Map<String, Set<String>> entityRelations;

    /**
     * 实体类型索引（类型 -> 实体ID集合）
     */
    private final Map<String, Set<String>> typeIndex;

    /**
     * 关系类型索引（关系类型 -> 关系ID集合）
     */
    private final Map<String, Set<String>> relationTypeIndex;

    public InMemoryGraphStore() {
        this.entities = new ConcurrentHashMap<>();
        this.relations = new ConcurrentHashMap<>();
        this.entityRelations = new ConcurrentHashMap<>();
        this.typeIndex = new ConcurrentHashMap<>();
        this.relationTypeIndex = new ConcurrentHashMap<>();
    }

    // ==================== 实体操作 ====================

    @Override
    public void addEntity(Entity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        // 检查是否已存在，如果存在则合并
        Entity existing = entities.get(entity.getId());
        if (existing != null) {
            existing.merge(entity);
        } else {
            entities.put(entity.getId(), entity);

            // 更新类型索引
            if (entity.getType() != null) {
                typeIndex.computeIfAbsent(entity.getType(), k -> ConcurrentHashMap.newKeySet())
                    .add(entity.getId());
            }
        }
    }

    @Override
    public void addEntities(List<Entity> entities) {
        if (entities != null) {
            entities.forEach(this::addEntity);
        }
    }

    @Override
    public Entity getEntity(String entityId) {
        return entities.get(entityId);
    }

    @Override
    public Entity getEntityByNameAndType(String name, String type) {
        if (name == null || type == null) {
            return null;
        }

        String id = type + ":" + name.toLowerCase().replaceAll("\\s+", "_");
        return entities.get(id);
    }

    @Override
    public void updateEntity(Entity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        Entity existing = entities.get(entity.getId());
        if (existing != null) {
            existing.merge(entity);
        } else {
            addEntity(entity);
        }
    }

    @Override
    public void deleteEntity(String entityId) {
        Entity entity = entities.remove(entityId);
        if (entity == null) {
            return;
        }

        // 删除类型索引
        if (entity.getType() != null) {
            Set<String> typeSet = typeIndex.get(entity.getType());
            if (typeSet != null) {
                typeSet.remove(entityId);
            }
        }

        // 删除所有相关的关系
        Set<String> relatedRelations = entityRelations.get(entityId);
        if (relatedRelations != null) {
            new HashSet<>(relatedRelations).forEach(this::deleteRelation);
            entityRelations.remove(entityId);
        }
    }

    @Override
    public List<Entity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public List<Entity> getEntitiesByType(String type) {
        Set<String> entityIds = typeIndex.get(type);
        if (entityIds == null) {
            return new ArrayList<>();
        }

        return entityIds.stream()
            .map(entities::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Entity> searchEntities(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }

        String lowerKeyword = keyword.toLowerCase();
        return entities.values().stream()
            .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }

    // ==================== 关系操作 ====================

    @Override
    public void addRelation(Relation relation) {
        if (relation == null || relation.getId() == null) {
            return;
        }

        relations.put(relation.getId(), relation);

        // 更新实体关系索引
        if (relation.getSource() != null) {
            entityRelations.computeIfAbsent(relation.getSource().getId(), k -> ConcurrentHashMap.newKeySet())
                .add(relation.getId());
        }
        if (relation.getTarget() != null) {
            entityRelations.computeIfAbsent(relation.getTarget().getId(), k -> ConcurrentHashMap.newKeySet())
                .add(relation.getId());
        }

        // 更新关系类型索引
        if (relation.getRelationType() != null) {
            relationTypeIndex.computeIfAbsent(relation.getRelationType(), k -> ConcurrentHashMap.newKeySet())
                .add(relation.getId());
        }
    }

    @Override
    public void addRelations(List<Relation> relations) {
        if (relations != null) {
            relations.forEach(this::addRelation);
        }
    }

    @Override
    public Relation getRelation(String relationId) {
        return relations.get(relationId);
    }

    @Override
    public void updateRelation(Relation relation) {
        if (relation == null || relation.getId() == null) {
            return;
        }

        if (relations.containsKey(relation.getId())) {
            relations.put(relation.getId(), relation);
        } else {
            addRelation(relation);
        }
    }

    @Override
    public void deleteRelation(String relationId) {
        Relation relation = relations.remove(relationId);
        if (relation == null) {
            return;
        }

        // 从实体关系索引中删除
        if (relation.getSource() != null) {
            Set<String> sourceRelations = entityRelations.get(relation.getSource().getId());
            if (sourceRelations != null) {
                sourceRelations.remove(relationId);
            }
        }
        if (relation.getTarget() != null) {
            Set<String> targetRelations = entityRelations.get(relation.getTarget().getId());
            if (targetRelations != null) {
                targetRelations.remove(relationId);
            }
        }

        // 从关系类型索引中删除
        if (relation.getRelationType() != null) {
            Set<String> typeSet = relationTypeIndex.get(relation.getRelationType());
            if (typeSet != null) {
                typeSet.remove(relationId);
            }
        }
    }

    @Override
    public List<Relation> getAllRelations() {
        return new ArrayList<>(relations.values());
    }

    @Override
    public List<Relation> getRelationsByEntity(String entityId) {
        Set<String> relationIds = entityRelations.get(entityId);
        if (relationIds == null) {
            return new ArrayList<>();
        }

        return relationIds.stream()
            .map(relations::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Relation> getOutgoingRelations(String sourceEntityId) {
        Set<String> relationIds = entityRelations.get(sourceEntityId);
        if (relationIds == null) {
            return new ArrayList<>();
        }

        return relationIds.stream()
            .map(relations::get)
            .filter(Objects::nonNull)
            .filter(r -> r.getSource() != null && r.getSource().getId().equals(sourceEntityId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Relation> getIncomingRelations(String targetEntityId) {
        Set<String> relationIds = entityRelations.get(targetEntityId);
        if (relationIds == null) {
            return new ArrayList<>();
        }

        return relationIds.stream()
            .map(relations::get)
            .filter(Objects::nonNull)
            .filter(r -> r.getTarget() != null && r.getTarget().getId().equals(targetEntityId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Relation> getRelationsByType(String relationType) {
        Set<String> relationIds = relationTypeIndex.get(relationType);
        if (relationIds == null) {
            return new ArrayList<>();
        }

        return relationIds.stream()
            .map(relations::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Relation> getRelationsBetween(String sourceId, String targetId) {
        Set<String> relationIds = entityRelations.get(sourceId);
        if (relationIds == null) {
            return new ArrayList<>();
        }

        return relationIds.stream()
            .map(relations::get)
            .filter(Objects::nonNull)
            .filter(r -> r.getSource() != null && r.getSource().getId().equals(sourceId))
            .filter(r -> r.getTarget() != null && r.getTarget().getId().equals(targetId))
            .collect(Collectors.toList());
    }

    // ==================== 图谱查询 ====================

    @Override
    public List<Entity> getNeighbors(String entityId) {
        return getNeighbors(entityId, 1);
    }

    @Override
    public List<Entity> getNeighbors(String entityId, int hops) {
        Set<Entity> neighbors = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.offer(entityId);
        visited.add(entityId);

        for (int hop = 0; hop < hops && !queue.isEmpty(); hop++) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                String currentId = queue.poll();
                List<Relation> relations = getRelationsByEntity(currentId);

                for (Relation relation : relations) {
                    Entity neighbor = relation.getOtherEntity(entities.get(currentId));
                    if (neighbor != null && !visited.contains(neighbor.getId())) {
                        neighbors.add(neighbor);
                        visited.add(neighbor.getId());
                        queue.offer(neighbor.getId());
                    }
                }
            }
        }

        return new ArrayList<>(neighbors);
    }

    @Override
    public SubGraph getSubGraph(String entityId, int depth) {
        Entity centerEntity = entities.get(entityId);
        if (centerEntity == null) {
            return new SubGraph();
        }

        SubGraph subGraph = new SubGraph(centerEntity, depth);
        Set<String> visitedEntities = new HashSet<>();
        Set<String> visitedRelations = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.offer(entityId);
        visitedEntities.add(entityId);
        subGraph.addEntity(centerEntity);

        for (int d = 0; d < depth && !queue.isEmpty(); d++) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                String currentId = queue.poll();
                List<Relation> relations = getRelationsByEntity(currentId);

                for (Relation relation : relations) {
                    if (!visitedRelations.contains(relation.getId())) {
                        subGraph.addRelation(relation);
                        visitedRelations.add(relation.getId());

                        Entity neighbor = relation.getOtherEntity(entities.get(currentId));
                        if (neighbor != null && !visitedEntities.contains(neighbor.getId())) {
                            subGraph.addEntity(neighbor);
                            visitedEntities.add(neighbor.getId());
                            queue.offer(neighbor.getId());
                        }
                    }
                }
            }
        }

        return subGraph;
    }

    @Override
    public List<Object> findShortestPath(String sourceId, String targetId) {
        if (sourceId == null || targetId == null) {
            return new ArrayList<>();
        }

        if (sourceId.equals(targetId)) {
            Entity entity = entities.get(sourceId);
            return entity != null ? Arrays.asList(entity) : new ArrayList<>();
        }

        // BFS查找最短路径
        Queue<String> queue = new LinkedList<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, Relation> previousRelation = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.offer(sourceId);
        visited.add(sourceId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (currentId.equals(targetId)) {
                // 构建路径
                return buildPath(sourceId, targetId, previous, previousRelation);
            }

            List<Relation> relations = getOutgoingRelations(currentId);
            for (Relation relation : relations) {
                Entity target = relation.getTarget();
                if (target != null && !visited.contains(target.getId())) {
                    visited.add(target.getId());
                    previous.put(target.getId(), currentId);
                    previousRelation.put(target.getId(), relation);
                    queue.offer(target.getId());
                }
            }
        }

        return new ArrayList<>(); // 没有找到路径
    }

    /**
     * 构建路径
     */
    private List<Object> buildPath(String sourceId, String targetId,
                                   Map<String, String> previous,
                                   Map<String, Relation> previousRelation) {
        List<Object> path = new ArrayList<>();
        String current = targetId;

        while (current != null && !current.equals(sourceId)) {
            Entity entity = entities.get(current);
            Relation relation = previousRelation.get(current);

            if (entity == null || relation == null) {
                break;
            }

            path.add(0, entity);
            path.add(0, relation);
            current = previous.get(current);
        }

        Entity sourceEntity = entities.get(sourceId);
        if (sourceEntity != null) {
            path.add(0, sourceEntity);
        }

        return path;
    }

    @Override
    public List<List<Object>> findAllPaths(String sourceId, String targetId, int maxDepth) {
        List<List<Object>> allPaths = new ArrayList<>();
        List<Object> currentPath = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        Entity sourceEntity = entities.get(sourceId);
        if (sourceEntity != null) {
            currentPath.add(sourceEntity);
            visited.add(sourceId);
            dfsAllPaths(sourceId, targetId, maxDepth, 0, currentPath, visited, allPaths);
        }

        return allPaths;
    }

    /**
     * DFS查找所有路径
     */
    private void dfsAllPaths(String currentId, String targetId, int maxDepth, int currentDepth,
                            List<Object> currentPath, Set<String> visited, List<List<Object>> allPaths) {
        if (currentDepth >= maxDepth) {
            return;
        }

        if (currentId.equals(targetId)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }

        List<Relation> relations = getOutgoingRelations(currentId);
        for (Relation relation : relations) {
            Entity target = relation.getTarget();
            if (target != null && !visited.contains(target.getId())) {
                currentPath.add(relation);
                currentPath.add(target);
                visited.add(target.getId());

                dfsAllPaths(target.getId(), targetId, maxDepth, currentDepth + 1, currentPath, visited, allPaths);

                visited.remove(target.getId());
                currentPath.remove(currentPath.size() - 1);
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    // ==================== 统计和分析 ====================

    @Override
    public int getEntityCount() {
        return entities.size();
    }

    @Override
    public int getRelationCount() {
        return relations.size();
    }

    @Override
    public int getEntityDegree(String entityId) {
        Set<String> relationIds = entityRelations.get(entityId);
        return relationIds != null ? relationIds.size() : 0;
    }

    @Override
    public List<Entity> getTopEntities(int topN) {
        return entities.values().stream()
            .sorted((e1, e2) -> {
                // 首先按重要性排序
                int importanceCompare = Double.compare(
                    e2.getImportance() != null ? e2.getImportance() : 0.0,
                    e1.getImportance() != null ? e1.getImportance() : 0.0
                );
                if (importanceCompare != 0) {
                    return importanceCompare;
                }
                // 其次按度数排序
                return Integer.compare(getEntityDegree(e2.getId()), getEntityDegree(e1.getId()));
            })
            .limit(topN)
            .collect(Collectors.toList());
    }

    // ==================== 维护操作 ====================

    @Override
    public void clear() {
        entities.clear();
        relations.clear();
        entityRelations.clear();
        typeIndex.clear();
        relationTypeIndex.clear();
    }

    @Override
    public void applyTimeDecay(double decayFactor) {
        relations.values().forEach(relation -> relation.applyTimeDecay(decayFactor));
    }

    @Override
    public int pruneWeakRelations(double threshold) {
        List<String> toRemove = relations.values().stream()
            .filter(r -> r.getWeight() != null && r.getWeight() < threshold)
            .map(Relation::getId)
            .collect(Collectors.toList());

        toRemove.forEach(this::deleteRelation);
        return toRemove.size();
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("entity_count", entities.size());
        stats.put("relation_count", relations.size());
        stats.put("entity_types", typeIndex.size());
        stats.put("relation_types", relationTypeIndex.size());

        // 平均度数
        double avgDegree = entities.isEmpty() ? 0 :
            entities.keySet().stream()
                .mapToInt(this::getEntityDegree)
                .average()
                .orElse(0.0);
        stats.put("avg_degree", avgDegree);

        // 平均权重
        double avgWeight = relations.isEmpty() ? 0 :
            relations.values().stream()
                .filter(r -> r.getWeight() != null)
                .mapToDouble(Relation::getWeight)
                .average()
                .orElse(0.0);
        stats.put("avg_relation_weight", avgWeight);

        return stats;
    }
}
