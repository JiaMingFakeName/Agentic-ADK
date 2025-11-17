package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/5 16:59
 */
@Accessors(chain = true)
@Data
public class AgentContext implements Serializable {

    private static final long serialVersionUID = -100424013702710518L;

    int currentStep = 0;
    int maxStep;
    String systemPrompt;
    List<ToolDeclaration> tools;
    String sessionId;
    Serializable state;

}
