package com.atguigu.gmall.model.to;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuDetailTo {
    //商品详情：商品分类
    private CategoryViewTo categoryView;
    //商品详情：skuInfo信息
    private SkuInfo skuInfo;
    //商品详情：商品实时价格
    private BigDecimal price;
    //商品详情：SpuSaleAttrList商品销售属性集合
    private List<SpuSaleAttr> spuSaleAttrList;
    //商品详情：json字符串，{"115|117":"44","114|117":"45"}
    private String valuesSkuJson;



}
