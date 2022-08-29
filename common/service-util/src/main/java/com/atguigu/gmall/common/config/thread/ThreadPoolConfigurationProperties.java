package com.atguigu.gmall.common.config.thread;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@ConfigurationProperties(prefix = "app.threadpool")
public class ThreadPoolConfigurationProperties {

    private Integer corePoolSize;//: 4
    private Integer maxPoolSize;//: 8
    private Long aliveTime;//: 300
    private Integer blockingQueue;//: 2000

}
