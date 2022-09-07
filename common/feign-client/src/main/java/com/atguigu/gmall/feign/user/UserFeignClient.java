package com.atguigu.gmall.feign.user;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-user")
@RequestMapping("/api/user")
public interface UserFeignClient {

    /**
     * 登录功能
     *
     * @param userInfo
     * @return
     */
    @PostMapping("/passport/login")
    Result login(@RequestBody UserInfo userInfo);


}