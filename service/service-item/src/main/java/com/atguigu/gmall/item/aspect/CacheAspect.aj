package com.atguigu.gmall.item.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class CacheAspect {

    /**
     * 整合AOP，面向切面编程，将
     * @param joinPoint
     * @return
     */
    @Around("@annotation(com.atguigu.gmall.item.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) {

        //前置通知：

        //返回通知：

        //异常通知：

        //后置通知：

        return null;
    }

}
