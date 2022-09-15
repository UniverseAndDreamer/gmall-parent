package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【order_info(订单表 订单表)】的数据库操作Service
* @createDate 2022-09-13 18:40:59
*/
public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrder(OrderSubmitVo vo, String tradeNo);

    void updateOrderStatus(Long orderId, Long userId, ProcessStatus closed, List<ProcessStatus> expected);
}
