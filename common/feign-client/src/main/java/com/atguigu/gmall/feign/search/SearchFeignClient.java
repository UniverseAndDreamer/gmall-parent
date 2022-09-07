package com.atguigu.gmall.feign.search;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParamVo;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-search")
@RequestMapping("/api/inner/rpc/search")
public interface SearchFeignClient {

    @PostMapping("/goods/search")
    Result<SearchResponseVo> search(SearchParamVo searchParam);

    @PostMapping("/goods/save")
    Result save(@RequestBody Goods goods);

    @DeleteMapping("/goods/{skuId}")
    Result delete(@PathVariable("skuId") Long skuId);

    @GetMapping("/goods/updateHotScore/{skuId}")
    Result updateHotScore(@PathVariable("skuId") Long skuId, @RequestParam("score") Long score);
}
