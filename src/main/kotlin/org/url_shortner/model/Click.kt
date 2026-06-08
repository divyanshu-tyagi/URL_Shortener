package org.url_shortner.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "clicks")
data class Click(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "short_code", nullable = false)
    val shortCode: String = "",

    @Column(name = "clicked_at")
    val clickedAt: Instant = Instant.now(),

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    val deviceType: DeviceType = DeviceType.UNKNOWN,

    @Column(name = "referrer", columnDefinition = "TEXT")
    val referrer: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null
)

enum class DeviceType {
    MOBILE, DESKTOP, TABLET, BOT, UNKNOWN
}