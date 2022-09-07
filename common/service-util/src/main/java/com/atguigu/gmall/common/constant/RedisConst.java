package com.atguigu.gmall.common.constant;

public class RedisConst {


    public static final String SKUDETAIL_KEY_PREFIX = "skuDetail:info:";//拼接skuId
    public static final String SKUDETAIL_VALUE_NULL = "x";

    public static final Long VALUE_TTL = 60 * 60 * 24 * 7L;
    public static final Long VALUE_NULL_TTL = 60 * 30L;


    public static final String BLOOM_SKUID = "bloom:skuid";
    public static final String LOCK_SKU_DETAIL = "lock:sku:detail:";//拼接skuId
    public static final int SEARCH_PAGE_SIZE = 10;

    public static final String SKU_HOTSCORE_PREFIX = "sku:hotScore:";//拼接skuId

    public static final String LOGIN_USER = "user:login:";//拼接token
}
