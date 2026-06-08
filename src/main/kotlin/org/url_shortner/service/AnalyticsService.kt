package org.url_shortner.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.url_shortner.dto.AnalyticsResponse
import org.url_shortner.dto.DailyClickCount
import org.url_shortner.dto.ReferrerCount
import org.url_shortner.repository.ClickRepository
import org.url_shortner.repository.UrlRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AnalyticsService(
    private val clickRepository: ClickRepository,
    private val urlRepository: UrlRepository
) {

    @Transactional(readOnly = true)
    fun getAnalytics(shortCode: String): AnalyticsResponse {
        urlRepository.findByShortCodeAndIsActiveTrue(shortCode)
            ?: throw NoSuchElementException("Short URL '$shortCode' not found")

        val now        = Instant.now()
        val since7d    = now.minus(7,  ChronoUnit.DAYS)
        val since30d   = now.minus(30, ChronoUnit.DAYS)

        val totalClicks     = clickRepository.countByShortCode(shortCode)
        val clicksLast7Days = clickRepository.countByShortCodeAndClickedAtAfter(shortCode, since7d)
        val clicksLast30Days= clickRepository.countByShortCodeAndClickedAtAfter(shortCode, since30d)

        val clicksByDay = clickRepository.clicksByDay(shortCode, since30d).map { row ->
            val r = row as Array<Any>
            DailyClickCount(date = row[0] as String, count = (row[1] as Number).toLong())
        }

        val deviceBreakdown = clickRepository.deviceBreakdown(shortCode)
            .associate { row ->
                val r = row as Array<Any>
                (row[0] as String) to (row[1] as Number).toLong()
            }

        val topReferrers = clickRepository.topReferrers(shortCode).map { row ->
            val r = row as Array<Any>
            ReferrerCount(referrer = row[0] as String, count = (row[1] as Number).toLong())
        }

        val peakHour = try {
            (clickRepository.peakHour(shortCode) as? Array<Any>)
                ?.let { row -> (row[0] as Number).toInt() }
        } catch (e: Exception) { null }

        return AnalyticsResponse(
            shortCode = shortCode,
            totalClicks = totalClicks,
            clicksLast7Days = clicksLast7Days,
            clicksLast30Days = clicksLast30Days,
            clicksByDay = clicksByDay,
            deviceBreakdown = deviceBreakdown,
            topReferrers = topReferrers,
            peakHour = peakHour
        )
    }
}