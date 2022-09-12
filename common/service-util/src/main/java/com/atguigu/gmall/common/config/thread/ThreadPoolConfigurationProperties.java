package com.atguigu.gmall.common.config.thread;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@ConfigurationProperties(prefix = "app.threadpool")
public class ThreadPoolConfigurationProperties {

    private Integer corePoolSize = 2;
    private Integer maxPoolSize = 4;
    private Long aliveTime = 300L;
    private Integer blockingQueue = 200;


}
