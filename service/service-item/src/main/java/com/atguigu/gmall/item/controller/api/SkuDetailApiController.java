package com.atguigu.gmall.item.controller.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.to.SkuDetailTo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Api(tags = "获取商品详情")
@RestController
@RequestMapping("/api/inner/rpc/item")
public class SkuDetailApiController {

    @Autowired
    private SkuDetailService skuDetailService;

    @GetMapping("/skuDetail/{skuId}")
    public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId) {
        SkuDetailTo skuDetailTo = skuDetailService.getSkuDetail(skuId);
        return Result.ok(skuDetailTo);
    }

}
