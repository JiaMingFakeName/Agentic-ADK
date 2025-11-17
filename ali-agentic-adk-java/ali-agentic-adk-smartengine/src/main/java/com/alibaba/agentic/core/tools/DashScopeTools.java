/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.JsonSchema;
import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Data
@Accessors(chain = true)
public class DashScopeTools implements FunctionTool {

    private String apiKey;

    private String appId;


    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        ApplicationParam.ApplicationParamBuilder builder = ApplicationParam.builder();
        if (StringUtils.isNotBlank(getAppId())) {
            builder.appId(getAppId());
        }
        if (StringUtils.isNotBlank(getApiKey())) {
            builder.apiKey(getApiKey());
        }
        Optional.ofNullable(args).ifPresent(arg -> {
            if (arg.containsKey("appId")) {
                builder.appId((String) arg.get("appId"));
            }
            if (arg.containsKey("apiKey")) {
                builder.apiKey((String) arg.get("apiKey"));
            }
            if (arg.containsKey("prompt")) {
                builder.prompt((String) arg.get("prompt"));
            }
        });


        return Flowable.create(emitter -> {
            try {
                Application application = new Application();
                ApplicationResult result = application.call(builder.build());
                Map<String, Object> output = new HashMap<>();
                output.put("text", result.getOutput().getText());
                output.put("sessionId", result.getOutput().getSessionId());
                emitter.onNext(output);
                emitter.onComplete();
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public String name() {
        return "dash_scope_tool";
    }

    @Override
    public String description() {
        return "这是一个阿里百炼的工具簇，集合了常用的信息查询能力，如天气、汇率、油价、IP等，统一提供标准化接口，便于集成和扩展。";
    }

    @Override
    public ToolDeclaration declaration() {
        return ToolDeclaration.builder()
                .name(name())
                .description(description())
                .parameters(JsonSchema.builder()
                        .type("OBJECT")
                        .properties(buildParametersProperties())
                        .required(Arrays.asList("appId", "prompt"))
                        .build())
                .response(
                        JsonSchema.builder()
                                .type("OBJECT")
                                .properties(buildResponseProperties())
                                .required(Arrays.asList("text", "sessionId"))
                                .build()
                )
                .build();
    }

    private String getApiKey() {
        if (apiKey == null) {
            apiKey = PropertyConstant.dashscopeApiKey;
        }
        return apiKey;
    }

    private Map<String, JsonSchema> buildParametersProperties() {
        Map<String, JsonSchema> properties = new HashMap<>();
        properties.put("appId", JsonSchema.builder()
                .type("STRING")
                .description("百炼智能应用ID")
                .build());
        properties.put("prompt", JsonSchema.builder()
                .type("STRING")
                .description("用户提示词")
                .build());
        properties.put("sessionId", JsonSchema.builder()
                .type("STRING")
                .description("会话ID（多轮时传入）")
                .build());
        return properties;
    }

    private Map<String, JsonSchema> buildResponseProperties() {
        Map<String, JsonSchema> properties = new HashMap<>();
        properties.put("text", JsonSchema.builder()
                .type("STRING").description("回复内容").build());
        properties.put("sessionId", JsonSchema.builder()
                .type("STRING").description("本轮返回的会话ID").build());
        return properties;
    }
}
