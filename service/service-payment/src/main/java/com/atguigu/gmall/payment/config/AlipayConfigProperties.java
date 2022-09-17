package com.atguigu.gmall.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "app.alipay")
public class AlipayConfigProperties {
    //appId
    private String appId;
    //应用私钥
    private String merchantPrivateKey;
    //支付宝公钥
    private String alipayPublicKey;
    //异步通知的页面路径
    private String notifyUrl;
    //页面跳转同步通知路径
    private String returnUrl;
    //签名方式
    private String signType;
    //字符编码格式
    private String charset;
    //支付宝网关
    private String gatewayUrl;
    //支付宝网关
    private String logPath;
}
