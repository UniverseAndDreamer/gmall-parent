package com.atguigu.gmall.cart.service.impl;
import java.math.BigDecimal;

import com.atguigu.gmall.common.util.Jsons;
import com.google.common.collect.Lists;
import java.sql.Timestamp;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.feign.product.SkuInfoFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.user.UserAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SkuInfoFeignClient skuInfoFeignClient;



    /**
     * 添加商品至购物车
     * @return
     * @param skuId
     * @param skuNum
     */
    @Override
    public SkuInfo addToCart(Long skuId, Integer skuNum) {
        //获取购物车的key
        String cartKey = determineCartKey();
        //给购物车添加指定商品
        SkuInfo skuInfo = addItemToCart(skuId, skuNum, cartKey);
        //给临时购物车设置过期时间
        UserAuthInfo currentAuthInfo = AuthUtils.getCurrentAuthInfo();
        if (currentAuthInfo.getUserId() == null) {
            //说明用户未登录
            String tempCartKey = RedisConst.CART_KEY + currentAuthInfo.getUserTempId();
            redisTemplate.expire(tempCartKey, 90, TimeUnit.DAYS);
        }

        return skuInfo;
    }

    /**
     * 添加商品至购物车
     * @param skuId
     * @param skuNum
     * @param cartKey
     * @return
     */
    private SkuInfo addItemToCart(Long skuId, Integer skuNum, String cartKey) {
        //1. 获取redis中的购物车
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        //2. 向购物车中添加商品
        //如果购物车中存在此商品，则将此商品数量改变
        //如果不存在此商品，则新增
        //2.1根据skuId获取商品
        SkuInfo data = skuInfoFeignClient.getSkuInfo(skuId).getData();
        Boolean skuExists = hashOps.hasKey(skuId.toString());
        if (!skuExists) {
            //说明商品在购物车中不存在,进行商品的新增
            //2.2类型转换
            CartInfo cartInfo = castToCartInfo(data, skuNum);
            //2.3将信息存入购物车
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
            hashOps.put(skuId.toString(), Jsons.toStr(cartInfo));
            data = castToSkuInfo(cartInfo);
        } else {
            //说明商品在购物车中存在，进行数量的改变
            String skuJson = hashOps.get(skuId.toString());
            CartInfo cartInfo = Jsons.toObj(skuJson, CartInfo.class);
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setUpdateTime(new Date());
            hashOps.put(skuId.toString(), Jsons.toStr(cartInfo));

            data = castToSkuInfo(cartInfo);
        }

        return data;
    }

    private SkuInfo castToSkuInfo(CartInfo cartInfo) {
        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setPrice(cartInfo.getSkuPrice());
        skuInfo.setSkuName(cartInfo.getSkuName());
        skuInfo.setSkuDefaultImg(cartInfo.getImgUrl());
        skuInfo.setId(cartInfo.getSkuId());

        return skuInfo;
    }

    private CartInfo castToCartInfo(SkuInfo data, Integer skuNum) {
        CartInfo cartInfo = new CartInfo();

        cartInfo.setSkuId(data.getId());
        cartInfo.setCartPrice(data.getPrice());
        cartInfo.setSkuNum(skuNum);
        cartInfo.setImgUrl(data.getSkuDefaultImg());
        cartInfo.setSkuName(data.getSkuName());
        cartInfo.setIsChecked(1);
        cartInfo.setCreateTime(new Date());

        cartInfo.setUpdateTime(new Date());
        cartInfo.setSkuPrice(data.getPrice());

        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        if (userId != null) {
            cartInfo.setUserId(userId.toString());
        } else {
            cartInfo.setUserId(AuthUtils.getCurrentAuthInfo().getUserTempId());
        }


        return cartInfo;
    }

    /**
     * 获取购物车key
     * @return
     */
    private String determineCartKey() {
        UserAuthInfo currentAuthInfo = AuthUtils.getCurrentAuthInfo();
        Long userId = currentAuthInfo.getUserId();
        String userTempId = currentAuthInfo.getUserTempId();
        String cartKey = "";
        if (userId != null) {
            //说明userId不为空，使用用户Id
            cartKey = RedisConst.CART_KEY + userId;
        } else {
            //使用userTempId
            cartKey = RedisConst.CART_KEY + userTempId;
        }
        return cartKey;

    }
}
