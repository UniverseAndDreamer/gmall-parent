package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.SkuValueJsonTo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service
* @createDate 2022-08-23 20:17:39
*/
public interface SpuSaleAttrService extends IService<SpuSaleAttr> {

    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuIdAndSkuId(Long spuId, Long skuId);

    List<SkuValueJsonTo> getSkuValueJsonList(Long spuId);

    String getSkuValueJsonStr(Long spuId);
}
