package com.deepinblog.cacheable.interceptor;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by louisyuu on 2021/8/24 3:26 下午
 * 生成公共数据的Key，如一些字典数据或者其它公共数据
 */
public class PublicDataKeyGenerator extends GenericKeyGenerator{

    @Override
    protected Long getUserId(HttpServletRequest request) {
        return null;
    }
}
