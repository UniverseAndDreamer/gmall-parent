package com.atguigu.gmall.order.config;


import com.alibaba.cloud.nacos.utils.NacosConfigUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.rabbit.constant.MqConst;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class OrderEventMqConfiguration {
    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(
                MqConst.ORDER_EVENT_EXCHANGE,
                true,
                false,
                null
        );
    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
//        arguments.put("x-message-ttl", RedisConst.ORDER_EXPIRE_TTL * 1000);
        arguments.put("x-message-ttl", 10 * 1000);
        arguments.put("x-dead-letter-exchange", MqConst.ORDER_EVENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MqConst.ORDER_DEAD_RK);
        return new Queue(
                MqConst.ORDER_DELAY_QUEUE,
                true,
                false,
                false,
                arguments
        );
    }

    /**
     * 交换机与延迟队列进行绑定
     * @return
     */
    @Bean
    public Binding orderEventExchangeToDelayQueue() {
        return new Binding(
                MqConst.ORDER_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_CREATED_RK,
                null
        );
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue orderDeadQueue() {
        return new Queue(
                MqConst.ORDER_DEAD_QUEUE,
                true,
                false,
                false,
                null
        );
    }

    /**
     * 死信队列与交换机进行绑定
     * @return
     */
    @Bean
    public Binding orderDeadQueueBinding() {
        return new Binding(
                MqConst.ORDER_DEAD_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_DEAD_RK,
                null
        );
    }


}
