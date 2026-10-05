package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminCloudScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MovieDetailPlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CinemaMuted
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMatrix
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.viewmodel.CinemaTab
import com.example.ui.viewmodel.CinemaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AetherCinemaApp()
            }
        }
    }
}

@Composable
fun AetherCinemaApp() {
    val context = LocalContext.current
    val viewModel: CinemaViewModel = viewModel(
        factory = CinemaViewModel.Factory(context)
    )

    val movies by viewModel.movies.collectAsStateWithLifecycle()
    val accessPasses by viewModel.accessPasses.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    val activePass by viewModel.activePass.collectAsStateWithLifecycle()
    val remainingMillis by viewModel.remainingMillis.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifyingCode.collectAsStateWithLifecycle()
    val authFeedback by viewModel.authFeedback.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedMovieId by viewModel.selectedMovieId.collectAsStateWithLifecycle()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()
    val isSyncingTelegram by viewModel.isSyncingTelegram.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val selectedMovie = movies.firstOrNull { it.id == selectedMovieId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Crossfade(
            targetState = activePass != null,
            label = "AuthToMainTransition"
        ) { isAuthenticated ->
            if (!isAuthenticated) {
                AuthScreen(
                    accessPasses = accessPasses,
                    isVerifying = isVerifying,
                    isSyncingTelegram = isSyncingTelegram,
                    authFeedback = authFeedback,
                    onValidateCode = viewModel::authenticateWithCode,
                    onSyncTelegramBot = viewModel::syncTelegramBotCodes,
                    onResetDemoLockPass = viewModel::resetFifteenSecondDemoPass,
                    onClearFeedback = viewModel::clearAuthFeedback
                )
            } else if (selectedMovie != null) {
                val similarMovies = movies.filter {
                    it.id != selectedMovie.id &&
                        (it.category.equals(selectedMovie.category, ignoreCase = true) || it.isTrending)
                }
                MovieDetailPlayerScreen(
                    movie = selectedMovie,
                    similarMovies = similarMovies,
                    onBack = viewModel::closeMovieDetail,
                    onToggleWatchlist = viewModel::toggleWatchlist,
                    onSaveProgress = viewModel::savePlaybackProgress,
                    onSelectSimilarMovie = { viewModel.openMovieDetail(it.id) },
                    onEditInStudio = {
                        viewModel.closeMovieDetail()
                        viewModel.selectTab(CinemaTab.ADMIN_STUDIO)
                    }
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpandedScreen = maxWidth >= 600.dp

                    if (isExpandedScreen) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            NavigationRail(
                                containerColor = ObsidianSurface,
                                contentColor = CinemaWhite,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(WindowInsets.safeDrawing)
                            ) {
                                CinemaTab.entries.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = {
                                            Icon(
                                                imageVector = when (tab) {
                                                    CinemaTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                                    CinemaTab.WATCHLIST -> if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                                    CinemaTab.ADMIN_STUDIO -> if (selected) Icons.Filled.CloudSync else Icons.Outlined.CloudSync
                                                    CinemaTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                                },
                                                contentDescription = tab.name
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (tab) {
                                                    CinemaTab.HOME -> "Cinema"
                                                    CinemaTab.WATCHLIST -> "Watchlist"
                                                    CinemaTab.ADMIN_STUDIO -> "Studio"
                                                    CinemaTab.SETTINGS -> "Settings"
                                                }
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = ObsidianVoid,
                                            selectedTextColor = ElectricCyan,
                                            indicatorColor = ElectricCyan,
                                            unselectedIconColor = CinemaMuted,
                                            unselectedTextColor = CinemaMuted
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }

                            Scaffold(
                                containerColor = ObsidianVoid,
                                contentWindowInsets = WindowInsets.safeDrawing
                            ) { innerPadding ->
                                MainTabContent(
                                    currentTab = currentTab,
                                    movies = movies,
                                    accessPasses = accessPasses,
                                    cloudConfig = cloudConfig,
                                    activePass = activePass,
                                    remainingMillis = remainingMillis,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    isSyncingCloud = isSyncingCloud,
                                    isSyncingTelegram = isSyncingTelegram,
                                    viewModel = viewModel,
                                    innerPadding = innerPadding
                                )
                            }
                        }
                    } else {
                        Scaffold(
                            containerColor = ObsidianVoid,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            bottomBar = {
                                NavigationBar(
                                    containerColor = ObsidianSurface,
                                    contentColor = CinemaWhite,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    CinemaTab.entries.forEach { tab ->
                                        val selected = currentTab == tab
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { viewModel.selectTab(tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = when (tab) {
                                                        CinemaTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                                        CinemaTab.WATCHLIST -> if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                                        CinemaTab.ADMIN_STUDIO -> if (selected) Icons.Filled.CloudSync else Icons.Outlined.CloudSync
                                                        CinemaTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                                    },
                                                    contentDescription = tab.name
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = when (tab) {
                                                        CinemaTab.HOME -> "Cinema"
                                                        CinemaTab.WATCHLIST -> "Watchlist"
                                                        CinemaTab.ADMIN_STUDIO -> "Studio"
                                                        CinemaTab.SETTINGS -> "Settings"
                                                    },
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = ObsidianVoid,
                                                selectedTextColor = ElectricCyan,
                                                indicatorColor = ElectricCyan,
                                                unselectedIconColor = CinemaMuted,
                                                unselectedTextColor = CinemaMuted
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            MainTabContent(
                                currentTab = currentTab,
                                movies = movies,
                                accessPasses = accessPasses,
                                cloudConfig = cloudConfig,
                                activePass = activePass,
                                remainingMillis = remainingMillis,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                isSyncingCloud = isSyncingCloud,
                                isSyncingTelegram = isSyncingTelegram,
                                viewModel = viewModel,
                                innerPadding = innerPadding
                            )
                        }
                    }
                }
            }
        }

        // Floating Status Notification Pill
        AnimatedVisibility(
            visible = statusBanner != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
        ) {
            statusBanner?.let { message ->
                Surface(
                    color = ObsidianElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ElectricCyan),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldMatrix,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabContent(
    currentTab: CinemaTab,
    movies: List<com.example.data.local.MovieEntity>,
    accessPasses: List<com.example.data.local.AccessPassEntity>,
    cloudConfig: com.example.data.local.CloudConfigEntity?,
    activePass: com.example.data.local.AccessPassEntity?,
    remainingMillis: Long,
    searchQuery: String,
    selectedCategory: String,
    isSyncingCloud: Boolean,
    isSyncingTelegram: Boolean,
    viewModel: CinemaViewModel,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    when (currentTab) {
        CinemaTab.HOME -> {
            HomeDashboardScreen(
                movies = movies,
                activePass = activePass,
                remainingMillis = remainingMillis,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onCategorySelect = viewModel::selectCategory,
                onMovieClick = { viewModel.openMovieDetail(it.id) },
                onToggleWatchlist = viewModel::toggleWatchlist,
                onOpenSettings = { viewModel.selectTab(CinemaTab.SETTINGS) },
                onOpenCloudStudio = { viewModel.selectTab(CinemaTab.ADMIN_STUDIO) },
                onSignOut = viewModel::signOut,
                contentPadding = innerPadding
            )
        }
        CinemaTab.WATCHLIST -> {
            WatchlistScreen(
                movies = movies,
                onMovieClick = { viewModel.openMovieDetail(it.id) },
                onToggleWatchlist = viewModel::toggleWatchlist,
                onExploreCatalog = { viewModel.selectTab(CinemaTab.HOME) },
                contentPadding = innerPadding
            )
        }
        CinemaTab.ADMIN_STUDIO -> {
            AdminCloudScreen(
                movies = movies,
                accessPasses = accessPasses,
                cloudConfig = cloudConfig,
                isSyncingCloud = isSyncingCloud,
                onBackToHome = { viewModel.selectTab(CinemaTab.HOME) },
                onSaveMovie = viewModel::saveOrUpdateMovie,
                onDeleteMovie = viewModel::deleteMovie,
                onSaveCloudConfig = viewModel::saveCloudSettings,
                onPullFromCloud = viewModel::pullFromCloudDatabase,
                onPushToCloud = viewModel::pushToCloudDatabase,
                onCreateAccessPass = viewModel::createOrUpdateAccessPass,
                onDeleteAccessPass = viewModel::deleteAccessPass,
                contentPadding = innerPadding
            )
        }
        CinemaTab.SETTINGS -> {
            SettingsScreen(
                activePass = activePass,
                remainingMillis = remainingMillis,
                cloudConfig = cloudConfig,
                isTelegramTokenConfigured = viewModel.isTelegramTokenConfigured,
                isSyncingTelegram = isSyncingTelegram,
                onSyncTelegramBot = viewModel::syncTelegramBotCodes,
                onSimulateRapidExpiry = viewModel::triggerRapidExpiryTest,
                onSignOutAndLock = viewModel::signOut,
                onBackToHome = { viewModel.selectTab(CinemaTab.HOME) },
                contentPadding = innerPadding
            )
        }
    }
}
