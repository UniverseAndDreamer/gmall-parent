package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.LoginSuccessVo;
import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.user.service.UserInfoService;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author 美貌与智慧并存
* @description 针对表【user_info(用户表)】的数据库操作Service实现
* @createDate 2022-09-07 19:45:39
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public LoginSuccessVo login(UserInfo userInfo) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getName, userInfo.getLoginName());
        queryWrapper.eq(UserInfo::getPasswd, MD5.encrypt(userInfo.getPasswd()));

        UserInfo info = this.getOne(queryWrapper);
        if (info == null) {
            //说明账号密码错误
            return null;
        }
        //说明账号密码正确
        //生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConst.LOGIN_USER + token, Jsons.toStr(info), 7, TimeUnit.DAYS);
        LoginSuccessVo loginSuccessVo = new LoginSuccessVo();
        loginSuccessVo.setToken(token);
        loginSuccessVo.setNickName(info.getNickName());

        return loginSuccessVo;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(RedisConst.LOGIN_USER + token);
    }
}




