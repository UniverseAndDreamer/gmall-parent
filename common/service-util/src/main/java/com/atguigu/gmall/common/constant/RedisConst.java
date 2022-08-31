package com.atguigu.gmall.common.constant;

public class RedisConst {


    public static final String SKUDETAIL_KEY_PREFIX = "skuDetail:info:";
    public static final String SKUDETAIL_VALUE_NULL = "x";

    public static final Long VALUE_TTL = 60 * 60 * 24 * 7L;
    public static final Long VALUE_NULL_TTL = 60 * 30L;


    public static final String BLOOM_SKUID = "bloom:skuid";
}
