package org.url_shortner.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class UrlCacheService(
    private val redis: StringRedisTemplate,
    @Value("\${app.url.cache-ttl-hours}") private val cacheTtlHours: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val ttl = Duration.ofHours(cacheTtlHours)
    private val nullSentinel = "__NULL__"

    fun get(shortCode: String): String? {
        return try {
            val value = redis.opsForValue().get("url:$shortCode")
            when (value) {
                null         -> null
                nullSentinel -> nullSentinel
                else         -> value
            }
        } catch (ex: Exception) {
            log.warn("Redis read failed for $shortCode: ${ex.message}")
            null
        }
    }

    fun set(shortCode: String, originalUrl: String) {
        try {
            redis.opsForValue().set("url:$shortCode", originalUrl, ttl)
        } catch (ex: Exception) {
            log.warn("Redis write failed for $shortCode: ${ex.message}")
        }
    }

    fun setNull(shortCode: String) {

        try {
            redis.opsForValue().set("url:$shortCode", nullSentinel, Duration.ofMinutes(5))
        } catch (ex: Exception) {
            log.warn("Redis null-sentinel write failed: ${ex.message}")
        }
    }

    fun evict(shortCode: String) {
        try {
            redis.delete("url:$shortCode")
            log.debug("Evicted cache for $shortCode")
        } catch (ex: Exception) {
            log.warn("Redis eviction failed for $shortCode: ${ex.message}")
        }
    }

    fun isNullSentinel(value: String?) = value == nullSentinel
}