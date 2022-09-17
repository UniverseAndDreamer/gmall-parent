package com.atguigu.gmall.order.config;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.rabbit.constant.MqConst;
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
        arguments.put("x-message-ttl", RedisConst.ORDER_EXPIRE_TTL * 1000);
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

    /**
     * springBoot 整合rabbitmq
     *  1.导入相关jar包
     *  2.在application.yaml文件中对rabbitmq进行配置
     *  3.配置自己的rabbitTemplate
     *      2.1创建一个配置类
     *      2.2在配置类中配置交换机，队列，绑定机制
     *  4.发送端消息端发送消息
     *  5.消费端创建监听器，监听消息队列
     *      4.1创建一个监听器的配置类
     *      4.2创建一个@RabbitListener注解的监听器
     *      4.3在监听器中配置监听消息的方式，以及确认消息等
     */


}
