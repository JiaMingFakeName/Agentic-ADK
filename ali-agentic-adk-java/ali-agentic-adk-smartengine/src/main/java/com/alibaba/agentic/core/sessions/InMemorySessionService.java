package com.alibaba.agentic.core.sessions;

import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/8 10:48
 */
@Data
public class InMemorySessionService implements BaseSessionService {


    private final Cache<String, Session> sessionMap =
            Caffeine.newBuilder()
                    .expireAfterWrite(20, TimeUnit.MINUTES)
                    .maximumSize(512)
                    .build();

    @Override
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, new Session().setId(sessionId).setMessageList(new ArrayList<>()));
        return sessionId;
    }

    @Override
    public void updateSession(Session session) {
        sessionMap.put(session.getId(), session);
    }

    @Override
    public Session appendMessage(String sessionId, Message message) {
        Session session = sessionMap.getIfPresent(sessionId);
        if (CollectionUtils.isEmpty(session.getMessageList())) {
            session.setMessageList(new ArrayList<>());
        }
        session.getMessageList().add(message);
        return session;
    }

    @Override
    public Session getSession(String sessionId) {
        return sessionMap.getIfPresent(sessionId);
    }

    @Override
    public void release(String sessionId) {
        sessionMap.invalidate(sessionId);
    }
}
