package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.feign.product.SkuInfoFeignClient;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.starter.cache.annotation.GmallCache;
import com.atguigu.starter.cache.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    private SkuInfoFeignClient skuInfoFeignClient;

    //    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) {
//        return skuDetailFeignClient.getSkuDetail(skuId).getData();
//    }
    @Autowired
    private ThreadPoolExecutor executor;

    Lock lock = new ReentrantLock();


    //    HashMap<Long, SkuDetailTo> map = new HashMap<>();
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Autowired
    private CacheService cacheService;

    public SkuDetailTo getSkuDetailV1(Long skuId) {



        SkuDetailTo skuDetailTo = new SkuDetailTo();
        //采用异步编排方式来对查询商品详情进行异步处理

        SkuInfo skuInfo = skuInfoFeignClient.getSkuInfo(skuId).getData();
        //1.设置图片列表
        List<SkuImage> skuImageList = skuInfoFeignClient.getSkuImageList(skuId).getData();
        skuInfo.setSkuImageList(skuImageList);
        //2.设置基本信息
        skuDetailTo.setSkuInfo(skuInfo);
        //3.设置实时价格
        BigDecimal price = skuInfoFeignClient.get1010Price(skuId).getData();
        skuDetailTo.setPrice(price);

        //4.设置分类
        CategoryViewTo categoryViewTo = skuInfoFeignClient.getCategoryView(skuInfo.getCategory3Id()).getData();
        skuDetailTo.setCategoryView(categoryViewTo);
        //5.设置sku属性
        List<SpuSaleAttr> spuSaleAttrList = skuInfoFeignClient.getSpuSaleAttrList(skuInfo.getSpuId(), skuId).getData();
        skuDetailTo.setSpuSaleAttrList(spuSaleAttrList);
        //6.设置skuValueJson
        String str = skuInfoFeignClient.getValueJson(skuInfo.getSpuId()).getData();
        skuDetailTo.setValuesSkuJson(str);

        return skuDetailTo;
    }

    public SkuDetailTo getSkuDetailRPC(Long skuId) throws Exception {


        SkuDetailTo skuDetailTo = new SkuDetailTo();
        //采用异步编排方式来对查询商品详情进行异步处理
        //2.设置基本信息
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = skuInfoFeignClient.getSkuInfo(skuId).getData();
            skuDetailTo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);

        //1.设置图片列表
        CompletableFuture<Void> imageFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SkuImage> skuImageList = skuInfoFeignClient.getSkuImageList(skuId).getData();
            skuInfo.setSkuImageList(skuImageList);
        }, executor);


        //3.设置实时价格
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = skuInfoFeignClient.get1010Price(skuId).getData();
            skuDetailTo.setPrice(price);
        }, executor);


        //4.设置分类
        CompletableFuture<Void> categoryFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            CategoryViewTo categoryViewTo = skuInfoFeignClient.getCategoryView(skuInfo.getCategory3Id()).getData();
            skuDetailTo.setCategoryView(categoryViewTo);
        }, executor);

        //5.设置sku属性
        CompletableFuture<Void> skuAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = skuInfoFeignClient.getSpuSaleAttrList(skuInfo.getSpuId(), skuId).getData();
            skuDetailTo.setSpuSaleAttrList(spuSaleAttrList);
        }, executor);

        //6.设置skuValueJson
        CompletableFuture<Void> valueJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            String str = skuInfoFeignClient.getValueJson(skuInfo.getSpuId()).getData();
            skuDetailTo.setValuesSkuJson(str);
        }, executor);

        //等待所有线程完成后，进行返回
        CompletableFuture.allOf(skuInfoFuture,
                imageFuture,
                priceFuture,
                categoryFuture,
                skuAttrFuture,
                valueJsonFuture)
                .join();


        return skuDetailTo;
    }

    public SkuDetailTo getSkuDetailWithCache(Long skuId) throws Exception {
        String cacheKey = RedisConst.SKUDETAIL_KEY_PREFIX + skuId;
        //1.从缓存中查询
        SkuDetailTo skuDetail = cacheService.getCacheData(cacheKey, SkuDetailTo.class);
        //2.判断缓存中是否查到
        if (skuDetail != null) {
            //说明缓存中有，直接返回
            return skuDetail;
        }
        //3.说明缓存未命中，询问bloom过滤器
        Boolean b = cacheService.containsInBloom(skuId);
        if (!b) {
            //bloom说无，一定无
            return null;
        }
        //4.bloom说有，可能有，加锁查询
        //试图抢锁
//        boolean b1 = lock.tryLock();
        boolean b1 = cacheService.tryLock(skuId);

        if (!b1) {
            //抢不到锁，睡眠1s后直接查询内存
            try {
                TimeUnit.SECONDS.sleep(1);
                return cacheService.getCacheData(cacheKey, SkuDetailTo.class);
            } catch (Exception e) {

            }
        }
        //抢到锁了,在数据库中进行查询
        SkuDetailTo skuDetailRPC = getSkuDetailRPC(skuId);
        log.info("{}商品缓存未命中，准备回源。。。。当前线程为：{}", skuId, Thread.currentThread().getName());
        //放入缓存
        cacheService.saveCacheData(cacheKey, skuDetailRPC);
        //bloom过滤器中添加此商品
        cacheService.addSkuIdForBloom(skuId);
        //解锁
//        lock.unlock();
        cacheService.unlock(skuId);

        return skuDetailRPC;
    }

    @Override
    @GmallCache(cacheKey = RedisConst.SKUDETAIL_KEY_PREFIX + "#{#params[0]}",
            bloomName = RedisConst.BLOOM_SKUID,
            bloomValue = "#{#params[0]}",
            lockName = RedisConst.LOCK_SKU_DETAIL + "#{#params[0]}",
            ttl=60*30L)
    public SkuDetailTo getSkuDetail(Long skuId) throws Exception {
        SkuDetailTo skuDetailRPC = getSkuDetailRPC(skuId);
        return skuDetailRPC;
    }
    //更新热度分的方法
    @Override
    public void updateHotScore(Long skuId) {
        //从redis中取出热度分
        Long increment = redisTemplate.opsForValue().increment(RedisConst.SKU_HOTSCORE_PREFIX + skuId);
        //远程调用接口对ES中的热度分进行更新
        if (increment % 100 == 0) {
            //累计到每100分更新一次
            searchFeignClient.updateHotScore(skuId, increment);
        }
    }


    //使用分布式缓存
