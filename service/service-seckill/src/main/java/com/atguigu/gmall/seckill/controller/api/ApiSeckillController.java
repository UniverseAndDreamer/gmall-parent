package com.atguigu.gmall.seckill.controller.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inner/rpc/seckill")
public class ApiSeckillController {
    @Autowired
    SeckillGoodsService seckillGoodsService;
    @Autowired
    private SeckillBizService seckillBizService;

    @GetMapping("/getSeckillGoodsList")
    public Result<List<SeckillGoods>> getSeckillGoodsList() {
        List<SeckillGoods> list = seckillGoodsService.getCurrentSeckillGoodsListFromCache();
        return Result.ok(list);
    }

    @GetMapping("/getSeckillGoodsBySkuId/{skuId}")
    public Result<SeckillGoods> getSeckillGoodsBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillGoods goods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        return Result.ok(goods);
    }

    @GetMapping("/order/confirmVo/{skuId}")
    public Result<SeckillConfirmVo> getSeckillConfirmVo(@PathVariable("skuId") Long skuId) {
        SeckillConfirmVo vo = seckillBizService.getSeckillConfirmVo(skuId);
        return Result.ok(vo);
    }
}
