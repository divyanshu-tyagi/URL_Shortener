package org.url_shortner.controller


import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.url_shortner.dto.AnalyticsResponse
import org.url_shortner.dto.ShortenRequest
import org.url_shortner.dto.ShortenResponse
import org.url_shortner.dto.UrlInfoResponse
import org.url_shortner.security.resolvedApiKey
import org.url_shortner.service.AnalyticsService
import org.url_shortner.service.UrlService


@RestController
@RequestMapping("/api/urls")
//@Tag(name = "URLs", description = "Create and manage short URLs")
class UrlController(
    private val urlService: UrlService,
    private val analyticsService: AnalyticsService
) {

    @PostMapping
//    @Operation(summary = "Shorten a URL")
    fun shorten(
        @Valid @RequestBody request: ShortenRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ShortenResponse> {
        val apiKey = httpRequest.resolvedApiKey()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(urlService.shorten(request, apiKey.id))
    }

    @GetMapping
//    @Operation(summary = "List all URLs created with this API key")
    fun list(httpRequest: HttpServletRequest): ResponseEntity<List<UrlInfoResponse>> {
        val apiKey = httpRequest.resolvedApiKey()
        return ResponseEntity.ok(urlService.listByApiKey(apiKey.id))
    }

    @DeleteMapping("/{shortCode}")
//    @Operation(summary = "Deactivate a short URL")
    fun delete(
        @PathVariable shortCode: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val apiKey = httpRequest.resolvedApiKey()
        urlService.deactivate(shortCode, apiKey.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{shortCode}/analytics")
//    @Operation(summary = "Get click analytics for a short URL")
    fun analytics(
        @PathVariable shortCode: String
    ): ResponseEntity<AnalyticsResponse> {
        return ResponseEntity.ok(analyticsService.getAnalytics(shortCode))
    }

    @GetMapping("/test-analytics")
    fun testAnalytics(): ResponseEntity<String> {
        return try {
            val result = analyticsService.getAnalytics("000005")
            ResponseEntity.ok("SUCCESS: $result")
        } catch (e: Exception) {
            ResponseEntity.status(500).body("ERROR: ${e::class.simpleName}: ${e.message}\n${e.stackTraceToString()}")
        }
    }
}