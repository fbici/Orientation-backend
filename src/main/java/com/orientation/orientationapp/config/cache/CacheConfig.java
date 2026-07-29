package com.orientation.orientationapp.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {
    // Cache manager is provided by RedisConfig
    // This configuration enables caching
}
