package com.atguigu.gmall.order.service.impl;
import java.math.BigDecimal;
import java.util.Date;

import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.order.service.PaymentInfoService;
import com.atguigu.gmall.order.mapper.PaymentInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @author 美貌与智慧并存
* @description 针对表【payment_info(支付信息表)】的数据库操作Service实现
* @createDate 2022-09-13 18:40:59
*/
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
    implements PaymentInfoService{
    @Autowired
    private OrderInfoService orderInfoService;

    @Override
    public PaymentInfo savePaymentInfo(Map<String, String> map) {
        PaymentInfo paymentInfo = new PaymentInfo();
        String outTradeNo = map.get("out_trade_no");
        String[] s = outTradeNo.split("_");
        long userId = Long.parseLong(s[1]);
        paymentInfo.setOutTradeNo(outTradeNo);
        //在新增前先进行查询，若已存在，则不进行新增
        LambdaQueryWrapper<PaymentInfo> queryWrapperForPaymentInfo = new LambdaQueryWrapper<>();
        queryWrapperForPaymentInfo.eq(PaymentInfo::getTradeNo, outTradeNo)
                .eq(PaymentInfo::getUserId, userId);
        PaymentInfo one = this.getOne(queryWrapperForPaymentInfo);
        if (one != null) {
            return null;
        }

        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getOutTradeNo, outTradeNo)
                .eq(OrderInfo::getUserId, userId);
        OrderInfo orderInfo = orderInfoService.getOne(queryWrapper);

        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType("Alipay");
        paymentInfo.setTradeNo(map.get("trade_no"));
        paymentInfo.setTotalAmount(new BigDecimal(map.get("total_amount")));

        paymentInfo.setSubject(map.get("subject"));
        paymentInfo.setCreateTime(new Date());
        Date callbackTime = DateUtil.parseDate(map.get("notify_time"), "yyyy-MM-dd HH:mm:ss");
        paymentInfo.setCallbackTime(callbackTime);
        paymentInfo.setCallbackContent(Jsons.toStr(map));

        paymentInfo.setUserId(userId);
        save(paymentInfo);
        return paymentInfo;
    }
}




