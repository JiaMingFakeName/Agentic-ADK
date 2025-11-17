package com.alibaba.agentic.dynamic.util;

import org.springframework.context.ApplicationContext;

import java.util.Map;

public class SpringContextHolder {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
    }

    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBeansOfType(clazz);
    }

    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext 未注入，请检查是否正确加载了 Spring 容器。");
        }
    }
}