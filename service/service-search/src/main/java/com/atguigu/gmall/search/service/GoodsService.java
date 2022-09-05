package com.atguigu.gmall.search.service;

import com.atguigu.gmall.model.list.Goods;

public interface GoodsService {


    void save(Goods goods);

    void deleteById(Long skuId);
}
