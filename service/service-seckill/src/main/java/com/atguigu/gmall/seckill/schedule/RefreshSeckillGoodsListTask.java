package com.atguigu.gmall.seckill.schedule;

import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
@Slf4j
@Component
public class RefreshSeckillGoodsListTask {
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private SeckillGoodsCacheOpsService cacheOpsService;

    /**
     * 定时任务：定期更新缓存中秒杀的商品列表
     */

    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshSeckillGoodsList() {
        System.out.println("开始更新秒杀商品列表。。。。");
        String date = DateUtil.formatDate(new Date());
        List<SeckillGoods> list =
                seckillGoodsMapper.getCurrentSeckillGoodsList(date);
        //二级缓存机制
        cacheOpsService.upSeckillGoods(list);
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearCache() {
        cacheOpsService.clearCache();
    }




}
