package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.product.SkuInfo;

public interface CartService {

    SkuInfo addToCart(Long skuId, Integer skuNum);
}
