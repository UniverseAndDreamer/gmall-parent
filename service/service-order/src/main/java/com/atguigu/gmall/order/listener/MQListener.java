package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.to.to.OrderMsg;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MQListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "haha")
    public void getMessage(Message message, Channel channel) throws IOException {

        String s = new String(message.getBody());
//        OrderMsg orderMsg = Jsons.toObj(message, OrderMsg.class);
        System.out.println("监听到消息，开始打印消息：s = " + s);

        Long increment = redisTemplate.opsForValue().increment(s);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (increment <= 10) {
                channel.basicNack(deliveryTag, false, true);
            } else {
                channel.basicNack(deliveryTag, false, false);
                redisTemplate.delete(s);
            }
        }
    }

}
