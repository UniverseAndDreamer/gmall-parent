package com.atguigu.gmall.model.to;

import com.atguigu.gmall.model.product.SkuInfo;
import lombok.Data;

@Data
public class SkuDetailTo {

    private CategoryViewTo categoryView;

    private SkuInfo skuInfo;


}
