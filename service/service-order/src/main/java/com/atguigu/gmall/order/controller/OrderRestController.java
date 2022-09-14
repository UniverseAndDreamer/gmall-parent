package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/order/auth")
public class OrderRestController {

//    api/order/auth/submitOrder?tradeNo=1663083841159_2

    @Autowired
    private OrderBizService orderBizService;

    @PostMapping("submitOrder")
    public Result submitOrder(@RequestParam("tradeNo") String tradeNo,
                              @RequestBody OrderSubmitVo vo) {
        Long orderId = orderBizService.submitOrder(tradeNo, vo);
        return Result.ok(orderId);
    }
}
