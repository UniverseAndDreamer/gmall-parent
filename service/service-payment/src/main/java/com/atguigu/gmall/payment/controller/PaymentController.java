package com.atguigu.gmall.payment.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.config.AlipayConfigProperties;
import com.atguigu.gmall.payment.service.PaymentService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

//api/payment/alipay/submit/777622239766904832
@Controller
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @ResponseBody
    @GetMapping("/alipay/submit/{orderId}")
    public String toPaymentPage(@PathVariable("orderId") Long orderId) throws AlipayApiException {

        String content = paymentService.getContent(orderId);
        return content;
    }




    @ResponseBody
    @RequestMapping("/success/notify")
    public String aliPaySuccessNotify() {

        return "success";
    }

}
