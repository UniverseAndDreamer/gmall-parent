package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/order/auth")
public class OrderRestController {


    @Autowired
    private OrderBizService orderBizService;

    /**
     * 提交订单信息
     * @param tradeNo
     * @param vo
     * @return
     */
    @PostMapping("submitOrder")
    public Result submitOrder(@RequestParam("tradeNo") String tradeNo,
                              @RequestBody OrderSubmitVo vo) {
        Long orderId = orderBizService.submitOrder(tradeNo, vo);
        return Result.ok(orderId.toString());
    }
}
