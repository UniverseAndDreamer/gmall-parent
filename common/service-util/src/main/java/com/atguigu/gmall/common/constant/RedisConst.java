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
    public static final String USERID_HEADER = "userId";

    public static final String CART_KEY = "cart:user:";//拼接用户Id或者用户的临时Id
    public static final String USERTEMPID_HEADER = "usertempid";

    public static final int CART_ITEMS_LIMIT = 200;
    public static final int CART_SKUNUM_LIMIT = 200;
    public static final String ORDER_TEMP_TOKEN = "order:temptoken:";//order:temptoken:交易号

    public static final long ORDER_EXPIRE_TTL = 45 * 60l;

    public static final long ORDER_REFUND_TTL = 60 * 60 * 24 * 7l;

    public static final String MQ_RETRY = "mq:message:";
    //
    public static final String CACHE_SECKILL_GOODS = "seckill:goods:";//加上日期

    public static final String CACHE_SECKILL_GOODS_STOCK = "seckill:goods:stock:";//加上商品Id

    public static final String SECKILL_CODE = "seckill:code:";//加上秒杀码
    public static final String SECKILL_ORDER = "seckill:goods:order:";//加上秒杀码
}
