package com.atguigu.gmall.product.schedule;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.bloom.BloomOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RebuildBloomTask {

    @Autowired
    private BloomOpsService bloomOpsService;
    @Autowired
    private BloomDataQueryService bloomDataQueryService;

    @Scheduled(cron = "1 1 3 * * 3")
    public void rebuildBloomTask() {
        bloomOpsService.buildBloom(RedisConst.BLOOM_SKUID, bloomDataQueryService);
    }
}
