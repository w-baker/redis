package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author W-ch
 * @Time 2023/1/17 17:46
 * @E-mail wang.xiaohong.0817@gmail.com
 * @File RedissonConfig .java
 * @Software IntelliJ IDEA
 */
@Configuration
@PropertySource(value = ("classpath:redissonConfig.properties"))
public class RedissonConfig {
    @Value(value = ("${redis.ip}"))
    private String redisIp;
    
    @Value(value = ("${redis.port}"))
    private String redisPort;
    
    @Value(value = ("${redis.pwd}"))
    private String redisPwd;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisIp + ":" + redisPort)
                .setPassword(redisPwd);
    
        return Redisson.create(config);
    }
    
}
