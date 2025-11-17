package com.alibaba.agentic.dynamic;

@FunctionalInterface
public interface AtomicPromptTemplateGetter {
    AtomicPromptTemplateInstance get();
}
