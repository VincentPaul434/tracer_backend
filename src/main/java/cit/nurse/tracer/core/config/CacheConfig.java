package cit.nurse.tracer.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.survey-responses.ttl:60s}") Duration surveyResponsesTtl,
            @Value("${app.cache.survey-responses.max-size:500}") long surveyResponsesMaxSize
    ) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("surveyResponsesPage");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(surveyResponsesTtl)
                        .maximumSize(surveyResponsesMaxSize)
        );
        return cacheManager;
    }
}
