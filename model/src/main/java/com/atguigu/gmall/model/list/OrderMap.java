package com.atguigu.gmall.model.list;

import lombok.Data;

@Data
public class OrderMap {
    //'1'   综合排序   '2'  价格排序
    private String type;
    //asc/desc 升序/降序
    private String sort;
}
