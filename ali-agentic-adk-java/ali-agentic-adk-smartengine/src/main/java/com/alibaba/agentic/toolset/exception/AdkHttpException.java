package com.alibaba.agentic.toolset.exception;

import lombok.Data;

@Data
public class AdkHttpException extends Exception {

    private String errorCode;

    private String errorMsg;

}
