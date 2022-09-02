package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.product.CategoryFeignClient;
import com.atguigu.gmall.model.to.CategoryTreeTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class IndexController {
    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        Result<List<CategoryTreeTo>> result = categoryFeignClient.getCategoryTreeToList();
        if (result.isOk()) {
            model.addAttribute("list", result.getData());
        }
        return "index/index";
    }


}
