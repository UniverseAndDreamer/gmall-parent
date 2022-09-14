package com.atguigu.gmall.order.biz.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.SkuInfoFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.order.CartInfoVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.google.common.collect.Lists;

import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderBizServiceImpl implements OrderBizService {
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WareFeignClient wareFeignClient;
    @Autowired
    private SkuInfoFeignClient skuInfoFeignClient;
    @Autowired
    private OrderInfoService orderInfoService;

    @Override
    public OrderConfirmDataVo getOrderConfirmData() {
        OrderConfirmDataVo orderConfirmDataVo = new OrderConfirmDataVo();
        //1.购物车中选中的商品的商品信息
        List<CartInfo> cartInfos =  cartFeignClient.getCheckedSku().getData();
        List<CartInfoVo> cartInfoVos = cartInfos.stream().map(cartInfo -> {
            CartInfoVo cartInfoVo = new CartInfoVo();
            cartInfoVo.setSkuId(cartInfo.getSkuId());
            cartInfoVo.setImgUrl(cartInfo.getImgUrl());
            cartInfoVo.setSkuName(cartInfo.getSkuName());
            cartInfoVo.setOrderPrice(cartInfo.getSkuPrice());
            cartInfoVo.setSkuNum(cartInfo.getSkuNum());
            cartInfoVo.setHasStock(wareFeignClient.hasStock(cartInfo.getSkuId(), cartInfo.getSkuNum()));
            return cartInfoVo;
        }).collect(Collectors.toList());
        orderConfirmDataVo.setDetailArrayList(cartInfoVos);

        //2.设置商品总数量
        Integer skuNum = cartInfoVos.stream().map(CartInfoVo::getSkuNum).reduce((integer1, integer2) -> integer1 + integer2).get();
        orderConfirmDataVo.setTotalNum(skuNum);
        //3.设置商品总金额
        BigDecimal totalAmount = cartInfoVos.stream()
                .map(cartInfoVo -> cartInfoVo.getOrderPrice().multiply(new BigDecimal(cartInfoVo.getSkuNum())))
                .reduce((bigDecimal1, bigDecimal2) -> bigDecimal1.add(bigDecimal2))
                .get();

        orderConfirmDataVo.setTotalAmount(totalAmount);

        //4.设置用户地址
        List<UserAddress> userAddresses = userFeignClient.getUserAddressList().getData();
        orderConfirmDataVo.setUserAddressList(userAddresses);
        //5.设置订单追踪号：
        //5.1  订单唯一追踪号 对外交易号，与第三方交互
        //5.2  用来防止重复提交，做防重令牌
        String tradeNo = generateTradeNo();
        orderConfirmDataVo.setTradeNo(tradeNo);

        return orderConfirmDataVo;
    }

    @Override
    public Long submitOrder(String tradeNo, OrderSubmitVo vo) {
        //1.验证令牌tradeNo
        boolean checkedTradeNo = checkedTradeNo(tradeNo);
        if (!checkedTradeNo) {
            throw new GmallException(ResultCodeEnum.TOKEN_INVALID);
        }
        //2.验证库存
        List<String> stockShortageSkuName = new ArrayList<>();
        vo.getOrderDetailList().stream()
                .forEach(cartInfoVo -> {
                    Long skuId = cartInfoVo.getSkuId();
                    Integer skuNum = cartInfoVo.getSkuNum();
                    String hasStock = wareFeignClient.hasStock(skuId, skuNum);
                    if (!"1".equals(hasStock)) {
                        //说明库存不足
                        stockShortageSkuName.add(cartInfoVo.getSkuName());
                    }
                });
        if (stockShortageSkuName.size() > 0) {
            //说明存在库存不足的商品
            GmallException gmallException = new GmallException(ResultCodeEnum.ORDER_NO_STOCK);
            String skuNames = stockShortageSkuName.stream()
                    .reduce((s1, s2) -> s1 + "," + s2).get();
            throw new GmallException(ResultCodeEnum.ORDER_NO_STOCK.getMessage() + skuNames,
                    ResultCodeEnum.ORDER_NO_STOCK.getCode());
        }
        //3.验证价格
        List<String> skuNames = new ArrayList<>();
        vo.getOrderDetailList().stream()
                .forEach(cartInfoVo -> {
                    BigDecimal orderPrice = cartInfoVo.getOrderPrice();
                    BigDecimal sku1010Price = skuInfoFeignClient.get1010Price(cartInfoVo.getSkuId()).getData();
                    if (!sku1010Price.equals(orderPrice)) {
                        //说明价格不等
                        skuNames.add(cartInfoVo.getSkuName());
                    }
                });
        if (skuNames.size() > 0) {
            //说明存在价格不等的商品
            String skuName = skuNames.stream().reduce((s1, s2) -> s1 + "," + s2).get();
            throw new GmallException(ResultCodeEnum.ORDER_PRICE_CHANGED.getMessage() + skuName,
                    ResultCodeEnum.ORDER_PRICE_CHANGED.getCode());
        }

        //4.将信息保存至数据库
        Long orderId = orderInfoService.saveOrder(vo, tradeNo);
        //5.删除购物车中选中的商品
        cartFeignClient.deleteChecked();

        return orderId;
    }

    /**
     * 生成订单追踪号，在订单提交时进行验证
     * @return
     */
    private String generateTradeNo() {
        long millis = System.currentTimeMillis();
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String tradeNo = millis + "_" + userId;
        redisTemplate.opsForValue().set(RedisConst.ORDER_TEMP_TOKEN + tradeNo, "1", 15, TimeUnit.MINUTES);
        return tradeNo;
    }

    private boolean checkedTradeNo(String tradeNo) {
        //校验成功后要进行删除，因此校验成功操作与删除操作必须具备原子性
        //因此使用lua脚本

        String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then" +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else" +
                "    return 0 " +
                "end";
//        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
//        String s = redisTemplate.opsForValue().get(RedisConst.ORDER_TEMP_TOKEN + tradeNo);
//        if (StringUtils.isEmpty(s)) {
//            //说明没查到
//            return false;
//        }
//        redisTemplate.delete(RedisConst.ORDER_TEMP_TOKEN + tradeNo);

        Long execute = redisTemplate.execute(
                new DefaultRedisScript<Long>(lua, Long.class),
                Arrays.asList(RedisConst.ORDER_TEMP_TOKEN + tradeNo),
                new String[]{"1"});
        if (execute > 0) {
            //说明校验成功
            return true;
        }
        return false;
    }

}
