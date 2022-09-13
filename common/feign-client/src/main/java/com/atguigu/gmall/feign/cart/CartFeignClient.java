package com.atguigu.gmall.feign.cart;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient("service-cart")
@RequestMapping("/api/inner/rpc/cart")
public interface CartFeignClient {


    @GetMapping("/addToCart")
    Result<Object> addToCart(@RequestParam("skuId") Long skuId,
                              @RequestParam("skuNum") Integer skuNum);

    @GetMapping("/deleteChecked")
    Result deleteChecked();
}
