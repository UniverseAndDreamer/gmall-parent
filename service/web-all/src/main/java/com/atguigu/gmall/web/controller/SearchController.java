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


        Result<SearchResponseVo> result = searchFeignClient.search(searchParamVo);
        SearchResponseVo data = result.getData();
        //1.以前检索页面点击传来的所有条件，原封不动返回给页面
        model.addAttribute("searchParam", data.getSearchParamVo());
        //2.品牌面包屑位置的显示
        model.addAttribute("trademarkParam",data.getTrademarkParam() );
        //3.平台属性面包屑
        model.addAttribute("propsParamList",data.getPropsParamList() );
        //4.品牌列表的展示
        model.addAttribute("trademarkList", data.getTrademarkList());
        //5.列表中所有商品的平台属性集合
        model.addAttribute("attrsList", data.getAttrsList());
        //6.拼接的url
        model.addAttribute("urlParam", data.getUrlParam());
        //7.排序规则
        model.addAttribute("orderMap", data.getOrderMap());
        //8.商品的集合
        model.addAttribute("goodsList", data.getGoodsList());
        //9.分页数据
        model.addAttribute("pageNo", data.getPageNo());
        model.addAttribute("totalPages", data.getTotalPages());

        return "list/index.html";
    }

}
