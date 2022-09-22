package com.atguigu.gmall.seckill.mapper;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【seckill_goods】的数据库操作Mapper
* @createDate 2022-09-19 19:39:59
* @Entity com.atguigu.gmall.seckill.domain.SeckillGoods
*/
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    List<SeckillGoods> getCurrentSeckillGoodsList(@Param("date") String date);

    void descStocking(@Param("skuId") Long skuId);
}




