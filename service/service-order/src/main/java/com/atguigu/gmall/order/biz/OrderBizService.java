package com.atguigu.gmall.order.biz;

import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;

public interface OrderBizService {


    OrderConfirmDataVo getOrderConfirmData();

    Long submitOrder(String tradeNo, OrderSubmitVo vo);

    void closeOrder(Long orderId, Long userId);
}
