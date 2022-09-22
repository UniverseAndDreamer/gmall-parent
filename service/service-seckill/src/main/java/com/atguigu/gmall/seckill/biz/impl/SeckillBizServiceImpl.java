package com.atguigu.gmall.seckill.biz.impl;
import java.math.BigDecimal;

import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import com.google.common.collect.Lists;
import com.atguigu.gmall.model.activity.CouponInfo;

import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.rabbit.SeckillTempOrderMsg;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillBizServiceImpl implements SeckillBizService {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SeckillGoodsCacheOpsService cacheOpsService;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    public String getSeckillSkuIdStr(Long skuId) {
        //获取当前商品，并进行校验
        SeckillGoods goods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        //1.判断当前商品是否在秒杀商品列表中
        if (goods == null) {
            //说明当前商品不是秒杀商品
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }
        //2.若在秒杀商品中，判断秒杀商品是否在其秒杀时间
        if (goods.getStartTime().after(new Date())) {
            //商品秒杀开始时间，迟于现在，，比如开始时间19点，现在18点
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }
        if (goods.getEndTime().before(new Date())) {
            //商品秒杀结束时间，早于现在，，比如结束时间19点，现在20点
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }
        //3.判断是否还有足够库存
        if (goods.getStockCount() == 0) {
            //说明库存为0，说明已售罄
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }
        //4.上述判断都放行，生成秒杀码
        //秒杀码：同一个用户===同一天==同一商品  只能获取一个
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();

        String skuIdStr = generateskuIdStr(userId, DateUtil.formatDate(new Date()), skuId);

        return skuIdStr;
    }

    @Override
    public ResultCodeEnum seckillOrder(Long skuId, String skuIdStr) {
        //校验秒杀码：skuIdStr
        boolean checkSkuIdStr = checkSkuIdStr(skuId,skuIdStr);
        if (!checkSkuIdStr) {
            return ResultCodeEnum.SECKILL_ILLEGAL;
        }
        //获取当前商品
        SeckillGoods goods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        if (goods == null) {
            //说明当前商品不在秒杀商品列表中
            return ResultCodeEnum.SECKILL_ILLEGAL;
        }
        //1.判断当前商品是否过了秒杀时间
        if (goods.getStartTime().after(new Date())) {
            //商品秒杀开始时间，迟于现在，，比如开始时间19点，现在18,说明秒杀没开始
            return ResultCodeEnum.SECKILL_NO_START;
        }
        if (goods.getEndTime().before(new Date())) {
            //商品秒杀结束时间，早于现在，，比如结束时间19点，现在20,说说明秒杀已经结束
            return ResultCodeEnum.SECKILL_END;
        }
        //2.判断是否还有足够库存数量
        if (goods.getStockCount() <= 0) {
            //说明库存为0，说明已售罄
            return ResultCodeEnum.SECKILL_FINISH;
        }
        //判断这个请求是否已经发过，（同一用户，同一商品，同一天只能发一次）
        Long increment = redisTemplate.opsForValue().increment(RedisConst.SECKILL_CODE + skuIdStr);
        if (increment > 2) {
            //说明这个请求已经发过一次了
            return ResultCodeEnum.SUCCESS;
        }
        //开始秒杀业务
        Long decrement = redisTemplate.opsForValue().decrement(RedisConst.CACHE_SECKILL_GOODS_STOCK + skuId);
        if (decrement > 0) {
            //数据库中对订单-1
            goods.setStockCount(goods.getStockCount() - 1);
            OrderInfo orderInfo = prepareTempSeckillOrder(skuId);
            //redis中设置参数
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER + skuIdStr, Jsons.toStr(orderInfo), 1, TimeUnit.DAYS);
            //真正扣库存，创建订单
            String str = Jsons.toStr(new SeckillTempOrderMsg(orderInfo.getUserId(), skuId, skuIdStr));
            rabbitTemplate.convertAndSend(
                    MqConst.SECKILL_EVENT_EXCHANGE,
                    MqConst.SECKILL_ORDERWAIT_RK,
                    str);
            return ResultCodeEnum.SUCCESS;
        } else {
            return ResultCodeEnum.SECKILL_FINISH;
        }
    }

    /**
     * 排队中检查订单状态
     * @param skuId
     * @return
     */
    @Override
    public ResultCodeEnum checkOrderStatus(Long skuId) {

        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String formatDate = DateUtil.formatDate(new Date());
        String code = MD5.encrypt(userId + "_" + formatDate + "_" + skuId);
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + code);

        //1.是否已经下过单
        if (StringUtils.isEmpty(json)) {
            //说明临时单都不存在,正在排队中
            return ResultCodeEnum.SECKILL_RUN;
        }
        if ("x".equals(json)) {
            //说明商品已售罄
            return ResultCodeEnum.SECKILL_FINISH;
        }

        OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);
        //是否下过单
        if (orderInfo.getId() != null && orderInfo.getId() > 0) {
            //说明订单已经下过
            return ResultCodeEnum.SECKILL_ORDER_SUCCESS;
        }

        if (orderInfo.getOperateTime() != null) {
            //说明抢单成功
            return ResultCodeEnum.SECKILL_SUCCESS;
        }

        return ResultCodeEnum.SUCCESS;
    }

    @Override
    public SeckillConfirmVo getSeckillConfirmVo(Long skuId) {
        SeckillConfirmVo seckillConfirmVo = null;
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String formatDate = DateUtil.formatDate(new Date());
        String code = MD5.encrypt(userId + "_" + formatDate + "_" + skuId);
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + code);
        if (!StringUtils.isEmpty(json) || !"x".equals(json)) {
            //json不为空也不为"x"
            OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);
            seckillConfirmVo = new SeckillConfirmVo();
            seckillConfirmVo.setTempOrder(orderInfo);
            seckillConfirmVo.setTotalNum(orderInfo.getOrderDetailList().size());
            seckillConfirmVo.setTotalAmount(orderInfo.getTotalAmount());
            List<UserAddress> userAddresses = userFeignClient.getUserAddressList().getData();
            seckillConfirmVo.setUserAddressList(userAddresses);
        }
        return seckillConfirmVo;
    }

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @return
     */
    @Override
    public Long submitSeckillOrder(OrderInfo orderInfo) {
        OrderInfo dbOrder = prepareOrderInfo(orderInfo);
        return dbOrder.getId();
    }

    private OrderInfo prepareOrderInfo(OrderInfo orderInfo) {
        //TODO
        OrderInfo redisData = null;
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        Long skuId = orderInfo.getOrderDetailList().get(0).getSkuId();
        String formatDate = DateUtil.formatDate(new Date());
        String code = MD5.encrypt(userId + "_" + formatDate + "_" + skuId);
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + code);
        if (!StringUtils.isEmpty(json) || !"x".equals(json)) {
            redisData = Jsons.toObj(json, OrderInfo.class);
            redisData.setConsignee(orderInfo.getConsignee());
            redisData.setConsigneeTel(orderInfo.getConsigneeTel());
            redisData.setOrderStatus(OrderStatus.UNPAID.name());
            redisData.setPaymentWay(orderInfo.getPaymentWay());

            redisData.setDeliveryAddress(orderInfo.getDeliveryAddress());

            redisData.setOrderComment(orderInfo.getOrderComment());
            redisData.setOutTradeNo(System.currentTimeMillis() + "_" + userId);

            redisData.setCreateTime(new Date());
            Date date = new Date(System.currentTimeMillis() + 1000 * 60 * 15);
            redisData.setExpireTime(date);
            redisData.setProcessStatus(ProcessStatus.UNPAID.name());
            redisData.setActivityReduceAmount(new BigDecimal("0"));
            redisData.setCouponAmount(new BigDecimal("0"));
            redisData.setOriginalTotalAmount(new BigDecimal("0"));
            redisData.setRefundableTime(new Date());
            redisData.setFeightFee(new BigDecimal("0"));
            redisData.setOperateTime(new Date());
            Long orderId = orderFeignClient.submitSeckillOrder(redisData).getData();
            redisData.setId(orderId);
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER+code,Jsons.toStr(redisData));
        }
        return redisData;
    }

    /**
     * 准备生成临时订单的订单信息
     * @param skuId
     * @return
     */
    private OrderInfo prepareTempSeckillOrder(Long skuId) {

        SeckillGoods goods = cacheOpsService.getSeckillGoodsBySkuId(skuId);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTotalAmount(goods.getCostPrice());
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        orderInfo.setUserId(userId);
        orderInfo.setTradeBody(goods.getSkuName());
        orderInfo.setImgUrl(goods.getSkuDefaultImg());

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(goods.getSkuId());
        orderDetail.setSkuName(goods.getSkuName());
        orderDetail.setUserId(userId);
        orderDetail.setImgUrl(goods.getSkuDefaultImg());
        orderDetail.setOrderPrice(goods.getPrice());
        orderDetail.setSkuNum(1);
        orderDetail.setHasStock("1");

        orderDetail.setSplitTotalAmount(goods.getCostPrice());
        orderDetail.setSplitCouponAmount(goods.getPrice().subtract(goods.getCostPrice()));
        List<OrderDetail> orderDetails = Arrays.asList(orderDetail);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    /**
     * 校验秒杀码
     * @param skuId
     * @param skuIdStr
     * @return
     */
    private boolean checkSkuIdStr(Long skuId,String skuIdStr) {
        //校验skuIdStr
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String formatDate = DateUtil.formatDate(new Date());
        String code = MD5.encrypt(userId + "_" + formatDate + "_" + skuId);
        if (code.equals(skuIdStr) && redisTemplate.hasKey(RedisConst.SECKILL_CODE + code)) {
            return true;
        }
        return false;
    }

    /**
     * 生成秒杀码
     * @param userId
     * @param formatDate
     * @param skuId
     * @return
     */
    private String generateskuIdStr(Long userId, String formatDate, Long skuId) {
        String code = MD5.encrypt(userId + "_" + formatDate + "_" + skuId);
        redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_CODE + code, "1", 1, TimeUnit.DAYS);
        return code;
    }

}
