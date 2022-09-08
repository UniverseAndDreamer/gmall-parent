package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;

    //    http://cart.gmall.com/addCart.html?skuId=49&skuNum=1&sourceType=query
    @GetMapping("/addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("skuNum") Integer skuNum,
                          Model model,
                          @RequestHeader(value = RedisConst.USERID_HEADER,required = false) String userId) {

        //TODO 把商品添加到购物车
        Result result = cartFeignClient.addToCart(skuId, skuNum);
//        Object data = result.getData();
        System.out.println("WebAll服务中的userId = " + userId);
        return "cart/addCart";
    }
}
