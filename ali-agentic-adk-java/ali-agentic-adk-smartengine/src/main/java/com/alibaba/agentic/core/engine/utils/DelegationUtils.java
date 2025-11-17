/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.engine.utils;

import com.alibaba.agentic.core.engine.converter.DataTypeConverter;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 19:52
 */
@Slf4j
public class DelegationUtils {

    private final static String RESULT_PREFIX = "out_";

    /**
     * 保存中间结果,且更新lastActivityId为当前节点id
     *
     * @param activityId
     * @param systemContext
     * @param result
     */
    public static void saveInterOutput(String activityId, SystemContext systemContext, Result result) {
        // log.info("saveInterOutput activityId: {} , result: {}, systemContext: {}", activityId, result, systemContext);
        if (systemContext.getInterOutput() == null) {
            systemContext.setInterOutput(new HashMap<>());
        }
        systemContext.setLastActivityId(activityId);
        if (MapUtils.isEmpty(result.getData())) {
            return;
        }
        systemContext.getInterOutput().put(getInterOutputKey(activityId), result.getData());
    }

    /**
     * 获取graph中上一节点的输出结果
     *
     * @param systemContext
     * @param typeReference
     * @return
     * @param <T>
     */
    public static <T> T getResultOfLastNode(SystemContext systemContext, TypeReference<T> typeReference) {
        if (systemContext.getInterOutput() == null || StringUtils.isBlank(systemContext.getLastActivityId())) {
            return null;
        }
        return getResultOfNode(systemContext, systemContext.getLastActivityId(), typeReference);
    }

    /**
     * 获取graph中loop节点的输出结果(返回为list)
     *
     * @param systemContext
     * @return
     * @param <T>
     */
    public static <T> List<T> getResultOfLoopNode(String activityId, SystemContext systemContext) {
        return DataTypeConverter.convertMap2ListWithIndexKeyOrder(DelegationUtils.getResultOfNode(systemContext, activityId, new TypeReference<Map<String, T>>() {}));
    }

    /**
     * 获取graph中上一节点的输出结果
     *
     * @param systemContext
     * @param clazz
     * @return
     * @param <T>
     */
    public  static <T> T getResultOfLastNode(SystemContext systemContext, Class<T> clazz) {
        if (systemContext.getInterOutput() == null || StringUtils.isBlank(systemContext.getLastActivityId())) {
            return null;
        }
        return getResultOfNode(systemContext, systemContext.getLastActivityId(), clazz);
    }



    public static <T> T getResultOfNode(SystemContext systemContext, String activityId, Class<T> clazz) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        String wrappedLoopId = LoopControlUtils.getAnyWrappedLoopId(systemContext, activityId);
        if (StringUtils.isNotEmpty(wrappedLoopId)) {
            Integer currentLoopIndexOfWrappedLoop = LoopExecutionContextUtils.getLoopIndex(systemContext, wrappedLoopId);
            Map<String, Object> resultFromLoop = LoopExecutionContextUtils.getResultFromLoop(systemContext, wrappedLoopId, currentLoopIndexOfWrappedLoop, activityId);
            return JSON.parseObject(JSON.toJSONString(resultFromLoop), clazz);
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> JSON.parseObject(JSON.toJSONString(map), clazz)).orElse(null);
    }

    public static <T> T getResultOfNode(SystemContext systemContext, String activityId, TypeReference<T> typeReference) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        String wrappedLoopId = LoopControlUtils.getAnyWrappedLoopId(systemContext, activityId);
        if (StringUtils.isNotEmpty(wrappedLoopId)) {
            Integer currentLoopIndexOfWrappedLoop = LoopExecutionContextUtils.getLoopIndex(systemContext, wrappedLoopId);
            Map<String, Object> resultFromLoop = LoopExecutionContextUtils.getResultFromLoop(systemContext, wrappedLoopId, currentLoopIndexOfWrappedLoop, activityId);
            return JSON.parseObject(JSON.toJSONString(resultFromLoop), typeReference);
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> JSON.parseObject(JSON.toJSONString(map), typeReference)).orElse(null);
    }


    public static Object getResultOfNode(SystemContext systemContext, String activityId, String key) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        String wrappedLoopId = LoopControlUtils.getAnyWrappedLoopId(systemContext, activityId);
        if (StringUtils.isNotEmpty(wrappedLoopId)) {
            Integer currentLoopIndexOfWrappedLoop = LoopExecutionContextUtils.getLoopIndex(systemContext, wrappedLoopId);
            Map<String, Object> resultFromLoop = LoopExecutionContextUtils.getResultFromLoop(systemContext, wrappedLoopId, currentLoopIndexOfWrappedLoop, activityId);
            return MapUtils.isEmpty(resultFromLoop) ? null : resultFromLoop.get(key);
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> map.get(key)).orElse(null);
    }

    public static Object getRequestParameter(SystemContext systemContext, String key) {
        return Optional.ofNullable(systemContext.getRequestParameter())
                .map(map -> map.get(key)).orElse(null);
    }


    private static String getInterOutputKey(String activityId) {
        return RESULT_PREFIX + activityId;
    }
}
