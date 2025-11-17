package com.alibaba.agentic.core.engine.enums;

public enum LoopTypeEnum {
    COUNTER("counter"),
    FOR_EACH("forEach"),
    WHILE("while"),
    ;

    private final String code;

    LoopTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
