package com.atguigu.gmall.feign.product;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("service-product")
@RequestMapping("/api/inner/rpc/product")
public interface SkuInfoFeignClient {

//    @GetMapping("/skuDetail/{skuId}")
//    Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId);


    /**
     * 获取商品详情中的skuInfo
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skuDetail/info/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 获取实时价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skuDetail/price/{skuId}")
    Result<BigDecimal> get1010Price(@PathVariable("skuId") Long skuId);

    /**
     * 获取categoryView
     *
     * @param category3Id
     * @return
     */
    @GetMapping("/skuDetail/category/{category3Id}")
    Result<CategoryViewTo> getCategoryView(@PathVariable("category3Id") Long category3Id);


    /**
     * 获取skuImageList
     */
    @GetMapping("/skuDetail/skuImageList/{skuId}")
    Result<List<SkuImage>> getSkuImageList(@PathVariable("skuId") Long skuId);

    /**
     * 5、查询spuSaleAttrList:查询条件为skuId
     */
    @GetMapping("/skuDetail/spuSaleAttrList/{spuId}/{skuId}")
    Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId,
                                                        @PathVariable("skuId") Long skuId);

    /**
     * 6.查询valueJson
     */

    @GetMapping("/skuDetail/valueJson/{spuId}")
    Result<String> getValueJson(@PathVariable("spuId") Long spuId);



}
