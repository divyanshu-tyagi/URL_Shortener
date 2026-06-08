package org.url_shortner.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class ShortenRequest(
    @field:NotBlank(message = "URL must not be blank")
    val url: String = "",

    @field:Size(min = 3, max = 20, message = "Alias must be 3–20 characters")
    val customAlias: String? = null,

    val expiryDays: Int? = null
)

data class CreateApiKeyRequest(
    @field:NotBlank(message = "Owner name must not be blank")
    val ownerName: String = ""
)
