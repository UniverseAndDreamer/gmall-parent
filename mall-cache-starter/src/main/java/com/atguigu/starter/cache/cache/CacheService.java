package com.atguigu.starter.cache.cache;


import java.lang.reflect.Type;

public interface CacheService {

    /**
     * 将缓存中的数据转为简单对象
     * @param cacheKey
     * @param tClass
     * @param <T>
     * @return
     */
    <T>T getCacheData(String cacheKey, Class<T> tClass);

    <T> T getCacheData(String cacheKey, Type type);

    Boolean containsInBloom(Long skuId);

    void saveCacheData(String s, Object formRPC);

    void saveCacheData(String s, Object formRPC, long ttl);

    void addSkuIdForBloom(Long skuId);

    boolean tryLock(Long skuId);

    void unlock(Long skuId);

    boolean containsInBloom(String bloomName, Object arg);

    boolean tryLock(String lockName);

    void unlock(String lockName);

    void delay2Delete(String cacheKey);

}
