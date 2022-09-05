package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.model.list.SearchParamVo;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    @Autowired
    private SearchFeignClient searchFeignClient;

    /**
     * 初步分析请求参数：
     *      searchParam: 至少应该有：c1ID,c2ID,c3ID,keyword
     * @param searchParamVo
     * @param model
     * @param request
     * @return
     */

    @GetMapping("/list.html")
    public String search(SearchParamVo searchParamVo, Model model, HttpServletRequest request) {


//        Result<SearchResponseVo> result = searchFeignClient.search(searchParamVo);


        return "list/index.html";
    }

}
