package com.atguigu.gmall.rabbit.constant;

public class MqConst {
    //订单延迟队列
    public static final String ORDER_DELAY_QUEUE = "order-delay-queue";
    //订单交换机
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";
    //消息创建时的路由key
    public static final String ORDER_CREATED_RK = "order.created";
    //订单死信队列
    public static final String ORDER_DEAD_QUEUE = "order-dead-queue";
    public static final String ORDER_DEAD_RK = "order.dead";
}
