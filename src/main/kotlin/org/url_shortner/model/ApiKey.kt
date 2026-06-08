package org.url_shortner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "api_keys")
data class ApiKey(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "key_hash", unique = true, nullable = false)
    val keyHash: String = "",

    @Column(name = "key_prefix", nullable = false)
    val keyPrefix: String = "",

    @Column(name = "owner_name", nullable = false)
    val ownerName: String = "",

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)