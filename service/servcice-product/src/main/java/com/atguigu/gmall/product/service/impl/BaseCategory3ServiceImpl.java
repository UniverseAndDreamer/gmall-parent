package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【base_category3(三级分类表)】的数据库操作Service实现
* @createDate 2022-08-22 23:56:27
*/
@Service
public class BaseCategory3ServiceImpl extends ServiceImpl<BaseCategory3Mapper, BaseCategory3>
    implements BaseCategory3Service{

    @Override
    public List<BaseCategory3> getCategory3List(Long c2Id) {
        LambdaQueryWrapper<BaseCategory3> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BaseCategory3::getCategory2Id, c2Id);
        return this.list(queryWrapper);
    }
}




