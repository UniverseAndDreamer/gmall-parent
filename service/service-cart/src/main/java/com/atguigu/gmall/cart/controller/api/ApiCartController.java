package com.atguigu.gmall.cart.controller.api;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/inner/rpc/cart")
public class ApiCartController {

    @GetMapping("/addToCart")
    public Result addToCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("skuNum") Integer skuNum,
                          @RequestHeader(value = RedisConst.USERID_HEADER, required = false) String userId) {

        System.out.println("service-cart服务中的userId = " + userId);
        return Result.ok();
    }





}
