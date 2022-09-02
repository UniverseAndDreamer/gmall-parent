package com.atguigu.starter.cache;

import com.atguigu.starter.cache.aspect.CacheAspect;
import com.atguigu.starter.cache.cache.CacheService;
import com.atguigu.starter.cache.cache.CacheServiceImpl;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class MallCacheAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public CacheService cacheService() {
        CacheServiceImpl cacheService = new CacheServiceImpl();
        return cacheService;
    }
    @Bean
    public CacheAspect cacheAspect() {
        return new CacheAspect();
    }

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
