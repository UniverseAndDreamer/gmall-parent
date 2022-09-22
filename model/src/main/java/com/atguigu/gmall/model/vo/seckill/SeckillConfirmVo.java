package com.atguigu.gmall.model.vo.seckill;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SeckillConfirmVo {

    private OrderInfo tempOrder;

    private Integer totalNum;

    private BigDecimal totalAmount;

    private List<UserAddress> userAddressList;

}
