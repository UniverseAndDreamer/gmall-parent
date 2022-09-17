package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

public interface PaymentService {
    String getContent(Long orderId) throws AlipayApiException;
}
