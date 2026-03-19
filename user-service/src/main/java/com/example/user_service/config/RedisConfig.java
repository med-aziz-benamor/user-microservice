package com.example.userservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * @EnableCaching active les annotations @Cacheable, @CachePut, @CacheEvict
 * Spring utilisera Redis automatiquement (configuré dans application.yml)
 */
@Configuration
@EnableCaching
public class RedisConfig {
    // Spring Boot auto-configure Redis — rien d'autre à faire ici
}
