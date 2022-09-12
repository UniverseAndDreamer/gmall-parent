package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import lombok.Data;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;

    /**
     * 获取用户的订单列表
     * @return
     */
    @GetMapping("/cartList")
    public Result<List<CartInfo>> cartList() {
        String cartKey = cartService.determineCartKey();
        List<CartInfo> cartInfos = cartService.cartList(cartKey);
        //查看购物车列表时，合并临时购物车
        cartService.mergeTempAndUserCart();
        return Result.ok(cartInfos);
    }

//    http://api.gmall.com/api/cart/checkCart/50/0

    /**
     * 修改购物车中商品选取的状态
     * @param skuId
     * @param checkState
     * @return
     */
    @GetMapping("/checkCart/{skuId}/{checkState}")
    public Result checkCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("checkState") Integer checkState) {
        cartService.checkCart(skuId, checkState);
        return Result.ok();
    }

    //    http://api.gmall.com/api/cart/addToCart/50/1

    /**
     * 修改购物车中商品的数量
     * @param skuId
     * @param skuNum
     * @return
     */
    @PostMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        cartService.addToCart(skuId, skuNum);
        return Result.ok();
    }

    //    http://api.gmall.com/api/cart/deleteCart/41

    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        cartService.deleteCart(skuId);
        return Result.ok();
    }



}
