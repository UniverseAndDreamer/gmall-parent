package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
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

    /**
     * 添加商品到购物车
     *
     * @param skuId
     * @param skuNum
     * @param model
     * @param userId
     * @return
     */
    //    http://cart.gmall.com/addCart.html?skuId=49&skuNum=1&sourceType=query
    @GetMapping("/addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("skuNum") Integer skuNum,
                          Model model,
                          @RequestHeader(value = RedisConst.USERID_HEADER, required = false) String userId,
                          @RequestHeader(value = RedisConst.USERTEMPID_HEADER, required = false) String userTempId) {
        System.out.println("userId = " + userId);
        System.out.println("userTempId = " + userTempId);
        Result<SkuInfo> skuInfoResult = cartFeignClient.addToCart(skuId, skuNum);
        model.addAttribute("skuInfo", skuInfoResult.getData());
        model.addAttribute("skuNum", skuNum);
        return "cart/addCart";
    }

    @GetMapping("/cart.html")
    public String cart() {

        return "/cart/index";
    }

}
