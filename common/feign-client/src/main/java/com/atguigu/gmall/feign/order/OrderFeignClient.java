package com.atguigu.gmall.feign.order;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-order")
@RequestMapping("/api/inner/rpc/order")
public interface OrderFeignClient {

    @GetMapping("/getOrderConfirmData")
    Result<OrderConfirmDataVo> getOrderConfirmData();

    @GetMapping("/getOrderInfo")
    Result<OrderInfo> getOrderInfo(@RequestParam("orderId") Long orderId);

    @PostMapping("/seckillOrder/submit")
    Result<Long> submitSeckillOrder(@RequestBody OrderInfo orderInfo);
}
