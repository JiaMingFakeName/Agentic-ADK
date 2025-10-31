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
package com.alibaba.langengine.core.agent.structured;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * StructuredChatOutputParserWithRetries
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class StructuredChatOutputParserWithRetries extends AgentOutputParser<Object> {

    private AgentOutputParser<Object> baseParser = new StructuredChatOutputParser();
    /**
     * Maximum parse attempts for repairing malformed model outputs.
     */
    private int maxParseAttempts = 3;

    @Override
    public String getFormatInstructions() {
        return PromptConstants.FORMAT_INSTRUCTIONS;
    }

    @Override
    public String getParserType() {
        return "structured_chat_with_retries";
    }

    @Override
    public Object parse(String text) {
        // Fast path
        try {
            return baseParser.parse(text);
        } catch (Throwable firstError) {
            log.debug("First parse failed, will try to repair. error={}", firstError.getMessage());
        }

        String candidate = text;
        for (int attempt = 1; attempt <= Math.max(1, maxParseAttempts); attempt++) {
            try {
                // 1) Try extracting JSON markdown block if present
                candidate = tryExtractJson(candidate);
                // 2) Light-weight bracket normalization
                candidate = normalizeBraces(candidate);
                // 3) Delegate to base parser
                Object parsed = baseParser.parse(candidate);
                if (parsed != null) {
                    return parsed;
                }
            } catch (Throwable e) {
                log.debug("Parse attempt {} failed: {}", attempt, e.getMessage());
            }
            // As a last resort, keep original text for next round
            candidate = text;
        }

        // Fallback: return AgentFinish with raw text
        return getAgentFinish(text);
    }

    private String tryExtractJson(String text) {
        try {
            // Use existing helper to extract fenced JSON if available
            // It tolerates ```json ... ``` and trims outer braces noise
            return PromptConverter.toJson(PromptConverter.parseJsonMarkdown(text));
        } catch (Throwable ignore) {
            return text;
        }
    }

    private String normalizeBraces(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("\\{\\{\\{\\{", "{")
                .replaceAll("\\}\\}\\}\\}", "}")
                .replaceAll("\\{\\{\\{", "{")
                .replaceAll("\\}\\}\\}", "}")
                .replaceAll("\\{\\{", "{")
                .replaceAll("\\}\\}", "}");
    }
}
