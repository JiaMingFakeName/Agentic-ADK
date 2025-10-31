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

import java.util.List;
import java.util.Set;

/**
 * 知识图谱存储接口
 *
 * 定义了知识图谱的基本操作：
 * - 实体的CRUD操作
 * - 关系的CRUD操作
 * - 图谱查询和遍历
 * - 子图提取
 *
 * 实现类可以基于不同的存储后端：
 * - InMemoryGraphStore: 内存存储（快速，适合小规模）
 * - Neo4jGraphStore: Neo4j图数据库（高性能，适合大规模）
 * - TigerGraphStore: TigerGraph图数据库
 * - RedisGraphStore: Redis Graph模块
 *
 * @author xiaoxuan.lp
 */
public interface GraphStore {

    // ==================== 实体操作 ====================

    /**
     * 添加实体到图谱
     *
     * @param entity 实体对象
     */
    void addEntity(Entity entity);

    /**
     * 批量添加实体
     *
     * @param entities 实体列表
     */
    void addEntities(List<Entity> entities);

    /**
     * 根据ID获取实体
     *
     * @param entityId 实体ID
     * @return 实体对象，不存在则返回null
     */
    Entity getEntity(String entityId);

    /**
     * 根据名称和类型获取实体
     *
     * @param name 实体名称
     * @param type 实体类型
     * @return 实体对象，不存在则返回null
     */
    Entity getEntityByNameAndType(String name, String type);

    /**
     * 更新实体信息
     *
     * @param entity 实体对象
     */
    void updateEntity(Entity entity);

    /**
     * 删除实体（同时删除相关的所有关系）
     *
     * @param entityId 实体ID
     */
    void deleteEntity(String entityId);

    /**
     * 获取所有实体
     *
     * @return 实体列表
     */
    List<Entity> getAllEntities();

    /**
     * 根据类型获取实体
     *
     * @param type 实体类型
     * @return 实体列表
     */
    List<Entity> getEntitiesByType(String type);

    /**
     * 搜索实体（根据名称模糊匹配）
     *
     * @param keyword 关键词
     * @return 匹配的实体列表
     */
    List<Entity> searchEntities(String keyword);

    // ==================== 关系操作 ====================

    /**
     * 添加关系到图谱
     *
     * @param relation 关系对象
     */
    void addRelation(Relation relation);

    /**
     * 批量添加关系
     *
     * @param relations 关系列表
     */
    void addRelations(List<Relation> relations);

    /**
     * 根据ID获取关系
     *
     * @param relationId 关系ID
     * @return 关系对象，不存在则返回null
     */
    Relation getRelation(String relationId);

    /**
     * 更新关系信息
     *
     * @param relation 关系对象
     */
    void updateRelation(Relation relation);

    /**
     * 删除关系
     *
     * @param relationId 关系ID
     */
    void deleteRelation(String relationId);

    /**
     * 获取所有关系
     *
     * @return 关系列表
     */
    List<Relation> getAllRelations();

    /**
     * 获取指定实体的所有关系（作为源或目标）
     *
     * @param entityId 实体ID
     * @return 关系列表
     */
    List<Relation> getRelationsByEntity(String entityId);

    /**
     * 获取从源实体出发的所有关系
     *
     * @param sourceEntityId 源实体ID
     * @return 关系列表
     */
    List<Relation> getOutgoingRelations(String sourceEntityId);

    /**
     * 获取指向目标实体的所有关系
     *
     * @param targetEntityId 目标实体ID
     * @return 关系列表
     */
    List<Relation> getIncomingRelations(String targetEntityId);

    /**
     * 获取指定类型的关系
     *
     * @param relationType 关系类型
     * @return 关系列表
     */
    List<Relation> getRelationsByType(String relationType);

    /**
     * 查询两个实体之间的关系
     *
     * @param sourceId 源实体ID
     * @param targetId 目标实体ID
     * @return 关系列表
     */
    List<Relation> getRelationsBetween(String sourceId, String targetId);

    // ==================== 图谱查询 ====================

    /**
     * 获取实体的一跳邻居
     *
     * @param entityId 实体ID
     * @return 邻居实体列表
     */
    List<Entity> getNeighbors(String entityId);

    /**
     * 获取实体的N跳邻居
     *
     * @param entityId 实体ID
     * @param hops 跳数
     * @return 邻居实体列表
     */
    List<Entity> getNeighbors(String entityId, int hops);

    /**
     * 获取实体的子图（包含实体及其邻居和关系）
     *
     * @param entityId 中心实体ID
     * @param depth 深度（跳数）
     * @return 子图对象
     */
    SubGraph getSubGraph(String entityId, int depth);

    /**
     * 查找两个实体之间的最短路径
     *
     * @param sourceId 源实体ID
     * @param targetId 目标实体ID
     * @return 路径（实体和关系的序列）
     */
    List<Object> findShortestPath(String sourceId, String targetId);

    /**
     * 查找满足条件的所有路径
     *
     * @param sourceId 源实体ID
     * @param targetId 目标实体ID
     * @param maxDepth 最大深度
     * @return 路径列表
     */
    List<List<Object>> findAllPaths(String sourceId, String targetId, int maxDepth);

    // ==================== 统计和分析 ====================

    /**
     * 获取图谱中的实体总数
     *
     * @return 实体数量
     */
    int getEntityCount();

    /**
     * 获取图谱中的关系总数
     *
     * @return 关系数量
     */
    int getRelationCount();

    /**
     * 获取实体的度（连接的关系数）
     *
     * @param entityId 实体ID
     * @return 度数
     */
    int getEntityDegree(String entityId);

    /**
     * 获取最重要的实体（按度或重要性排序）
     *
     * @param topN 返回前N个
     * @return 实体列表
     */
    List<Entity> getTopEntities(int topN);

    // ==================== 维护操作 ====================

    /**
     * 清空整个图谱
     */
    void clear();

    /**
     * 应用时间衰减到所有关系
     *
     * @param decayFactor 衰减因子
     */
    void applyTimeDecay(double decayFactor);

    /**
     * 删除弱关系（权重低于阈值）
     *
     * @param threshold 权重阈值
     * @return 删除的关系数量
     */
    int pruneWeakRelations(double threshold);

    /**
     * 获取图谱的统计信息
     *
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getStatistics();
}
