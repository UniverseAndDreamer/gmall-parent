package com.atguigu.gmall.order.listener;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.rabbit.WareDeduceSkuInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.model.to.rabbit.WareDeduceMsg;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.PaymentInfoService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OrderPaidListener {
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderDetailService orderDetailService;


    @RabbitListener(queues = MqConst.ORDER_PAID_QUEUE)
    public void paidListener(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Map<String,String> map = Jsons.toObj(message, Map.class);
        //拿到支付宝的交易号
        String trade_no = map.get("trade_no");
        String uniqueKey = RedisConst.MQ_RETRY + "order:paid:" + trade_no;
        try {
            //保存订单信息
            PaymentInfo paymentInfo = paymentInfoService.savePaymentInfo(map);
            //修改订单状态
            orderInfoService.updateOrderStatus(
                    paymentInfo.getOrderId(),
                    paymentInfo.getUserId(),
                    ProcessStatus.PAID,
                    Arrays.asList(ProcessStatus.UNPAID, ProcessStatus.CLOSED));
            //通知库存系统扣减库存
            WareDeduceMsg msg = prepareWareDeduceMsg(paymentInfo);
            rabbitTemplate.convertAndSend(
                    MqConst.WARE_STOCK_EXCHANGE,
                    MqConst.WARE_STOCK_RK,
                    Jsons.toStr(msg));
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            rabbitService.retryConsumeMsg(
                    10l,
                    message,
                    channel,
                    uniqueKey);
        }
    }

    /**
     * 准备扣减库存时的消息内容
     * @param paymentInfo
     * @return
     */
    private WareDeduceMsg prepareWareDeduceMsg(PaymentInfo paymentInfo) {
        WareDeduceMsg wareDeduceMsg = new WareDeduceMsg();

        wareDeduceMsg.setOrderId(paymentInfo.getOrderId());

        OrderInfo orderInfo = orderInfoService.getByIdAndUserId(paymentInfo.getOrderId(), paymentInfo.getUserId());
        wareDeduceMsg.setConsignee(orderInfo.getConsignee());
        wareDeduceMsg.setConsigneeTel(orderInfo.getConsigneeTel());
        wareDeduceMsg.setOrderComment(orderInfo.getOrderComment());
        wareDeduceMsg.setOrderBody(orderInfo.getTradeBody());
        wareDeduceMsg.setDeliveryAddress(orderInfo.getDeliveryAddress());

        wareDeduceMsg.setPaymentWay("2");

        List<WareDeduceSkuInfo> details = new ArrayList<>();
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        orderDetailList.stream().forEach(orderDetail -> {
            WareDeduceSkuInfo wareDeduceSkuInfo = new WareDeduceSkuInfo();
            wareDeduceSkuInfo.setSkuId(orderDetail.getSkuId());
            wareDeduceSkuInfo.setSkuNum(orderDetail.getSkuNum());
            wareDeduceSkuInfo.setSkuName(orderDetail.getSkuName());
            details.add(wareDeduceSkuInfo);
        });
        wareDeduceMsg.setDetails(details);

        return wareDeduceMsg;
    }

}
