package com.alibaba.agentic.agent.dflow;

import com.alibaba.dflow.internal.ContextStack;
import io.a2a.spec.Event;
import io.a2a.spec.Message;

public interface StreamEmitter {
    void streamEmit(ContextStack contextStack, com.google.adk.events.Event event);
    void systemEmit(String taskId, Event event);
}
