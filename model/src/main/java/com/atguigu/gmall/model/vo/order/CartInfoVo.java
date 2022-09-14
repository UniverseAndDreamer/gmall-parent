package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartInfoVo {

    private Long skuId;

    private String imgUrl;

    private String skuName;

    private BigDecimal orderPrice;

    private Integer skuNum;
    //是否有库存
    private String hasStock;

}
