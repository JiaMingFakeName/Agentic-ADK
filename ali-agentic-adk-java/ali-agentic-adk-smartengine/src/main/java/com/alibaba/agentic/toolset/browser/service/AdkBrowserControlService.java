package com.alibaba.agentic.toolset.browser.service;

import com.alibaba.agentic.toolset.browser.dto.AdkComputerUseConnectRequest;
import com.alibaba.agentic.toolset.browser.dto.AdkComputerUseConnectResult;
import com.alibaba.agentic.toolset.browser.dto.AdkComputerUseExecuteRequest;
import com.alibaba.agentic.toolset.browser.dto.AdkComputerUseExecuteResult;

public interface AdkBrowserControlService {
    /**
     * 与浏览器建立连接
     *
     * @return         连接结果，类型由调用者指定
     * @throws Exception 如果连接过程中发生错误
     */
    AdkComputerUseConnectResult connect(AdkComputerUseConnectRequest request) throws Exception;

    /**
     * 向浏览器发送命令
     *
     * @param command  要执行的命令
     * @return         命令执行的结果
     * @throws Exception 如果命令执行过程中发生错误
     */
    AdkComputerUseExecuteResult executeCommand(AdkComputerUseExecuteRequest command) throws Exception;

    /**
     * 浏览器任务执行完成后的回调方法
     *
     * @param result   任务执行的结果
     */
    AdkComputerUseExecuteResult  onTaskComplete(AdkComputerUseExecuteResult result);

    /**
     * 断开与浏览器的连接
     *
     * @throws Exception 如果断开连接过程中发生错误
     */
    AdkComputerUseConnectResult disconnect(AdkComputerUseConnectRequest request) throws Exception;

}
