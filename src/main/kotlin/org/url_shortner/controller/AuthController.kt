package org.url_shortner.controller


import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.url_shortner.dto.ApiKeyResponse
import org.url_shortner.dto.CreateApiKeyRequest
import org.url_shortner.service.AnalyticsService
import org.url_shortner.service.ApiKeyService

@RestController
@RequestMapping("/api/auth")
//@Tag(name = "Auth", description = "API key management")
class AuthController(
    private val apiKeyService: ApiKeyService
) {

    @PostMapping("/keys")
//    @Operation(
//        summary = "Generate a new API key",
//        description = "The raw API key is returned ONCE. Store it securely — it cannot be retrieved again."
//    )
    fun createKey(
        @Valid @RequestBody request: CreateApiKeyRequest
    ): ResponseEntity<ApiKeyResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(apiKeyService.createApiKey(request.ownerName))
    }

}