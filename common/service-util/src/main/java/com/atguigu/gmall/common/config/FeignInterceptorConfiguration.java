package com.atguigu.gmall.common.config;

import com.atguigu.gmall.common.constant.RedisConst;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


@Configuration
public class FeignInterceptorConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        //获取
        return (requestTemplate) -> {
            /*
            RequestContextHolder是SpringMVC提供的一个：当前线程与请求绑定的一个容器
                从其中可以根据当前线程来获取当前请求，进而获取当前请求中的信息
             */
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();


            String userId = request.getHeader(RedisConst.USERID_HEADER);
            requestTemplate.header(RedisConst.USERID_HEADER, userId);

            String userTempId = request.getHeader(RedisConst.USERTEMPID_HEADER);
            requestTemplate.header(RedisConst.USERTEMPID_HEADER, userTempId);
        };
    }
}
