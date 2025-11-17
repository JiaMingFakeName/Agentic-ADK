package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.agents.Coordinator;
import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.delegation.DelegationCoordinator;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.executor.AgentContext;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/2 21:29
 */
@Data
public class CoordinatorFlowNode extends FlowNode {


    private String coordinatorName;

    private AgentContext agentContext;


    public CoordinatorFlowNode(String coordinatorName, AgentContext agentContext) {
        this.coordinatorName = coordinatorName;
        this.agentContext = agentContext;
    }

    @Override
    protected String getNodeType() {
        return NodeType.COORDINATOR;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationCoordinator.class.getName();
    }

    @Override
    protected void addProperties(Element serviceTask) {
        Element extensionElements = serviceTask.addElement("extensionElements");
        Element properties = extensionElements.addElement("smart:properties");

        Element toolNameProperties = properties.addElement("smart:property");
        toolNameProperties.addAttribute("name", "coordinator");
        toolNameProperties.addAttribute("value", coordinatorName);

        Element extraProperties = properties.addElement("smart:property");
        extraProperties.addAttribute("name", "agentContext");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(agentContext);
            byte[] data = bos.toByteArray();
            extraProperties.addAttribute("value", Base64.getEncoder().encodeToString(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
