package org.url_shortner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "urls")
data class Url(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "short_code", unique = true, nullable = false)
    val shortCode: String = "",

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    val originalUrl: String = "",

    @Column(name = "api_key_id")
    val apiKeyId: Long? = null,

    @Column(name = "custom_alias")
    val customAlias: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true
)
