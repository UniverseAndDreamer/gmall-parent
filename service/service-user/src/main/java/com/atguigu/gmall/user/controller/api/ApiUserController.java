package com.atguigu.gmall.user.controller.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.user.LoginSuccessVo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class ApiUserController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 登录功能
     * @param userInfo
     * @return
     */
    @PostMapping("/passport/login")
    public Result login(@RequestBody UserInfo userInfo) {
        LoginSuccessVo vo = userInfoService.login(userInfo);

        if (vo != null) {
            //说明登录成功，返回
            return Result.ok(vo);
        }
        //说明登录失败
        return Result.build("", ResultCodeEnum.LOGIN_ERROR);
    }

    /**
     * 注销功能
     * @param token
     * @return
     */
    @GetMapping("/passport/logout")
    public Result logout(@RequestHeader("token")String token) {
        userInfoService.logout(token);
        return Result.ok();
    }


    @GetMapping("/getUserAddressList")
    public Result<List<UserAddress>> getUserAddressList() {

        List<UserAddress> userAddresses =  userInfoService.getUserAddressList();
        return Result.ok(userAddresses);
    }
}
