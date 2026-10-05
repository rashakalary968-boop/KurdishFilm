package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccessPassEntity
import com.example.data.local.CloudConfigEntity
import com.example.ui.components.GlowingBadge
import com.example.ui.components.KurdishTimeFormatter
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.CinemaMuted
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMatrix
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    activePass: AccessPassEntity?,
    remainingMillis: Long,
    cloudConfig: CloudConfigEntity?,
    isTelegramTokenConfigured: Boolean,
    isSyncingTelegram: Boolean,
    onSyncTelegramBot: () -> Unit,
    onSimulateRapidExpiry: (Int) -> Unit,
    onSignOutAndLock: () -> Unit,
    onBackToHome: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onBackToHome() }

    val primaryKurdishText = KurdishTimeFormatter.formatPrimaryKurdishCountdown(remainingMillis)
    val breakdown = KurdishTimeFormatter.formatDetailedKurdishUnits(remainingMillis)
    val totalValidityMs = (activePass?.validityDurationMs ?: (30L * 86400_000L)).coerceAtLeast(1000L)
    val progressFraction = (remainingMillis.toFloat() / totalValidityMs.toFloat()).coerceIn(0f, 1f)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // 1. Header
        item {
            Column {
                GlowingBadge(
                    text = "ڕێکخستنەکان • SUBSCRIPTION & BOT SETTINGS",
                    accentColor = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ڕێکخستنەکان و ماوەی بەشداریکردن",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CinemaWhite,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Real-time @KurdishFlim_4bot code countdown & automatic security lock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaSilver
                )
            }
        }

        // 2. Hero Real-Time Kurdish Countdown Card
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricCyan, AuroraViolet, CrimsonPulse)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_countdown_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "ماوەی کارابوونی کۆد • ACTIVE CODE TIMER",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Kurdish Sorani Countdown Banner (exact requested phrasing)
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kurdish_countdown_banner")
                    ) {
                        Text(
                            text = primaryKurdishText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 19.sp,
                                lineHeight = 28.sp
                            ),
                            color = CinemaWhite,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Box Live Ticking Units (ڕۆژ / کاتژمێر / خولەک / چرکە)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CountdownUnitBox(
                            valueKurdish = breakdown.daysKurdish,
                            labelKurdish = "ڕۆژ (Days)",
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitBox(
                            valueKurdish = breakdown.hoursKurdish,
                            labelKurdish = "کاتژمێر (Hrs)",
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitBox(
                            valueKurdish = breakdown.minutesKurdish,
                            labelKurdish = "خولەک (Min)",
                            accentColor = AuroraViolet,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitBox(
                            valueKurdish = breakdown.secondsKurdish,
                            labelKurdish = "چرکە (Sec)",
                            accentColor = if (breakdown.days == 0L && breakdown.hours == 0L) CrimsonPulse else SolarGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Remaining Validity Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ماوەی گشتی کۆد: ${KurdishTimeFormatter.formatDurationLabelKurdish(totalValidityMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaSilver
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}% ماوە",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = if (progressFraction < 0.15f) CrimsonPulse else ElectricCyan,
                            trackColor = ObsidianElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    if (activePass != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = ObsidianBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        MetadataRow(
                            label = "کۆدی چالاک (Active Code)",
                            value = activePass.code,
                            valueColor = ElectricCyan
                        )
                        MetadataRow(
                            label = "سەرچاوەی بۆت (Bot Source)",
                            value = activePass.botSource,
                            valueColor = EmeraldMatrix
                        )
                        if (activePass.activatedAtEpochMillis > 0L) {
                            MetadataRow(
                                label = "کاتی چالاککردن (Activated At)",
                                value = dateFormat.format(Date(activePass.activatedAtEpochMillis)),
                                valueColor = CinemaSilver
                            )
                        }
                        MetadataRow(
                            label = "کاتی بەسەرچوون (Expires At)",
                            value = dateFormat.format(Date(activePass.expiryEpochMillis)),
                            valueColor = SolarGold
                        )
                    }
                }
            }
        }

        // 3. @KurdishFlim_4bot Live Synchronization & Auto-Lock Testing Card
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldMatrix,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "پەیوەندی بۆتی تێلێگرام • @KurdishFlim_4bot SYNC",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldMatrix
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = cloudConfig?.lastTelegramSyncStatus
                            ?: "Connected to @KurdishFlim_4bot time-based validation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isTelegramTokenConfigured) {
                            "TELEGRAM_BOT_TOKEN is active via BuildConfig. New codes sent to @KurdishFlim_4bot are synced automatically."
                        } else {
                            "Tip: Set TELEGRAM_BOT_TOKEN in the AI Studio Secrets panel for live polling from @KurdishFlim_4bot."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaSilver
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSyncTelegramBot,
                            enabled = !isSyncingTelegram,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_telegram_bot_button")
                        ) {
                            if (isSyncingTelegram) {
                                CircularProgressIndicator(
                                    color = ObsidianVoid,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYNC @KurdishFlim_4bot",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = { onSimulateRapidExpiry(10) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CrimsonPulse),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_autolock_10s_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CrimsonPulse,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TEST 10s AUTO-LOCK",
                                color = CrimsonPulse,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Secure Integration Architecture & Protocol Specification Card
        item {
            Surface(
                color = ObsidianCard.copy(alpha = 0.85f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, AuroraViolet.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AuroraViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SECURE @KurdishFlim_4bot INTEGRATION LOGIC",
                            style = MaterialTheme.typography.labelLarge,
                            color = AuroraViolet
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SecurityProtocolStep(
                        stepNumber = "1",
                        title = "Telegram Bot Code Issuance (@KurdishFlim_4bot)",
                        description = "Admin sends `/gen <CODE> <DURATION>` (e.g., `/gen KF-30D-9921 30d` or `/gen KF-7D-VIP1 7d`) in @KurdishFlim_4bot, or generates structured duration keys (`KF-30D-XXXX`, `KF-7D-XXXX`, `KF-25D12H-XXXX`)."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityProtocolStep(
                        stepNumber = "2",
                        title = "First-Activation Timestamp Binding",
                        description = "When the user enters the code, the app reads `validityDurationMs`, binds `activatedAtEpochMillis = System.currentTimeMillis()`, and locks `expiryEpochMillis = activatedAt + validityDurationMs` in Room."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityProtocolStep(
                        stepNumber = "3",
                        title = "1-Second Watchdog & Strict Expiry Lock",
                        description = "A background coroutine checks `remainingMs = expiryEpochMillis - now` every 1,000ms. When `remainingMs <= 0`, playback stops immediately, the session is wiped, and the expired key is permanently blocked from re-entry."
                    )
                }
            }
        }

        // 5. Lock & Switch Code Action
        item {
            OutlinedButton(
                onClick = onSignOutAndLock,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrimsonPulse.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ObsidianSurface,
                    contentColor = CrimsonPulse
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_lock_now_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "قفڵکردنی بەرنامە و گۆڕینی کۆد • LOCK & SWITCH CODE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CountdownUnitBox(
    valueKurdish: String,
    labelKurdish: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text = valueKurdish,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = labelKurdish,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaSilver,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CinemaMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecurityProtocolStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AuroraViolet.copy(alpha = 0.2f))
                .border(1.dp, AuroraViolet, CircleShape)
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall,
                color = AuroraViolet,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CinemaWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaSilver
            )
        }
    }
}
