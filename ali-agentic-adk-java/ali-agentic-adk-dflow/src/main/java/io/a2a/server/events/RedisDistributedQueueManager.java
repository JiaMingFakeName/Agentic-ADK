package io.a2a.server.events;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.a2a.server.events.*;
import io.a2a.spec.*;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RedisDistributedQueueManager implements QueueManager {
    
    private JedisPool jedisPool;
    private static final int DEFAULT_EXPIRE_TIME = 1800; // 30分钟过期时间（秒）
    private static final String KEY_PREFIX = "_A2A_qm_"; // Redis key前缀
    
    // Redis key常量
    private static final String QUEUE_EVENTS_KEY = "queue:events:";
    private static final String QUEUE_META_KEY = "queue:meta:";
    private static final String QUEUE_LOCK_KEY = "queue:lock:";

    
    public RedisDistributedQueueManager(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
    public JedisPool getJedisPool() {
        return jedisPool;
    }
    
    @Override
    public void add(String taskId, EventQueue queue) {
        // 在Redis中创建队列元数据
        try (Jedis jedis = jedisPool.getResource()) {
            String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
            
            // 检查队列是否已存在
            if (jedis.exists(metaKey)) {
                throw new TaskQueueExistsException();
            }
            
            Map<String, String> meta = new HashMap<>();
            meta.put("created", String.valueOf(System.currentTimeMillis()));
            meta.put("lastAccess", String.valueOf(System.currentTimeMillis()));
            meta.put("status", "active");
            jedis.hmset(metaKey, meta);
        }
    }
    
    @Override
    public EventQueue get(String taskId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
            // 检查队列是否存在
            if (jedis.exists(metaKey)) {
                // 更新最后访问时间
                jedis.hset(metaKey, "lastAccess", String.valueOf(System.currentTimeMillis()));
                return new RedisDurableEventQueue(taskId, jedisPool);
            }
            return null;
        }
    }
    
    @Override
    public EventQueue tap(String taskId) {
        EventQueue queue = get(taskId);
        return queue == null ? null : queue.tap();
    }
    
    @Override
    public void close(String taskId) {
        // 不直接关闭队列，而是设置过期时间
        try (Jedis jedis = jedisPool.getResource()) {
            String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
            
            // 检查队列是否存在
            if (!jedis.exists(metaKey)) {
                throw new NoTaskQueueException();
            }
            
            // 为队列相关key设置过期时间
            String eventsKey = KEY_PREFIX + QUEUE_EVENTS_KEY + taskId;
            jedis.expire(eventsKey, DEFAULT_EXPIRE_TIME);
            jedis.expire(metaKey, DEFAULT_EXPIRE_TIME);
        }
    }
    
    @Override
    public EventQueue createOrTap(String taskId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String lockKey = KEY_PREFIX + QUEUE_LOCK_KEY + taskId;
            String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
            
            // 使用SETNX获取分布式锁
            String result = jedis.set(lockKey, "locked", new SetParams().nx().ex(30));
            
            if ("OK".equals(result)) {
                // 成功获取锁，检查队列是否存在
                if (!jedis.exists(metaKey)) {
                    // 队列不存在，创建新队列
                    Map<String, String> meta = new HashMap<>();
                    meta.put("created", String.valueOf(System.currentTimeMillis()));
                    meta.put("lastAccess", String.valueOf(System.currentTimeMillis()));
                    meta.put("status", "active");
                    jedis.hmset(metaKey, meta);
                    return new RedisDurableEventQueue(taskId, jedisPool);
                } else {
                    // 队列已存在，返回现有队列
                    return new RedisDurableEventQueue(taskId, jedisPool).tap();
                }
            } else {
                // 未能获取锁，等待一段时间后重试或返回现有队列
                return new RedisDurableEventQueue(taskId, jedisPool).tap();
            }
        }
    }

    @Override
    public void awaitQueuePollerStart(EventQueue eventQueue) throws InterruptedException {
        eventQueue.awaitQueuePollerStart();
    }

    public static class RedisDurableEventQueue extends EventQueue {
        
        private String taskId;
        private JedisPool jedisPool;
        private static final int DEFAULT_EXPIRE_TIME = 1800; // 30分钟过期时间（秒）
        private static final String KEY_PREFIX = "_A2A_qm_"; // Redis key前缀
        
        // Redis key常量
        private static final String QUEUE_EVENTS_KEY = "queue:events:";
        private static final String QUEUE_META_KEY = "queue:meta:";
        
        // 用于实现awaitQueuePollerStart和signalQueuePollerStarted的Redis key
        private static final String QUEUE_POLLER_KEY = "queue:poller:";
        
        public RedisDurableEventQueue(String taskId, JedisPool jedisPool) {
            this.taskId = taskId;
            this.jedisPool = jedisPool;
        }
        
        @Override
        public void awaitQueuePollerStart() throws InterruptedException {
            try (Jedis jedis = jedisPool.getResource()) {
                String pollerKey = KEY_PREFIX + QUEUE_POLLER_KEY + taskId;
                
                // 使用Redis的BLPOP命令实现阻塞等待
                // 这里我们等待一个特殊的key，当signalQueuePollerStarted被调用时会向这个key推送一个值
                jedis.blpop(10, pollerKey); // 等待10秒
            }
        }
        
        @Override
        protected void signalQueuePollerStarted() {
            try (Jedis jedis = jedisPool.getResource()) {
                String pollerKey = KEY_PREFIX + QUEUE_POLLER_KEY + taskId;
                
                // 向特殊的key推送一个值，以唤醒等待的线程
                jedis.lpush(pollerKey, "started");
                
                // 设置key的过期时间，避免数据堆积
                jedis.expire(pollerKey, 60);
            }
        }
        
        @Override
        public void enqueueEvent(Event event) {
            // 检查队列是否已关闭
            try (Jedis jedis = jedisPool.getResource()) {
                String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
                String status = jedis.hget(metaKey, "status");
                if ("closed".equals(status)) {
                    // 队列已关闭，不添加新事件
                    return;
                }
                
                String eventsKey = KEY_PREFIX + QUEUE_EVENTS_KEY + taskId;
                // 将事件序列化后添加到Redis List
                jedis.lpush(eventsKey, serializeEvent(event));
                
                // 更新元数据中的最后访问时间
                jedis.hset(metaKey, "lastAccess", String.valueOf(System.currentTimeMillis()));
            }
        }
        
        @Override
        public Event dequeueEvent(int waitMilliSeconds) throws EventQueueClosedException {
            try (Jedis jedis = jedisPool.getResource()) {
                String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
                String eventsKey = KEY_PREFIX + QUEUE_EVENTS_KEY + taskId;
                
                // 检查队列是否已关闭且为空
                String status = jedis.hget(metaKey, "status");
                Long queueLength = jedis.llen(eventsKey);
                
                if ("closed".equals(status) && (queueLength == null || queueLength == 0)) {
                    throw new EventQueueClosedException();
                }
                
                // 使用BRPOP阻塞式获取队列元素
                // BRPOP命令返回一个包含两个元素的数组：
                // 第一个元素（索引0）是被弹出元素的键名
                // 第二个元素（索引1）是被弹出的元素值
                java.util.List<String> result = jedis.brpop(waitMilliSeconds / 1000, eventsKey);
                
                if (result != null && result.size() == 2) {
                    // 反序列化事件
                    return deserializeEvent(result.get(1));
                }
                
                return null;
            }
        }
        
        @Override
        EventQueue tap() {
            // 创建当前队列的一个副本引用
            return new RedisDurableEventQueue(taskId, jedisPool);
        }
        
        @Override
        public void doClose() {
            try (Jedis jedis = jedisPool.getResource()) {
                String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
                // 更新队列状态为closed
                jedis.hset(metaKey, "status", "closed");
            }
        }
        
        @Override
        public void close() {
            // 不直接关闭队列，而是设置过期时间
            try (Jedis jedis = jedisPool.getResource()) {
                String eventsKey = KEY_PREFIX + QUEUE_EVENTS_KEY + taskId;
                String metaKey = KEY_PREFIX + QUEUE_META_KEY + taskId;
                
                // 为队列相关key设置过期时间
                jedis.expire(eventsKey, DEFAULT_EXPIRE_TIME);
                jedis.expire(metaKey, DEFAULT_EXPIRE_TIME);
            }
        }
        
        // 事件序列化方法（使用JsonUtil实现）
        private String serializeEvent(Event event) {
            try {
                return JsonUtil.OBJECT_MAPPER.writeValueAsString(event);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize event", e);
                // 如果序列化失败，返回null或者抛出异常
                throw new RuntimeException("Failed to serialize event", e);
            }
        }
        
        // 事件反序列化方法（使用JsonUtil实现）
        private Event deserializeEvent(String eventStr) {
            try {
                return deserialize(eventStr);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize event", e);
                // 如果反序列化失败，返回null或者抛出异常
                throw new RuntimeException("Failed to deserialize event", e);
            }
        }
        private Event deserialize(String data) throws JsonProcessingException {
            if (data == null || data.isEmpty()) {
                return null;
            }
            JsonNode jsonNode = JsonUtil.OBJECT_MAPPER.readTree(data);
            if (jsonNode.has("kind")) {
                String kind = jsonNode.get("kind").asText();
                return switch (kind) {
                    case "message" -> JsonUtil.OBJECT_MAPPER.readValue(data, Message.class);
                    case "task" -> JsonUtil.OBJECT_MAPPER.readValue(data, Task.class);
                    case "status-update" -> JsonUtil.OBJECT_MAPPER.readValue(data, TaskStatusUpdateEvent.class);
                    case "artifact-update" -> JsonUtil.OBJECT_MAPPER.readValue(data, TaskArtifactUpdateEvent.class);
                    default -> throw new RuntimeException("Unknown event kind: " + kind);
                };
            }
            throw new RuntimeException("Unknown event kind");
        }
    }
}

class JsonUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule()).addMixIn(Part.class, PartMixIn.class).addMixIn(EventKind.class, EventKindMixIn.class).addMixIn(StreamingEventKind.class, StreamingEventKindMixIn.class);
    }

    private abstract static class PartMixIn {
        @JsonIgnore
        public abstract Part.Kind getKind();
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NONE
    )
    public interface EventKindMixIn {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NONE
    )
    public interface StreamingEventKindMixIn {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NONE
    )
    public interface TaskArtifactUpdateEventMixIn {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NONE
    )
    public interface TaskStatusUpdateEventMixIn {
    }
}
