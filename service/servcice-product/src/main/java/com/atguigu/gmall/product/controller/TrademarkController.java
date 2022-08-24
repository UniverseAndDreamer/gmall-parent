package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product")
public class TrademarkController {
//    /baseTrademark/{page}/{limit}

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     * 分页查询
     *
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/baseTrademark/{page}/{limit}")
    public Result getBaseTrademarkPage(@PathVariable("page") Long page,
                                       @PathVariable("limit") Long limit) {

        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        Page<BaseTrademark> trademarkPage = baseTrademarkService.page(baseTrademarkPage);
        return Result.ok(trademarkPage);
    }

    /**
     * 新增品牌
     * 请求体中含有json入参时，应该使用requestBody，不能省略
     *
     * @param baseTrademark
     * @return
     */
    @PostMapping("baseTrademark/save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    ///admin/product/baseTrademark/get/13
    @GetMapping("/baseTrademark/get/{id}")
    public Result getById(@PathVariable("id") Long id) {
        BaseTrademark item = baseTrademarkService.getById(id);
        return Result.ok(item);
    }

    /**
     * 更新品牌
     * @param baseTrademark
     * @return
     */
    //baseTrademark/updat
    @PutMapping("/baseTrademark/update")
    public Result update(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    /**
     * 根据Id删除
     * @param id
     * @return
     */
    //    /admin/product/baseTrademark/remove/{id}
    @DeleteMapping("/baseTrademark/remove/{id}")
    public Result removeById(@PathVariable("id") Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
}
