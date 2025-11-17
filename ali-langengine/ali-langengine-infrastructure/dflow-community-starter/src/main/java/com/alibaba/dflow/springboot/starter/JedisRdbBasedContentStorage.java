package com.alibaba.dflow.springboot.starter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.dflow.config.GlobalStoreInterface;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.InternalHelper;
import com.alibaba.dflow.springboot.starter.persist.KeepAliveStorage;
import com.alibaba.dflow.springboot.starter.persist.RdbBasedContentStorage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(CustomDFlowTair.class)
@ConditionalOnBean(JedisPool.class)
public class JedisRdbBasedContentStorage {
    @Autowired
    JedisPool jedisPool;

    @Autowired
    private StarterProperties properties;

    @Bean
    public ContextStoreInterface getContextStoreInterface(){
        return new RdbBasedContentStorage(jedisPool,properties.getExpireDay());
    }

    @Bean
    public GlobalStoreInterface getGlobalStoreInterface(){
        return new KeepAliveStorage(jedisPool,properties.getEnv());
    }
}