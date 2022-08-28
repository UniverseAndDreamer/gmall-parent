package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.web.feign.SkuDetailFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;

    @GetMapping("/{skuId}.html")
    public String getSkuDetail(@PathVariable("skuId") Long skuId, Model model) {

        Result<SkuDetailTo> result = skuDetailFeignClient.getSkuDetail(skuId);
        if (result.isOk()) {
            SkuDetailTo skuDetail = result.getData();
            //1.查询分类,条件
            model.addAttribute("categoryView", skuDetail.getCategoryView());
            model.addAttribute("skuInfo", skuDetail.getSkuInfo());
            model.addAttribute("price", skuDetail.getPrice());
            model.addAttribute("spuSaleAttrList", skuDetail.getSpuSaleAttrList());
            model.addAttribute("valuesSkuJson", skuDetail.getValuesSkuJson());
        }
        return "item/index";
    }
}
