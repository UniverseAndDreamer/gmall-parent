package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/pay.html")
    public String toPaymentPage(@RequestParam("orderId") Long orderId,
                                Model model) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId).getData();
        model.addAttribute("orderInfo", orderInfo);
        return "/payment/pay";
    }

}