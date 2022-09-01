package com.atguigu.gmall.product.bloom.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.bloom.BloomOpsService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class BloomOpsServiceImpl implements BloomOpsService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void buildBloom(String bloomName, BloomDataQueryService bloomDataQueryService) {
        //旧的bloom过滤器
        RBloomFilter<Object> oldBloomFilter = redissonClient.getBloomFilter(bloomName);
        //新的bloom过滤器
        String newBloomName = bloomName + "_new";
        //得到新的bloom过滤器
        RBloomFilter<Object> newBloomFilter = redissonClient.getBloomFilter(newBloomName);
        //将新的bloom过滤器初始化
        if (!newBloomFilter.isExists()) {
            newBloomFilter.tryInit(5000000, 0.00001);
        }
        List queryData = bloomDataQueryService.queryData();
        queryData.forEach(data->{
            newBloomFilter.add(data);
        });
        log.info("bloom过滤器初始化完成。。。初始化的数据量为： " + queryData.size());

        //新Bloom初始化完成后，将旧bloom改名，新Bloom改为旧bloom名
        String tempBloomName = bloomName + "_temp";
        oldBloomFilter.rename(tempBloomName);
        newBloomFilter.rename(bloomName);
        //异步删除旧bloom
        redissonClient.getBloomFilter(tempBloomName).deleteAsync();

    }
}
