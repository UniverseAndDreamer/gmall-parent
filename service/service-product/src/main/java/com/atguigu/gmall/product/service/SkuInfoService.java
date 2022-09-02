package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【sku_info(库存单元表)】的数据库操作Service
* @createDate 2022-08-23 20:17:39
*/
public interface SkuInfoService extends IService<SkuInfo> {

    void saveSkuInfo(SkuInfo skuInfo);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuDetailTo getSkuDetail(Long skuId);


    BigDecimal select1010Price(Long skuId);

    List<Long> getAllSkuIds();

    void updateByIdAndCache(SkuInfo skuInfo);
}
