package org.url_shortner.controller


import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.url_shortner.service.ClickTrackingService
import org.url_shortner.service.UrlService
import java.net.URI


@RestController
class RedirectController(
    private val urlService: UrlService,
    private val clickTrackingService: ClickTrackingService
) {

    @GetMapping("/{shortCode}")
    fun redirect(
        @PathVariable shortCode: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val originalUrl = urlService.resolve(shortCode)
            ?: return ResponseEntity.notFound().build()

        clickTrackingService.track(shortCode, request)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(originalUrl))
            .build()
    }
}