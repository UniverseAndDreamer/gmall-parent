package com.atguigu.starter.cache.cache;

import com.atguigu.starter.cache.constant.RedisConst;
import com.atguigu.starter.cache.util.Jsons;
import com.fasterxml.jackson.core.type.TypeReference;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;


@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public <T> T getCacheData(String s, Class<T> tClass) {
        String json = redisTemplate.opsForValue().get(s);
        if (json == null || RedisConst.VALUE_NULL_TTL.equals(s)) {
            //说明缓存中无此数据
            return null;
        }
        //说明缓存中存在数据
        T t = Jsons.toObj(json, tClass);
        return t;
    }

    @Override
    public <T> T getCacheData(String cacheKey, Type type) {
        String json = redisTemplate.opsForValue().get(cacheKey);
        if (json == null || RedisConst.VALUE_NULL_TTL.equals(cacheKey)) {
            //说明缓存中无此数据
            return null;
        }
        //说明缓存中存在数据
        T t = Jsons.toObj(json, new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        });
        return t;


    }

    @Override
    public Boolean containsInBloom(Long skuId) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.BLOOM_SKUID);
        return bloomFilter.contains(skuId);
    }

    /**
     * 缓存数据
     * @param s
     * @param formRPC
     */
    @Override
    public void saveCacheData(String s, Object formRPC) {
        if (formRPC == null) {
            //说明要缓存的数据为空
            redisTemplate.opsForValue().set(s, RedisConst.SKUDETAIL_VALUE_NULL, RedisConst.VALUE_NULL_TTL, TimeUnit.SECONDS);
        } else {
            String jsonValue = Jsons.toStr(formRPC);
            redisTemplate.opsForValue().set(s, jsonValue, RedisConst.VALUE_TTL, TimeUnit.SECONDS);
        }
    }

    /**
     * 向Bloom过滤器中添加数据
     * @param skuId
     */
    @Override
    public void addSkuIdForBloom(Long skuId) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.BLOOM_SKUID);
        bloomFilter.add(skuId);
    }

    @Override
    public boolean tryLock(Long skuId) {

        RLock lock = redissonClient.getLock(RedisConst.LOCK_SKU_DETAIL + skuId);
        boolean b = lock.tryLock();
        return b;
    }

    @Override
    public void unlock(Long skuId) {
        RLock lock = redissonClient.getLock(RedisConst.LOCK_SKU_DETAIL + skuId);
        lock.unlock();

    }

    @Override
    public boolean containsInBloom(String bloomName, Object arg) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(bloomName);

        return bloomFilter.contains(arg);
    }

    @Override
    public boolean tryLock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        boolean b = lock.tryLock();
        return b;
    }

    @Override
    public void unlock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        lock.unlock();
    }



}
