package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【sku_info(库存单元表)】的数据库操作Service实现
* @createDate 2022-08-23 20:17:39
*/
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo>
    implements SkuInfoService{

    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private SkuImageService skuImageService;
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1.存储sku基本信息
        this.save(skuInfo);
        Long id = skuInfo.getId();
        Long spuId = skuInfo.getSpuId();
        //2.存储skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        skuAttrValueList.forEach(skuAttrValue -> skuAttrValue.setSkuId(id));
        skuAttrValueService.saveBatch(skuAttrValueList);
        //3.存储skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSkuId(id);
            skuSaleAttrValue.setSpuId(spuId);
        });
        skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        //4.存储skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(skuImage -> skuImage.setSkuId(id));
        skuImageService.saveBatch(skuImageList);
    }

    @Override
    public void onSale(Long skuId) {
        skuInfoMapper.updateSaleState(skuId,1);
    }

    @Override
    public void cancelSale(Long skuId) {
        skuInfoMapper.updateSaleState(skuId,0);
    }
}




