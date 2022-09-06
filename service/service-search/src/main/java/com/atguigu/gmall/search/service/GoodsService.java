package com.atguigu.gmall.search.service;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParamVo;
import com.atguigu.gmall.model.list.SearchResponseVo;

public interface GoodsService {


    void save(Goods goods);

    void deleteById(Long skuId);

    SearchResponseVo search(SearchParamVo searchParam);
}
