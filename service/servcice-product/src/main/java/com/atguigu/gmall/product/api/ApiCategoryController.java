package com.atguigu.gmall.product.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.CategoryTreeTo;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "商品分类RPC远程调用接口")
@RestController
@RequestMapping("/api/inner/rpc/product")
public class ApiCategoryController {

    @Autowired
    private BaseCategory2Service baseCategory2Service;
    @ApiOperation("三级分类的树形结构查询")
    @GetMapping("/category/tree")
    public Result<List<CategoryTreeTo>> getCategoryTreeToList() {
        List<CategoryTreeTo> list = baseCategory2Service.getCategoryTreeToList();
        return Result.ok(list);
    }
}
