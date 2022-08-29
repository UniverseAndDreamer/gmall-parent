package com.atguigu.gmall.common.annotation;

import com.atguigu.gmall.common.config.thread.ThreadPoolAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 此注解将ThreadPoolAutoConfiguration
 */
@Import(ThreadPoolAutoConfiguration.class)
public @interface EnableThreadPool {
}
