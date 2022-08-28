package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;

//    @Override
//    public SkuDetailTo getSkuDetail(Long skuId) {
//        return skuDetailFeignClient.getSkuDetail(skuId).getData();
//    }

    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {



        SkuDetailTo skuDetailTo = new SkuDetailTo();

        SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
        //1.设置图片列表
        List<SkuImage> skuImageList = skuDetailFeignClient.getSkuImageList(skuId).getData();
        skuInfo.setSkuImageList(skuImageList);
        //2.设置基本信息
        skuDetailTo.setSkuInfo(skuInfo);
        //3.设置实时价格
        BigDecimal price = skuDetailFeignClient.get1010Price(skuId).getData();
        skuDetailTo.setPrice(price);

        //4.设置分类
        CategoryViewTo categoryViewTo = skuDetailFeignClient.getCategoryView(skuInfo.getCategory3Id()).getData();
        skuDetailTo.setCategoryView(categoryViewTo);
        //5.设置sku属性
        List<SpuSaleAttr> spuSaleAttrList = skuDetailFeignClient.getSpuSaleAttrList(skuInfo.getSpuId(), skuId).getData();
        skuDetailTo.setSpuSaleAttrList(spuSaleAttrList);
        //6.设置skuValueJson
        String str = skuDetailFeignClient.getValueJson(skuInfo.getSpuId()).getData();
        skuDetailTo.setValuesSkuJson(str);

        return skuDetailTo;
    }
}
