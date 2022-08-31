package com.atguigu.gmall.item.cache;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
        if (json == null || json.equals(RedisConst.VALUE_NULL_TTL)) {
            //说明缓存中无此数据
            return null;
        }
        //说明缓存中存在数据
        T t = Jsons.toObj(s, tClass);
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
}
