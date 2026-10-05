package com.example.ui.components

object KurdishTimeFormatter {

    private val westernToKurdishDigits = mapOf(
        '0' to '٠',
        '1' to '١',
        '2' to '٢',
        '3' to '٣',
        '4' to '٤',
        '5' to '٥',
        '6' to '٦',
        '7' to '٧',
        '8' to '٨',
        '9' to '٩'
    )

    fun toKurdishDigits(number: Long): String {
        return number.toString().map { ch -> westernToKurdishDigits[ch] ?: ch }.joinToString("")
    }

    fun toKurdishDigits(text: String): String {
        return text.map { ch -> westernToKurdishDigits[ch] ?: ch }.joinToString("")
    }

    /**
     * Formats remaining time in the exact requested Kurdish Sorani format:
     * «کاتی ماوەی کۆدەکەت: ٢٥ ڕۆژ و ١٢ کاتژمێر»
     */
    fun formatPrimaryKurdishCountdown(remainingMs: Long): String {
        if (remainingMs <= 0L) {
            return "«کاتی ماوەی کۆدەکەت: ٠ ڕۆژ و ٠ کاتژمێر (بەسەرچووە)»"
        }
        val totalSeconds = remainingMs / 1000L
        val days = totalSeconds / 86400L
        val hours = (totalSeconds % 86400L) / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        val daysKurdish = toKurdishDigits(days)
        val hoursKurdish = toKurdishDigits(hours)

        return if (days == 0L && hours == 0L) {
            val minK = toKurdishDigits(minutes)
            val secK = toKurdishDigits(seconds)
            "«کاتی ماوەی کۆدەکەت: ٠ ڕۆژ و ٠ کاتژمێر ($minK خولەک و $secK چرکە)»"
        } else {
            "«کاتی ماوەی کۆدەکەت: $daysKurdish ڕۆژ و $hoursKurdish کاتژمێر»"
        }
    }

    /**
     * Detailed real-time breakdown including days, hours, minutes, and seconds in Kurdish Sorani.
     */
    fun formatDetailedKurdishUnits(remainingMs: Long): KurdishCountdownBreakdown {
        val clamped = remainingMs.coerceAtLeast(0L)
        val totalSeconds = clamped / 1000L
        val days = totalSeconds / 86400L
        val hours = (totalSeconds % 86400L) / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return KurdishCountdownBreakdown(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            daysKurdish = toKurdishDigits(days),
            hoursKurdish = toKurdishDigits(hours),
            minutesKurdish = toKurdishDigits(minutes),
            secondsKurdish = toKurdishDigits(seconds)
        )
    }

    fun formatDurationLabelKurdish(durationMs: Long): String {
        val totalHours = (durationMs / 3600_000L).coerceAtLeast(0L)
        val days = totalHours / 24L
        val remHours = totalHours % 24L
        return when {
            days > 0L && remHours > 0L -> "${toKurdishDigits(days)} ڕۆژ و ${toKurdishDigits(remHours)} کاتژمێر"
            days > 0L -> "${toKurdishDigits(days)} ڕۆژ"
            totalHours > 0L -> "${toKurdishDigits(totalHours)} کاتژمێر"
            else -> "${toKurdishDigits((durationMs / 1000L).coerceAtLeast(1L))} چرکە"
        }
    }
}

data class KurdishCountdownBreakdown(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val daysKurdish: String,
    val hoursKurdish: String,
    val minutesKurdish: String,
    val secondsKurdish: String
)
