package com.atguigu.gmall.item.cache;


public interface CacheService {


    <T>T getCacheData(String s, Class<T> tClass);


    Boolean containsInBloom(Long skuId);

    void saveCacheData(String s, Object formRPC);

    void addSkuIdForBloom(Long skuId);
}
