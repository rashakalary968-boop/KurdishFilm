package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "movies")
@JsonClass(generateAdapter = true)
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val category: String,
    val rating: Double,
    val releaseYear: Int,
    val durationMinutes: Int,
    val director: String,
    val castMembers: String,
    val posterUrl: String,
    val backdropUrl: String,
    val videoStreamUrl: String,
    val isTrending: Boolean = false,
    val isFeatured: Boolean = false,
    val isWatchlisted: Boolean = false,
    val lastPlaybackPositionMs: Long = 0L,
    val audioFormat: String = "Dolby Atmos • 4K HDR",
    val cloudSource: String = "Aether Cloud Vault",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "access_passes")
@JsonClass(generateAdapter = true)
data class AccessPassEntity(
    @PrimaryKey val code: String,
    val planName: String,
    val holderName: String,
    val validityDurationMs: Long = 30L * 24 * 3600 * 1000L,
    val activatedAtEpochMillis: Long = 0L,
    val expiryEpochMillis: Long,
    val maxResolution: String = "4K HDR10+",
    val botSource: String = "@KurdishFlim_4bot",
    val isAdmin: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "cloud_config")
data class CloudConfigEntity(
    @PrimaryKey val id: Int = 1,
    val providerType: String = "FIREBASE_REST", // FIREBASE_REST, SUPABASE_REST, CUSTOM_JSON
    val endpointUrl: String = "",
    val authPassEndpointUrl: String = "",
    val apiKeyOrToken: String = "",
    val autoSyncOnStartup: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val lastSyncStatus: String = "Local Cloud Ready • Configure Firebase/Supabase URL to sync externally",
    val activeSessionCode: String = "",
    val telegramBotUsername: String = "@KurdishFlim_4bot",
    val lastTelegramUpdateId: Long = 0L,
    val lastTelegramSyncStatus: String = "Connected to @KurdishFlim_4bot • Time-based validation active"
)
