package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/product")
public class SpuInfoController {
//?category3Id=61

    /**
     * 分页和条件查询spuInfo
     */
    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private SpuSaleAttrService spuSaleAttrService;
    @Autowired
    private BaseSaleAttrService baseSaleAttrService;
    @Autowired
    private SpuImageService spuImageService;
    @Autowired
    private SpuSaleAttrValueService spuSaleAttrValueService;


    @GetMapping("/{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable("page") Long page,
                                 @PathVariable("limit") Long limit,
                                 @RequestParam("category3Id") Long category3Id) {
        Page<SpuInfo> infoPage = spuInfoService.getSpuInfoPage(page, limit, category3Id);
        return Result.ok(infoPage);
    }

    /**
     * 获取spu商品属性列表
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result getBaseSaleAttrList() {
        List<BaseSaleAttr> list = baseSaleAttrService.list();
        return Result.ok(list);
    }

    /**
     * 对spu商品信息进行新增
     * @param spuInfo
     * @return
     */

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


    @ApiOperation("根据SpuId获取spuImage")
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId) {
        LambdaQueryWrapper<SpuImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuImage::getSpuId, spuId);
        List<SpuImage> list = spuImageService.list(queryWrapper);
        return Result.ok(list);
    }

    //    spuSaleAttrList/28
    @ApiOperation("根据SpuId获取spuSaleAttr")
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> list = spuSaleAttrService.getSpuSaleAttrListBySpuId(spuId);
        return Result.ok(list);
    }

}
