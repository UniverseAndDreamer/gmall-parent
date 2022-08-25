package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import jdk.nashorn.internal.objects.NativeUint8Array;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 美貌与智慧并存
* @description 针对表【spu_info(商品表)】的数据库操作Service实现
* @createDate 2022-08-23 20:17:39
*/
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo>
    implements SpuInfoService{
    @Resource
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private SpuImageService spuImageService;

    @Resource
    private SpuSaleAttrService spuSaleAttrService;

    @Resource
    private SpuSaleAttrValueService spuSaleAttrValueService;



    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1对spuInfo表进行新增
        spuInfoMapper.insert(spuInfo);
        Long spuId = spuInfo.getId();
        //2.对spuInfo中的图片集合进行新增
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuId);
            });
        }
        spuImageService.saveBatch(spuImageList);

        //3.对spuInfo中的spuSaleAttrList进行新增
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuId);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuId);
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                    });
                    //3.1对spuSaleAttr中的属性spuSaleAttrValueList属性进行新增
                    spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
                }
            });
            spuSaleAttrService.saveBatch(spuSaleAttrList);
        }

    }
    @Override
    public Page<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id) {
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);
        LambdaQueryWrapper<SpuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuInfo::getCategory3Id, category3Id);
        return this.page(spuInfoPage, queryWrapper);
    }


}




