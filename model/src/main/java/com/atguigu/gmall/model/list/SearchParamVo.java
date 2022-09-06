package com.atguigu.gmall.model.list;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    private Long category1Id;
    private Long category2Id;
    private Long category3Id;
    private String keyword;
    private List<String> props;
    //trademark=1:小米
    private String trademark;
    //order=1:desc
    private String order = "order=1:desc";
    private Integer pageNo = 1;



}
