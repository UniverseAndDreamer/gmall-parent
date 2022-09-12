package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.gateway.config.AuthUrlProperties;
import com.atguigu.gmall.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@Slf4j
public class GlobalAuthFilter implements GlobalFilter {

    @Autowired
    private AuthUrlProperties authUrlProperties;
    @Autowired
    private StringRedisTemplate redisTemplate;

    AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        String uri = exchange.getRequest().getURI().toString();
        log.info("{} 请求开始。。。。", path);
        //1.静态资源  --->直接放行
        List<String> anyoneUrls = authUrlProperties.getNoAuthUrl();
        for (String url : anyoneUrls) {
            boolean match = matcher.match(url, path);
            if (match) {
                //说明是静态资源
                return chain.filter(exchange);
            }
        }

        //2.禁止访问的请求
        List<String> denyUrls = authUrlProperties.getDenyUrl();
        for (String denyUrl : denyUrls) {
            boolean match = matcher.match(denyUrl, path);
            if (match) {
                //说明请求中存在禁止访问的路径
                Result<String> result = Result.build("", ResultCodeEnum.PERMISSION);
                return responseResult(result, exchange);
            }
        }


        //3.需要登录的请求    --->进行鉴权验证
        List<String> loginedUrl = authUrlProperties.getLoginedUrl();
        for (String url : loginedUrl) {
            boolean match = matcher.match(url, path);
            if (match) {
                //说明存在需要登录的请求，需要对其进行验证
                String token = getTokenValue(exchange);
                //对token进行校验
                UserInfo userInfo = getTokenUserInfo(token);
                if (userInfo != null) {
                    //说明携带的token正确，放行，同时进行ID透传
//                    exchange.getRequest().getHeaders().add("userId", userInfo.getId().toString());
                    return chain.filter(userIdTransport(userInfo, exchange));
                } else {
                    //说明携带了假的token。或者并没有携带token，打回登录
                    Mono<Void> end = redirectToCustomPage(authUrlProperties.getLoginPage() + "?originUrl=" + uri, exchange);
                    return end;
                }
            }
        }

        //4.说明普通请求 ,即不需要登录也能访问，但是如果普通请求携带了token，
        //  则对token进行验证，若token为假，则打回登录，若为真，则透传用户ID
        String tokenValue = getTokenValue(exchange);
        UserInfo info = getTokenUserInfo(tokenValue);
        if (info != null) {
            //说明携带了token，token为真，ID进行透传
            ServerWebExchange serverWebExchange = userIdTransport(info, exchange);
            return chain.filter(serverWebExchange);
        } else {
            if (!StringUtils.isEmpty(tokenValue)) {
                //说明传了假的token，打回登录页面
                return redirectToCustomPage(authUrlProperties.getLoginPage() + "?originUrl=" + uri, exchange);
            }
        }

        //没有携带token进行正常访问,透传用户临时ID
        exchange = userIdTransport(info, exchange);
        Mono<Void> filter = chain.filter(exchange);
        return filter;



    }



    /**
     * 没有权限，直接响应数据
     *
     * @param result
     * @param exchange
     * @return
     */
    private Mono<Void> responseResult(Result<String> result, ServerWebExchange exchange) {

        ServerHttpResponse response = exchange.getResponse();
        //设置响应的状态码
        response.setStatusCode(HttpStatus.OK);
        //设置响应类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        //将响应的数据转为Json放入dataBuffer中
        String resultJson = Jsons.toStr(result);
        DataBuffer dataBuffer = response.bufferFactory().wrap(resultJson.getBytes());

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    /**
     * 将ID放入请求头中，进行传递
     * @param userInfo
     * @param exchange
     * @return
     */
    private ServerWebExchange userIdTransport(UserInfo userInfo, ServerWebExchange exchange) {
        //构建新的请求
        ServerHttpRequest.Builder reqBuilder = exchange.getRequest().mutate();

        if (userInfo != null) {
            reqBuilder.header(RedisConst.USERID_HEADER, userInfo.getId().toString());
        }
        //用户没登录
        String userTempId = getUserTempId(exchange);
        reqBuilder.header(RedisConst.USERTEMPID_HEADER, userTempId);

        ServerWebExchange newExchange = exchange
                .mutate()
                .request(reqBuilder.build())
                .response(exchange.getResponse())
                .build();

        return newExchange;
    }

    private String getUserTempId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String userTempId = request.getHeaders().getFirst("userTempId");

        if (StringUtils.isEmpty(userTempId)) {
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if (httpCookie != null) {
                userTempId = httpCookie.getValue();
            }
        }
        return userTempId;
    }

    /**
     * 设置重定向
     *
     * @param location
     * @param exchange
     * @return
     */
    private Mono<Void> redirectToCustomPage(String location, ServerWebExchange exchange) {
        //1.构建新请求
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().add(HttpHeaders.LOCATION, location);
        //重定向后，应该将网页中缓存的token进行删除，不然会发生重定向次数过多的异常
        //异常出现原因：   网页中检查到缓存中有token--->判定token为假--->
        //              重定向至登陆页面(此时仍携带假的token)--->重定向后的登录
        //              页面仍然对假的token进行判断--->无限循环
        ResponseCookie tokenCookie = ResponseCookie
                .from("token", "xincookie")
                .maxAge(0)
                .path("/")
                .domain(".gmall.com")
                .build();
        response.getCookies().set("token", tokenCookie);
        return response.setComplete();
    }

    private UserInfo getTokenUserInfo(String token) {
        if (!StringUtils.isEmpty(token)) {
            //说明token不为null
            String infoJson = redisTemplate.opsForValue().get(RedisConst.LOGIN_USER + token);
            UserInfo userInfo = Jsons.toObj(infoJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }

    private String getTokenValue(ServerWebExchange exchange) {
        String tokenValue = "";
        tokenValue = exchange.getRequest().getHeaders().getFirst("token");
        if (!StringUtils.isEmpty(tokenValue)) {
            //说明头中存在token
            return tokenValue;
        }
        HttpCookie token = exchange.getRequest().getCookies().getFirst("token");
        if (token!=null) {
            //说明cookie中存在token
            tokenValue = token.getValue();
        }
        return tokenValue;
    }

//    public static void main(String[] args) {
//        List<Integer> integers = Arrays.asList(1, 2, 3, 45, 5, 6);
//
//        Integer integer1 = integers.stream()
//                .map(integer -> integer + 2)
//                .reduce((a, b) -> {
//                    int sum = 0;
//                    return sum = a + b;
//                }).get();
//        System.out.println(integer1);
//
//    }
}
