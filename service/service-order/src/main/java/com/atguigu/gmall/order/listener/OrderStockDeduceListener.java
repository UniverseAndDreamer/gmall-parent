package com.atguigu.gmall.order.listener;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.rabbit.WareDeduceStatusMsg;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class OrderStockDeduceListener {
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private OrderInfoService orderInfoService;
    @RabbitListener(bindings = @QueueBinding(
                value = @Queue(
                        value = MqConst.WARE_ORDER_QUEUE,
                        durable = "true",
                        exclusive = "false",
                        autoDelete = "false"),
                exchange = @Exchange(
                        name = MqConst.WARE_ORDER_EXCHANGE),
                key = MqConst.WARE_ORDER_RK
        ))
    public void stockDeduceListener(Message message, Channel channel) throws IOException {
        //处理监听结果
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        WareDeduceStatusMsg wareDeduceStatusMsg = Jsons.toObj(message, WareDeduceStatusMsg.class);
        Long orderId = wareDeduceStatusMsg.getOrderId();
        try {
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            String status = wareDeduceStatusMsg.getStatus();
            ProcessStatus processStatus = null;
            switch (status) {
                case "DEDUCTED":
                    processStatus = ProcessStatus.WAITING_DELEVER;break;
                case "OUT_OF_STOCK":
                    processStatus = ProcessStatus.STOCK_OVER_EXCEPTIOPN;
                    break;
                default:
                    processStatus = ProcessStatus.PAID;
            }
            //扣减库存之后，修改订单状态
            orderInfoService.updateOrderStatus(
                    orderId, orderInfo.getUserId(),
                    processStatus,
                    Arrays.asList(ProcessStatus.PAID));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            String uniqueKey = RedisConst.MQ_RETRY + "stock:order:deduce:" + orderId;
            rabbitService.retryConsumeMsg(
                    10L,
                    message,
                    channel,
                    uniqueKey);
        }


    }

}
