package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {


    @Autowired
    private OrderFeignClient orderFeignClient;


    /**
     * 跳转至trade页面
     *
     * @return
     */
    @GetMapping("/trade.html")
    public String toTradePage(Model model) {
        Result<OrderConfirmDataVo> result = orderFeignClient.getOrderConfirmData();
        if (result.isOk()) {
            OrderConfirmDataVo data = result.getData();
            model.addAttribute("detailArrayList", data.getDetailArrayList());
            model.addAttribute("totalNum", data.getTotalNum());
            model.addAttribute("totalAmount", data.getTotalAmount());
            model.addAttribute("tradeNo", data.getTradeNo());
            model.addAttribute("userAddressList", data.getUserAddressList());
            return "/order/trade";
        } else {
            return "order.error";
        }

    }

}
