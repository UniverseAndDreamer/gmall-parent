package com.atguigu.gmall.order.service.impl;
import java.math.BigDecimal;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.to.to.OrderMsg;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.atguigu.gmall.model.activity.CouponInfo;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author 美貌与智慧并存
* @description 针对表【order_info(订单表 订单表)】的数据库操作Service实现
* @createDate 2022-09-13 18:40:59
*/
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
    implements OrderInfoService{
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Transactional
    @Override
    public Long saveOrder(OrderSubmitVo vo, String tradeNo) {
        //1.保存订单信息
        OrderInfo orderInfo = prepareOrderInfo(vo, tradeNo);
        this.save(orderInfo);
        //2.保存订单详情
        List<OrderDetail> orderDetails = prepareOrderDetailList(vo, orderInfo);

        orderDetailService.saveBatch(orderDetails);
        //3.向rabbitmq中发送消息，45min后检查订单状态，
        //  若是FINISHED或者UNPAID,则对订单进行关闭处理
        OrderMsg orderMsg = new OrderMsg(AuthUtils.getCurrentAuthInfo().getUserId(), orderInfo.getId());
        //向消息队列中发送订单创建成功的消息
        rabbitTemplate.convertAndSend(
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_CREATED_RK,
                Jsons.toStr(orderMsg));
        return orderInfo.getId();
    }

    @Override
    public void updateOrderStatus(Long orderId, Long userId, ProcessStatus closed, List<ProcessStatus> expected) {
        //更新值
        String orderStatus = closed.getOrderStatus().name();
        String processStatus = closed.name();
        //期望原值
        List<String> statusList = expected.stream().map(status -> status.name()).collect(Collectors.toList());


        orderInfoMapper.updateOrderStatus(orderId, userId, orderStatus, processStatus,statusList);
    }

    //准备OrderDetails
    private List<OrderDetail> prepareOrderDetailList(OrderSubmitVo vo, OrderInfo orderInfo) {

        List<OrderDetail> detailList = vo.getOrderDetailList().stream().map(cartInfoVo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderInfo.getId());
            orderDetail.setSkuId(cartInfoVo.getSkuId());
            orderDetail.setSkuName(cartInfoVo.getSkuName());
            orderDetail.setImgUrl(cartInfoVo.getImgUrl());
            orderDetail.setOrderPrice(cartInfoVo.getOrderPrice());
            orderDetail.setSkuNum(cartInfoVo.getSkuNum());
            orderDetail.setHasStock(cartInfoVo.getHasStock());
            orderDetail.setCreateTime(new Date());
            //实际支付金额
            orderDetail.setSplitTotalAmount(cartInfoVo.getOrderPrice().multiply(new BigDecimal(cartInfoVo.getSkuNum() + "")));
            //促销分摊金额
            orderDetail.setSplitActivityAmount(new BigDecimal("0"));
            //优惠券分摊金额
            orderDetail.setSplitCouponAmount(new BigDecimal("0"));
            //设置userId
            orderDetail.setUserId(AuthUtils.getCurrentAuthInfo().getUserId());

            return orderDetail;
        }).collect(Collectors.toList());

        return detailList;
    }
    //准备orderInfo
    private OrderInfo prepareOrderInfo(OrderSubmitVo vo, String tradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setConsignee(vo.getConsignee());
        orderInfo.setConsigneeTel(vo.getConsigneeTel());
        BigDecimal totalAmount = vo.getOrderDetailList().stream()
                .map(cartInfoVo -> cartInfoVo.getOrderPrice().multiply(new BigDecimal(cartInfoVo.getSkuNum())))
                .reduce(BigDecimal::add).get();
        orderInfo.setTotalAmount(totalAmount);
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //用户Id
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        orderInfo.setUserId(userId);
        //支付方式
        orderInfo.setPaymentWay(vo.getPaymentWay());
        //送货地址
        orderInfo.setDeliveryAddress(vo.getDeliveryAddress());
        //订单备注
        orderInfo.setOrderComment(vo.getOrderComment());
        //订单追踪号
        orderInfo.setOutTradeNo(tradeNo);
        //订单体
        orderInfo.setTradeBody(vo.getOrderDetailList().get(0).getSkuName());
        orderInfo.setCreateTime(new Date());
        //订单过期时间
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + RedisConst.ORDER_EXPIRE_TTL * 1000));
        //订单的进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //物流单编号，发货后才有
        orderInfo.setTrackingNo("");
        //父订单Id，拆单后才有
        orderInfo.setParentOrderId(0L);
        //订单图片
        orderInfo.setImgUrl(vo.getOrderDetailList().get(0).getImgUrl());
        //当前单被优惠活动减掉的金额
        orderInfo.setActivityReduceAmount(new BigDecimal("0"));
        //当前单被优惠券减掉的金额
        orderInfo.setCouponAmount(new BigDecimal("0"));
        //订单原始总额
        orderInfo.setOriginalTotalAmount(totalAmount);
        //设置可退款日期（签收后7天）
        orderInfo.setRefundableTime(new Date(System.currentTimeMillis() + RedisConst.ORDER_REFUND_TTL * 1000));
        //运费
        orderInfo.setFeightFee(new BigDecimal("0"));

        orderInfo.setOperateTime(new Date());

        return orderInfo;
    }

}




