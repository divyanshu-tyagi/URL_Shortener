package org.url_shortner.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.url_shortner.model.Click
import java.time.Instant


@Repository
interface ClickRepository : JpaRepository<Click, Long> {

    fun countByShortCode(shortCode: String): Long

    fun countByShortCodeAndClickedAtAfter(shortCode: String, after: Instant): Long

    @Query("""
        SELECT TO_CHAR(c.clicked_at, 'YYYY-MM-DD') as date, COUNT(*) as count
        FROM clicks c
        WHERE c.short_code = :shortCode
          AND c.clicked_at >= :since
        GROUP BY TO_CHAR(c.clicked_at, 'YYYY-MM-DD')
        ORDER BY date ASC
    """, nativeQuery = true)
    fun clicksByDay(
        @Param("shortCode") shortCode: String,
        @Param("since") since: Instant
    ): List<Any>

    @Query("""
        SELECT c.device_type, COUNT(*) as count
        FROM clicks c
        WHERE c.short_code = :shortCode
        GROUP BY c.device_type
    """, nativeQuery = true)
    fun deviceBreakdown(@Param("shortCode") shortCode: String): List<Any>

    @Query("""
        SELECT COALESCE(c.referrer, 'Direct') as referrer, COUNT(*) as count
        FROM clicks c
        WHERE c.short_code = :shortCode
        GROUP BY COALESCE(c.referrer, 'Direct')
        ORDER BY count DESC
        LIMIT 10
    """, nativeQuery = true)
    fun topReferrers(@Param("shortCode") shortCode: String): List<Any>

    @Query("""
        SELECT EXTRACT(HOUR FROM c.clicked_at)::INT as hour, COUNT(*) as count
        FROM clicks c
        WHERE c.short_code = :shortCode
        GROUP BY EXTRACT(HOUR FROM c.clicked_at)
        ORDER BY count DESC
        LIMIT 1
    """, nativeQuery = true)
    fun peakHour(@Param("shortCode") shortCode: String): Any?
}