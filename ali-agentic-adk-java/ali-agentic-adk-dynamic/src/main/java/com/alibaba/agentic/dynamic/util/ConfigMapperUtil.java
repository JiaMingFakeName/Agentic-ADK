package com.alibaba.agentic.dynamic.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 配置映射工具类
 * 用于将Map格式的配置动态映射到Builder对象
 * 支持自动类型转换和反射方法查找
 * 
 * @author agentic-adk
 */
@Slf4j
public class ConfigMapperUtil {

    /**
     * 将Map配置动态应用到Builder对象
     * 通过反射自动查找并调用Builder的setter方法
     * 
     * @param builder 目标Builder对象
     * @param configs 配置参数Map
     * @param <T> Builder类型
     */
    public static <T> void applyConfigs(T builder, Map<String, Object> configs) {
        if (builder == null || configs == null || configs.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String configKey = entry.getKey();
            Object configValue = entry.getValue();
            
            if (configValue == null) {
                continue;
            }
            
            try {
                // 尝试查找对应的setter方法
                Method setterMethod = findSetterMethod(builder.getClass(), configKey);
                if (setterMethod != null) {
                    // 转换为目标类型
                    Object convertedValue = convertValue(configValue, setterMethod.getParameterTypes()[0]);
                    // 设置方法可访问以绕过权限检查
                    setterMethod.setAccessible(true);
                    setterMethod.invoke(builder, convertedValue);
                }
            } catch (Exception e) {
                // 静默失败，不影响其他参数的设置
                log.info("applyConfigs error, configKey: {}, configValue: {}", configKey, configValue);
            }
        }
    }

    /**
     * 查找Builder中对应的setter方法
     * 支持两种命名风格：
     * 1. Builder风格：methodName(value) - 方法名与属性名相同
     * 2. JavaBean风格：setPropertyName(value) - 标准setter方法
     * 3. 模糊匹配：忽略大小写和特殊字符（_等）的匹配
     * 
     * @param builderClass Builder类
     * @param propertyName 属性名
     * @return 找到的Method，没有则返回null
     */
    private static Method findSetterMethod(Class<?> builderClass, String propertyName) {
        try {
            // 方式1：尝试查找Builder风格的setter方法（方法名与属性名相同）
            for (Method method : builderClass.getMethods()) {
                if (method.getName().equals(propertyName) && 
                    method.getParameterCount() == 1) {
                    return method;
                }
            }
            
            // 方式2：尝试查找JavaBean风格的setter方法（setXxx格式）
            String setterName = "set" + capitalizeFirstLetter(propertyName);
            for (Method method : builderClass.getMethods()) {
                if (method.getName().equals(setterName) && 
                    method.getParameterCount() == 1) {
                    return method;
                }
            }
            
            // 方式3：模糊匹配 - 忽略大小写和特殊字符
            String normalizedProperty = normalizePropertyName(propertyName);
            for (Method method : builderClass.getMethods()) {
                if (method.getParameterCount() == 1) {
                    String methodName = method.getName();
                    
                    // Builder风格模糊匹配
                    if (normalizePropertyName(methodName).equals(normalizedProperty)) {
                        return method;
                    }
                    
                    // JavaBean风格模糊匹配
                    if (methodName.startsWith("set") && methodName.length() > 3) {
                        String propertyPart = methodName.substring(3);
                        if (normalizePropertyName(propertyPart).equals(normalizedProperty)) {
                            return method;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    /**
     * 规范化属性名：转小写并移除特殊字符
     * 
     * @param name 原始属性名
     * @return 规范化后的属性名
     */
    private static String normalizePropertyName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.toLowerCase().replaceAll("[_\\-]", "");
    }

    /**
     * 将字符串首字母大写
     * 
     * @param str 原始字符串
     * @return 首字母大写后的字符串
     */
    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将配置值转换为目标类型
     * 支持常见的类型转换：
     * - Number类型的双向自动转换（大转小、小转大）
     * - String → Number 的解析转换
     * - Boolean、List等其他类型
     * 
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        // 如果类型已经匹配，直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        // Number类型转换 - 支持所有数值类型互转
        if (value instanceof Number) {
            return convertNumberToTarget((Number) value, targetType);
        }
        
        // String → Number 转换
        if (value instanceof String && isNumberType(targetType)) {
            return parseStringToNumber((String) value, targetType);
        }
        
        // Boolean类型转换
        if (targetType == Boolean.class || targetType == boolean.class) {
            return convertToBoolean(value);
        }
        
        // String类型转换
        if (targetType == String.class) {
            return value.toString();
        }
        
        // List类型 - 直接返回
        if (List.class.isAssignableFrom(targetType)) {
            return value;
        }
        
        // 其他复杂类型，直接返回
        return value;
    }

    /**
     * 判断目标类型是否为数值类型
     * 
     * @param targetType 目标类型
     * @return 是否为数值类型
     */
    private static boolean isNumberType(Class<?> targetType) {
        return targetType == Float.class || targetType == float.class ||
               targetType == Double.class || targetType == double.class ||
               targetType == Integer.class || targetType == int.class ||
               targetType == Long.class || targetType == long.class ||
               targetType == Short.class || targetType == short.class ||
               targetType == Byte.class || targetType == byte.class;
    }

    /**
     * 将Number转换为目标数值类型
     * 
     * @param numValue Number值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convertNumberToTarget(Number numValue, Class<?> targetType) {
        // 转换为 Float/float
        if (targetType == Float.class || targetType == float.class) {
            return numValue.floatValue();
        } 
        // 转换为 Double/double
        else if (targetType == Double.class || targetType == double.class) {
            return numValue.doubleValue();
        } 
        // 转换为 Integer/int
        else if (targetType == Integer.class || targetType == int.class) {
            return numValue.intValue();
        } 
        // 转换为 Long/long
        else if (targetType == Long.class || targetType == long.class) {
            return numValue.longValue();
        } 
        // 转换为 Short/short
        else if (targetType == Short.class || targetType == short.class) {
            return numValue.shortValue();
        } 
        // 转换为 Byte/byte
        else if (targetType == Byte.class || targetType == byte.class) {
            return numValue.byteValue();
        }
        return numValue;
    }

    /**
     * 将String解析为目标数值类型
     * 
     * @param strValue 字符串值
     * @param targetType 目标类型
     * @return 解析后的数值
     */
    private static Object parseStringToNumber(String strValue, Class<?> targetType) {
        try {
            String trimmed = strValue.trim();
            
            // 转换为 Float/float
            if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(trimmed);
            } 
            // 转换为 Double/double
            else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(trimmed);
            } 
            // 转换为 Integer/int
            else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(trimmed);
            } 
            // 转换为 Long/long
            else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(trimmed);
            } 
            // 转换为 Short/short
            else if (targetType == Short.class || targetType == short.class) {
                return Short.parseShort(trimmed);
            } 
            // 转换为 Byte/byte
            else if (targetType == Byte.class || targetType == byte.class) {
                return Byte.parseByte(trimmed);
            }
        } catch (NumberFormatException e) {
            // 解析失败，返回null或默认值
        }
        return null;
    }

    /**
     * 将值转换为Boolean类型
     * 
     * @param value 原始值
     * @return Boolean值
     */
    private static Object convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Number) {
            // 数字转布尔：0 = false, 非0 = true
            return ((Number) value).intValue() != 0;
        }
        return null;
    }
}
