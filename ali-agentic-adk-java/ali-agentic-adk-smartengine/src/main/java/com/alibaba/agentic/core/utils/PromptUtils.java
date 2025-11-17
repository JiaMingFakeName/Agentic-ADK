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
package com.alibaba.agentic.core.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Map;
import java.util.function.Supplier;

public class PromptUtils {

    public static String generatePrompt(String promptTemplate, Map<String, String> params) {
        return replace(promptTemplate, params);
    }

    public static String generatePrompt(String promptTemplate, Supplier<Map<String, String>> paramSupplier) {
        return replace(promptTemplate, paramSupplier.get());
    }

    public static String generatePrompt(Supplier<String> promptTemplateSupplier, Supplier<Map<String, String>> paramSupplier) {
        return replace(promptTemplateSupplier.get(), paramSupplier.get());
    }

    private static String replace(String originContent, Map<String, String> promptParams) {
        //替换prompt中的占位符变量，兼容一下两种类型的占位符
        if(MapUtils.isNotEmpty(promptParams)) {
            StrSubstitutor strSubstitutor = new StrSubstitutor(promptParams);
            String replacedPrompt = strSubstitutor.replace(originContent);

            strSubstitutor = new StrSubstitutor(promptParams, "$!{", "}");
            replacedPrompt =strSubstitutor.replace(replacedPrompt);

            strSubstitutor = new StrSubstitutor(promptParams, "{", "}");
            replacedPrompt =strSubstitutor.replace(replacedPrompt);
            return replacedPrompt;
        }
        return originContent;
    }

}
