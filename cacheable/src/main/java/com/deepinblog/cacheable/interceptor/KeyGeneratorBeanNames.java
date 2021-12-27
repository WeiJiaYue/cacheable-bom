package com.deepinblog.cacheable.interceptor;


/**
 * Created by louisyuu on 2021/8/24 3:26 下午
 * <p>
 * Bean names of {@link org.springframework.cache.interceptor.KeyGenerator} on spring ioc container
 */
public interface KeyGeneratorBeanNames {
    String AUTH_KEY_GENERATOR = "AUTH_KEY_GENERATOR";
    String PUBLIC_DATA_KEY_GENERATOR = "PUBLIC_DATA_KEY_GENERATOR";
}