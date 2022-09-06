package com.atguigu.gmall.model.list;

import lombok.Data;

import java.util.List;

@Data
public class AttrVo {

    private Long attrId;

    private String attrName;

    private List<String> attrValueList;


}
