package com.atguigu.gmall.payment.config;


import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfiguration {
    @Autowired
    private AlipayConfigProperties alipayConfigProperties;

    @Bean
    public AlipayClient alipayClient() {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfigProperties.getGatewayUrl(),
                alipayConfigProperties.getAppId(),
                alipayConfigProperties.getMerchantPrivateKey(),
                "json",
                alipayConfigProperties.getCharset(),
                alipayConfigProperties.getAlipayPublicKey(),
                alipayConfigProperties.getSignType());
        return alipayClient;
    }

}
