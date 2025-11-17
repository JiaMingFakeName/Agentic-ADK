package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.runner.AsyncConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 17:08
 */
@Slf4j
@Component
public class AsyncConsumerTest implements AsyncConsumer {
    @Override
    public void accept(Result result) {
        log.info("async result consumer: {}", result);
    }
}
