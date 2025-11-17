package com.alibaba.dflow.springboot.starter;

import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.dflow.config.GlobalStoreInterface;
import com.alibaba.dflow.springboot.starter.persist.KeepAliveStorage;

import redis.clients.jedis.JedisPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(CustomDFlowTair.class)
public class RdbBasedContentStorage {
    @Autowired
    CustomDFlowTair tair;

    @Autowired
    private StarterProperties properties;

    @Bean
    public ContextStoreInterface getContextStoreInterface() throws Exception {
        if(tair.getJedisPool() != null){
            return new com.alibaba.dflow.springboot.starter.persist.RdbBasedContentStorage(tair.getJedisPool(),properties.getExpireDay());
        }
        throw new Exception("CustomDFlowTair has neither jedis nor rcclient implementation");
    }

    @Bean
    public GlobalStoreInterface getGlobalStoreInterface() throws Exception {
        if(tair.getJedisPool() != null) {
            return new KeepAliveStorage(tair.getJedisPool(),properties.getEnv());
        }
        throw new Exception("CustomDFlowTair have neither jedis or rcclient impl");
    }
}
