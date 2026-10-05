package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccessPassEntity
import com.example.data.local.CinemaDatabase
import com.example.data.local.CloudConfigEntity
import com.example.data.local.MovieEntity
import com.example.data.remote.CloudSyncService
import com.example.data.repository.AuthValidationResult
import com.example.data.repository.CinemaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class CinemaTab {
    HOME,
    WATCHLIST,
    ADMIN_STUDIO,
    SETTINGS
}

data class AuthFeedback(
    val isError: Boolean,
    val title: String,
    val detail: String
)

class CinemaViewModel(
    private val repository: CinemaRepository
) : ViewModel() {

    val movies: StateFlow<List<MovieEntity>> = repository.moviesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val accessPasses: StateFlow<List<AccessPassEntity>> = repository.accessPassesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cloudConfig: StateFlow<CloudConfigEntity?> = repository.cloudConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _activePass = MutableStateFlow<AccessPassEntity?>(null)
    val activePass: StateFlow<AccessPassEntity?> = _activePass.asStateFlow()

    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private val _daysRemaining = MutableStateFlow(0)
    val daysRemaining: StateFlow<Int> = _daysRemaining.asStateFlow()

    private val _isVerifyingCode = MutableStateFlow(false)
    val isVerifyingCode: StateFlow<Boolean> = _isVerifyingCode.asStateFlow()

    private val _authFeedback = MutableStateFlow<AuthFeedback?>(null)
    val authFeedback: StateFlow<AuthFeedback?> = _authFeedback.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentTab = MutableStateFlow(CinemaTab.HOME)
    val currentTab: StateFlow<CinemaTab> = _currentTab.asStateFlow()

    private val _selectedMovieId = MutableStateFlow<String?>(null)
    val selectedMovieId: StateFlow<String?> = _selectedMovieId.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _isSyncingTelegram = MutableStateFlow(false)
    val isSyncingTelegram: StateFlow<Boolean> = _isSyncingTelegram.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val isTelegramTokenConfigured: Boolean = repository.isTelegramTokenConfigured()

    private var watchdogJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            // Restore active session if still valid
            val restoredPass = repository.getActiveSessionPass()
            if (restoredPass != null) {
                val rem = (restoredPass.expiryEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                if (rem > 0L) {
                    _activePass.value = restoredPass
                    _remainingMillis.value = rem
                    _daysRemaining.value = (rem / (1000L * 60 * 60 * 24)).toInt()
                    startSubscriptionWatchdog()
                }
            }
            if (repository.isTelegramTokenConfigured()) {
                repository.syncWithTelegramBot()
            }
            val config = cloudConfig.value
            if (config != null && config.autoSyncOnStartup && config.endpointUrl.isNotBlank()) {
                repository.syncFromCloud(config)
            }
        }
    }

    /**
     * Real-time 1-second watchdog that updates the countdown timer and
     * automatically locks the app the exact moment the active code expires.
     */
    private fun startSubscriptionWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = viewModelScope.launch {
            while (isActive) {
                val pass = _activePass.value ?: break
                val now = System.currentTimeMillis()
                val rem = pass.expiryEpochMillis - now

                if (rem <= 0L || !pass.isActive) {
                    // AUTO-LOCK TRIGGERED
                    _remainingMillis.value = 0L
                    _daysRemaining.value = 0
                    _selectedMovieId.value = null
                    _currentTab.value = CinemaTab.HOME
                    _activePass.value = null
                    repository.clearActiveSession()
                    _authFeedback.value = AuthFeedback(
                        isError = true,
                        title = "ماوەی کۆدەکەت تەواو بوو • Auto-Locked",
                        detail = "کاتی دیاریکراو بۆ کۆدی '${pass.code}' کۆتایی هات و بەرنامەکە بە شێوەیەکی خۆکار قفڵ کرا. تکایە کۆدێکی نوێ لە @KurdishFlim_4bot داخڵ بکە."
                    )
                    break
                } else {
                    _remainingMillis.value = rem
                    _daysRemaining.value = (rem / (1000L * 60 * 60 * 24)).toInt()
                }
                delay(1000L)
            }
        }
    }

    fun authenticateWithCode(codeInput: String) {
        viewModelScope.launch {
            _isVerifyingCode.value = true
            _authFeedback.value = null
            delay(300)
            val result = repository.validateAccessCode(codeInput)
            _isVerifyingCode.value = false

            when (result) {
                is AuthValidationResult.Valid -> {
                    _activePass.value = result.pass
                    _remainingMillis.value = result.remainingMillis
                    _daysRemaining.value = (result.remainingMillis / (1000L * 60 * 60 * 24)).toInt()
                    _authFeedback.value = null
                    startSubscriptionWatchdog()
                }
                is AuthValidationResult.Expired -> {
                    _authFeedback.value = AuthFeedback(
                        isError = true,
                        title = "کۆدەکە بەسەرچووە • Code Expired",
                        detail = "کۆدی '${result.pass.code}' لە بەرواری ${result.expiredDateFormatted} بەسەرچووە و ڕێگەپێدراو نییە. تکایە کۆدێکی نوێ لە @KurdishFlim_4bot وەربگرە."
                    )
                }
                is AuthValidationResult.Revoked -> {
                    _authFeedback.value = AuthFeedback(
                        isError = true,
                        title = "کۆدەکە ڕاگیراوە • Access Pass Revoked",
                        detail = "کۆدی '${result.pass.code}' لەلایەن @KurdishFlim_4bot چالاک نییە."
                    )
                }
                is AuthValidationResult.Invalid -> {
                    _authFeedback.value = AuthFeedback(
                        isError = true,
                        title = "پشتڕاستکردنەوە سەرنەکەوت • Verification Failed",
                        detail = result.message
                    )
                }
            }
        }
    }

    fun syncTelegramBotCodes() {
        viewModelScope.launch {
            _isSyncingTelegram.value = true
            val res = repository.syncWithTelegramBot()
            _isSyncingTelegram.value = false
            showBanner(res.statusMessage)
        }
    }

    fun triggerRapidExpiryTest(seconds: Int = 10) {
        val current = _activePass.value ?: return
        viewModelScope.launch {
            val updated = repository.setPassExpiryInSeconds(current.code, seconds)
            if (updated != null) {
                _activePass.value = updated
                _remainingMillis.value = seconds * 1000L
                startSubscriptionWatchdog()
                showBanner("تاقیکردنەوەی قفڵبوونی خۆکار: لە $seconds چرکەدا قفڵ دەبێت!")
            }
        }
    }

    fun resetFifteenSecondDemoPass() {
        viewModelScope.launch {
            val updated = repository.resetPassDuration("KF-15S-LOCK", 15_000L)
            if (updated != null) {
                showBanner("کۆدی KF-15S-LOCK نوێکرایەوە بۆ ١٥ چرکە تاقیکردنەوە")
            }
        }
    }

    fun clearAuthFeedback() {
        _authFeedback.value = null
    }

    fun signOut() {
        watchdogJob?.cancel()
        viewModelScope.launch {
            repository.clearActiveSession()
        }
        _selectedMovieId.value = null
        _currentTab.value = CinemaTab.HOME
        _activePass.value = null
        _authFeedback.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectTab(tab: CinemaTab) {
        _selectedMovieId.value = null
        _currentTab.value = tab
    }

    fun openMovieDetail(movieId: String) {
        _selectedMovieId.value = movieId
    }

    fun closeMovieDetail() {
        _selectedMovieId.value = null
    }

    fun toggleWatchlist(movie: MovieEntity) {
        viewModelScope.launch {
            repository.toggleWatchlist(movie.id, movie.isWatchlisted)
            showBanner(
                if (!movie.isWatchlisted) "Added \"${movie.title}\" to Watchlist"
                else "Removed \"${movie.title}\" from Watchlist"
            )
        }
    }

    fun savePlaybackProgress(movieId: String, positionMs: Long) {
        viewModelScope.launch {
            repository.updatePlaybackPosition(movieId, positionMs)
        }
    }

    fun saveOrUpdateMovie(
        existingId: String?,
        title: String,
        tagline: String,
        description: String,
        category: String,
        rating: Double,
        releaseYear: Int,
        durationMinutes: Int,
        director: String,
        castMembers: String,
        posterUrl: String,
        videoStreamUrl: String,
        isTrending: Boolean,
        isFeatured: Boolean
    ) {
        viewModelScope.launch {
            val movie = MovieEntity(
                id = existingId ?: "mov_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                tagline = tagline.trim().ifEmpty { "Streaming in 4K HDR on AetherCinema" },
                description = description.trim(),
                category = category.trim().ifEmpty { "Sci-Fi" },
                rating = rating.coerceIn(1.0, 10.0),
                releaseYear = releaseYear,
                durationMinutes = durationMinutes.coerceAtLeast(1),
                director = director.trim().ifEmpty { "Aether Studios" },
                castMembers = castMembers.trim().ifEmpty { "Featured Cast" },
                posterUrl = posterUrl.trim().ifEmpty { "drawable:img_poster_cyber" },
                backdropUrl = posterUrl.trim().ifEmpty { "drawable:img_hero_solaris" },
                videoStreamUrl = videoStreamUrl.trim().ifEmpty {
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                },
                isTrending = isTrending,
                isFeatured = isFeatured,
                cloudSource = "Admin Studio"
            )
            repository.saveMovie(movie)
            showBanner("Saved \"${movie.title}\" to Dynamic Catalog")
        }
    }

    fun deleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.deleteMovie(movie.id)
            if (_selectedMovieId.value == movie.id) {
                _selectedMovieId.value = null
            }
            showBanner("Deleted \"${movie.title}\" from catalog")
        }
    }

    fun createOrUpdateAccessPass(
        code: String,
        planName: String,
        holderName: String,
        daysValid: Int,
        isAdmin: Boolean,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val validityMs = daysValid.toLong() * 24L * 3600L * 1000L
            val expiry = now + validityMs
            val pass = AccessPassEntity(
                code = code.trim().uppercase(),
                planName = planName.trim().ifEmpty { "KurdishFlim Bot Pass" },
                holderName = holderName.trim().ifEmpty { "@KurdishFlim_4bot Subscriber" },
                validityDurationMs = validityMs.coerceAtLeast(1000L),
                activatedAtEpochMillis = 0L,
                expiryEpochMillis = expiry,
                maxResolution = if (isAdmin) "8K Master • Admin" else "4K HDR10+",
                botSource = "@KurdishFlim_4bot",
                isAdmin = isAdmin,
                isActive = isActive
            )
            repository.saveAccessPass(pass)
            showBanner("Saved @KurdishFlim_4bot Pass ${pass.code}")
        }
    }

    fun deleteAccessPass(code: String) {
        viewModelScope.launch {
            repository.deleteAccessPass(code)
            showBanner("Deleted Access Pass $code")
        }
    }

    fun saveCloudSettings(
        providerType: String,
        endpointUrl: String,
        authPassEndpointUrl: String,
        apiKeyOrToken: String,
        autoSync: Boolean
    ) {
        viewModelScope.launch {
            val current = cloudConfig.value ?: CloudConfigEntity()
            val updated = current.copy(
                providerType = providerType,
                endpointUrl = endpointUrl.trim(),
                authPassEndpointUrl = authPassEndpointUrl.trim(),
                apiKeyOrToken = apiKeyOrToken.trim(),
                autoSyncOnStartup = autoSync,
                lastSyncStatus = if (endpointUrl.isBlank()) {
                    "Local Vault Active • Ready for Cloud URL"
                } else {
                    "Cloud Endpoint Configured ($providerType)"
                }
            )
            repository.saveCloudConfig(updated)
            showBanner("Saved Cloud Database Configuration")
        }
    }

    fun pullFromCloudDatabase() {
        val config = cloudConfig.value ?: return
        viewModelScope.launch {
            _isSyncingCloud.value = true
            val result = repository.syncFromCloud(config)
            _isSyncingCloud.value = false
            showBanner(result.message)
        }
    }

    fun pushToCloudDatabase() {
        val config = cloudConfig.value ?: return
        viewModelScope.launch {
            _isSyncingCloud.value = true
            val result = repository.pushToCloud(config)
            _isSyncingCloud.value = false
            showBanner(result.message)
        }
    }

    private fun showBanner(message: String) {
        viewModelScope.launch {
            _statusBanner.value = message
            delay(3500)
            if (_statusBanner.value == message) {
                _statusBanner.value = null
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = CinemaDatabase.getInstance(context)
            val repository = CinemaRepository(database.cinemaDao(), CloudSyncService())
            return CinemaViewModel(repository) as T
        }
    }
}
