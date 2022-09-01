package com.atguigu.gmall.item.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GmallCache {

    String cacheKey() default "";

    String bloomName() default "";

    String bloomValue() default "";


    String lockName() default "lock:global";
}
