package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsCacheOpsService {

    void upSeckillGoods(List<SeckillGoods> list);

    List<SeckillGoods> getSeckillGoodsFromLocal();

    void clearCache();

    SeckillGoods getSeckillGoodsBySkuId(Long skuId);
}
