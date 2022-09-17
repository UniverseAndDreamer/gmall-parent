package com.atguigu.gmall.payment;


import com.atguigu.gmall.common.annotation.EnableAutoExceptionHandler;
import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableAutoExceptionHandler
@EnableFeignClients({"com.atguigu.gmall.feign.order"})
@EnableAutoFeignInterceptor
@SpringCloudApplication
public class PaymentMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMainApplication.class, args);
    }
}
