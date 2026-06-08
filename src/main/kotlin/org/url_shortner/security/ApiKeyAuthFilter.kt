package org.url_shortner.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.url_shortner.model.ApiKey
import org.url_shortner.service.ApiKeyService
import org.url_shortner.service.RateLimiter

const val API_KEY_ATTRIBUTE = "resolvedApiKey"

@Component
class ApiKeyAuthFilter(
    private val apiKeyService: ApiKeyService,
    private val rateLimiter: RateLimiter
) : OncePerRequestFilter() {

// Only apply to protected routes
private val protectedPrefixes = listOf("/api/urls")

override fun shouldNotFilter(request: HttpServletRequest): Boolean {
val path = request.requestURI
return protectedPrefixes.none { path.startsWith(it) }
}

override fun doFilterInternal(
request: HttpServletRequest,
response: HttpServletResponse,
chain: FilterChain
) {
val rawKey = request.getHeader("X-API-Key")

if (rawKey.isNullOrBlank()) {
response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-API-Key header")
return
}

val apiKey = apiKeyService.validate(rawKey)
if (apiKey == null) {
response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or inactive API key")
return
}

val result = rateLimiter.check(rawKey)
response.setHeader("X-RateLimit-Limit",     rateLimiter.toString())
response.setHeader("X-RateLimit-Remaining", result.remaining.toString())
response.setHeader("X-RateLimit-Reset",     result.resetAtEpochSecond.toString())

if (!result.allowed) {
response.status = 429
response.setHeader("Retry-After", result.resetAtEpochSecond.toString())
response.writer.write("""{"error":"Too Many Requests","message":"Rate limit exceeded. Try again after ${result.resetAtEpochSecond}"}""")
return
}

request.setAttribute(API_KEY_ATTRIBUTE, apiKey)
chain.doFilter(request, response)
}
}

fun HttpServletRequest.resolvedApiKey(): ApiKey =
getAttribute(API_KEY_ATTRIBUTE) as? ApiKey
?: throw IllegalStateException("ApiKey not found in request — filter not applied?")