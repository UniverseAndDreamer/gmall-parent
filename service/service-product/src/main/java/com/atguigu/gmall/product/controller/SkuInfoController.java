package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/list/{pn}/{ps}")
    public Result list(@PathVariable("pn") Long pn,
                       @PathVariable("ps") Long ps) {

        Page<SkuInfo> skuInfoPage = new Page<>(pn, ps);
        Page<SkuInfo> page = skuInfoService.page(skuInfoPage);
        return Result.ok(page);
    }

    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuInfoService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * 商品上架：把商品数据存入ES中
     * @param skuId
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        skuInfoService.onSale(skuId);
        return Result.ok();
    }

    /**
     * 商品下架：删除ES中指定的商品
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        skuInfoService.cancelSale(skuId);
        return Result.ok();
    }

    /**
     * 延迟双删
     * @param skuInfo
     * @return
     */
    public Result update(SkuInfo skuInfo) {
        skuInfoService.updateByIdAndCache(skuInfo);
        return Result.ok();
    }



}
