package com.atguigu.gmall.item.service.impl;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;

    //    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) {
//        return skuDetailFeignClient.getSkuDetail(skuId).getData();
//    }
    @Autowired
    private ThreadPoolExecutor executor;

//    HashMap<Long, SkuDetailTo> map = new HashMap<>();
    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) {
//
//
//
//        SkuDetailTo skuDetailTo = new SkuDetailTo();
//        //采用异步编排方式来对查询商品详情进行异步处理
//
//        SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
//        //1.设置图片列表
//        List<SkuImage> skuImageList = skuDetailFeignClient.getSkuImageList(skuId).getData();
//        skuInfo.setSkuImageList(skuImageList);
//        //2.设置基本信息
//        skuDetailTo.setSkuInfo(skuInfo);
//        //3.设置实时价格
//        BigDecimal price = skuDetailFeignClient.get1010Price(skuId).getData();
//        skuDetailTo.setPrice(price);
//
//        //4.设置分类
//        CategoryViewTo categoryViewTo = skuDetailFeignClient.getCategoryView(skuInfo.getCategory3Id()).getData();
//        skuDetailTo.setCategoryView(categoryViewTo);
//        //5.设置sku属性
//        List<SpuSaleAttr> spuSaleAttrList = skuDetailFeignClient.getSpuSaleAttrList(skuInfo.getSpuId(), skuId).getData();
//        skuDetailTo.setSpuSaleAttrList(spuSaleAttrList);
//        //6.设置skuValueJson
//        String str = skuDetailFeignClient.getValueJson(skuInfo.getSpuId()).getData();
//        skuDetailTo.setValuesSkuJson(str);
//
//        return skuDetailTo;
//    }


    public SkuDetailTo getSkuDetailRPC(Long skuId) throws Exception {


        SkuDetailTo skuDetailTo = new SkuDetailTo();
        //采用异步编排方式来对查询商品详情进行异步处理
        //2.设置基本信息
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
            skuDetailTo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);

        //1.设置图片列表
        CompletableFuture<Void> imageFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SkuImage> skuImageList = skuDetailFeignClient.getSkuImageList(skuId).getData();
            skuInfo.setSkuImageList(skuImageList);
        }, executor);


        //3.设置实时价格
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = skuDetailFeignClient.get1010Price(skuId).getData();
            skuDetailTo.setPrice(price);
        }, executor);


        //4.设置分类
        CompletableFuture<Void> categoryFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            CategoryViewTo categoryViewTo = skuDetailFeignClient.getCategoryView(skuInfo.getCategory3Id()).getData();
            skuDetailTo.setCategoryView(categoryViewTo);
        }, executor);

        //5.设置sku属性
        CompletableFuture<Void> skuAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = skuDetailFeignClient.getSpuSaleAttrList(skuInfo.getSpuId(), skuId).getData();
            skuDetailTo.setSpuSaleAttrList(spuSaleAttrList);
        }, executor);

        //6.设置skuValueJson
        CompletableFuture<Void> valueJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            String str = skuDetailFeignClient.getValueJson(skuInfo.getSpuId()).getData();
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

    @Override
    public SkuDetailTo getSkuDetail(Long skuId) throws  Exception {


        String json = redisTemplate.opsForValue().get("skuDetail:info:" + skuId);
        if ("x".equals(json)) {
            return null;
        }

        if (StringUtils.isEmpty(json)) {
            //说明为空，且是第一次查询
            SkuDetailTo skuDetailRPC = getSkuDetailRPC(skuId);
            if (skuDetailRPC==null) {
                //如果数据库中没有此数据，则在redis中缓存一个值，表明此值最近被查询过，解决缓存穿透攻击问题
                redisTemplate.opsForValue().set("skuDetail:info:" + skuId, "x", 30, TimeUnit.MINUTES);
            }
            redisTemplate.opsForValue().set("skuDetail:info:" + skuId, Jsons.toStr(skuDetailRPC), 7, TimeUnit.DAYS);
            return skuDetailRPC;
        }
        //缓存中有，且数据不为"x",将json字符串转为对象返回
        return Jsons.toObj(json, SkuDetailTo.class);
    }
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
    //使用redis缓存
}
