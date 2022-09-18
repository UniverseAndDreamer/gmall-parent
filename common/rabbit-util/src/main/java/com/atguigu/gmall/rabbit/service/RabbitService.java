package com.atguigu.gmall.rabbit.service;

import com.atguigu.gmall.common.constant.RedisConst;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
public class RabbitService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     *
     * @param maxNum 最大重试次数
     * @param message 消息内容
     * @param channel 信道
     * @param uniqKey redis中记录消息重试次数的唯一key
     * @throws IOException
     */
    public void retryConsumeMsg(Long maxNum, Message message, Channel channel,String uniqKey) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long aLong = redisTemplate.opsForValue().increment(uniqKey);
        if (aLong <= maxNum) {
            channel.basicNack(deliveryTag, false, true);
        } else {
            channel.basicNack(deliveryTag, false, false);
            redisTemplate.delete(uniqKey);
            log.error("订单消费消息失败，业务消息内容：{}，{}次消费失败", new String(message.getBody()), aLong);
        }

    }
}
