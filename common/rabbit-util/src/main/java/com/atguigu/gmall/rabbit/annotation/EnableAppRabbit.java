package com.atguigu.gmall.rabbit.annotation;


import com.atguigu.gmall.rabbit.config.MqConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(MqConfig.class)
public @interface EnableAppRabbit {

}
