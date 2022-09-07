package com.atguigu.gmall.user.service;


import com.atguigu.gmall.model.user.LoginSuccessVo;
import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 美貌与智慧并存
* @description 针对表【user_info(用户表)】的数据库操作Service
* @createDate 2022-09-07 19:45:39
*/
public interface UserInfoService extends IService<UserInfo> {

    LoginSuccessVo login(UserInfo userInfo);

    void logout(String token);
}
