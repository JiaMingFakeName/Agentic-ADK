package com.alibaba.dflow.springboot.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 描述：配置信息 实体
 *
 * @Author shf
 * @Date 2019/5/7 22:08
 * @Version V1.0
 **/
@ConfigurationProperties(prefix = "com.alibaba.dflow")
public class CustomProperties {

    public String metaqTopic;
    public String metaqCid;
    public String groupName;
    public Boolean strict;
    public Integer expireDay;

    public String env;

    public String getMetaqTopic() {
        return metaqTopic;
    }
    public void setMetaqTopic(String metaqTopic) {this.metaqTopic = metaqTopic;}
    public void setMetaqCid(String cid){this.metaqCid = cid;}
    public String getMetaqCid() {
        return metaqCid;
    }
    public String getGroupName() {
        return groupName;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public void setExpireDay(Integer expireDay) {
        this.expireDay = expireDay;
    }

    public Integer getExpireDay() {
        return expireDay;
    }

    public String getEnv() {
        return env;
    }
}
