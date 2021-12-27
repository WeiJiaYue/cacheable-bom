package com.deepinblog.cacheable;

import com.deepinblog.cacheable.interceptor.AuthorizationKeyGenerator;
import com.deepinblog.cacheable.interceptor.KeyGeneratorBeanNames;
import com.deepinblog.cacheable.interceptor.PublicDataKeyGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2021/8/24 4:55 下午
 */
@Configuration
@EnableConfigurationProperties(CacheNameProperties.class)
//也可以放在具体项目XXXApplicationStarter上面
@EnableCaching
public class RedisL2CacheAutoConfiguration {
    //CacheNames与TTL的配置类
    private final CacheNameProperties cacheNameProperties;

    public RedisL2CacheAutoConfiguration(CacheNameProperties cacheNameProperties) {
        this.cacheNameProperties = cacheNameProperties;
    }

    @Bean(KeyGeneratorBeanNames.AUTH_KEY_GENERATOR)
    public AuthorizationKeyGenerator ybKeyGenerator() {
        return new AuthorizationKeyGenerator();
    }


    @Bean(KeyGeneratorBeanNames.PUBLIC_DATA_KEY_GENERATOR)
    public PublicDataKeyGenerator publicDataKeyGenerator() {
        return new PublicDataKeyGenerator();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                          CacheManagerCustomizers customizerInvoker) {

        //分别创建String和JSON格式序列化对象，对缓存数据key和value进行转换
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        /**
         * 1.定制缓存通用配置（通用时效&序列化方式）
         */
        RedisCacheConfiguration config =
                //生成一个默认配置，通过config对象即可对缓存进行自定义配置
                RedisCacheConfiguration.defaultCacheConfig()
                        //通用设置缓存的默认过期时间，也是使用Duration设置
                        .entryTtl(Duration.ofMinutes(5))
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer))
                        //todo 是否需要enable caching null values。根据自身要求来。开启存放null值防止缓存击穿?
                        .disableCachingNullValues();

        /**
         * 2.对每个缓存空间(每个CacheName或者方法级别)应用不同的TTL配置
         */
        Map<String, RedisCacheConfiguration> cacheNamesConfigMap = cacheNameProperties.configTtl(config);

        /**
         * 3.使用自定义的缓存配置初始化一个cacheManager
         */
        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheNamesConfigMap)
                .build();
        return customizerInvoker.customize(cacheManager);

    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizers cacheManagerCustomizers(
            ObjectProvider<List<CacheManagerCustomizer<?>>> customizers) {
        return new CacheManagerCustomizers(customizers.getIfAvailable());
    }
}
