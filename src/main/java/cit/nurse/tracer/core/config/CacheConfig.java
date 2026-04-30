package cit.nurse.tracer.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
        public CacheManager cacheManager(
            @Value("${app.cache.survey-responses.ttl:60s}") Duration surveyResponsesTtl,
            @Value("${app.cache.survey-responses.max-size:500}") long surveyResponsesMaxSize,
            @Value("${app.cache.survey-analytics.ttl:120s}") Duration surveyAnalyticsTtl,
            @Value("${app.cache.survey-analytics.max-size:200}") long surveyAnalyticsMaxSize
        ) {
        CaffeineCache surveyResponsesCache = new CaffeineCache(
            "surveyResponsesPage",
            Caffeine.newBuilder()
                .expireAfterWrite(surveyResponsesTtl)
                .maximumSize(surveyResponsesMaxSize)
                .build()
        );

        CaffeineCache surveyAnalyticsCache = new CaffeineCache(
            "surveyAnalytics",
            Caffeine.newBuilder()
                .expireAfterWrite(surveyAnalyticsTtl)
                .maximumSize(surveyAnalyticsMaxSize)
                .build()
        );

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(surveyResponsesCache, surveyAnalyticsCache));
        return cacheManager;
    }
}
