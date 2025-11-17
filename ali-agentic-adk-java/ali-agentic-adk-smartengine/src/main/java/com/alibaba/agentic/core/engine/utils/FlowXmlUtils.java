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

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.flows.storage.FlowStorageService;
import com.alibaba.agentic.core.utils.ApplicationContextUtil;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.service.command.RepositoryCommandService;
import com.alibaba.smart.framework.engine.service.query.RepositoryQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.dom4j.Element;

import java.util.Map;

@Slf4j
public class FlowXmlUtils {


    public static Element genEdge(Element process, String sourceCode, String targetCode) {
        Element edge = process.addElement("sequenceFlow");
        edge.addAttribute("id", "flow_edge_" + sourceCode + "_" + targetCode);
        edge.addAttribute("sourceRef", sourceCode);
        edge.addAttribute("targetRef", targetCode);
        return edge;
    }

    public static Element genEdge(Element process, String sourceCode, String targetCode, Map<String, String> propertiesMap) {
        if (MapUtils.isEmpty(propertiesMap)) {
            return genEdge(process, sourceCode, targetCode);
        }
        Element edge = genEdge(process, sourceCode, targetCode);

        propertiesMap.forEach((k, v) -> {
            if (v != null) {
                edge.addAttribute(k, v);
            }
        });
        return edge;
    }

    public static void deployProcessDefinition(String defineId, String version, String xml) {
        SmartEngine smartEngine = (SmartEngine) ApplicationContextUtil.getBean("adkSmartEngine");
        FlowStorageService flowStorageService = (FlowStorageService) ApplicationContextUtil.getBean(FlowStorageService.class);
        RepositoryQueryService repositoryQueryService = smartEngine.getRepositoryQueryService();
        try {
            String bpmnXml = flowStorageService.getBpmnXml(defineId, version);
            if (bpmnXml != null) {
                log.info("bpmnXml already exists for defineId: {}, version: {}. Overwriting existing FlowDefinition!", defineId, version);
            }
            flowStorageService.saveBpmnXml(new FlowDefinition(defineId, version, xml));
            ProcessDefinition processDefinition = repositoryQueryService.getCachedProcessDefinition(defineId, version);
            if (processDefinition != null) {
                return;
            }
            RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
            repositoryCommandService.deployWithUTF8Content(xml);
            log.info("deployProcessDefinition success, defineId: {}, version: {}", defineId, version);
        } catch (Exception e) {
            log.error("deployProcessDefinition fail, defineId: {}, version: {}", defineId, version, e);
            throw new EngineException(e);
        }
    }


    public static void cacheProcessDefinition(String defineId, String version) {
        SmartEngine smartEngine = (SmartEngine) ApplicationContextUtil.getBean("adkSmartEngine");
        FlowStorageService flowStorageService = (FlowStorageService) ApplicationContextUtil.getBean(FlowStorageService.class);
        RepositoryQueryService repositoryQueryService = smartEngine.getRepositoryQueryService();
        ProcessDefinition processDefinition = repositoryQueryService.getCachedProcessDefinition(defineId, version);
        if (processDefinition != null) {
            return;
        }
        String bpmnXml = flowStorageService.getBpmnXml(defineId, version);
        AssertUtils.assertNotBlank(bpmnXml, String.format("bpmnXml not exists for defineId: {}, version: {}!", defineId, version));
        RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
        repositoryCommandService.deployWithUTF8Content(bpmnXml);
        log.info("deployProcessDefinition success, defineId: {}, version: {}", defineId, version);
    }

}
