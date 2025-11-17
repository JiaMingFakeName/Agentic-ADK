package com.alibaba.agentic.dynamic.domain;

// 定义语法类型枚举
public enum SyntaxType {
    STRICT,      // ${!xxx}
    HANDLEBARS,  // {{xx}}
    SIMPLE,      // {xx}
    COMMENT      // ##xx##
}