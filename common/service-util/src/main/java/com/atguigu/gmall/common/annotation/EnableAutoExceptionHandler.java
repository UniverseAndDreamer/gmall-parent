package com.atguigu.gmall.common.annotation;

import com.atguigu.gmall.common.config.thread.ThreadPoolAutoConfiguration;
import com.atguigu.gmall.common.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 此注解将导入GlobalExceptionHandler
 */
@Import(GlobalExceptionHandler.class)
public @interface EnableAutoExceptionHandler {
}
/**
 * 异常处理器：
 *      1.创建一个异常处理类
 *      2.加入ControllerAdvice注解
 *      3.创建异常处理方法
 */
