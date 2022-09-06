package com.atguigu.gmall.model.list;

import com.atguigu.gmall.model.product.BaseTrademark;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    private SearchParamVo searchParamVo;

    private String trademarkParam;

    private List<SearchAttr> propsParamList;

    private List<TrademarkVo> trademarkList;

    private List<AttrVo> attrsList;

    private String urlParam;

    private OrderMap orderMap;

    private List<Goods> goodsList;

    private Integer pageNo;

    private Long totalPages;

}
