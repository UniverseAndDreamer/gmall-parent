package com.atguigu.gmall.seckill.biz;

import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;

public interface SeckillBizService {
    String getSeckillSkuIdStr(Long skuId);

    ResultCodeEnum seckillOrder(Long skuId, String skuIdStr);

    ResultCodeEnum checkOrderStatus(Long skuId);

    SeckillConfirmVo getSeckillConfirmVo(Long skuId);

    Long submitSeckillOrder(OrderInfo orderInfo);
}
