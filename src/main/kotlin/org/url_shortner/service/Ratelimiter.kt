package org.url_shortner.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

data class RateLimitResult(
    val allowed: Boolean,
    val remaining: Int,
    val resetAtEpochSecond: Long
)


@Component
class RateLimiter(
    private val redis: StringRedisTemplate,
    @Value("\${app.rate-limit.requests-per-minute}") private val limit: Int
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun check(apiKey: String): RateLimitResult {
        return try {
            checkInternal(apiKey)
        } catch (ex: Exception) {
            log.warn("Redis unavailable for rate limiting, failing open: ${ex.message}")
            RateLimitResult(allowed = true, remaining = limit, resetAtEpochSecond = nextMinuteBucket())
        }
    }

    private fun checkInternal(apiKey: String): RateLimitResult {
        val now = Instant.now()
        val currentBucket = now.epochSecond / 60
        val prevBucket = currentBucket - 1
        val secondsIntoCurrentMinute = now.epochSecond % 60
        val overlapFraction = (60 - secondsIntoCurrentMinute) / 60.0

        val currentKey = "rate:$apiKey:$currentBucket"
        val prevKey    = "rate:$apiKey:$prevBucket"

        val currentCount = redis.opsForValue().get(currentKey)?.toLong() ?: 0L
        val prevCount    = redis.opsForValue().get(prevKey)?.toLong() ?: 0L

        val weight = (prevCount * overlapFraction + currentCount).toLong()

        return if (weight >= limit) {
            val resetAt = (currentBucket + 1) * 60
            RateLimitResult(allowed = false, remaining = 0, resetAtEpochSecond = resetAt)
        } else {
            val ops = redis.opsForValue()
            val newCount = ops.increment(currentKey) ?: 1L
            if (newCount == 1L) {
                redis.expire(currentKey, Duration.ofSeconds(120))
            }
            val remaining = (limit - weight - 1).coerceAtLeast(0).toInt()
            val resetAt = (currentBucket + 1) * 60
            RateLimitResult(allowed = true, remaining = remaining, resetAtEpochSecond = resetAt)
        }
    }

    private fun nextMinuteBucket(): Long {
        val currentBucket = Instant.now().epochSecond / 60
        return (currentBucket + 1) * 60
    }
}