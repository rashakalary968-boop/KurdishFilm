package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccessPassEntity
import com.example.data.local.MovieEntity
import com.example.ui.components.GlowingBadge
import com.example.ui.components.MovieArtwork
import com.example.ui.components.RatingPill
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

@Composable
fun HomeDashboardScreen(
    movies: List<MovieEntity>,
    activePass: AccessPassEntity?,
    remainingMillis: Long,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onMovieClick: (MovieEntity) -> Unit,
    onToggleWatchlist: (MovieEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCloudStudio: () -> Unit,
    onSignOut: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val categories = remember(movies) {
        val dynamicGenres = movies.map { it.category }.distinct()
        listOf("All") + (listOf("Sci-Fi", "Cyberpunk", "Thriller", "Mystery", "Action", "Animation") + dynamicGenres).distinct()
    }

    val filteredMovies = remember(movies, searchQuery, selectedCategory) {
        movies.filter { movie ->
            val matchesCategory = selectedCategory == "All" ||
                movie.category.equals(selectedCategory, ignoreCase = true)
            val query = searchQuery.trim()
            val matchesSearch = query.isEmpty() ||
                movie.title.contains(query, ignoreCase = true) ||
                movie.director.contains(query, ignoreCase = true) ||
                movie.castMembers.contains(query, ignoreCase = true) ||
                movie.category.contains(query, ignoreCase = true) ||
                movie.tagline.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val featuredMovie = remember(movies) {
        movies.firstOrNull { it.isFeatured } ?: movies.firstOrNull()
    }

    val trendingMovies = remember(movies, selectedCategory) {
        movies.filter {
            it.isTrending && (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true))
        }
    }

    val continueWatchingMovies = remember(movies) {
        movies.filter { it.lastPlaybackPositionMs > 2000L }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // 1. Top Cinema Header & Active Subscription Pass Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AETHER",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "CINEMA",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaWhite,
                            fontWeight = FontWeight.Light
                        )
                    }
                    activePass?.let { _ ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenSettings() }
                                .testTag("header_kurdish_timer_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldMatrix,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = com.example.ui.components.KurdishTimeFormatter.formatPrimaryKurdishCountdown(remainingMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onOpenCloudStudio,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), CircleShape)
                            .testTag("header_cloud_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud & Catalog Studio",
                            tint = ElectricCyan
                        )
                    }

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .border(1.dp, ObsidianBorder, CircleShape)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.logout_label),
                            tint = CinemaSilver
                        )
                    }
                }
            }
        }

        // 2. Real-Time Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        color = CinemaMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = CinemaSilver
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedContainerColor = ObsidianCard,
                    unfocusedContainerColor = ObsidianSurface,
                    cursorColor = ElectricCyan,
                    focusedTextColor = CinemaWhite,
                    unfocusedTextColor = CinemaWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("search_bar_input")
            )
        }

        // 3. Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = ObsidianVoid,
                            containerColor = ObsidianCard,
                            labelColor = CinemaSilver
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ObsidianBorder,
                            selectedBorderColor = ElectricCyan
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("category_chip_${category.lowercase()}")
                    )
                }
            }
        }

        // 4. Hero Spotlight Banner (shown when not actively typing a search query)
        if (searchQuery.isBlank() && featuredMovie != null && selectedCategory == "All") {
            item {
                HeroSpotlightBanner(
                    movie = featuredMovie,
                    onWatchClick = { onMovieClick(featuredMovie) },
                    onToggleWatchlist = { onToggleWatchlist(featuredMovie) }
                )
            }
        }

        // 5. Continue Watching Section (if any movies have saved progress)
        if (searchQuery.isBlank() && continueWatchingMovies.isNotEmpty() && selectedCategory == "All") {
            item {
                SectionHeader(
                    title = "Continue Watching",
                    subtitle = "Pick up right where you left off",
                    accentColor = AuroraViolet
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueWatchingMovies, key = { "cw_${it.id}" }) { movie ->
                        ContinueWatchingCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 6. Trending Now Horizontal Carousel (when not searching)
        if (searchQuery.isBlank() && trendingMovies.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.trending_section),
                    subtitle = "Top streamed films in 4K HDR",
                    accentColor = CrimsonPulse
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(trendingMovies, key = { _, m -> "trend_${m.id}" }) { index, movie ->
                        TrendingMovieCard(
                            rank = index + 1,
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            onToggleWatchlist = { onToggleWatchlist(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 7. Dynamic Catalog / Search Results Section
        item {
            val headerTitle = if (searchQuery.isNotBlank()) {
                "Search Results (${filteredMovies.size})"
            } else if (selectedCategory != "All") {
                "$selectedCategory Releases (${filteredMovies.size})"
            } else {
                stringResource(R.string.catalog_section)
            }

            SectionHeader(
                title = headerTitle,
                subtitle = if (searchQuery.isNotBlank()) {
                    "Instant matches for \"$searchQuery\""
                } else {
                    "Dynamically synced cinema catalog"
                },
                accentColor = ElectricCyan
            )
        }

        if (filteredMovies.isEmpty()) {
            item {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching movies found",
                            style = MaterialTheme.typography.titleLarge,
                            color = CinemaWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try another title or add a new movie dynamically in the Cloud & Catalog Studio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaSilver
                        )
                    }
                }
            }
        } else {
            items(filteredMovies, key = { "catalog_${it.id}" }) { movie ->
                CatalogMovieRowCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    onToggleWatchlist = { onToggleWatchlist(movie) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSpotlightBanner(
    movie: MovieEntity,
    onWatchClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ElectricCyan.copy(alpha = 0.7f),
                    ObsidianBorder,
                    CrimsonPulse.copy(alpha = 0.6f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(330.dp)
            .clickable { onWatchClick() }
            .testTag("hero_banner_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MovieArtwork(
                artworkUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianVoid.copy(alpha = 0.15f),
                                ObsidianVoid.copy(alpha = 0.55f),
                                ObsidianVoid.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                GlowingBadge(
                    text = "FEATURED PREMIERE • ${movie.category}",
                    accentColor = ElectricCyan
                )
                RatingPill(rating = movie.rating)
            }

            // Bottom Details & Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = CinemaWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${movie.releaseYear} • ${movie.durationMinutes} min • ${movie.audioFormat}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onWatchClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianVoid
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_stream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.watch_now).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleWatchlist,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (movie.isWatchlisted) SolarGold else CinemaSilver),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ObsidianVoid.copy(alpha = 0.7f),
                            contentColor = if (movie.isWatchlisted) SolarGold else CinemaWhite
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (movie.isWatchlisted) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Toggle Watchlist",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (movie.isWatchlisted) "Saved" else "Watchlist",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = CinemaWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaMuted
            )
        }
    }
}

@Composable
private fun TrendingMovieCard(
    rank: Int,
    movie: MovieEntity,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier
            .width(185.dp)
            .clickable { onClick() }
            .testTag("trending_card_${movie.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
            ) {
                MovieArtwork(
                    artworkUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ObsidianVoid.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Rank Badge
                Surface(
                    color = CrimsonPulse,
                    shape = RoundedCornerShape(bottomEnd = 14.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                RatingPill(
                    rating = movie.rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ObsidianVoid.copy(alpha = 0.78f))
                ) {
                    Icon(
                        imageVector = if (movie.isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (movie.isWatchlisted) SolarGold else CinemaWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${movie.category} • ${movie.releaseYear}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    movie: MovieEntity,
    onClick: () -> Unit
) {
    val progressFraction = ((movie.lastPlaybackPositionMs / 1000f) / (movie.durationMinutes * 60f))
        .coerceIn(0.08f, 0.95f)

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AuroraViolet.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                MovieArtwork(
                    artworkUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianVoid.copy(alpha = 0.45f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = ObsidianVoid
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progressFraction },
                color = ElectricCyan,
                trackColor = ObsidianElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val minutesWatched = (movie.lastPlaybackPositionMs / 60000L).coerceAtLeast(1L)
                Text(
                    text = "Resume at ${minutesWatched}m • ${movie.durationMinutes}m total",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaSilver
                )
            }
        }
    }
}

@Composable
fun CatalogMovieRowCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("catalog_movie_${movie.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(126.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                MovieArtwork(
                    artworkUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowingBadge(
                        text = movie.category,
                        accentColor = ElectricCyan
                    )
                    RatingPill(rating = movie.rating)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${movie.releaseYear} • ${movie.durationMinutes}m • ${movie.cloudSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleWatchlist,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (movie.isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (movie.isWatchlisted) SolarGold else CinemaSilver,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.18f))
                                .border(1.dp, ElectricCyan, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
