package com.atguigu.gmall.search.service.impl;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.google.common.collect.Lists;

import com.atguigu.gmall.search.repositories.GoodsRepository;
import com.atguigu.gmall.search.service.GoodsService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;


    @Override
    public void save(Goods goods) {
        goodsRepository.save(goods);
    }

    @Override
    public void deleteById(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public SearchResponseVo search(SearchParamVo searchParam) {
        //动态条件（searchParam）构建DSL语句，从ES中进行查询，并返回数据
        Query query = buildQueryDsl(searchParam);
        //查询出结果
        SearchHits<Goods> goodsHits = restTemplate.search(query, Goods.class, IndexCoordinates.of("goods"));
        //将搜索结果进行替换
        SearchResponseVo searchResponseVo = buildSearchResponseResult(searchParam, goodsHits);

        return searchResponseVo;
    }

    /**
     * 动态构建DSL语句
     *
     * @param searchParam
     * @return
     */
    private Query buildQueryDsl(SearchParamVo searchParam) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.构建分类条件
        if (searchParam.getCategory1Id()!=null) {
            //说明查询条件包含category1Id
            boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        if (searchParam.getCategory2Id()!=null) {
            //说明查询条件包含category2Id
            boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (searchParam.getCategory3Id()!=null) {
            //说明查询条件包含category3Id
            boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        //2.构造品牌的条件：  trademark=1:小米
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            boolQueryBuilder.must(QueryBuilders.termQuery("tmId", Long.parseLong(split[0])));
        }
        //3.构造关键字的条件
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            //说明此次查询包含关键字
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword));
        }
        //4.构造平台属性的条件：props=23:4G:运行内存&props=24:128G:机身内存
        List<String> props = searchParam.getProps();
        if (props != null && props.size() > 0) {
            BoolQueryBuilder finalBoolQueryBuilder = boolQueryBuilder;
            props.stream().forEach(prop->{
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", attrValue));

                NestedQueryBuilder attrsQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                finalBoolQueryBuilder.must(attrsQuery);
            });
        }
        NativeSearchQuery query = new NativeSearchQuery(boolQueryBuilder);
        //5.构造排序条件    "order=1:desc"
        String order = searchParam.getOrder();
        String[] split = order.split(":");

        String orderField = "hotScore";
        switch (split[0]) {
            case "1":
                orderField = "hotScore";
                break;
            case "2":
                orderField = "price";
                break;
            case "3":
                orderField = "createTime";
                break;
            default:
                orderField = "hotScore";
        }
        Sort sort = Sort.by(orderField);
        if (split[1].equals("asc")) {
            sort = sort.ascending();
        } else {
            sort = sort.descending();
        }
        query.addSort(sort);

        //6.构造分页条件
        Integer pageNo = searchParam.getPageNo();
        query.setPageable(PageRequest.of(pageNo-1, RedisConst.SEARCH_PAGE_SIZE));

        //7.根据keyword查询时，keyword高亮显示
        if (StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title")
                    .preTags("<span style = 'color:red'>").postTags("</span>");
            HighlightQuery highlightQuery = new HighlightQuery(highlightBuilder);
            query.setHighlightQuery(highlightQuery);
        }
        //8=========聚合分析上面DSL检索到的所有商品涉及了多少种品牌和多少种平台属性
        //8.1 聚合分析品牌属性DSL语句
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId").size(1000);
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName").size(1));
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoAgg").field("tmLogoUrl").size(1));
        query.addAggregation(tmIdAgg);
        //8.1 聚合分析平台属性的DSL语句
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attrAgg", "attrs");
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(1000);
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(1000);
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);
        attrAgg.subAggregation(attrIdAgg);

        query.addAggregation(attrAgg);

        return query;
    }
    //TODO 动态构建响应结果
    private SearchResponseVo buildSearchResponseResult(SearchParamVo searchParam, SearchHits<Goods> goodsHits) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        List<SearchHit<Goods>> searchHits = goodsHits.getSearchHits();
        //1.检索的请求参数
        searchResponseVo.setSearchParamVo(searchParam);
        //2.构建品牌的面包屑    trademark:  1:小米
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            //说明请求参数中存在 品牌
            searchResponseVo.setTrademarkParam("品牌：" + searchParam.getTrademark().split(":")[1]);
        }
        //3.构建平台属性面包屑
        List<String> props = searchParam.getProps();
        if (props != null && props.size() > 0) {
            List<SearchAttr> searchAttrList = new ArrayList<>();
            for (String prop : props) {
                //prop=24:128G:机身内存
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(Long.parseLong(prop.split(":")[0]));
                searchAttr.setAttrValue(prop.split(":")[1]);
                searchAttr.setAttrName(prop.split(":")[2]);
                searchAttrList.add(searchAttr);
            }
            searchResponseVo.setPropsParamList(searchAttrList);
        }


        //TODO 4 ES聚合分析商品列表中所有商品的品牌信息
        List<TrademarkVo> baseTrademarks = buildTrademarkList(goodsHits);
        searchResponseVo.setTrademarkList(baseTrademarks);

        //TODO 5 ES聚合分析商品泪飙中所有商品的平台属性信息
        List<AttrVo> attrsList = buildAttrList(goodsHits);

        searchResponseVo.setAttrsList(attrsList);


        //6.构建旧的url
        String url = appendUrlParam(searchParam);
        searchResponseVo.setUrlParam(url);
        //7.构建排序方式
        String order = searchParam.getOrder();
        String[] split = order.split(":");
        OrderMap orderMap = new OrderMap();
        orderMap.setType(split[0]);
        orderMap.setSort(split[1]);
        searchResponseVo.setOrderMap(orderMap);
        //8.构建商品列表
        List<Goods> goods = new ArrayList<>();
        searchHits.forEach(goodsSearchHit -> {
            Goods content = goodsSearchHit.getContent();
            goods.add(content);
        });
        searchResponseVo.setGoodsList(goods);
        //9.构建分页数据
        searchResponseVo.setPageNo(searchParam.getPageNo());
        Long totalPages = getTotalPages(goodsHits);
        searchResponseVo.setTotalPages(totalPages);

        return searchResponseVo;
    }


    private List<AttrVo> buildAttrList(SearchHits<Goods> goodsHits) {
        ParsedNested attrAgg = goodsHits.getAggregations().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<AttrVo> attrsList = new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            List<String> attrValueList = new ArrayList<>();
            long attrId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            for (Terms.Bucket attrValueAggBucket : attrValueAgg.getBuckets()) {
                String attrValueName = attrValueAggBucket.getKeyAsString();
                attrValueList.add(attrValueName);
            }
            AttrVo attrVo = new AttrVo();
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValueList(attrValueList);
            attrsList.add(attrVo);
        }
        return attrsList;
    }


    private List<TrademarkVo> buildTrademarkList(SearchHits<Goods> goodsHits) {
        ParsedLongTerms tmIdAgg = goodsHits.getAggregations().get("tmIdAgg");
        List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        List<TrademarkVo> baseTrademarks = new ArrayList<>();
        buckets.forEach(bucket -> {
            Long trademarkId = (Long) bucket.getKey();
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            String trademarkName = (String) tmNameAgg.getBuckets().get(0).getKey();
            ParsedStringTerms tmLogoAgg = bucket.getAggregations().get("tmLogoAgg");
            String trademarkLogoUrl = (String) tmLogoAgg.getBuckets().get(0).getKey();

            TrademarkVo baseTrademark = new TrademarkVo();
            baseTrademark.setTmName(trademarkName);
            baseTrademark.setTmLogoUrl(trademarkLogoUrl);
            baseTrademark.setTmId(trademarkId);

            baseTrademarks.add(baseTrademark);
        });
        return baseTrademarks;
    }

    private String appendUrlParam(SearchParamVo searchParam) {
        StringBuilder stringBuilder = new StringBuilder("list.html?");
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            stringBuilder.append("&category1Id=" + searchParam.getCategory1Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            stringBuilder.append("&category2Id=" + searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            stringBuilder.append("&category3Id=" + searchParam.getCategory3Id());
        }

        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            stringBuilder.append("&trademark=" + searchParam.getTrademark());
        }
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            stringBuilder.append("&keyword=" + searchParam.getKeyword());
        }
        List<String> props = searchParam.getProps();
        if (props != null && props.size() > 0) {
            props.forEach(prop->{
                stringBuilder.append("&props=" + prop);
            });
        }
        String url = stringBuilder.toString();
        return url;
    }

    private Long getTotalPages(SearchHits<Goods> goodsHits) {
        long totalHits = goodsHits.getTotalHits();
        if (totalHits % RedisConst.SEARCH_PAGE_SIZE == 0) {
            //说明商品数量可以整除pageSize
            return totalHits / RedisConst.SEARCH_PAGE_SIZE;
        }
        return totalHits / RedisConst.SEARCH_PAGE_SIZE + 1;
    }
}
