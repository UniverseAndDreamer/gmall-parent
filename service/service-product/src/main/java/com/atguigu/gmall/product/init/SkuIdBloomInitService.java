package com.atguigu.gmall.product.init;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;


@Service
@Slf4j
public class SkuIdBloomInitService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SkuInfoService skuInfoService;


    @PostConstruct
    public void initSkuIdBloom() {

        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.BLOOM_SKUID);
        boolean exists = bloomFilter.isExists();
        if (!exists) {
            //说明bloom过滤器未被初始化
            bloomFilter.tryInit(5000000, 0.00001);
        }
        List<Long> list = skuInfoService.getAllSkuIds();

        list.forEach(id->{
            bloomFilter.add(id);});
        log.info("bloom初始化完成，总计添加了{}条数据", list.size());

    }

}
