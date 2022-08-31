package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 美貌与智慧并存
* @description 针对表【base_category3(三级分类表)】的数据库操作Mapper
* @createDate 2022-08-22 23:56:27
* @Entity com.atguigu.gmall.product.domain.BaseCategory3
*/
public interface BaseCategory3Mapper extends BaseMapper<BaseCategory3> {

    CategoryViewTo getCategoryView(@Param("category3Id") Long category3Id);
}




