package org.url_shortner.service


import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.url_shortner.dto.ShortenRequest
import org.url_shortner.dto.ShortenResponse
import org.url_shortner.dto.UrlInfoResponse
import org.url_shortner.model.Url
import org.url_shortner.repository.ClickRepository
import org.url_shortner.repository.UrlRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val clickRepository: ClickRepository,
    private val base62Encoder: Base62Encoder,
    private val cacheService: UrlCacheService,
    @Value("\${app.base-url}") private val baseUrl: String,
    @Value("\${app.url.default-expiry-days}") private val defaultExpiryDays: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun shorten(request: ShortenRequest, apiKeyId: Long): ShortenResponse {
        validateUrl(request.url)

        if (request.customAlias != null) {
            if (urlRepository.existsByShortCode(request.customAlias)) {
                throw IllegalArgumentException("Alias '${request.customAlias}' is already taken")
            }
            val url = urlRepository.save(
                Url(
                    shortCode    = request.customAlias,
                    originalUrl  = request.url,
                    apiKeyId     = apiKeyId,
                    customAlias  = true,
                    expiresAt    = computeExpiry(request.expiryDays)
                )
            )
            return toResponse(url)
        }

        val temp = urlRepository.saveAndFlush(
            Url(
                shortCode   = "tmp",
                originalUrl = request.url,
                apiKeyId    = apiKeyId,
                expiresAt   = computeExpiry(request.expiryDays)
            )
        )

        val shortCode = base62Encoder.encode(temp.id)
        val final = urlRepository.save(temp.copy(shortCode = shortCode))

        log.info("Created: $shortCode → ${request.url.take(60)}")
        return toResponse(final)
    }

    @Transactional(readOnly = true)
    fun resolve(shortCode: String): String? {

        val cached = cacheService.get(shortCode)
        if (cached != null) {
            if (cacheService.isNullSentinel(cached)) return null
            return cached
        }

        val url = urlRepository.findByShortCodeAndIsActiveTrue(shortCode)

        if (url == null || isExpired(url)) {
            cacheService.setNull(shortCode)
            return null
        }

        cacheService.set(shortCode, url.originalUrl)
        return url.originalUrl
    }

    @Transactional
    fun deactivate(shortCode: String, apiKeyId: Long) {
        val url = urlRepository.findByShortCodeAndIsActiveTrue(shortCode)
            ?: throw NoSuchElementException("Short URL '$shortCode' not found")

        if (url.apiKeyId != apiKeyId) {
            throw SecurityException("You do not own this short URL")
        }

        urlRepository.save(url.copy(isActive = false))
        cacheService.evict(shortCode)
        log.info("Deactivated short URL: $shortCode")
    }

    @Transactional(readOnly = true)
    fun listByApiKey(apiKeyId: Long): List<UrlInfoResponse> {
        return urlRepository.findByApiKeyIdOrderByCreatedAtDesc(apiKeyId).map { url ->
            UrlInfoResponse(
                shortCode    = url.shortCode,
                shortUrl     = "$baseUrl/${url.shortCode}",
                originalUrl  = url.originalUrl,
                createdAt    = url.createdAt,
                expiresAt    = url.expiresAt,
                totalClicks  = clickRepository.countByShortCode(url.shortCode)
            )
        }
    }


    private fun computeExpiry(expiryDays: Int?): Instant {
        val days = expiryDays?.toLong() ?: defaultExpiryDays
        return Instant.now().plus(days, ChronoUnit.DAYS)
    }

    private fun isExpired(url: Url): Boolean {
        return url.expiresAt != null && Instant.now().isAfter(url.expiresAt)
    }

    private fun toResponse(url: Url) = ShortenResponse(
        shortCode    = url.shortCode,
        shortUrl     = "$baseUrl/${url.shortCode}",
        originalUrl  = url.originalUrl,
        createdAt    = url.createdAt,
        expiresAt    = url.expiresAt
    )

    private fun validateUrl(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("URL must start with http:// or https://")
        }
        if (url.length > 2048) {
            throw IllegalArgumentException("URL is too long (max 2048 characters)")
        }
    }
}