package com.alibaba.agentic.toolset.sandbox.service;

import com.alibaba.agentic.toolset.sandbox.dto.SandboxConfig;
import com.alibaba.agentic.toolset.sandbox.dto.SandboxInfo;
import com.alibaba.agentic.toolset.sandbox.dto.SandboxRequest;
import com.alibaba.agentic.toolset.sandbox.dto.SandboxResponse;
import com.alibaba.agentic.toolset.exception.AdkSandboxException;

import java.util.List;
import java.util.Map;

public interface AdkSandboxService {

    /**
     * 创建一个新的沙箱
     *
     * @param sandboxConfig 沙箱配置信息
     * @return 新创建的沙箱的唯一标识符
     * @throws AdkSandboxException 如果创建过程中发生错误
     */
    String createSandbox(SandboxConfig sandboxConfig) throws AdkSandboxException;

    /**
     * 更新现有的沙箱
     *
     * @param sandboxId 要更新的沙箱的唯一标识符
     * @param updatedConfig 更新后的沙箱配置信息
     * @throws AdkSandboxException 如果更新过程中发生错误
     */
    void updateSandbox(String sandboxId, SandboxConfig updatedConfig) throws AdkSandboxException;

    /**
     * 调用沙箱服务
     *
     * @param sandboxId 要调用的沙箱的唯一标识符
     * @param serviceRequest 服务请求参数
     * @return 服务调用的结果
     * @throws AdkSandboxException 如果调用过程中发生错误
     */
    SandboxResponse invokeSandboxService(String sandboxId, SandboxRequest serviceRequest) throws AdkSandboxException;

    /**
     * 查询沙箱信息
     *
     * @param sandboxId 要查询的沙箱的唯一标识符
     * @return 沙箱信息
     * @throws AdkSandboxException 如果查询过程中发生错误
     */
    SandboxInfo querySandbox(String sandboxId) throws AdkSandboxException;

    /**
     * 自定义参数查询沙箱信息
     *
     * @param params 自定义参数
     * @return 沙箱信息
     * @throws AdkSandboxException 如果查询过程中发生错误
     */
    List<SandboxInfo> querySandbox(Map<String, Object> params) throws AdkSandboxException;


    /**
     * 查询所有沙箱
     *
     * @return 所有沙箱的列表
     * @throws AdkSandboxException 如果查询过程中发生错误
     */
    List<SandboxInfo> queryAllSandboxes() throws AdkSandboxException;
}
