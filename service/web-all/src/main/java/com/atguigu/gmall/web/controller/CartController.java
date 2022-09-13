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
     * @return
     */
    //    http://cart.gmall.com/addCart.html?skuId=49&skuNum=1&sourceType=query
    @GetMapping("/addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("skuNum") Integer skuNum,
                          Model model) {
        Result<Object> skuInfoResult = cartFeignClient.addToCart(skuId, skuNum);
        if (Result.ok().isOk()) {
            model.addAttribute("skuInfo", skuInfoResult.getData());
            model.addAttribute("skuNum", skuNum);
            return "cart/addCart";
        } else {
            model.addAttribute("msg", skuInfoResult.getData());
            return "cart/error";
        }

    }

    /**
     * 跳转至购物车首页
     * @return
     */
    @GetMapping("/cart.html")
    public String cart() {

        return "/cart/index";
    }

    //    http://cart.gmall.com/cart.html
    @GetMapping("/cart/deleteChecked")
    public String deleteChecked() {
        cartFeignClient.deleteChecked();
        return "redirect:http://cart.gmall.com/cart.html";
    }



}
