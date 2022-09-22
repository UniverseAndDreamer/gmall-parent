package com.atguigu.gmall.model.to.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillTempOrderMsg {

    private Long userId;
    private Long skuId;
    private String skuCode;//秒杀码


}
