package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataUnit;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;

/**
* @author 美貌与智慧并存
* @description 针对表【seckill_goods】的数据库操作Service实现
* @createDate 2022-09-19 19:39:59
*/
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
    implements SeckillGoodsService{
    @Resource
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 从数据库中获取当前时间的秒杀商品列表
     * @return
     */
    @Override
    public List<SeckillGoods> getCurrentSeckillGoodsList() {
        String date = DateUtil.formatDate(new Date());
        List<SeckillGoods> list = seckillGoodsMapper.getCurrentSeckillGoodsList(date);
        return list;
    }

    @Override
    public List<SeckillGoods> getCurrentSeckillGoodsListFromCache() {

        return cacheOpsService.getSeckillGoodsFromLocal();
    }

    @Override
    public SeckillGoods getSeckillGoodsBySkuId(Long skuId) {

        return cacheOpsService.getSeckillGoodsBySkuId(skuId);
    }

    @Override
    public void descStocking(Long skuId) {
        seckillGoodsMapper.descStocking(skuId);
    }




}




