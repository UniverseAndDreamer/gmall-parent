package com.atguigu.gmall.seckill.service;


import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【seckill_goods】的数据库操作Service
* @createDate 2022-09-19 19:39:59
*/
public interface SeckillGoodsService extends IService<SeckillGoods> {

    List<SeckillGoods> getCurrentSeckillGoodsList();

    List<SeckillGoods> getCurrentSeckillGoodsListFromCache();

    SeckillGoods getSeckillGoodsBySkuId(Long skuId);

    void descStocking(Long skuId);

}
