package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.executor.SystemContext;

import java.util.List;

public interface LoopConfigureBehavior extends BaseCondition {

    List<?> obtainLoopItems(SystemContext systemContext);
}
