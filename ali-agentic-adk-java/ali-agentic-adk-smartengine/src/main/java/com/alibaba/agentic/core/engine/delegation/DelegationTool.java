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
package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.node.sub.ToolParam;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION
 * 工具管理、调用代理
 *
 * @author baliang.smy
 * @date 2025/7/16 10:44
 */
@Component
@Slf4j
public class DelegationTool extends FrameworkDelegationBase {

    private final static Map<String, BaseTool> toolMap = new ConcurrentHashMap<>();

    /**
     * 注册工具
     *
     * @param tool
     */
    public static void register(BaseTool tool) {
        if (toolMap.containsKey(tool.name())) {
            log.warn(String.format("duplicated tool name of %s", tool.name()));
            return;
        }
        toolMap.put(tool.name(), tool);
    }

    @Autowired
    public void registerTools(Optional<List<BaseTool>> optionalBaseTools) {
        optionalBaseTools.ifPresent(tools -> {
            for (BaseTool tool : tools) {
                register(tool);
            }
        });
    }

    @PostConstruct
    public void init() {
        ServiceLoader<BaseTool> loader = ServiceLoader.load(BaseTool.class);
        for (BaseTool tool : loader) {
            register(tool);
        }
    }

    /**
     * 获取工具
     *
     * @param toolName
     * @return
     */
    public static BaseTool getTool(String toolName) {
        log.info("adk smart engine DelegationTool getTool, toolMap: {}, toolName: {}", JSON.toJSONString(toolMap), toolName);
        if (MapUtils.isEmpty(toolMap) || !toolMap.containsKey(toolName)) {
            throw new BaseException(String.format("tool:%s is not exits.", toolName), ErrorEnum.SYSTEM_ERROR);
        }
        return toolMap.get(toolName);
    }

//    @Override
//    public void execute(ExecutionContext executionContext) {
//        log.info("adk smart engine DelegationTool execute start, request: {}", executionContext.getRequest());
//        super.execute(executionContext);
//        log.info("adk smart engine DelegationTool execute finished, request: {}, response: {}", executionContext.getRequest(), executionContext.getResponse());
//    }

    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        log.info("adk smart engine DelegationTool invoke start, request: {}", request);
        FunctionCallRequest functionCallRequest = new JSONObject(request.getParam()).toJavaObject(FunctionCallRequest.class);
        BaseTool tool = getTool(functionCallRequest.getName());
        if (InvokeMode.ASYNC.equals(systemContext.getInvokeMode()) && !systemContext.isPause()) {
            try {
                return Flowable.just(tool.asyncRun(request.getAsyncTaskId(), functionCallRequest.getToolParameter(), systemContext));
            } catch (Throwable throwable) {
                return Flowable.just(AsyncTaskResult.fail(request.getAsyncTaskId(), throwable));
            }
        }
        return tool.run(functionCallRequest.getToolParameter(), systemContext)
                .map(Result::success)
                .onErrorReturn(Result::fail);
    }

    @Override
    public Map<String, Object> generateRequest(ExecutionContext executionContext, SystemContext systemContext, String activityId) {
        FunctionCallRequest request = new FunctionCallRequest();
        Map<String, Object> properties = super.generateRequest(executionContext, systemContext, activityId);

        if (MapUtils.isEmpty(properties)) {
            return JSONObject.parseObject(JSONObject.toJSONString(request));
        }
        request.setName(String.valueOf(properties.get("toolName")));

        List<ToolParam> paramList = JSONArray.parseArray(String.valueOf(properties.get("paramList")), ToolParam.class);
        if (CollectionUtils.isEmpty(paramList)) {
            request.setToolParameter(new HashMap<>());
        } else {
            Map<String, Object> toolParameter = new HashMap<>();
            for (ToolParam param : paramList) {
                toolParameter.put(param.getName(), param.getValue());
            }
            request.setToolParameter(toolParameter);
        }
        log.info("adk smart engine DelegationTool.generateRequest invoke DelegationTool.getTool, request: {}", request);
        BaseTool tool = getTool(request.getName());
        JSONObject jsonObject = new ObjectMapper().convertValue(request, JSONObject.class);
        jsonObject.put(PropertyConstant.NODE_SUPPORT_ASYNC, tool.supportAsync(systemContext));
        return jsonObject;
    }

}
