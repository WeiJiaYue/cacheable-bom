package com.deepinblog.cacheable;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Map的Key为与{@link Cacheable#cacheNames()}相同，并且不能用符号点[.]拼接。请务必用符号中划线[-]
 * <pre>
 * 如错误示例❌sales.customer。正确示例✅sales-customers
 * </pre>
 * <pre>
 * TTL {@link Cacheable}使用两部曲
 * 1.在application-xxx.yml文件中配置自定义的TTL
 * cacheable:
 *    #cache name ttl config
 *    cache-names:
 *      sales-customers:
 *          time-to-live: 60
 *          unit: SECONDS
 * </pre>
 * <pre>
 * 2.在XXXServiceImpl的某个方法上配置如下注解
 * @Cacheable(cacheNames = "sales-customers", keyGenerator = KeyGeneratorBeanNames.BEAN_NAME)
 * </pre>
 */

@Data
@ConfigurationProperties(prefix = "cacheable")
public class CacheNameProperties {


    private final static Random random = new Random();

    /**
     * 随机TTL，防止大面积的缓存Key同时失效
     * 是否开启TTL自动偏移
     */
    private boolean enableTtlAutoOffset = true;

    /**
     * 随机TTL，防止大面积的缓存Key同时失效。这个算法可以自身再实现。这里只是简单的随机偏移函数
     * TTL自动便宜的秒数
     */
    private int defaultOffsetSeconds = 30;

    /**
     * CacheName与TTL的映射
     */
    private final Map<String, Duration> cacheNames = new HashMap<>();

    @Data
    public static class Duration {
        /**
         * Your Caching value's time to live
         */
        private long timeToLive;
        /**
         * {@link ChronoUnit#name()}
         * 全部大写SECONDS,MINUTES,HOURS,DAYS
         */
        private String unit;
    }



    public Map<String, RedisCacheConfiguration> configTtl(RedisCacheConfiguration commonConfig) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        for (Map.Entry<String, Duration> entry : cacheNames.entrySet()) {
            //entry.getKey() is cache name
            cacheConfigurations.put(entry.getKey(), commonConfig.entryTtl(toJavaDuration(entry.getKey(), entry.getValue().getTimeToLive(), entry.getValue().getUnit())));
        }
        return cacheConfigurations;
    }


    public java.time.Duration toJavaDuration(String cacheName, long timeToLive, String unit) {
        if (!enableTtlAutoOffset) {
            return java.time.Duration.of(timeToLive, getChronoUnit(cacheName, unit));
        }
        int offsetSeconds = random.nextInt(defaultOffsetSeconds);
        java.time.Duration offsetDuration = java.time.Duration.ofSeconds(offsetSeconds);
        return java.time.Duration.of(timeToLive, getChronoUnit(cacheName, unit)).plus(offsetDuration);
    }

    private ChronoUnit getChronoUnit(String cacheName, String unitName) {
        List<ChronoUnit> results = Arrays.stream(ChronoUnit.values()).filter(e -> unitName.equals(e.name())).collect(Collectors.toList());
        if (results.isEmpty()) {
            throw new IllegalArgumentException(cacheName + "'s unit is illegal,please refer to java.time.temporal.ChronoUnit");
        }
        return results.get(0);
    }
}