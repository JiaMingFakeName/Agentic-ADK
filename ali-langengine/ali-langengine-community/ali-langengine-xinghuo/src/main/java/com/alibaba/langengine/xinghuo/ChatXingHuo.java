/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.xinghuo;

import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 星火大模型
 * https://console.xfyun.cn/services/bm2
 * https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
 * https://console.xfyun.cn/services/bm2
 *
 * @author liuchunhe.lch on 2023/9/10 19:16
 * well-meaning people get together do meaningful things
 **/
@Slf4j
@Data
public class ChatXingHuo extends BaseLLM<ChatCompletionRequest> {
    
    /**
     * 默认最大token数
     */
    private static final int DEFAULT_MAX_TOKENS = 2048;
    
    /**
     * 默认温度
     */
    private static final double DEFAULT_TEMPERATURE = 0.5;

    @Override
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        WebSocketListener socketListener = new WebSocketListener(consumer);
        try {
            socketListener.createWebSocket();
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            
            // 处理可能为null的maxTokens和temperature
            int maxTokens = getMaxTokens() != null ? getMaxTokens() : DEFAULT_MAX_TOKENS;
            double temperature = getTemperature() != null ? getTemperature() : DEFAULT_TEMPERATURE;
            
            socketListener.sendMsg(socketListener.webSocket, sessionId, maxTokens, temperature, prompt);
            // 等待服务端返回完毕后关闭
            while (true) {
                Thread.sleep(200);
                if (socketListener.getWsCloseFlag()) {
                    break;
                }
            }
            socketListener.getWebSocket().close(1000, "完成");

            String message = socketListener.getAnswer();
            if (StringUtils.isNotBlank(message)) {
                message = message.replace("json", "");
            }
            //解决stop问题
            if (!CollectionUtils.isEmpty(stops)) {
                if (StringUtils.isNotBlank(message)) {
                    message = message.split(stops.get(0))[0];
                }
            }

            log.warn("xinghuo answer:" + message);

            return message;
        } catch (Throwable throwable) {
            log.error("调用星火大模型异常", throwable);
            throw new RuntimeException("调用星火大模型异常", throwable);
        }
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }
}