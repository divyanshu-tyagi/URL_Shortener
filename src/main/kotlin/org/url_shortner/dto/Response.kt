package org.url_shortner.dto

import java.time.Instant


data class ShortenResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val expiresAt: Instant?,
    val createdAt: Instant
)

data class ApiKeyResponse(
    val apiKey: String,
    val keyPrefix: String,
    val ownerName: String,
    val createdAt: Instant
)

data class UrlInfoResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val totalClicks: Long
)