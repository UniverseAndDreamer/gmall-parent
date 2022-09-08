package com.atguigu.gmall.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthUrlProperties {

    private List<String> noAuthUrl;     //任意请求都可以访问的url
    private List<String> loginedUrl;    //只有登录后的请求才可以访问
    private String loginPage;
    private List<String> denyUrl;

}
