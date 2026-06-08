package org.url_shortner.service

import eu.bitwalker.useragentutils.UserAgent
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.url_shortner.model.Click
import org.url_shortner.model.DeviceType
import org.url_shortner.repository.ClickRepository


@Service
class ClickTrackingService(
    private val clickRepository: ClickRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    fun track(shortCode: String, request: HttpServletRequest) {
        try {
            val userAgentStr = request.getHeader("User-Agent") ?: ""
            val referrer     = request.getHeader("Referer")
            val ip           = extractIp(request)
            val deviceType   = parseDeviceType(userAgentStr)

            clickRepository.save(
                Click(
                    shortCode = shortCode,
                    ipAddress = ip,
                    deviceType = deviceType,
                    referrer = sanitizeReferrer(referrer),
                    userAgent = userAgentStr.take(500)
                )
            )
        } catch (ex: Exception) {
            log.error("Failed to track click for $shortCode: ${ex.message}")
        }
    }

    private fun extractIp(request: HttpServletRequest): String {
        // Respect reverse-proxy headers
        val forwardedFor = request.getHeader("X-Forwarded-For")
        return if (!forwardedFor.isNullOrBlank()) {
            forwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr ?: "unknown"
        }
    }

    private fun parseDeviceType(userAgentStr: String): DeviceType {
        if (userAgentStr.isBlank()) return DeviceType.UNKNOWN
        return try {
            val ua = UserAgent.parseUserAgentString(userAgentStr)
            when {
                ua.browser.browserType.name.contains("BOT", ignoreCase = true) -> DeviceType.BOT
                ua.operatingSystem.deviceType.name == "MOBILE"                 -> DeviceType.MOBILE
                ua.operatingSystem.deviceType.name == "TABLET"                 -> DeviceType.TABLET
                ua.operatingSystem.deviceType.name == "COMPUTER"               -> DeviceType.DESKTOP
                else                                                            -> DeviceType.UNKNOWN
            }
        } catch (ex: Exception) {
            DeviceType.UNKNOWN
        }
    }

    private fun sanitizeReferrer(referrer: String?): String? {
        if (referrer.isNullOrBlank()) return null
        return try {
            val url = java.net.URL(referrer)
            url.host
        } catch (ex: Exception) {
            referrer.take(200)
        }
    }
}