package com.atguigu.gmall.order.controller.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inner/rpc/order")
public class ApiOrderController {

    @Autowired
    private OrderBizService orderBizService;

    @GetMapping("/getOrderConfirmData")
    public Result<OrderConfirmDataVo> getOrderConfirmData() {
        OrderConfirmDataVo vo = orderBizService.getOrderConfirmData();
        return Result.ok(vo);
    }
}
