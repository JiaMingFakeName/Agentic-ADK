package com.alibaba.dflow.springboot.starter.persist;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.dflow.config.GlobalStoreInterface;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.InternalHelper;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class KeepAliveStorage implements GlobalStoreInterface {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveStorage.class);
    JedisPool jedisPool;

    String env;

    private static final long DURATION = 10000;

    public KeepAliveStorage(JedisPool jedisPool, String env) {
        this.jedisPool = jedisPool;
        this.env = env;
        executors.submit(new Runnable() {
            @Override
            public void run() {
                while (true){
                    HashSet<String> hashSet = new HashSet<>();
                    hashSet.addAll(alives);
                    hashSet.stream().forEach(y -> update(y));
                    try {
                        Thread.sleep(DURATION);
                    } catch (InterruptedException e) {
                        log.error("Interrupted",e);
                    }
                }
            }
        });
    }

    public ScheduledExecutorService executors = new ScheduledThreadPoolExecutor(1,
        new BasicThreadFactory.Builder().namingPattern("keepalive-schedule-pool-%d").daemon(true).build());

    public Set<String> alives =  ConcurrentHashMap.newKeySet();

    @Override
    public Long incr(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            return rdb.incr(env+key);
        }
    }

    @Override
    public Long decr(String s) {
        try (Jedis rdb = jedisPool.getResource()) {
            return rdb.decr(env+s);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            return rdb.get(env+key);
        }
    }

    @Override
    public void put(String key, String context) {
        try (Jedis rdb = jedisPool.getResource()) {
            rdb.set(env+key, context);
        }
    }

    private void update(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            rdb.hset(env+key, InternalHelper.getIp(), String.valueOf(System.currentTimeMillis()));
        }
    }

    @Override
    public int keepAlive(String key) {
        alives.add(key);
        update(key);
        return getAlived(key);
    }

    @Override
    public int getAlived(String key) {
        try (Jedis rdb = jedisPool.getResource()) {
            long now = System.currentTimeMillis();
            Map<String, String> r = rdb.hgetAll(env+key);
            return (int) r.values().stream()
                .filter(x ->
                    now - Long.parseLong(x) < DURATION * 2  //20秒内
                ).count();
        }
    }
    @Override
    public List<String> getIPs(String s) {
        try (Jedis rdb = jedisPool.getResource()) {
            Map<String, String> r = rdb.hgetAll(env+s);
            long now = System.currentTimeMillis();
            return r.entrySet().stream().filter(x ->
                now - Long.parseLong(x.getValue()) < DURATION * 2  //20秒内
            ).map(x->x.getKey()).collect(Collectors.toList());
        }
    }
}