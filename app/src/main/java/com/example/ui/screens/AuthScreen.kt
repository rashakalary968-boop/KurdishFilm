package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccessPassEntity
import com.example.ui.components.GlowingBadge
import com.example.ui.components.KurdishTimeFormatter
import com.example.ui.theme.AuroraViolet
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
import com.example.ui.viewmodel.AuthFeedback

@Composable
fun AuthScreen(
    accessPasses: List<AccessPassEntity>,
    isVerifying: Boolean,
    isSyncingTelegram: Boolean,
    authFeedback: AuthFeedback?,
    onValidateCode: (String) -> Unit,
    onSyncTelegramBot: () -> Unit,
    onResetDemoLockPass: () -> Unit,
    onClearFeedback: () -> Unit
) {
    var enteredCode by rememberSaveable { mutableStateOf("KF-25D12H-VIP") }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_solaris),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianVoid.copy(alpha = 0.45f),
                            ObsidianVoid.copy(alpha = 0.88f),
                            ObsidianVoid,
                            ObsidianVoid
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricCyan, CrimsonPulse)
                        )
                    )
                    .padding(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            GlowingBadge(
                text = "TELEGRAM BOT VERIFIED • @KurdishFlim_4bot",
                accentColor = ElectricCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.displayMedium,
                color = CinemaWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "کۆدی وەرگیراو لە بۆتی @KurdishFlim_4bot داخڵ بکە بۆ کردنەوەی بەرنامەکە",
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaSilver,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                color = ObsidianSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.55f),
                            ObsidianBorder,
                            CrimsonPulse.copy(alpha = 0.45f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "@KurdishFlim_4bot CODE",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }

                        TextButton(
                            onClick = onSyncTelegramBot,
                            enabled = !isSyncingTelegram
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Bot",
                                tint = EmeraldMatrix,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncingTelegram) "Syncing..." else "Sync Bot",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldMatrix
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = {
                            enteredCode = it.uppercase()
                            if (authFeedback != null) onClearFeedback()
                        },
                        label = { Text("کۆدی چالاککردن • Access Code") },
                        placeholder = { Text("e.g. KF-30D-8821 or KF-25D12H-VIP") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.4.sp,
                            color = CinemaWhite
                        ),
                        trailingIcon = {
                            if (enteredCode.isNotEmpty()) {
                                IconButton(onClick = {
                                    enteredCode = ""
                                    onClearFeedback()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear access code",
                                        tint = CinemaSilver
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onValidateCode(enteredCode) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = CinemaSilver,
                            cursorColor = ElectricCyan,
                            focusedContainerColor = ObsidianCard,
                            unfocusedContainerColor = ObsidianCard
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("access_code_input")
                    )

                    AnimatedVisibility(
                        visible = authFeedback != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        authFeedback?.let { feedback ->
                            Surface(
                                color = CrimsonPulse.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CrimsonPulse.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .testTag("auth_error_banner")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = CrimsonPulse,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = feedback.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CrimsonPulse,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = feedback.detail,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CinemaWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onValidateCode(enteredCode) },
                        enabled = !isVerifying,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianVoid
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("validate_code_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                color = ObsidianVoid,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "پشتڕاستکردنەوەی کۆد...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.validate_button).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "@KurdishFlim_4bot Time-Based Passes",
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaWhite
                    )
                    TextButton(onClick = onResetDemoLockPass) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset 15s Demo",
                            tint = SolarGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset 15s Test",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val now = System.currentTimeMillis()

                accessPasses.forEach { pass ->
                    val isExpired = pass.expiryEpochMillis <= now || !pass.isActive
                    val accent = when {
                        isExpired -> CrimsonPulse
                        pass.code == "KF-15S-LOCK" -> SolarGold
                        pass.isAdmin -> AuroraViolet
                        else -> EmeraldMatrix
                    }
                    val durationKurdish = KurdishTimeFormatter.formatDurationLabelKurdish(pass.validityDurationMs)
                    val statusLabel = when {
                        !pass.isActive -> "REVOKED"
                        isExpired -> "بەسەرچووە • EXPIRED"
                        else -> "ماوە: $durationKurdish"
                    }

                    Surface(
                        color = if (enteredCode.equals(pass.code, ignoreCase = true)) {
                            ObsidianElevated
                        } else {
                            ObsidianCard.copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (enteredCode.equals(pass.code, ignoreCase = true)) accent
                            else ObsidianBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                enteredCode = pass.code
                                onValidateCode(pass.code)
                            }
                            .testTag("quick_pass_${pass.code}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.16f))
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isExpired -> Icons.Default.Schedule
                                            pass.isAdmin -> Icons.Default.AdminPanelSettings
                                            else -> Icons.Default.VerifiedUser
                                        },
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = pass.code,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = CinemaWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${pass.planName} • ${pass.botSource}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaSilver
                                    )
                                }
                            }

                            GlowingBadge(
                                text = statusLabel,
                                accentColor = accent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
