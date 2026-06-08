package org.url_shortner


import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.url_shortner.service.Base62Encoder
import org.url_shortner.service.RateLimiter

class Base62EncoderTest {

    private val encoder = Base62Encoder()

    @Test
    fun `encode produces 6-char minimum code`() {
        val code = encoder.encode(1L)
        assertTrue(code.length >= 6, "Code should be at least 6 chars: $code")
    }

    @Test
    fun `encode is deterministic`() {
        assertEquals(encoder.encode(42L), encoder.encode(42L))
    }

    @Test
    fun `encode produces unique codes for different IDs`() {
        val codes = (1L..1000L).map { encoder.encode(it) }.toSet()
        assertEquals(1000, codes.size, "All codes should be unique")
    }

    @Test
    fun `encode only uses URL-safe characters`() {
        val validChars = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        (1L..100L).forEach { id ->
            encoder.encode(id).forEach { ch ->
                assertTrue(ch in validChars, "Char '$ch' is not URL-safe")
            }
        }
    }

    @Test
    fun `encode throws for non-positive IDs`() {
        assertThrows<IllegalArgumentException> { encoder.encode(0L) }
        assertThrows<IllegalArgumentException> { encoder.encode(-1L) }
    }
}

class RateLimiterTest {

    private val redis: StringRedisTemplate = mockk()
    private val ops: ValueOperations<String, String> = mockk()

    init {
        every { redis.opsForValue() } returns ops
        every { redis.expire(any(), any()) } returns true
    }

    @Test
    fun `allows request when under limit`() {
        every { ops.get(any()) } returns "3"          // prev window: 3 requests
        every { ops.increment(any()) } returns 1L      // current: first request

        val limiter = RateLimiter(redis, limit = 10)
        val result = limiter.check("test_key")

        assertTrue(result.allowed)
        assertTrue(result.remaining >= 0)
    }

    @Test
    fun `rejects request when over limit`() {
        every { ops.get(any()) } returns "10"          // already at limit
        every { ops.increment(any()) } returns 11L

        val limiter = RateLimiter(redis, limit = 10)
        val result = limiter.check("test_key")

        assertFalse(result.allowed)
        assertEquals(0, result.remaining)
    }

    @Test
    fun `fails open when Redis is unavailable`() {
        every { ops.get(any()) } throws RuntimeException("Redis connection refused")

        val limiter = RateLimiter(redis, limit = 10)
        val result = limiter.check("test_key")

        // Must allow request — never let Redis outage block users
        assertTrue(result.allowed, "Should fail open when Redis is down")
    }
}