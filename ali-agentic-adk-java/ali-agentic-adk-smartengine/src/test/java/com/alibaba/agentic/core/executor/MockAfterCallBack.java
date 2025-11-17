package com.alibaba.agentic.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 17:32
 */
@Slf4j
//@Component
public class MockAfterCallBack implements Callback {

    @Override
    public void execute(SystemContext systemContext, Request request, Result result, CallbackChain chain) {
        log.info("after callback");
        chain.execute(systemContext, request, result);
    }

    @Override
    public void receive(SystemContext systemContext, Request request, Result result, CallbackChain chain) {
        log.info("after receive callback");
        chain.receive(systemContext, request, result);
    }

    @Override
    public TYPE getType() {
        return TYPE.after;
    }
}
