package com.atguigu.gmall.cart.controller.api;


import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inner/rpc/cart")
public class ApiCartController {

    @Autowired
    private CartService cartService;


    /**
     * 获取选中的商品
     * @return
     */
    @GetMapping("/getCheckedSku")
    public Result<List<CartInfo>> getCheckedSku() {
        List<CartInfo> cartInfos = cartService.getCheckedSku();
        return Result.ok(cartInfos);
    }

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

    /**
     * 删除选中的商品
     * @return
     */
    @GetMapping("/deleteChecked")
    public Result deleteChecked() {
        cartService.deleteChecked();
        return null;
    }









}
