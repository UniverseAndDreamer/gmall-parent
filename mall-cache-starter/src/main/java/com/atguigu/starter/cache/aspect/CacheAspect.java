package com.atguigu.starter.cache.aspect;


import com.atguigu.starter.cache.annotation.GmallCache;
import com.atguigu.starter.cache.cache.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheAspect {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedissonClient redissonClient;

    ExpressionParser parser = new SpelExpressionParser();
    ParserContext parserContext = new TemplateParserContext();


    /**
     *
     * @param joinPoint
     * @return
     */
    @Around("@annotation(com.atguigu.starter.cache.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //前置通知：
        System.out.println("前置通知。。。。");
        //  1.获取缓存名
        Object[] args = joinPoint.getArgs();
        Object arg = args[0];

        String cacheKey = determineCacheKey(joinPoint);
        //  2.从缓存中查询数据
        //      2.1得到方法的返回值类型
        Type type = getMethodGenericReturnType(joinPoint);

        Object cacheData = cacheService.getCacheData(cacheKey, type);
        if (cacheData != null) {
            //说明缓存中存在此数据
            return cacheData;
        }
        //3.说明缓存中不存在此数据，询问Bloom过滤器中有没有此数据,而有些数据不用问布隆
        String bloomName = determineBloomName(joinPoint);
        //3.1 bloom有可能不存在
        boolean contains = false;
        if (!StringUtils.isEmpty(bloomName)) {
            //说明bloom不为空，需要询问bloom
            Object bValue = determineBloomValue(joinPoint);
            contains = cacheService.containsInBloom(bloomName, arg);
        }

        if (!contains) {
            //bloom说无，一定无
            return null;
        }
        //4.bloom说有，可能有
        //加锁查询
        boolean lock = false;
        String lockName = "";

        try {
            lockName = determineLockName(joinPoint);
            lock = cacheService.tryLock(lockName);
            if (lock) {
                //5.加锁成功，调用业务方法
                Object proceed = joinPoint.proceed(args);
                //返回通知：
                //6.存入缓存中,
                cacheService.saveCacheData(cacheKey, proceed);
                return proceed;
            }else{
                //加锁失败，当前线程睡眠1s后直接查询内存
                TimeUnit.SECONDS.sleep(1);
                return cacheService.getCacheData(cacheKey, type);
            }
        }finally {
            //7.解锁
            cacheService.unlock(lockName);
        }
    }

    private String determineLockName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String expression = method.getAnnotation(GmallCache.class).lockName();
        String lockName = evaluateExpression(expression, joinPoint, String.class);
        return lockName;
    }

    private Object determineBloomValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String expression = method.getAnnotation(GmallCache.class).bloomValue();
        Object obj = evaluateExpression(expression, joinPoint, Object.class);
        return obj;
    }

    private String determineBloomName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        String bloomName = annotation.bloomName();
        return bloomName;
    }

    private Type getMethodGenericReturnType(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Type genericReturnType = method.getGenericReturnType();
        return genericReturnType;
    }

    /**
     * 通过注解计算cacheKey
     * @param joinPoint
     * @return
     */
    private String determineCacheKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String expression = method.getAnnotation(GmallCache.class).cacheKey();
        String cacheKey = evaluateExpression(expression, joinPoint, String.class);
        return cacheKey;
    }

    private <T> T evaluateExpression(String expression, ProceedingJoinPoint joinPoint, Class<T> clz) {

        Expression parseExpression = parser.parseExpression(expression, parserContext);
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        context.setVariable("params", args);
        T value = parseExpression.getValue(context, clz);
        return value;
    }


}
