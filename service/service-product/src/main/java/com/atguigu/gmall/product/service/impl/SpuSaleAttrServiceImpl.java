package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.model.to.SkuValueJsonTo;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service实现
* @createDate 2022-08-23 20:17:39
*/
@Service
public class SpuSaleAttrServiceImpl extends ServiceImpl<SpuSaleAttrMapper, SpuSaleAttr>
    implements SpuSaleAttrService{

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(Long spuId) {

        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdAndSkuId(Long spuId, Long skuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuIdAndSkuId(spuId, skuId);
    }

    @Override
    public List<SkuValueJsonTo> getSkuValueJsonList(Long spuId) {
        return spuSaleAttrMapper.getSkuValueJsonList(spuId);
    }

    @Override
    public String getSkuValueJsonStr(Long spuId) {
        HashMap<String, Long> map = new HashMap<>();
        List<SkuValueJsonTo> skuValueJsonToList = spuSaleAttrService.getSkuValueJsonList(spuId);
        skuValueJsonToList.forEach(skuValueJsonTo -> {
            map.put(skuValueJsonTo.getValueJson(), skuValueJsonTo.getSkuId());
        });
        return Jsons.toStr(map);
    }
}




