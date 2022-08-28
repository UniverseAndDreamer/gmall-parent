package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.print.DocFlavor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolAutoConfiguration {

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        //TODO

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                4,
                4,
                300,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(2000),
                (r) -> {
                    Thread thread = new Thread(r);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());

        return threadPoolExecutor;
    }

}
