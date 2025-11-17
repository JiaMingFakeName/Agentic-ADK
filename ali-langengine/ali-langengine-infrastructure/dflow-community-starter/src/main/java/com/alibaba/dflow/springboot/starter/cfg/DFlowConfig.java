package com.alibaba.dflow.springboot.starter.cfg;

import jakarta.annotation.PostConstruct;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.dflow.config.GlobalStoreInterface;
import com.alibaba.dflow.internal.DFlowDelay.DFlowDelayManager;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.SubscribableChannel;

import com.alibaba.dflow.springboot.starter.CustomProperties;
import com.alibaba.dflow.springboot.starter.StarterProperties;

@Configuration("DFlow")
@Primary
@EnableConfigurationProperties(CustomProperties.class)
public class DFlowConfig {
    org.slf4j.Logger logger = LoggerFactory.getLogger(DFlowConfig.class);

    @Autowired
    private StarterProperties properties;

    @Autowired
    ContextStoreInterface contextStoreInterface;

    @Autowired
    GlobalStoreInterface globalStoreInterface;

    @Autowired
    DFlowDelayManager redisDelayManager;

    @Autowired
    HttpResender httpResender;

    @Autowired(required = true)
    SubscribableChannel subscribableChannel;

    @PostConstruct
    private void initDflow() throws Exception {
        DFlow.setStrictMode(properties.getStrictMode());
        DFlow.setSubscribableChannel(subscribableChannel);
        DFlow.setDelayManager(redisDelayManager);
        DFlow.setStorage(contextStoreInterface);
        DFlow.setGlobalStorage(globalStoreInterface);
        DFlow.setRequestResender(httpResender);
        //首次初始化慢
        com.alibaba.dflow.internal.InternalHelper.getIp();
        logger.info("DFlow initialized");
    }


}
