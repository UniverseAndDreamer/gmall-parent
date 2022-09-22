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
    //死信路由key
    public static final String ORDER_DEAD_RK = "order.dead";
    //支付成功单队列
    public static final String ORDER_PAID_QUEUE = "order-paid-queue";
    public static final String ORDER_PAID_RK = "order.paid";

    public static final String WARE_STOCK_EXCHANGE = "exchange.direct.ware.stock";

    public static final String WARE_STOCK_RK = "ware.stock";
    //库存扣减结果队列
    public static final String WARE_ORDER_QUEUE = "queue.ware.order";
    //库存扣减交换机
    public static final String WARE_ORDER_EXCHANGE = "exchange.direct.ware.order";
    //库存扣减路由键
    public static final String WARE_ORDER_RK = "ware.order";
    public static final String SECKILL_EVENT_EXCHANGE = "seckill-event-exchange";
    public static final String SECKILL_ORDERWAIT_RK = "seckill.order.wait";
    public static final String SECKILL_ORDERWAIT_QUEUE = "seckill-orderwait-queue";


    public static final String ORDER_SECKILLOK_RK = "order.seckill.created";
    public static final String ORDER_SECKILLOK_QUEUE = "order-seckill-create-queue";
}
