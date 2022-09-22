package com.atguigu.gmall.seckill.listener;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.rabbit.SeckillTempOrderMsg;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class SeckillWaitingListener {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = MqConst.SECKILL_ORDERWAIT_QUEUE,
                    durable = "true",
                    exclusive = "false",
                    autoDelete = "false"),
            exchange = @Exchange(name = MqConst.SECKILL_EVENT_EXCHANGE, type = "topic"),
            key = MqConst.SECKILL_ORDERWAIT_RK
    ))
    public void seckillWaiting(Message message, Channel channel) throws IOException {
        log.info("监听到秒杀消息。。。");
        long tag = message.getMessageProperties().getDeliveryTag();
        SeckillTempOrderMsg orderMsg = Jsons.toObj(message, SeckillTempOrderMsg.class);
        Long skuId = orderMsg.getSkuId();
        String skuCode = orderMsg.getSkuCode();
        try {
            //数据库中真正扣减库存
            seckillGoodsService.descStocking(skuId);
            //扣减成功,向订单服务发起消息，创建真正的订单
            rabbitTemplate.convertAndSend(
                    MqConst.ORDER_EVENT_EXCHANGE,
                    MqConst.ORDER_SECKILLOK_RK,
                    Jsons.toStr(orderMsg));
            //redis修改标志位
            String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + skuCode);
            OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);
            orderInfo.setOperateTime(new Date());
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER + skuCode, Jsons.toStr(orderInfo));
            //代表库存扣减过了
            channel.basicAck(tag, false);
        } catch (DataIntegrityViolationException e) {
            //说明扣减库存失败，库存中无商品，将redis中的商品库存置为“x”
            System.err.println(e);
            redisTemplate.opsForValue().set(RedisConst.CACHE_SECKILL_GOODS_STOCK + skuId, "x");
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            //说明出现业务失败，进行重试
            log.error("扣库存业务出现异常：{}", e);
            rabbitService.retryConsumeMsg(10L, message, channel, RedisConst.MQ_RETRY + skuCode);
        }

    }



}
