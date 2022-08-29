package com.atguigu.gmall.item.service;

import com.atguigu.gmall.model.to.SkuDetailTo;

import java.util.concurrent.ExecutionException;

public interface SkuDetailService {
    SkuDetailTo getSkuDetail(Long skuId) throws Exception;
}
