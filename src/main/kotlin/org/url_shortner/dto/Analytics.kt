package org.url_shortner.dto


data class AnalyticsResponse(
    val shortCode: String,
    val totalClicks: Long,
    val clicksLast7Days: Long,
    val clicksLast30Days: Long,
    val clicksByDay: List<DailyClickCount>,
    val deviceBreakdown: Map<String, Long>,
    val topReferrers: List<ReferrerCount>,
    val peakHour: Int?
)

data class DailyClickCount(
    val date: String,
    val count: Long
)

data class ReferrerCount(
    val referrer: String,
    val count: Long
)