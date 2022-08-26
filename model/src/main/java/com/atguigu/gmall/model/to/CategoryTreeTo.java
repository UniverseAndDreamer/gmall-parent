package com.atguigu.gmall.model.to;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分类属性")
public class CategoryTreeTo {

    @ApiModelProperty(value = "分类Id")
    private Long categoryId;
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    @ApiModelProperty(value = "子分类集合")
    private List<CategoryTreeTo> categoryChild;

}
