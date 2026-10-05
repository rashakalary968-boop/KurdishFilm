package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.TelegramBotSyncService
import com.example.ui.components.KurdishTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AetherCinema", appName)
    }

    @Test
    fun `format 25 days and 12 hours in Kurdish Sorani`() {
        val durationMs = (25L * 24L * 3600_000L) + (12L * 3600_000L)
        val formatted = KurdishTimeFormatter.formatPrimaryKurdishCountdown(durationMs)
        assertEquals("«کاتی ماوەی کۆدەکەت: ٢٥ ڕۆژ و ١٢ کاتژمێر»", formatted)
    }

    @Test
    fun `extract duration from KurdishFlim bot codes`() {
        val service = TelegramBotSyncService()
        assertEquals(30L * 24L * 3600_000L, service.extractEmbeddedDurationMs("KF-30D-4KPRO"))
        assertEquals(7L * 24L * 3600_000L, service.extractEmbeddedDurationMs("KF-7D-WEEKLY"))
        assertEquals(
            (25L * 24L * 3600_000L) + (12L * 3600_000L),
            service.extractEmbeddedDurationMs("KF-25D12H-VIP")
        )
        assertEquals(15_000L, service.extractEmbeddedDurationMs("KF-15S-LOCK"))
    }
}
