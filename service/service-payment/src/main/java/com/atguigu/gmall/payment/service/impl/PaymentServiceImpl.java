package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.config.AlipayConfigProperties;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;

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
        return alipayClient.pageExecute(alipayRequest).getBody();
    }

    @Override
    public boolean rsaCheckV1(HashMap<String, String> map) throws AlipayApiException {
        boolean b = AlipaySignature.rsaCheckV1(map,
                alipayConfigProperties.getAlipayPublicKey(),
                alipayConfigProperties.getCharset(),
                alipayConfigProperties.getSignType());

        return b;
    }

    /**
     * 发送支付成功的消息
     * @param map
     */
    @Override
    public void sendPaidMsg(HashMap<String, String> map) {
        rabbitTemplate.convertAndSend(
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_PAID_RK,
                Jsons.toStr(map)
        );

    }
}
