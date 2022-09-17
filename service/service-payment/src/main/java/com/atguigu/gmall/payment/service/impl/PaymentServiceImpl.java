package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.config.AlipayConfigProperties;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AlipayConfigProperties alipayConfigProperties;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    public String getContent(Long orderId) throws AlipayApiException {
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(alipayConfigProperties.getReturnUrl());
        alipayRequest.setNotifyUrl(alipayConfigProperties.getNotifyUrl());

        //通过远程调用来获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId).getData();
        //设置请求参数
        HashMap<String, String> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("total_amount", orderInfo.getTotalAmount().toString());
        map.put("subject", "尚品汇订单："+orderInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizContent(Jsons.toStr(map));
        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return result;
    }
}
