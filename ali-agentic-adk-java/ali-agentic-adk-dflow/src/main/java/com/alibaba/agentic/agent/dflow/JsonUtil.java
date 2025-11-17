package com.alibaba.agentic.agent.dflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.FunctionResponseScheduling;
import io.a2a.spec.EventKind;
import io.a2a.spec.Part;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JsonUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature()) // 启用源位置追踪
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .addMixIn(Part.class, PartMixIn.class)
                .addMixIn(EventKind.class, EventKindMixIn.class)
                .addMixIn(FunctionResponse.Builder.class, FunctionResponseBuilderMixIn.class)
                .addMixIn(com.google.genai.types.Part.Builder.class, PartBuilderMixIn.class);
        // .enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION)
        ;
    }

    /**
     * Avoid having duplicate field 'kind'
     */
    private abstract static class PartMixIn {
        @JsonIgnore
        public abstract Part.Kind getKind();

    }

    private interface EventKindMixIn {
        @JsonIgnore
        String getKind();
    }
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    interface FunctionResponseBuilderMixIn {

        @JsonSetter(nulls = Nulls.SKIP)
        FunctionResponse.Builder willContinue(boolean value);

        @JsonSetter(nulls = Nulls.SKIP)
        FunctionResponse.Builder scheduling(FunctionResponseScheduling value);

        @JsonSetter(nulls = Nulls.SKIP)
        FunctionResponse.Builder id(String value);

        @JsonSetter(nulls = Nulls.SKIP)
        FunctionResponse.Builder response(Map<String, Object> value);

    }
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    interface PartBuilderMixIn {

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder videoMetadata(com.google.genai.types.VideoMetadata value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder thought(Boolean value); // 注意：原方法是 boolean -> Boolean 才能接受 null

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder inlineData(com.google.genai.types.Blob value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder fileData(com.google.genai.types.FileData value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder thoughtSignature(byte[] value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder codeExecutionResult(com.google.genai.types.CodeExecutionResult value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder executableCode(com.google.genai.types.ExecutableCode value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder functionCall(com.google.genai.types.FunctionCall value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder functionResponse(com.google.genai.types.FunctionResponse value);

        @JsonSetter(nulls = Nulls.SKIP)
        com.google.genai.types.Part.Builder text(String value);
    }

}
