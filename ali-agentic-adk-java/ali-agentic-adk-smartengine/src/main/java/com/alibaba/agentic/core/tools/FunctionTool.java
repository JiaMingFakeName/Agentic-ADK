package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.engine.delegation.domain.JsonSchema;
import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.adk.tools.Annotations;
import io.reactivex.rxjava3.core.Flowable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface FunctionTool extends BaseTool {

    static FunctionTool create(Object instance, Method func) {
        String name = func.isAnnotationPresent(Annotations.Schema.class) && !func.getAnnotation(Annotations.Schema.class).name().isEmpty()
                ? func.getAnnotation(Annotations.Schema.class).name() : func.getName();
        String description = func.isAnnotationPresent(Annotations.Schema.class)
                ? func.getAnnotation(Annotations.Schema.class).description() : "";

        ToolDeclaration.Builder builder = ToolDeclaration.builder().name(name).description(description);
        ToolDeclaration toolDeclaration = build(func, builder); // ← 修改：返回 ToolDeclaration

        return new FunctionTool() {
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Parameter[] parameters = func.getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    String paramName =
                            parameters[i].isAnnotationPresent(Annotations.Schema.class)
                                    && !parameters[i].getAnnotation(Annotations.Schema.class).name().isEmpty()
                                    ? parameters[i].getAnnotation(Annotations.Schema.class).name()
                                    : parameters[i].getName();
                    if (parameters[i].getType().isAssignableFrom(SystemContext.class)) {
                        arguments[i] = systemContext;
                        continue;
                    }
                    if (!args.containsKey(paramName)) {
                        throw new BaseException(
                                String.format(
                                        "The parameter '%s' was not found in the arguments provided by the model.",
                                        paramName), ErrorEnum.SYSTEM_ERROR);
                    }
                    Class<?> paramType = parameters[i].getType();
                    Object argValue = args.get(paramName);
                    if (paramType.equals(List.class)) {
                        if (argValue instanceof List) {
                            Type type =
                                    ((ParameterizedType) parameters[i].getParameterizedType())
                                            .getActualTypeArguments()[0];
                            arguments[i] = createList((List<Object>) argValue, (Class) type);
                            continue;
                        }
                    } else if (argValue instanceof Map) {
                        arguments[i] = new ObjectMapper().convertValue(argValue, paramType);
                        continue;
                    }
                    arguments[i] = castValue(argValue, paramType);
                }
                try {
                    Object result = func.invoke(instance, arguments);
                    if (result == null) {
                        return Flowable.empty();
                    } else if (result instanceof Flowable) {
                        return (Flowable<Map<String, Object>>) result;
                    } else {
                        return Flowable.just((Map<String, Object>) result);
                    }
                } catch (Exception e) {
                    return Flowable.error(e);
                }
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public ToolDeclaration declaration() { // ← 修改返回类型
                return toolDeclaration;
            }
        };
    }

    static List<Object> createList(List<Object> values, Class<?> type) {
        List<Object> list = new ArrayList<>();
        if (type == null) {
            return list;
        }
        Class<?> cls = type;
        for (Object value : values) {
            if (cls == Integer.class
                    || cls == Long.class
                    || cls == Double.class
                    || cls == Float.class
                    || cls == Boolean.class
                    || cls == String.class) {
                list.add(castValue(value, cls));
            } else {
                list.add(new ObjectMapper().convertValue(value, type));
            }
        }
        return list;
    }

    static Object castValue(Object value, Class<?> type) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            if (value instanceof Integer) {
                return value;
            }
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            if (value instanceof Long || value instanceof Integer) {
                return value;
            }
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            if (value instanceof Double) {
                return ((Double) value).doubleValue();
            }
            if (value instanceof Float) {
                return ((Float) value).doubleValue();
            }
            if (value instanceof Integer) {
                return ((Integer) value).doubleValue();
            }
            if (value instanceof Long) {
                return ((Long) value).doubleValue();
            }
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            if (value instanceof Double) {
                return ((Double) value).floatValue();
            }
            if (value instanceof Float) {
                return ((Float) value).floatValue();
            }
            if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            }
            if (value instanceof Long) {
                return ((Long) value).floatValue();
            }
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            if (value instanceof Boolean) {
                return value;
            }
        } else if (type.equals(String.class)) {
            if (value instanceof String) {
                return value;
            }
        }
        return new ObjectMapper().convertValue(value, type);
    }

    // 修改：返回 ToolDeclaration
    static ToolDeclaration build(Method func, ToolDeclaration.Builder builder) {
        List<String> required = new ArrayList<>();
        Map<String, JsonSchema> properties = new LinkedHashMap<>(); // ← 修改类型

        for (Parameter param : func.getParameters()) {
            String paramName =
                    param.isAnnotationPresent(Annotations.Schema.class)
                            && !param.getAnnotation(Annotations.Schema.class).name().isEmpty()
                            ? param.getAnnotation(Annotations.Schema.class).name()
                            : param.getName();
            if (param.getType().isAssignableFrom(SystemContext.class)) {
                continue;
            }
            required.add(paramName);
            properties.put(paramName, buildSchemaFromParameter(param)); // ← 返回 JsonSchema
        }

        // 构建 parameters JsonSchema
        JsonSchema paramsSchema = JsonSchema.builder()
                .type("object")
                .properties(properties)
                .required(required)
                .build();

        builder.parameters(paramsSchema);

        Type returnType = func.getGenericReturnType();
        if (returnType != Void.TYPE) {
            Type realReturnType = returnType;
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedReturnType = (ParameterizedType) returnType;
                String returnTypeName = ((Class<?>) parameterizedReturnType.getRawType()).getName();
                if (returnTypeName.equals("io.reactivex.rxjava3.core.Flowable")) {
                    returnType = parameterizedReturnType.getActualTypeArguments()[0];
                    if (returnType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) returnType;
                        returnTypeName = ((Class<?>) parameterizedType.getRawType()).getName();
                    }
                }
                if (returnTypeName.equals("java.util.Map")
                        || returnTypeName.equals("com.google.common.collect.ImmutableMap")) {
                    // 构建 response schema（如果你的 ToolDeclaration 支持 response）
                    // 👇 如果你 ToolDeclaration 没有 response 字段，可以注释掉或扩展它
                    // builder.response(buildSchemaFromType(returnType));
                    return builder.build();
                }
            }
            throw new BaseException(
                    "Return type should be Map or Flowable<Map>, but it was "
                            + realReturnType.getTypeName(), ErrorEnum.SYSTEM_ERROR);
        }
        return builder.build();
    }

    // 修改：返回 JsonSchema
    static JsonSchema buildSchemaFromParameter(Parameter param) {
        JsonSchema.Builder builder = JsonSchema.builder();

        if (param.isAnnotationPresent(Annotations.Schema.class)
                && !param.getAnnotation(Annotations.Schema.class).description().isEmpty()) {
            builder.description(param.getAnnotation(Annotations.Schema.class).description());
        }

        Class<?> paramType = param.getType();
        if (paramType == String.class) {
            builder.type("string");
        } else if (paramType == Boolean.class || paramType == boolean.class) {
            builder.type("boolean");
        } else if (paramType == Integer.class || paramType == int.class) {
            builder.type("integer");
        } else if (paramType == Double.class || paramType == double.class ||
                paramType == Float.class || paramType == float.class ||
                paramType == Long.class || paramType == long.class) {
            builder.type("number");
        } else if (paramType == List.class) {
            Type elementType = ((ParameterizedType) param.getParameterizedType()).getActualTypeArguments()[0];
            builder.type("array").items(buildSchemaFromType(elementType));
        } else if (paramType == Map.class) {
            builder.type("object");
        } else {
            // 处理自定义对象
            ObjectMapper objectMapper = new ObjectMapper();
            BeanDescription beanDescription = objectMapper.getSerializationConfig()
                    .introspect(objectMapper.constructType(paramType));
            Map<String, JsonSchema> props = new LinkedHashMap<>();
            for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                props.put(property.getName(), buildSchemaFromType(property.getRawPrimaryType()));
            }
            builder.type("object").properties(props);
        }

        return builder.build();
    }

    static JsonSchema buildSchemaFromType(Type type) {
        JsonSchema.Builder builder = JsonSchema.builder();

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (List.class.isAssignableFrom(rawType)) {
                Type elementType = parameterizedType.getActualTypeArguments()[0];
                return builder.type("array").items(buildSchemaFromType(elementType)).build();
            } else if (Map.class.isAssignableFrom(rawType)) {
                return builder.type("object").build();
            } else {
                throw new IllegalArgumentException("Unsupported generic type: " + type);
            }
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz == String.class) {
                builder.type("string");
            } else if (clazz == Boolean.class || clazz == boolean.class) {
                builder.type("boolean");
            } else if (clazz == Integer.class || clazz == int.class) {
                builder.type("integer");
            } else if (clazz == Double.class || clazz == double.class ||
                    clazz == Float.class || clazz == float.class ||
                    clazz == Long.class || clazz == long.class) {
                builder.type("number");
            } else if (Map.class.isAssignableFrom(clazz)) {
                builder.type("object");
            } else {
                // 自定义对象
                ObjectMapper objectMapper = new ObjectMapper();
                BeanDescription beanDescription = objectMapper.getSerializationConfig()
                        .introspect(objectMapper.constructType(clazz));
                Map<String, JsonSchema> props = new LinkedHashMap<>();
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    props.put(property.getName(), buildSchemaFromType(property.getRawPrimaryType()));
                }
                builder.type("object").properties(props);
            }
        }

        return builder.build();
    }

    String description();

    ToolDeclaration declaration(); // ← 修改返回类型
}