package com.alibaba.agentic.core.sessions;

import com.alibaba.agentic.core.engine.delegation.domain.Message;

import java.io.Serializable;
import java.util.List;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/27 15:57
 */
public interface BaseSessionService extends Serializable {

    String createSession();

    void updateSession(Session session);

    Session appendMessage(String sessionId, Message message);

    Session getSession(String sessionId);

    void release(String sessionId);
}
