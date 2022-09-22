package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity/seckill/auth")
public class SeckillRestController {

    @Autowired
    private SeckillBizService seckillBizService;

//    /api/activity/seckill/auth/submitOrder
    @PostMapping("/submitOrder")
    public Result<?> submitOrder(@RequestBody OrderInfo orderInfo) {
        Long orderId = seckillBizService.submitSeckillOrder(orderInfo);
        return Result.ok(orderId.toString());
    }


    /**
     * 获取SeckillSkuIdStr
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId) {
        String skuIdStr = seckillBizService.getSeckillSkuIdStr(skuId);
        return Result.ok(skuIdStr);
    }


    /**
     * 订单
     * @param skuIdStr
     * @param skuId
     * @return
     */
    @PostMapping("/seckillOrder/{skuId}")
    public Result seckillOrder(@RequestParam("skuIdStr") String skuIdStr,
                               @PathVariable("skuId") Long skuId) {

        ResultCodeEnum resultCodeEnum = seckillBizService.seckillOrder(skuId, skuIdStr);
        return Result.build("", resultCodeEnum);
    }


    /**
     * 检验订单
     * @param skuId
     * @return
     */
    @GetMapping("/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId) {
        System.out.println("skuId = " + skuId);

        ResultCodeEnum resultCodeEnum = seckillBizService.checkOrderStatus(skuId);
        return Result.build("", resultCodeEnum);
    }
}
