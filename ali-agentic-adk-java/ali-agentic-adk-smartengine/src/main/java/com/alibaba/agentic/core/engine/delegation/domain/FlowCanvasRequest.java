package com.alibaba.agentic.core.engine.delegation.domain;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import lombok.Data;

import java.util.Map;

@Data
public class FlowCanvasRequest {

    private FlowDefinition flowDefinition;

    private Map<String, Object> request;

}
