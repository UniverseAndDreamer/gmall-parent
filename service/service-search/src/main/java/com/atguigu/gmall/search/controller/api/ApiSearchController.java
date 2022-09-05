package com.atguigu.gmall.search.controller.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParamVo;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.search.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/inner/rpc/search")
public class ApiSearchController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/goods/search")
    public Result<SearchResponseVo> search(@RequestBody SearchParamVo searchParam) {

        return Result.ok();
    }

    @PostMapping("/goods/save")
    public Result save(@RequestBody Goods goods) {
        goodsService.save(goods);
        return Result.ok();
    }

    /**
     * ES中进行查询数据时，默认最多显示10条数据
     * @param skuId
     * @return
     */
    @DeleteMapping("/goods/{skuId}")
    public Result delete(@PathVariable("skuId") Long skuId) {
        goodsService.deleteById(skuId);
        return Result.ok();
    }

}
