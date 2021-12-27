package com.deepinblog.cacheable.sample;

import com.deepinblog.cacheable.interceptor.KeyGeneratorBeanNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by louisyuu on 2021/12/24 4:35 下午
 */

@SpringBootApplication
@RestController
public class CacheableSampleApp {

    public static void main(String[] args) {
        SpringApplication.run(CacheableSampleApp.class, args);
    }

    @Autowired
    private SampleService sampleService;

    @GetMapping("hello")
    //cacheNames 对应 application.yml配置的

    public String get() {
        long s = System.currentTimeMillis();
        String d = sampleService.get();
        long e = System.currentTimeMillis();

        return d + " 本次调用消耗了" + (e - s) + "ms";
    }


    @Component
    class SampleService {

        @Cacheable(cacheNames = "hello-world", keyGenerator = KeyGeneratorBeanNames.PUBLIC_DATA_KEY_GENERATOR)
        public String get() {
            return simulateLoadFromDB();
        }
    }


    //模拟从数据库加载数据
    private String simulateLoadFromDB() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        return "Hello World";
    }

}
