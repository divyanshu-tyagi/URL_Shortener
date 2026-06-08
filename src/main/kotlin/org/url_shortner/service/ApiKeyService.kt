package org.url_shortner.service


import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.url_shortner.dto.ApiKeyResponse
import org.url_shortner.model.ApiKey
import org.url_shortner.repository.ApiKeyRepository
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Service
class ApiKeyService(private val apiKeyRepository: ApiKeyRepository) {

    private val secureRandom = SecureRandom()

    @Transactional
    fun createApiKey(ownerName: String): ApiKeyResponse {
        val rawKey    = generateRawKey()
        val keyHash   = sha256(rawKey)
        val keyPrefix = rawKey.take(8)

        val saved = apiKeyRepository.save(
            ApiKey(
                keyHash = keyHash,
                keyPrefix = keyPrefix,
                ownerName = ownerName
            )
        )

        return ApiKeyResponse(
            apiKey    = rawKey,
            keyPrefix = keyPrefix,
            ownerName = ownerName,
            createdAt = saved.createdAt
        )
    }

    fun validate(rawKey: String): ApiKey? {
        val hash = sha256(rawKey)
        return apiKeyRepository.findByKeyHashAndIsActiveTrue(hash)
    }


    private fun generateRawKey(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return "sk_$encoded"
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash   = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}