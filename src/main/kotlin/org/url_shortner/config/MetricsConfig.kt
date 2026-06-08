package org.url_shortner.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {

    @Bean
    fun urlsCreatedCounter(registry: MeterRegistry): Counter =
        Counter.builder("urls.created.total")
            .description("Total number of short URLs created")
            .register(registry)

    @Bean
    fun urlRedirectsCounter(registry: MeterRegistry): Counter =
        Counter.builder("url.redirects.total")
            .description("Total number of redirects served")
            .register(registry)

    @Bean
    fun rateLimitHitsCounter(registry: MeterRegistry): Counter =
        Counter.builder("rate.limit.hits.total")
            .description("Total number of rate limit rejections")
            .register(registry)

    @Bean
    fun cacheHitsCounter(registry: MeterRegistry): Counter =
        Counter.builder("cache.hits.total")
            .description("Redis cache hits on URL resolution")
            .register(registry)

    @Bean
    fun cacheMissesCounter(registry: MeterRegistry): Counter =
        Counter.builder("cache.misses.total")
            .description("Redis cache misses — fell through to DB")
            .register(registry)
}