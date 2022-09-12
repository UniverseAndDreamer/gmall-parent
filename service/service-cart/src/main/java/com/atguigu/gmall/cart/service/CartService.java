package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;

import java.util.List;

public interface CartService {

    SkuInfo addToCart(Long skuId, Integer skuNum);

    List<CartInfo> cartList(String cartKey);

    void checkCart(Long skuId, Integer checkState);

    void deleteCart(Long skuId);

    void deleteChecked();

    void mergeTempAndUserCart();

    String determineCartKey();
}
