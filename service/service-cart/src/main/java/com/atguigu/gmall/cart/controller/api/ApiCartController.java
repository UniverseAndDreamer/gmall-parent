package com.atguigu.gmall.cart.controller.api;


import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inner/rpc/cart")
public class ApiCartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品至购物车
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/addToCart")
    public Result<SkuInfo> addToCart(@RequestParam("skuId") Long skuId,
                                     @RequestParam("skuNum") Integer skuNum) {

        SkuInfo skuInfo = cartService.addToCart(skuId,skuNum);
        return Result.ok(skuInfo);
    }









}
