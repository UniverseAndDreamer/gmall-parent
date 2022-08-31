package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 美貌与智慧并存
* @description 针对表【spu_info(商品表)】的数据库操作Service
* @createDate 2022-08-23 20:17:39
*/
public interface SpuInfoService extends IService<SpuInfo> {

    Page<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id);

    void saveSpuInfo(SpuInfo spuInfo);
}
