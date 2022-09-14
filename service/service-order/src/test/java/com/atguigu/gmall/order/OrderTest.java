package com.atguigu.gmall.order;


import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class OrderTest {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Test
    public void testOrderInfo() {

        OrderInfo orderInfo = orderInfoMapper.selectById(205);
        System.out.println(orderInfo);
    }
    


    @Test
    public void testQueryAll(){
        List<OrderInfo> orderInfos = orderInfoMapper.selectList(null);
        for (OrderInfo orderInfo : orderInfos) {
            System.out.println(orderInfo.getId() +"<=====>"+ orderInfo.getUserId());
        }
    }
    @Test
    public void testQuery(){
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, 2L);
        List<OrderInfo> orderInfos = orderInfoMapper.selectList(queryWrapper);
        orderInfos.forEach(orderInfo -> System.out.println(orderInfo.toString()));

        System.out.println("===========================");
        LambdaQueryWrapper<OrderInfo> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, 2L);
        List<OrderInfo> orderInfos2 = orderInfoMapper.selectList(queryWrapper2);
        orderInfos2.forEach(orderInfo -> System.out.println(orderInfo.toString()));

    }


    @Test
    public void testSharding(){
        OrderInfo info = new OrderInfo();
        info.setTotalAmount(new BigDecimal("333"));
        info.setUserId(1L);
        orderInfoMapper.insert(info);


        System.out.println("1号用户订单插入完成....去 1库1表找");


        OrderInfo info2 = new OrderInfo();
        info2.setTotalAmount(new BigDecimal("111"));
        info2.setUserId(2L);
        orderInfoMapper.insert(info2);
        System.out.println("2号用户订单插入完成....去 0库2表找");

        //

    }

//    @Autowired
//    private WareFeignClient wareFeignClient;
//    @Test
//    public void testFeign(){
//        String s = wareFeignClient.hasStock(43L, 10000);
//        System.out.println(s);
//    }

}
