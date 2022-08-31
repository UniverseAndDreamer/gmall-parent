package com.atguigu.gmall.product.controller;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("admin/product")
public class BaseAttrInfoController {

    @Autowired
    private BaseAttrInfoService baseAttrInfoService;
    @Autowired
    private BaseAttrValueService baseAttrValueService;


    /**
     * 通过categoryId来查询attrInfo
     *
     * @param c1Id
     * @param c2Id
     * @param c3Id
     * @return
     */
    @GetMapping("attrInfoList/{c1Id}/{c2Id}/{c3Id}")
    public Result attrInfoList(@PathVariable("c1Id") Long c1Id,
                               @PathVariable("c2Id") Long c2Id,
                               @PathVariable("c3Id") Long c3Id) {
        List<BaseAttrInfo> list = baseAttrInfoService.getAttrInfoListByCategoryIds(c1Id, c2Id, c3Id);
        return Result.ok(list);
    }

    /**
     * save新增 or 修改
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        //说明是新增
        baseAttrInfoService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 通过attrId来查询
     * @param attrId
     * @return
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId) {
        LambdaQueryWrapper<BaseAttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseAttrValue::getAttrId, attrId);
        return Result.ok(baseAttrValueService.list(queryWrapper));
    }


}
