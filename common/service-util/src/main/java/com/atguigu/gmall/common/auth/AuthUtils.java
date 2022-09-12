package com.atguigu.gmall.common.auth;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.user.UserAuthInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import javax.servlet.http.HttpServletRequest;

public class AuthUtils {

    public static UserAuthInfo getCurrentAuthInfo() {
        //获取当前线程绑定的请求
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String userId = request.getHeader(RedisConst.USERID_HEADER);

        UserAuthInfo info = new UserAuthInfo();

        if (!StringUtils.isEmpty(userId)) {
            info.setUserId(Long.parseLong(userId));
        }

        String userTempId = request.getHeader(RedisConst.USERTEMPID_HEADER);
        info.setUserTempId(userTempId);
        return info;
    }

}
