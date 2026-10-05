package com.example.data.repository

import com.example.data.local.AccessPassEntity
import com.example.data.local.CinemaDao
import com.example.data.local.CloudConfigEntity
import com.example.data.local.MovieEntity
import com.example.data.remote.CloudSyncResult
import com.example.data.remote.CloudSyncService
import com.example.data.remote.TelegramBotSyncResult
import com.example.data.remote.TelegramBotSyncService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class AuthValidationResult {
    data class Valid(val pass: AccessPassEntity, val remainingMillis: Long) : AuthValidationResult()
    data class Expired(val pass: AccessPassEntity, val expiredDateFormatted: String) : AuthValidationResult()
    data class Revoked(val pass: AccessPassEntity) : AuthValidationResult()
    data class Invalid(val message: String) : AuthValidationResult()
}

class CinemaRepository(
    private val dao: CinemaDao,
    private val cloudSyncService: CloudSyncService,
    private val telegramBotSyncService: TelegramBotSyncService = TelegramBotSyncService()
) {
    val moviesFlow: Flow<List<MovieEntity>> = dao.observeAllMovies()
    val accessPassesFlow: Flow<List<AccessPassEntity>> = dao.observeAllAccessPasses()
    val cloudConfigFlow: Flow<CloudConfigEntity?> = dao.observeCloudConfig()

    fun isTelegramTokenConfigured(): Boolean = telegramBotSyncService.isBotTokenConfigured()

    suspend fun ensureSeedData() {
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L
        val dayMs = 24L * hourMs

        if (dao.getAccessPassCount() == 0) {
            // Duration for 25 days and 12 hours (+ 50 mins so it displays 25 days & 12 hours cleanly)
            val duration25d12h = (25L * dayMs) + (12L * hourMs) + (50L * 60_000L)
            val duration30d = 30L * dayMs
            val duration7d = 7L * dayMs
            val duration15s = 15_000L

            val defaultPasses = listOf(
                AccessPassEntity(
                    code = "KF-25D12H-VIP",
                    planName = "KurdishFlim VIP Pass (25d 12h)",
                    holderName = "@KurdishFlim_4bot Member",
                    validityDurationMs = duration25d12h,
                    activatedAtEpochMillis = 0L,
                    expiryEpochMillis = now + duration25d12h,
                    maxResolution = "4K HDR10+ • KurdishSub",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                ),
                AccessPassEntity(
                    code = "KF-30D-4KPRO",
                    planName = "KurdishFlim 30-Day Monthly",
                    holderName = "@KurdishFlim_4bot Subscriber",
                    validityDurationMs = duration30d,
                    activatedAtEpochMillis = 0L,
                    expiryEpochMillis = now + duration30d,
                    maxResolution = "4K HDR10+ • Dolby Atmos",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                ),
                AccessPassEntity(
                    code = "KF-7D-WEEKLY",
                    planName = "KurdishFlim 7-Day Weekly",
                    holderName = "@KurdishFlim_4bot Member",
                    validityDurationMs = duration7d,
                    activatedAtEpochMillis = 0L,
                    expiryEpochMillis = now + duration7d,
                    maxResolution = "1080p Full HD",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                ),
                AccessPassEntity(
                    code = "KF-15S-LOCK",
                    planName = "15-Second Rapid Auto-Lock Test",
                    holderName = "Expiry Watchdog Tester",
                    validityDurationMs = duration15s,
                    activatedAtEpochMillis = 0L,
                    expiryEpochMillis = now + duration15s,
                    maxResolution = "4K Test • Auto-Locks in 15s",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                ),
                AccessPassEntity(
                    code = "ADMIN-MASTER-KEY",
                    planName = "Studio Director & Bot Admin",
                    holderName = "System Administrator",
                    validityDurationMs = 365L * dayMs,
                    activatedAtEpochMillis = 0L,
                    expiryEpochMillis = now + (365L * dayMs),
                    maxResolution = "8K Master • Cloud Write Access",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = true,
                    isActive = true
                ),
                AccessPassEntity(
                    code = "EXPIRED-2024",
                    planName = "Expired Telegram Bot Key",
                    holderName = "Past Subscriber",
                    validityDurationMs = 30L * dayMs,
                    activatedAtEpochMillis = now - (210L * dayMs),
                    expiryEpochMillis = now - (180L * dayMs),
                    maxResolution = "1080p HD",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                )
            )
            dao.upsertAccessPasses(defaultPasses)
        }

        if (dao.getCloudConfigOnce() == null) {
            dao.saveCloudConfig(
                CloudConfigEntity(
                    id = 1,
                    providerType = "FIREBASE_REST",
                    endpointUrl = "",
                    authPassEndpointUrl = "",
                    apiKeyOrToken = "",
                    autoSyncOnStartup = true,
                    lastSyncTimestamp = now,
                    lastSyncStatus = "Local Vault Active • Ready for Firebase / Supabase REST URL",
                    activeSessionCode = "",
                    telegramBotUsername = "@KurdishFlim_4bot",
                    lastTelegramSyncStatus = "Synced with @KurdishFlim_4bot time-based validation"
                )
            )
        }

        if (dao.getMovieCount() == 0) {
            val initialMovies = listOf(
                MovieEntity(
                    id = "mov_solaris_odyssey",
                    title = "Solaris Odyssey: Eclipse Protocol",
                    tagline = "Beyond the event horizon lies the last human memory.",
                    description = "When an orbital resonance station around a dying binary star intercepts a quantum signal from Earth's forgotten past, Commander Lyra Vance must pilot the deep-space vessel Aether-IX across the gravitational singularity before time collapses.",
                    category = "Sci-Fi",
                    rating = 9.4,
                    releaseYear = 2026,
                    durationMinutes = 148,
                    director = "Denis Vance-Korsakov",
                    castMembers = "Elena Rostova, Marcus Sterling, Kenji Takahashi, Aria Chen",
                    posterUrl = "drawable:img_hero_solaris",
                    backdropUrl = "drawable:img_hero_solaris",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    isTrending = true,
                    isFeatured = true,
                    audioFormat = "IMAX Enhanced • 4K HDR10+",
                    cloudSource = "Aether Cloud Vault"
                ),
                MovieEntity(
                    id = "mov_neon_valkyrie",
                    title = "Neon Valkyrie: 2099",
                    tagline = "In a city of synthetic memories, truth is the deadliest virus.",
                    description = "Down in the rain-drenched neon canyons of Neo-Veridia, cybernetic investigator Kaelen Vex hunts a rogue neural architect who is rewriting the memories of the city's elite syndicate leaders.",
                    category = "Cyberpunk",
                    rating = 9.1,
                    releaseYear = 2026,
                    durationMinutes = 132,
                    director = "Sora Nakashima",
                    castMembers = "Rina Hayakawa, Viktor Krow, Zoe Alvarez, Damon Locke",
                    posterUrl = "drawable:img_poster_cyber",
                    backdropUrl = "drawable:img_poster_cyber",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    isTrending = true,
                    isFeatured = true,
                    audioFormat = "Dolby Atmos • 4K UHD",
                    cloudSource = "Aether Cloud Vault"
                ),
                MovieEntity(
                    id = "mov_chronos_monolith",
                    title = "The Chronos Monolith",
                    tagline = "Every second you borrow costs a lifetime.",
                    description = "Inside a subterranean brutalist vault beneath Geneva, physicists unlock a temporal refraction chamber capable of folding causality—only to discover future versions of themselves trying to seal the chamber from the other side.",
                    category = "Thriller",
                    rating = 8.9,
                    releaseYear = 2025,
                    durationMinutes = 126,
                    director = "Julian Lindqvist",
                    castMembers = "Clara Moreau, Henrik Skarsgard, Dev Patel-Vance",
                    posterUrl = "drawable:img_poster_nebula",
                    backdropUrl = "drawable:img_poster_nebula",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    isTrending = true,
                    isFeatured = false,
                    audioFormat = "DTS:X • 4K HDR",
                    cloudSource = "Aether Cloud Vault"
                ),
                MovieEntity(
                    id = "mov_mariana_echo",
                    title = "The Mariana Echo",
                    tagline = "Seven miles down, something is answering our sonar.",
                    description = "An experimental deep-trench bathyscaphe descends into the Challenger Abyss to recover a downed satellite core, uncovering a colossal bioluminescent citadel that predates human civilization.",
                    category = "Mystery",
                    rating = 8.7,
                    releaseYear = 2026,
                    durationMinutes = 119,
                    director = "Camila Ortega",
                    castMembers = "Mateo Silva, Freya Lindholm, Idris Cole",
                    posterUrl = "drawable:img_poster_abyss",
                    backdropUrl = "drawable:img_poster_abyss",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    isTrending = true,
                    isFeatured = false,
                    audioFormat = "Dolby Atmos • 4K HDR",
                    cloudSource = "Aether Cloud Vault"
                ),
                MovieEntity(
                    id = "mov_apex_velocity",
                    title = "Apex Velocity: Hyperion",
                    tagline = "Zero brakes. Mach-2 ground effect.",
                    description = "High-stakes transcontinental mag-lev racers compete across the Saharan solar glass flats in an unsanctioned twilight grand prix where one millisecond separates glory from oblivion.",
                    category = "Action",
                    rating = 8.5,
                    releaseYear = 2025,
                    durationMinutes = 114,
                    director = "Marco Bellini",
                    castMembers = "Lucas Vance, Nadia Al-Mansoor, Tarek Zayd",
                    posterUrl = "drawable:img_poster_cyber",
                    backdropUrl = "drawable:img_hero_solaris",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    isTrending = false,
                    isFeatured = false,
                    audioFormat = "Dolby Digital 5.1 • 4K",
                    cloudSource = "Aether Cloud Vault"
                ),
                MovieEntity(
                    id = "mov_sylvan_legends",
                    title = "Horizon of the Astral Wilds",
                    tagline = "A mythic journey across floating worlds.",
                    description = "An acclaimed open-movie animated fable following a guardian spirit protecting an ancient forest sanctuary from mechanical sky-harvesters.",
                    category = "Animation",
                    rating = 8.8,
                    releaseYear = 2025,
                    durationMinutes = 102,
                    director = "Sacha Goedegebure",
                    castMembers = "Open Cinema Ensemble",
                    posterUrl = "drawable:img_poster_nebula",
                    backdropUrl = "drawable:img_poster_nebula",
                    videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    isTrending = false,
                    isFeatured = false,
                    audioFormat = "Stereo Mastering • 1080p",
                    cloudSource = "Aether Cloud Vault"
                )
            )
            dao.upsertMovies(initialMovies)
        }
    }

    /**
     * Synchronizes newly issued codes and revocations from @KurdishFlim_4bot via Telegram Bot API.
     */
    suspend fun syncWithTelegramBot(): TelegramBotSyncResult {
        val result = telegramBotSyncService.syncPassesFromTelegramBot()
        if (result.syncedPasses.isNotEmpty()) {
            // Preserve existing activation timestamps for codes already activated on this device
            for (remotePass in result.syncedPasses) {
                val existing = dao.findAccessPass(remotePass.code)
                if (existing != null && existing.activatedAtEpochMillis > 0L) {
                    dao.upsertAccessPass(
                        existing.copy(
                            isActive = remotePass.isActive,
                            botSource = result.botUsername
                        )
                    )
                } else {
                    dao.upsertAccessPass(remotePass)
                }
            }
        }
        val config = dao.getCloudConfigOnce() ?: CloudConfigEntity()
        dao.saveCloudConfig(
            config.copy(
                telegramBotUsername = result.botUsername,
                lastTelegramSyncStatus = result.statusMessage
            )
        )
        return result
    }

    /**
     * Validates a code generated by @KurdishFlim_4bot:
     * 1. Attempts live sync with @KurdishFlim_4bot & remote cloud endpoint.
     * 2. If the code is a valid structured @KurdishFlim_4bot token (e.g. `KF-30D-XXXX`, `KF-7D-XXXX`),
     *    reads its embedded validity duration on first activation and binds `expiryEpochMillis = now + validityDurationMs`.
     * 3. Strictly blocks any code whose `expiryEpochMillis <= System.currentTimeMillis()`.
     */
    suspend fun validateAccessCode(rawCode: String): AuthValidationResult {
        val code = rawCode.trim().uppercase(Locale.US)
        if (code.isEmpty()) {
            return AuthValidationResult.Invalid("تکایە کۆدی چوونەژوورەوە بنووسە • Please enter your @KurdishFlim_4bot access code.")
        }

        // Sync with Telegram Bot (@KurdishFlim_4bot) if token is configured
        if (telegramBotSyncService.isBotTokenConfigured()) {
            syncWithTelegramBot()
        }

        // Also check remote cloud endpoint if configured
        val cloudConfig = dao.getCloudConfigOnce()
        if (cloudConfig != null && cloudConfig.authPassEndpointUrl.isNotBlank()) {
            val remotePass = cloudSyncService.validateCodeRemotely(cloudConfig, code)
            if (remotePass != null) {
                val localExisting = dao.findAccessPass(code)
                if (localExisting == null) {
                    dao.upsertAccessPass(remotePass)
                }
            }
        }

        val now = System.currentTimeMillis()
        var pass = dao.findAccessPass(code)

        // If not in local DB yet, check if it matches @KurdishFlim_4bot self-contained duration format (e.g., KF-30D-XXXX, KF-7D-XXXX)
        if (pass == null) {
            val embeddedDurationMs = telegramBotSyncService.extractEmbeddedDurationMs(code)
            if (embeddedDurationMs != null && embeddedDurationMs > 0L) {
                val durationLabel = telegramBotSyncService.formatDurationShort(embeddedDurationMs)
                val newBotPass = AccessPassEntity(
                    code = code,
                    planName = "KurdishFlim Bot Pass ($durationLabel)",
                    holderName = "@KurdishFlim_4bot Subscriber",
                    validityDurationMs = embeddedDurationMs,
                    activatedAtEpochMillis = now,
                    expiryEpochMillis = now + embeddedDurationMs,
                    maxResolution = "4K HDR10+ • KurdishSub",
                    botSource = "@KurdishFlim_4bot",
                    isAdmin = false,
                    isActive = true
                )
                dao.upsertAccessPass(newBotPass)
                pass = newBotPass
            }
        }

        if (pass == null) {
            return AuthValidationResult.Invalid(
                "کۆدەکە هەڵەیە یان نەدۆزرایەوە (\"$code\"). تکایە کۆدێکی دروست لە @KurdishFlim_4bot وەربگرە."
            )
        }

        if (!pass.isActive) {
            return AuthValidationResult.Revoked(pass)
        }

        // If this pass has not been activated yet (activatedAtEpochMillis == 0L), bind its validity duration starting NOW
        if (pass.activatedAtEpochMillis == 0L && pass.expiryEpochMillis > now) {
            val boundExpiry = now + pass.validityDurationMs
            val activatedPass = pass.copy(
                activatedAtEpochMillis = now,
                expiryEpochMillis = boundExpiry
            )
            dao.upsertAccessPass(activatedPass)
            pass = activatedPass
        }

        // Strict Expiration Check
        if (pass.expiryEpochMillis <= now) {
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)
            val formattedDate = formatter.format(Date(pass.expiryEpochMillis))
            return AuthValidationResult.Expired(pass, formattedDate)
        }

        // Persist active session code in CloudConfigEntity
        val cfg = dao.getCloudConfigOnce() ?: CloudConfigEntity()
        dao.saveCloudConfig(cfg.copy(activeSessionCode = pass.code))

        val remainingMs = (pass.expiryEpochMillis - now).coerceAtLeast(0L)
        return AuthValidationResult.Valid(pass, remainingMs)
    }

    suspend fun getActiveSessionPass(): AccessPassEntity? {
        val cfg = dao.getCloudConfigOnce() ?: return null
        val activeCode = cfg.activeSessionCode.trim()
        if (activeCode.isEmpty()) return null
        val pass = dao.findAccessPass(activeCode) ?: return null
        if (!pass.isActive || pass.expiryEpochMillis <= System.currentTimeMillis()) {
            dao.saveCloudConfig(cfg.copy(activeSessionCode = ""))
            return null
        }
        return pass
    }

    suspend fun clearActiveSession() {
        val cfg = dao.getCloudConfigOnce() ?: return
        dao.saveCloudConfig(cfg.copy(activeSessionCode = ""))
    }

    /**
     * Allows testing the real-time countdown and automatic lock by setting the current
     * active pass to expire in [secondsFromNow] seconds.
     */
    suspend fun setPassExpiryInSeconds(code: String, secondsFromNow: Int): AccessPassEntity? {
        val pass = dao.findAccessPass(code) ?: return null
        val now = System.currentTimeMillis()
        val updated = pass.copy(
            activatedAtEpochMillis = if (pass.activatedAtEpochMillis > 0L) pass.activatedAtEpochMillis else now,
            expiryEpochMillis = now + (secondsFromNow * 1000L)
        )
        dao.upsertAccessPass(updated)
        return updated
    }

    /**
     * Resets a demo pass (like KF-15S-LOCK) so the user can test it again if desired.
     */
    suspend fun resetPassDuration(code: String, durationMs: Long): AccessPassEntity? {
        val pass = dao.findAccessPass(code) ?: return null
        val now = System.currentTimeMillis()
        val updated = pass.copy(
            validityDurationMs = durationMs,
            activatedAtEpochMillis = now,
            expiryEpochMillis = now + durationMs,
            isActive = true
        )
        dao.upsertAccessPass(updated)
        return updated
    }

    suspend fun saveMovie(movie: MovieEntity) {
        dao.upsertMovie(movie.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMovie(movieId: String) {
        dao.deleteMovieById(movieId)
    }

    suspend fun toggleWatchlist(movieId: String, currentStatus: Boolean) {
        dao.updateWatchlistStatus(movieId, !currentStatus)
    }

    suspend fun updatePlaybackPosition(movieId: String, positionMs: Long) {
        dao.updatePlaybackPosition(movieId, positionMs)
    }

    suspend fun saveAccessPass(pass: AccessPassEntity) {
        dao.upsertAccessPass(pass.copy(code = pass.code.trim().uppercase(Locale.US)))
    }

    suspend fun deleteAccessPass(code: String) {
        dao.deleteAccessPass(code)
    }

    suspend fun saveCloudConfig(config: CloudConfigEntity) {
        dao.saveCloudConfig(config)
    }

    suspend fun syncFromCloud(config: CloudConfigEntity): CloudSyncResult {
        val result = cloudSyncService.pullMoviesFromCloud(config)
        if (result.success && result.movies.isNotEmpty()) {
            dao.upsertMovies(result.movies)
        }
        dao.saveCloudConfig(
            config.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                lastSyncStatus = result.message
            )
        )
        return result
    }

    suspend fun pushToCloud(config: CloudConfigEntity): CloudSyncResult {
        val allMovies = dao.getAllMoviesOnce()
        val result = cloudSyncService.pushCatalogToCloud(config, allMovies)
        dao.saveCloudConfig(
            config.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                lastSyncStatus = result.message
            )
        )
        return result
    }
}
