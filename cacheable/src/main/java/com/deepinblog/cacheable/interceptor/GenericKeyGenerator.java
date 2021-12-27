package com.deepinblog.cacheable.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by louisyuu on 2021/8/25 2:55 下午
 */
@Slf4j
public abstract class GenericKeyGenerator implements KeyGenerator {

    private static final byte[] SECRET_KEY = "_secret_key".getBytes();

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();


    @Value("${spring.application.name}")
    private String appName;

    protected abstract Long getUserId(HttpServletRequest request);


    /**
     * 生成CacheKey的前缀
     * 规则 ${spring.application.name}:SimpleClassName:MethodName:
     * 如：XXX-Server:XXXServiceImpl:getUsers:
     */
    protected String getKeyPrefix(Object target, Method method) {
        return appName + ":" + target.getClass().getSimpleName() + ":" + method.getName() + ":";
    }


    /**
     * 获取HttpServletRequest对象
     */
    protected HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        return sra.getRequest();
    }

    //生成Key，如XXX-Server:XXXClass:getUsers:NONEparamshashing
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String keyPrefix = getKeyPrefix(target, method);
        HttpServletRequest request = getRequest();
        Long userId;
        //单元测试进入的逻辑
        if (request == null || request instanceof MockHttpServletRequest) {
            userId = getLocalUserId();
            if (log.isDebugEnabled()) {
                log.debug("User id in mock model {}", userId);
            }
        } else {
            //用户级别的cacheKey
            userId = getUserId(request);
            if (log.isDebugEnabled()) {
                log.debug("User id in web model {}", userId);
            }
        }
        //如果是公共数据，那么userId为NONE
        String userIdString = userId == null ? "NONE:" : userId + ":";
        String paramHash = hash(params);
        return keyPrefix + userIdString + paramHash;
    }


    public static String generateKeyManually(
            String cacheName,
            String appName,
            Object target,
            String methodName,
            Long userId,
            Object... params) {
        String keyPrefix = cacheName + "::" + appName + ":" + target.getClass().getSimpleName() + ":" + methodName + ":";
        String userIdString = userId == null ? "NONE:" : userId + ":";
        String paramHash = String.valueOf(Arrays.hashCode(params));
        return keyPrefix + userIdString + paramHash;
    }

    //自己实现，对请求参数hash，生成唯一的cacheKey
    public String hash(Object... params) {
        //Hashing.hmacMd5(SECRET_KEY).hashString(preHash, UTF_8).toString();
        return String.valueOf(Arrays.hashCode(params));
    }


    public static void setLocalUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }


    public static Long getLocalUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 移除本地变量，如果设置了那么必须调用
     */
    public static void clearLocalUserId() {
        USER_ID_HOLDER.remove();
    }


}
