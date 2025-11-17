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

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.FlowCanvasRequest;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import com.alibaba.agentic.core.runner.pipeline.PipelineUtil;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DelegationFlowCanvas extends FrameworkDelegationBase {


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        FlowCanvasRequest flowCanvasRequest = new JSONObject(request.getParam()).toJavaObject(FlowCanvasRequest.class);
        AssertUtils.assertNotNull(flowCanvasRequest.getFlowDefinition());

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ExecutionConstant.ORIGIN_REQUEST, request);
        requestMap.put(ExecutionConstant.SYSTEM_CONTEXT, systemContext);
        return Flowable.fromStream(PipelineUtil.doPipe(PipelineRequest.builder()
                .flowDefinition(flowCanvasRequest.getFlowDefinition())
                .request(requestMap).build()).blockingStream());
    }


    @Override
    protected Map<String, Object> generateRequest(ExecutionContext executionContext, SystemContext systemContext, String activityId) {
        FlowCanvasRequest request = new FlowCanvasRequest();
        Map<String, Object> properties = super.generateRequest(executionContext, systemContext, activityId);
        if (MapUtils.isEmpty(properties)) {
            return JSONObject.parseObject(JSONObject.toJSONString(request));
        }
        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setDefinitionId(String.valueOf(properties.get("flowDefinitionId")));
        flowDefinition.setVersion(String.valueOf(properties.get("flowVersion")));
        flowDefinition.setBpmnXml(String.valueOf(properties.get("flowBpmn")));
        request.setRequest(JSONObject.parseObject(String.valueOf(properties.get("parameter"))));
        request.setFlowDefinition(flowDefinition);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(request));
        jsonObject.put(PropertyConstant.NODE_SUPPORT_ASYNC, false);
        return jsonObject;
    }
}
