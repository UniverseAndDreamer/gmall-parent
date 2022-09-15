package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.to.to.OrderMsg;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderCloseListener {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OrderBizService orderBizService;
    /**
     * 监听死信队列中的消息
     * @param message
     * @param channel
     */
    @RabbitListener(queues = MqConst.ORDER_DEAD_QUEUE)
    public void orderClose(Message message, Channel channel) throws IOException {
        ////监听到死信队列中的消息
        OrderMsg orderMsg = Jsons.toObj(message, OrderMsg.class);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //正常回复
            log.info("监听到超时订单 {}，正在关闭。。。", orderMsg);
            orderBizService.closeOrder(orderMsg.getOrderId(), orderMsg.getUserId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("订单关闭业务失败，业务消息内容：{}，失败原因：{}", orderMsg, e);
            Long aLong = redisTemplate.opsForValue().increment(RedisConst.MQ_RETRY + "order:" + orderMsg.getOrderId());
            if (aLong <= 10) {
                channel.basicNack(deliveryTag, false, true);
            }
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