//    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) throws  Exception {
//
//
//        String json = redisTemplate.opsForValue().get("skuDetail:info:" + skuId);
//        if ("x".equals(json)) {
//            //查到值为"x",说明数据库中暂无此条数据，直接返回，避免缓存穿透攻击
//            return null;
//        }
//
//        if (StringUtils.isEmpty(json)) {
//            //说明为空，且是第一次查询
//            SkuDetailTo skuDetailRPC = getSkuDetailRPC(skuId);
//            if (skuDetailRPC==null) {
//                //如果数据库中没有此数据，则在redis中缓存一个值，表明此值最近被查询过，解决缓存穿透攻击问题
//                redisTemplate.opsForValue().set("skuDetail:info:" + skuId, "x", 30, TimeUnit.MINUTES);
//            }
//            redisTemplate.opsForValue().set("skuDetail:info:" + skuId, Jsons.toStr(skuDetailRPC), 7, TimeUnit.DAYS);
//            return skuDetailRPC;
//        }
//        //缓存中有，且数据不为"x",将json字符串转为对象返回
//        return Jsons.toObj(json, SkuDetailTo.class);
//    }
    //使用本地缓存
//    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) throws Exception {
//
//        SkuDetailTo skuDetailTo = map.get(skuId);
//        if (skuDetailTo != null) {
//            return skuDetailTo;
//        }
//        SkuDetailTo skuDetailRPC = getSkuDetailRPC(skuId);
//        map.put(skuId, skuDetailRPC);
//        return skuDetailRPC;
//    }
    /**
     * redis缓存存在的三个问题
     *      1.缓存穿透：
     *          理解：高并发查询redis、MySQL中都不存在的数据
     *          影响：造成系统频繁的去回源：即高并发的去查询数据库的数据
     *               缓存穿透是正常的，但是缓存穿透攻击不被允许
     *          解决方案：加入空值缓存
     *
     *
     *      2.缓存穿透：
     *          理解：高并发查询大量的缓存不存在的数据，但数据库中有的数据
     *          影响：造成系统频繁的去回源：即高并发的去查询数据库的数据
     *          解决方案：加分布式锁（分布式项目） /单体项目（本地锁）
     *
     *
     *      3.缓存雪崩：
     *          理解：缓存中的大量数据在同一时间失效，同时，遭到用户的高并发访问，造成高并发的数据库IO
     *          影响：高并发的去查询数据库的数据
     *          解决方案：在对数据库中的数据进行缓存时，过期时间设置：业务时长*2+随机时长
     *                  （在业务中此种情况较为极端）
     *
     */
}
