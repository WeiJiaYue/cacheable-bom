package com.deepinblog.cacheable.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by louisyuu on 2021/8/24 3:26 下午
 * 生成通过网关授权的、每个用户级别的Key
 * 示例Key
 * ${spring.application.name}:SimpleClassName:MethodName:userId:hash(params)
 * xxx-server:XXXServiceImpl:getUsers:402:c5b0af9092353a1789bb2a5c0c83a9b2
 */
public class AuthorizationKeyGenerator extends GenericKeyGenerator {


    @Override
    protected Long getUserId(HttpServletRequest request) {
        String userJson =request.getHeader("userinfo");
        if(StringUtils.isEmpty(userJson)){
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(userJson);
        return jsonObject.getLong("userId");
    }




}