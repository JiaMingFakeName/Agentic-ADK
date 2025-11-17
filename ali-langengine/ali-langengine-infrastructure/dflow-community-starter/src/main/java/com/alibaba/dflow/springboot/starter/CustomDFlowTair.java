package com.alibaba.dflow.springboot.starter;

import redis.clients.jedis.JedisPool;

public interface CustomDFlowTair {
    JedisPool getJedisPool();
}
