package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 对话消息体（适用于 chat 场景）
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Message implements Serializable {
    
    private static final long serialVersionUID = 275135781977761073L;

    public Message(Role role, String content, List<FunctionCallRequest> functionCalls) {
        this.role = role;
        this.content = content;
        this.functionCalls = functionCalls;
    }

    /**
     * 消息角色（assistant、user、system、tool）
     */
    private Role role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 多模态的消息内容
     */
    private List<Content> contents;

    /**
     * 消息关联的函数调用请求列表（如需调用函数时使用，可为空）
     */
    private List<FunctionCallRequest> functionCalls;

    /**
     * 工具调用id
     */
    private String toolCallId;


    public enum Role {
        assistant,
        user,
        system,
        tool
    }

}