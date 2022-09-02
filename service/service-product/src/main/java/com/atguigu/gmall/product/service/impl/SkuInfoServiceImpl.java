package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.model.to.SkuValueJsonTo;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.service.*;
import com.atguigu.starter.cache.cache.CacheService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
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
    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;
    @Autowired
    private CacheService cacheService;
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

    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {
        SkuDetailTo skuDetail = new SkuDetailTo();
        //1.查询skuInfo
        SkuInfo skuInfo = this.getById(skuId);
        skuDetail.setSkuInfo(skuInfo);

        //2.查询CategoryView
        Long category3Id = skuInfo.getCategory3Id();
        CategoryViewTo categoryView = baseCategory3Mapper.getCategoryView(category3Id);
        skuDetail.setCategoryView(categoryView);
        //3、查询实时价格price
        BigDecimal price = this.select1010Price(skuId);
        skuDetail.setPrice(price);

        //4、查询skuImageList放入skuInfo中
        LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuImage::getSkuId, skuId);
        List<SkuImage> list = skuImageService.list(queryWrapper);
        skuInfo.setSkuImageList(list);

        //5、查询spuSaleAttrList:查询条件为skuId
        Long spuId = skuInfo.getSpuId();
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrService.getSpuSaleAttrListBySpuIdAndSkuId(spuId, skuId);
        skuDetail.setSpuSaleAttrList(spuSaleAttrList);
        //6.查询valueJson
        HashMap<String, Long> map = new HashMap<>();
        List<SkuValueJsonTo> skuValueJsonToList = spuSaleAttrService.getSkuValueJsonList(spuId);
        skuValueJsonToList.forEach(skuValueJsonTo -> {
            map.put(skuValueJsonTo.getValueJson(), skuValueJsonTo.getSkuId());
        });
        skuDetail.setValuesSkuJson(Jsons.toStr(map));

        return skuDetail;
    }

    @Override
    public BigDecimal select1010Price(Long skuId) {

        return skuInfoMapper.select1010Price(skuId);
    }

    @Override
    public List<Long> getAllSkuIds() {

        return skuInfoMapper.getAllSkuIds();

    }

    @Override
    public void updateByIdAndCache(SkuInfo skuInfo) {
        this.updateById(skuInfo);
        cacheService.delay2Delete(skuInfo.getId() + "");
    }


}




