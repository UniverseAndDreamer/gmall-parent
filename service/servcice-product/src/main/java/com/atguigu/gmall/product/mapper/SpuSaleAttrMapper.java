package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.SkuValueJsonTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Mapper
* @createDate 2022-08-23 20:17:39
* @Entity com.atguigu.gmall.product.domain.SpuSaleAttr
*/
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(@Param("spuId") Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuIdAndSkuId(@Param("spuId") Long spuId, @Param("skuId") Long skuId);

    List<SkuValueJsonTo> getSkuValueJsonList(Long spuId);
}




