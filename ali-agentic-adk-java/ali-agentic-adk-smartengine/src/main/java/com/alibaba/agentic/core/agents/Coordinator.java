package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.executor.SystemContext;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/27 16:06
 */
public interface Coordinator {

    /**
     * 运行一次
     * @param systemContext
     */
    Flowable<Map<String, Object>> step(SystemContext systemContext);

}
