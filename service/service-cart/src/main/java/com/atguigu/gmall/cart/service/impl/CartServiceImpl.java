package com.atguigu.gmall.cart.service.impl;

import java.math.BigDecimal;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
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
import jodd.time.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SkuInfoFeignClient skuInfoFeignClient;
    @Autowired
    private ThreadPoolExecutor executor;


    /**
     * 添加商品至购物车
     * @param skuId
     * @param skuNum
     * @return
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
     * 获取购物车列表
     * @return
     */
    @Override
    public List<CartInfo> cartList(String cartKey) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        List<String> values = hashOps.values();
        List<CartInfo> cartInfoList = values.stream()
                .map(str -> {
                    CartInfo cartInfo = Jsons.toObj(str, CartInfo.class);
                    return cartInfo;
                })
                .sorted((t2, t1) -> {
                    int i = t1.getCreateTime().compareTo(t2.getCreateTime());
                    return i;
                })
                .collect(Collectors.toList());
        //再对商品的价格进行异步更新,异步执行，不保证立即执行
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        executor.submit(() -> {
            //发起异步请求时，给当前异步线程绑定老请求信息
            RequestContextHolder.setRequestAttributes(requestAttributes);
            updateCartItemsPrice(cartKey, cartInfoList);
            //重置请求中的数据
            RequestContextHolder.resetRequestAttributes();
        });
        return cartInfoList;
    }

    /**
     * 修改订单选取的状态
     * @param skuId
     * @param checkState
     * @return
     */
    @Override
    public void checkCart(Long skuId, Integer checkState) {
        String cartKey = determineCartKey();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = Jsons.toObj(hashOps.get(skuId.toString()), CartInfo.class);
        cartInfo.setIsChecked(checkState);
        cartInfo.setUpdateTime(new Date());
        hashOps.put(skuId.toString(), Jsons.toStr(cartInfo));
    }

    /**
     * 删除所选的商品
     * @param skuId
     */
    @Override
    public void deleteCart(Long skuId) {
        String cartKey = determineCartKey();
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate.boundHashOps(cartKey);
        hashOps.delete(skuId.toString());
    }

    /**
     * 删除选中的商品
     */
    @Override
    public void deleteChecked() {
        //1.得出选中的商品Id
        String cartKey = determineCartKey();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        hashOps.values().stream()
                .map(str->{
                    CartInfo cartInfo = Jsons.toObj(str, CartInfo.class);
                    return cartInfo;
                })
                .filter(cartInfo -> {
                    Integer isChecked = cartInfo.getIsChecked();
                    return isChecked.equals(1);
                })
                .forEach(cartInfo -> {
                    String str = Jsons.toStr(cartInfo);
                    hashOps.delete(cartInfo.getSkuId().toString());
                });

    }

    /**
     * 购物车合并
     */
    @Override
    public void mergeTempAndUserCart() {
        //购物车合并条件：登录+临时购物车有东西
        UserAuthInfo info = AuthUtils.getCurrentAuthInfo();
        String userTempId = info.getUserTempId();
        if (info.getUserId() != null && !StringUtils.isEmpty(userTempId)) {
            //说明用户登录了,获取临时购物车
            String tempCartKey = RedisConst.CART_KEY + userTempId;
            List<CartInfo> cartInfos = cartList(tempCartKey);
            if (cartInfos != null && cartInfos.size() > 0) {
                //说明临时购物车中有东西，进行合并
                String cartKey = determineCartKey();
                for (CartInfo cartInfo : cartInfos) {
                    Long skuId = cartInfo.getSkuId();
                    Integer skuNum = cartInfo.getSkuNum();
                    addItemToCart(skuId,skuNum , cartKey);
                    redisTemplate.opsForHash().delete(tempCartKey, skuId.toString());

                }
            }
        }
        //
    }

    //TODO 购物车统一异常处理
    /**
     * 更新购物车实时价格
     * @param cartKey
     * @param cartInfoList
     */
    private void updateCartItemsPrice(String cartKey, List<CartInfo> cartInfoList) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        System.out.println(Thread.currentThread().getName() + "进程开始更新实时价格");
        cartInfoList.stream()
                .forEach(cartInfo -> {
                    BigDecimal price = skuInfoFeignClient.get1010Price(cartInfo.getSkuId()).getData();
                    //设置最新价格
                    cartInfo.setSkuPrice(price);
                    cartInfo.setUpdateTime(new Date());
                    //保存进购物车
                    hashOps.put(cartInfo.getSkuId().toString(), Jsons.toStr(cartInfo));
                });
        System.out.println(Thread.currentThread().getName() + "进程更新实时价格结束");
    }

    /**
     * 添加商品至购物车
     *
     * @param skuId
     * @param skuNum
     * @param cartKey
     * @return
     */
    private SkuInfo addItemToCart(Long skuId, Integer skuNum, String cartKey) {
        //1. 获取redis中的购物车
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        //2. 向购物车中添加商品
        //如果购物车中存在此商品，则将此商品数量改变，如果不存在此商品，则新增
        //2.1根据skuId获取商品
        SkuInfo data = skuInfoFeignClient.getSkuInfo(skuId).getData();
        Boolean skuExists = hashOps.hasKey(skuId.toString());
        if (!skuExists) {
            //说明商品在购物车中不存在,进行商品的新增
            // 新增时应该判断购物车中商品的品类是否超过200
            List<CartInfo> cartInfos = cartList(cartKey);

            if (cartInfos.size() + 1 > 2) {
                //说明超过数量限制
                throw new GmallException(ResultCodeEnum.FAIL);
            }
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
            //如果新增的商品数量超出限制，不予新增
            if (cartInfo.getSkuNum()+skuNum>200) {
                throw new GmallException(ResultCodeEnum.FAIL);
            }
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setUpdateTime(new Date());
            hashOps.put(skuId.toString(), Jsons.toStr(cartInfo));

            data = castToSkuInfo(cartInfo);
        }

        return data;
    }

    /**
     * 类型转换CartInfo  to  SkuInfo
     * @param cartInfo
     * @return
     */
    private SkuInfo castToSkuInfo(CartInfo cartInfo) {
        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setPrice(cartInfo.getSkuPrice());
        skuInfo.setSkuName(cartInfo.getSkuName());
        skuInfo.setSkuDefaultImg(cartInfo.getImgUrl());
        skuInfo.setId(cartInfo.getSkuId());

        return skuInfo;
    }

    /**
     * 类型转换  SkuInfo to CartInfo
     * @param data
     * @param skuNum
     * @return
     */
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
    @Override
    public String determineCartKey() {
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
