package com.atguigu.gmall.product.controller.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Api("商品详情")
@RestController
@RequestMapping("/api/inner/rpc/product")
public class ApiSkuDetailController {
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private BaseCategory3Service baseCategory3Service;
    @Autowired
    private SkuImageService skuImageService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

//    @GetMapping("/skuDetail/{skuId}")
//    public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId) {
//        SkuDetailTo skuDetailTo = skuInfoService.getSkuDetail(skuId);
//        return Result.ok(skuDetailTo);
//    }

    /**
     * 获取商品详情中的skuInfo
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skuDetail/info/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        return Result.ok(skuInfo);
    }

    /**
     * 获取实时价格
     * @param skuId
     * @return
     */

    @GetMapping("/skuDetail/price/{skuId}")
    public Result<BigDecimal> get1010Price(@PathVariable("skuId") Long skuId) {
        BigDecimal price = skuInfoService.select1010Price(skuId);
        return Result.ok(price);
    }

    /**
     * 获取categoryView
     * @param category3Id
     * @return
     */
    @GetMapping("/skuDetail/category/{category3Id}")
    public Result<CategoryViewTo> getCategoryView(@PathVariable("category3Id") Long category3Id) {
        CategoryViewTo categoryViewTo = baseCategory3Service.getCategoryView(category3Id);
        return Result.ok(categoryViewTo);
    }

    /**
     * 获取skuImageList
     */
    @GetMapping("/skuDetail/skuImageList/{skuId}")
    public Result<List<SkuImage>> getSkuImageList(@PathVariable("skuId") Long skuId) {
        LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuImage::getSkuId, skuId);
        return Result.ok(skuImageService.list(queryWrapper));
    }

    /**
     * 5、查询spuSaleAttrList:查询条件为skuId
     */
    @GetMapping("/skuDetail/spuSaleAttrList/{spuId}/{skuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId,
                                                        @PathVariable("skuId") Long skuId) {
        List<SpuSaleAttr> spuSaleAttrListBySpuIdAndSkuId = spuSaleAttrService.getSpuSaleAttrListBySpuIdAndSkuId(spuId, skuId);
        return Result.ok(spuSaleAttrListBySpuIdAndSkuId);
    }

    /**
     * 6.查询valueJson
     */

    @GetMapping("/skuDetail/valueJson/{spuId}")
    public Result<String> getValueJson(@PathVariable("spuId") Long spuId) {
        String str = spuSaleAttrService.getSkuValueJsonStr(spuId);
        return Result.ok(str);
    }




}
