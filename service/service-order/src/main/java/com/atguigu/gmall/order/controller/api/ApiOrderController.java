package com.atguigu.gmall.order.controller.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inner/rpc/order")
public class ApiOrderController {

    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private OrderInfoService orderInfoService;

    @GetMapping("/getOrderConfirmData")
    public Result<OrderConfirmDataVo> getOrderConfirmData() {
        OrderConfirmDataVo vo = orderBizService.getOrderConfirmData();
        return Result.ok(vo);
    }

    @GetMapping("/getOrderInfo")
    public Result<OrderInfo> getOrderInfo(@RequestParam("orderId") Long orderId) {
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        return Result.ok(orderInfo);
    }

    @PostMapping("/seckillOrder/submit")
    public Result<Long> submitSeckillOrder(@RequestBody OrderInfo orderInfo) {
        Long orderId = orderInfoService.submitSeckillOrder(orderInfo);
        return Result.ok(orderId);
    }

}
