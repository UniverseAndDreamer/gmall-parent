package com.atguigu.gmall.rabbit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@EnableRabbit
@Configuration
public class MqConfig {


    @Bean
    public RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer,
                                         ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);
        //感知消息是否发送至交换机，无论成功失败都会有返回
        template.setConfirmCallback((
                correlationData,
                ack,
                cause) -> {
            if (!ack) {
                log.error("消息投送失败，保存至数据库,消息：{}", correlationData);
            }

        });
        //感知消息是否成功发送至队列：成功不做回应，只有发送失败才有回应
        template.setReturnCallback((
                message,
                replyCode,
                replyText,
                exchange,
                routingKey)->{
            //走到这里说明消息发送失败
            log.error("消息发送失败，应保存至数据库   {}",message);
        });
        //设置消息发送失败后的重试器，默认为重试三次
        template.setRetryTemplate(new RetryTemplate());
        return template;
    }

}
