package org.url_shortner.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.url_shortner.model.ApiKey

@Repository
interface ApiKeyRepository: JpaRepository<ApiKey, Long> {
    fun findByKeyHashAndIsActiveTrue(keyHash: String): ApiKey?
}