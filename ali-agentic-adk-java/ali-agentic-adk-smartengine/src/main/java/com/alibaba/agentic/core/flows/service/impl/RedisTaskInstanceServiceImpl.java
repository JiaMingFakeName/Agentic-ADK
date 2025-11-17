package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.executor.AgentContext;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.flows.storage.redis.AdkRedisTemplate;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Base64;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/12 14:42
 */
@Slf4j
public class RedisTaskInstanceServiceImpl implements TaskInstanceService {

    private final AdkRedisTemplate redisTemplate;
    private final String prefix;

    private final long expireTime;

    public RedisTaskInstanceServiceImpl(AdkRedisTemplate redisTemplate, String prefix, long expireTime) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    @Override
    public String persistTaskInstance(TaskInstance taskInstance) {
        log.info("persistTaskInstance id: {}, taskInstance: {}", taskInstance.getId(), taskInstance);
        String key = prefix + ":task_instance:" + taskInstance.getId();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(taskInstance);
            byte[] data = bos.toByteArray();

            log.info("redis persistTaskInstance, taskInstance: {}, key: {}, data: {}, base64: {}",
                    JSON.toJSONString(taskInstance), key, data, Base64.getEncoder().encodeToString(data));

            redisTemplate.setEx(key, Base64.getEncoder().encodeToString(data), expireTime);
            return taskInstance.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public TaskInstance getTaskInstance(String taskId) {
        String key = prefix + ":task_instance:" + taskId;
        String taskStr = redisTemplate.get(key);
        log.info("taskStr: {}", taskStr);
        if (StringUtils.isBlank(taskStr)) {
            return null;
        }
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((taskStr)));

            log.info("redis getTaskInstance, taskInstanceBase64: {}, decoded: {}", taskStr, Base64.getDecoder().decode((taskStr)));

            ObjectInputStream in = new ObjectInputStream(bis);
            return (TaskInstance) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTaskInstance(String taskId) {
        String key = prefix + ":task_instance:" + taskId;
        redisTemplate.delete(key);
    }
}
