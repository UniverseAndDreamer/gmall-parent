package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

import java.util.HashMap;

public interface PaymentService {
    String getContent(Long orderId) throws AlipayApiException;

    boolean rsaCheckV1(HashMap<String, String> map) throws AlipayApiException;

    void sendPaidMsg(HashMap<String, String> map);
}
