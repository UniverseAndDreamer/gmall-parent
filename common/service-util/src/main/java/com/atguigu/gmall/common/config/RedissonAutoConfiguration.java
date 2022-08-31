package com.atguigu.gmall.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedissonAutoConfiguration {
    @Autowired
    private RedisProperties redisProperties;


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String password = redisProperties.getPassword();
        String host = redisProperties.getHost();
        int port = redisProperties.getPort();
        config.useSingleServer().setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }




}
