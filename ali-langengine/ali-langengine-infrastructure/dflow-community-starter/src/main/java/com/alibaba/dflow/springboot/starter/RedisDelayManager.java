package com.alibaba.dflow.springboot.starter;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.dflow.internal.DFlowDelay.DFlowDelayManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(CustomDFlowTair.class)
public class RedisDelayManager extends DFlowDelayManager implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(RedisDelayManager.class);
    private final JedisPool jedisPool;
    private final ScheduledExecutorService scheduler;
    private static final String TASK_KEY_PREFIX = "RedisDelayManagerTask:";
    private static final String TASK_QUEUE = "RedisDelayManagerTaskQueue";

    public RedisDelayManager(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startPolling();
    }

    @Override
    protected void addTask(String idname, String traceId, long timeout) throws Exception {
        if (timeout <= 4000) {
            new Thread(() -> {
                try {
                    Thread.sleep(timeout);
                    resume(idname, traceId);
                } catch (Exception e) {
                    log.error("Error resuming task", e);
                }
            }).start();
            return;
        }
        try(Jedis jedis = jedisPool.getResource()) {
            long executeTime = System.currentTimeMillis() + timeout;
            String taskKey = TASK_KEY_PREFIX + idname + ":" + traceId;

            jedis.hset(taskKey, "idname", idname);
            jedis.hset(taskKey, "traceId", traceId);
            jedis.hset(taskKey, "executeTime", String.valueOf(executeTime));

            jedis.zadd(TASK_QUEUE, executeTime, taskKey);
        }
    }

    private void startPolling() {
        scheduler.scheduleAtFixedRate(this::pollTasks, 0, 1, TimeUnit.SECONDS);
    }

    private void pollTasks() {
        try(Jedis jedis = jedisPool.getResource()) {
            long currentTime = System.currentTimeMillis();
            Collection<String> tasks = jedis.zrangeByScore(TASK_QUEUE, 0, currentTime);

            for (String taskKey : tasks) {
                Map<String, String> taskInfo = jedis.hgetAll(taskKey);
                String idname = taskInfo.get("idname");
                String traceId = taskInfo.get("traceId");

                try {
                    log.info("resume task: {} idname:{}, traceId:{}", taskKey, idname, traceId);
                    resume(idname, traceId);
                    jedis.zrem(TASK_QUEUE, taskKey);
                    jedis.del(taskKey);
                } catch (Exception e) {
                    log.error("Error resuming task", e);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdown();
    }
}
