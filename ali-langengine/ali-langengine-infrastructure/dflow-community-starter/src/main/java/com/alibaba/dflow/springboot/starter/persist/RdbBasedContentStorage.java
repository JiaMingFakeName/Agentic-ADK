package com.alibaba.dflow.springboot.starter.persist;

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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class RdbBasedContentStorage implements ContextStoreInterface {

    private static final Logger log = LoggerFactory.getLogger(RdbBasedContentStorage.class);
    private final int expireDay;
    private final JedisPool jedisPool;

    public RdbBasedContentStorage(JedisPool jedisPool, int expireDay) {
        this.expireDay = expireDay;
        this.jedisPool = jedisPool;
    }

    @Override
    public ContextStack getContext(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            String result = rdb.get(key);
            if (result == null) {
                log.info("redis get result is null, key:{}", key);
                return null;
            }
            return ContextStack.rebuild(result);
        }
    }

    @Override
    public void putContext(String key, ContextStack context) {
        try (Jedis rdb = jedisPool.getResource()) {
            String result = rdb.set(key, context.dump());
            log.info("redis set key:{}, result:{}", key, result);
        }
    }

    @Override
    public void expireContext(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            log.info("History Data print: {}", rdb.get(key));
            rdb.expire(key, expireDay * 24 * 3600);
        }

    }

    @Override
    public void removeContext(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            rdb.del(key);
        }
    }
}
