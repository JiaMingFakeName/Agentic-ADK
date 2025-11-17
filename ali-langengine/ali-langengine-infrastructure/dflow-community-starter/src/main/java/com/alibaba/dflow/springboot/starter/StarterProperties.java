package com.alibaba.dflow.springboot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：配置信息 实体
 *
 **/
@Configuration
public class StarterProperties {
    Logger logger = LoggerFactory.getLogger(StarterProperties.class);

    @Value("${app.name}")
    public String appname;
    @Autowired
    CustomProperties customProperties;
    public String getMetaQTopic() {
        if(customProperties.getMetaqTopic() == null){
            logger.error("metaqTopic is null");
        }
        return customProperties.getMetaqTopic();
    }

    public String getMetaQCid() {
        return customProperties.getMetaqCid();
    }

    public String getGroupName() {
        if(customProperties.getGroupName() == null){

            return appname;
        }
        return customProperties.getGroupName();
    }

    public boolean getStrictMode() {
        if(customProperties.getStrict() == null){
            return false;
        }
        return customProperties.getStrict();
    }

    public Integer getExpireDay() {
        if(customProperties.getExpireDay() == null){
            return 8;
        }
        return customProperties.getExpireDay();
    }

    public String getEnv() {
        if(customProperties.getEnv() == null){
            return "dev";
        }
        return customProperties.getEnv();
    }
}
