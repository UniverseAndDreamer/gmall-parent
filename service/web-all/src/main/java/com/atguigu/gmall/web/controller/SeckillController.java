package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.seckill.SeckillFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.seckill.SeckillConfirmVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SeckillController {
    //    http://activity.gmall.com/seckill.html
    @Autowired
    SeckillFeignClient seckillFeignClient;

    /**
     * 跳转向秒杀页面首页
     *
     * @return
     */
    @GetMapping("/seckill.html")
    public String toSeckillPage(Model model) {

        List<SeckillGoods> list = seckillFeignClient.getSeckillGoodsList().getData();
        model.addAttribute("list", list);
        return "/seckill/index";
    }

    //    /seckill/49.html
    @GetMapping("/seckill/{skuId}.html")
    public String seckillDetail(@PathVariable("skuId") Long skuId,
                                Model model) {

        Result<SeckillGoods> seckillGoods = seckillFeignClient.getSeckillGoodsBySkuId(skuId);
        model.addAttribute("item", seckillGoods.getData());
        return "seckill/item";
    }


    @GetMapping("/seckill/queue.html")
    public String seckillQueuePage(@RequestParam("skuId") Long skuId,
                                   @RequestParam("skuIdStr") String skuIdStr,
                                   Model model) {
        model.addAttribute("skuId", skuId);
        model.addAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

//    seckill/trade.html

    /**
     * 跳转向秒杀订单页面
     * @param model
     * @return
     */
    @GetMapping("/seckill/trade.html")
    public String seckillTradePage(Model model,
                                   @RequestParam("skuId") Long skuId) {
        SeckillConfirmVo data = seckillFeignClient.getSeckillConfirmVo(skuId).getData();
        model.addAttribute("userAddressList", data.getUserAddressList());
        model.addAttribute("detailArrayList", data.getTempOrder().getOrderDetailList());
        model.addAttribute("totalNum", data.getTotalNum());
        model.addAttribute("totalAmount", data.getTotalAmount());
        return "seckill/trade";
    }

}
