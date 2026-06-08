package org.url_shortner.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.url_shortner.model.Url

@Repository
interface UrlRepository: JpaRepository<Url, Long> {
    fun findByShortCodeAndIsActiveTrue(shortCode: String): Url?
    fun findByApiKeyIdOrderByCreatedAtDesc(apiKeyId: Long): List<Url>
    fun existsByShortCode(shortCode: String): Boolean
}