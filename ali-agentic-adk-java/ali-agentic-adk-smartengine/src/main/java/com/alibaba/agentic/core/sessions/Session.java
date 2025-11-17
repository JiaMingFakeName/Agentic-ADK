package com.alibaba.agentic.core.sessions;

import com.alibaba.agentic.core.engine.delegation.domain.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/5 11:22
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Session {

    private String id;

    private List<Message> messageList = new ArrayList<>();

}
