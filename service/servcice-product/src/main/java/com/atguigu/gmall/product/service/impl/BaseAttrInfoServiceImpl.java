package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 美貌与智慧并存
* @description 针对表【base_attr_info(属性表)】的数据库操作Service实现
* @createDate 2022-08-23 20:17:39
*/
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo>
    implements BaseAttrInfoService{

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrInfoListByCategoryIds(Long c1Id, Long c2Id, Long c3Id) {
        return baseAttrInfoMapper.getAttrInfoListByCategoryIds(c1Id, c2Id, c3Id);
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() == null || baseAttrInfo.getId() == 0) {
            //说明是新增
            saveAttr(baseAttrInfo);
        } else {
            //说明是修改
            updateAttr(baseAttrInfo);
        }

    }

    private void updateAttr(BaseAttrInfo baseAttrInfo) {
        //1.修改baseAttrInfo
        baseAttrInfoMapper.updateById(baseAttrInfo);
        //2.修改attrValueList,分为三部分：删除没有携带的，修改携带id的，新增没携带idde
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //2.1.1 删除没有携带的数据
        // 从前端传过来的数据中拿出属性attrValueList的id，并映射为集合
        List<Long> ids = attrValueList.stream().filter(baseAttrValue -> baseAttrValue.getId()!=null).map(BaseAttrValue::getId).collect(Collectors.toList());

        //根据条件：与attr_id相同，且不在ids集合中，意味着用户再修改属性时，将此attrValue删除
        if (ids.size() > 0) {
            //说明存在要修改的属性，将要修改的属性进行排出后，删除未携带的
            LambdaQueryWrapper<BaseAttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseAttrValue::getAttrId, baseAttrInfo.getId());
            queryWrapper.notIn(BaseAttrValue::getId, ids);
            baseAttrValueMapper.delete(queryWrapper);
        } else {
            //说明不存在要修改的属性，则要将旧属性进行删除
            LambdaQueryWrapper<BaseAttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseAttrValue::getAttrId, baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);
        }


        //遍历前端传递过来的数据，进行新增或者修改
        for (BaseAttrValue baseAttrValue : attrValueList) {
            if (baseAttrValue.getId() == null) {
                //说明是新增的
                //2.1.2新增没携带id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            } else {
                //说明是修改
                //2.1.3修改携带id的
                baseAttrValueMapper.updateById(baseAttrValue);
            }
        }
    }

    private void saveAttr(BaseAttrInfo baseAttrInfo) {
        baseAttrInfoMapper.insert(baseAttrInfo);
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            //说明此baseAttr增加的baseAttrValue不为空
            baseAttrInfo.getAttrValueList().forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }
}




