package com.alibaba.agentic.toolset.browser.dto;

import lombok.Data;

/**
 * ADK计算机使用的通用包装类
 *
 */
@Data
public class AdkComputerUseConnectResult {

    private boolean success;

    private String errorCode;

    private String errorMsg;


}
