package com.atguigu.gmall.common.config.thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ThreadPoolConfigurationProperties.class)
/**
 * @EnableConfigurationProperties(ThreadPoolConfigurationProperties.class)
 * 上述注解两层含义：
 *      1.将ThreadPoolConfigurationProperties中的所有属性与该配置进行绑定
 *      2.将ThreadPoolConfigurationProperties自动注入到容器中
 */
public class ThreadPoolAutoConfiguration {
    @Autowired
    private ThreadPoolConfigurationProperties threadPoolConfigurationProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        //向spring容器中注入线程池对象

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                threadPoolConfigurationProperties.getCorePoolSize(),
                threadPoolConfigurationProperties.getMaxPoolSize(),
                threadPoolConfigurationProperties.getAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(threadPoolConfigurationProperties.getBlockingQueue()),
                (r) -> {
                    int i = 1;
                    Thread thread = new Thread(r);
                    thread.setName("[" + applicationName + "]" + "core-thread-" + i);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());

        return threadPoolExecutor;
    }

}
