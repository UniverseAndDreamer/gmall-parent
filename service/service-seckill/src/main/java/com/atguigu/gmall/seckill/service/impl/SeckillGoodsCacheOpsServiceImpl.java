package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillGoodsCacheOpsServiceImpl implements SeckillGoodsCacheOpsService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    //本地缓存
    Map<Long, SeckillGoods> goodsLocalCache = new ConcurrentHashMap<>();

    /**
     * 将秒杀商品存入缓存
     * @param list
     */
    @Override
    public void upSeckillGoods(List<SeckillGoods> list) {
        //存储至--redis中
        String date = DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate.boundHashOps(RedisConst.CACHE_SECKILL_GOODS + date);
        hashOps.expire(2, TimeUnit.DAYS);
        list.stream().forEach(seckillGoods -> {
            //redis存入商品
            hashOps.put(seckillGoods.getSkuId().toString(), Jsons.toStr(seckillGoods));
            //redis存储商品的库存,cacheKey -->  seckill:goods:stock:49
            String cacheKey = RedisConst.CACHE_SECKILL_GOODS_STOCK + seckillGoods.getSkuId();
            redisTemplate.opsForValue().setIfAbsent(cacheKey, seckillGoods.getStockCount().toString(),1, TimeUnit.DAYS);
            //存入本地库存中
            goodsLocalCache.put(seckillGoods.getSkuId(), seckillGoods);
        });

    }

    /**
     * 从本地缓存中获取SeckillGoodsList
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoodsFromLocal() {
        List<SeckillGoods> list = goodsLocalCache.values().stream()
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());
        if (list == null || list.size() == 0) {
            syncLocalAndRedisCache();
            list = goodsLocalCache.values().stream()
                    .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                    .collect(Collectors.toList());
        }
        return list;
    }

    /**
     * 将查询到的seckillGoodsList同步入本地缓存
     */
    private void syncLocalAndRedisCache() {
        List<SeckillGoods> goods = getSeckillGoodsFromRemote();
        goods.stream().forEach(item->{
            goodsLocalCache.put(item.getSkuId(), item);
        });
    }

    /**
     * redis中查询seckillGoods列表
     * @return
     */
    private List<SeckillGoods> getSeckillGoodsFromRemote() {
        String date = DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate.boundHashOps(RedisConst.CACHE_SECKILL_GOODS + date);
        List<SeckillGoods> goods = hashOps.values().stream()
                .map(s -> Jsons.toObj(s, SeckillGoods.class))
                .collect(Collectors.toList());
        return goods;
    }

    /**
     * 清除本地缓存
     */
    @Override
    public void clearCache() {
        goodsLocalCache.clear();

    }

    @Override
    public SeckillGoods getSeckillGoodsBySkuId(Long skuId) {
        SeckillGoods seckillGoods = goodsLocalCache.get(skuId);
        if (seckillGoods == null) {
            //说明本地缓存中没有
            syncLocalAndRedisCache();
            seckillGoods = goodsLocalCache.get(skuId);
        }
        return seckillGoods;
    }


}
