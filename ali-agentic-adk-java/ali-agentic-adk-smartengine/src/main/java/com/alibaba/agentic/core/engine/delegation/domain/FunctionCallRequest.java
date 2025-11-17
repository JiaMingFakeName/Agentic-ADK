package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/16 15:38
 */
@Data
@Accessors(chain = true)
public class FunctionCallRequest implements Serializable {

    private static final long serialVersionUID = 7289715371316774631L;

    private String id;

    private String name;

    private Map<String, Object> toolParameter;

}
