package com.atguigu.gmall.feign.seckill;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-seckill")
@RequestMapping("/api/inner/rpc/seckill")
public interface SeckillFeignClient {

    @GetMapping("/getSeckillGoodsList")
    Result<List<SeckillGoods>> getSeckillGoodsList();

    @GetMapping("/getSeckillGoodsBySkuId/{skuId}")
    Result<SeckillGoods> getSeckillGoodsBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("/order/confirmVo/{skuId}")
    Result<SeckillConfirmVo> getSeckillConfirmVo(@PathVariable("skuId") Long skuId);

}
