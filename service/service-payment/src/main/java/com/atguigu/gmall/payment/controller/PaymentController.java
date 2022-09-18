package com.atguigu.gmall.payment.controller;


import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@Controller
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @ResponseBody
    @GetMapping("/alipay/submit/{orderId}")
    public String toPaymentPage(@PathVariable("orderId") Long orderId) throws AlipayApiException {

        String content = paymentService.getContent(orderId);
        return content;
    }

    @GetMapping("/alipay/successPage")
    public String returnSuccessPage(@RequestParam HashMap<String, String> map) {

        return "redirect:http://api.gmall.com/success";
    }


    @ResponseBody
    @RequestMapping("/success/notify")
    public String aliPaySuccessNotify(@RequestParam HashMap<String, String> map) throws AlipayApiException {
        System.out.println("map = " + Jsons.toStr(map));
        boolean b = paymentService.rsaCheckV1(map);
        if (b) {
            //代表用户支付成功，应该修改订单状态
            log.info("异步通知抵达。支付成功，验签通过。数据：{}", Jsons.toStr(map));
            paymentService.sendPaidMsg(map);
            return "success";
        }
        return null;
    }

}
