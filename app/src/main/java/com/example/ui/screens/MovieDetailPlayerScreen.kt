package com.example.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MovieDetailPlayerScreen(
    movie: MovieEntity,
    similarMovies: List<MovieEntity>,
    onBack: () -> Unit,
    onToggleWatchlist: (MovieEntity) -> Unit,
    onSaveProgress: (String, Long) -> Unit,
    onSelectSimilarMovie: (MovieEntity) -> Unit,
    onEditInStudio: () -> Unit
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasStreamError by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var currentPosMs by remember(movie.id) { mutableLongStateOf(movie.lastPlaybackPositionMs) }
    var totalDurationMs by remember(movie.id) { mutableLongStateOf(movie.durationMinutes * 60_000L) }
    var speedIndex by remember { mutableIntStateOf(0) }
    val speeds = remember { listOf(1.0f, 1.25f, 1.5f, 2.0f) }

    // Poll playback position while video is active
    LaunchedEffect(videoViewRef, isPlaying, movie.id) {
        while (true) {
            val vv = videoViewRef
            if (vv != null && isPlaying && !isBuffering) {
                try {
                    if (vv.isPlaying) {
                        val pos = vv.currentPosition.toLong()
                        val dur = vv.duration.toLong()
                        if (pos > 0L) {
                            currentPosMs = pos
                            onSaveProgress(movie.id, pos)
                        }
                        if (dur > 0L) {
                            totalDurationMs = dur
                        }
                    }
                } catch (_: Exception) {
                }
            }
            delay(500L)
        }
    }

    DisposableEffect(movie.id) {
        onDispose {
            try {
                val pos = videoViewRef?.currentPosition?.toLong() ?: currentPosMs
                if (pos > 0L) {
                    onSaveProgress(movie.id, pos)
                }
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scrollState)
    ) {
        // 1. Embedded Widescreen Video Player Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
                .testTag("embedded_video_player_box")
        ) {
            // Backdrop poster shown while stream buffers or if offline
            MovieArtwork(
                artworkUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize()
            )

            if (!hasStreamError && movie.videoStreamUrl.isNotBlank()) {
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            videoViewRef = this
                            setVideoURI(Uri.parse(movie.videoStreamUrl))
                            setOnPreparedListener { mp ->
                                mediaPlayerRef = mp
                                isBuffering = false
                                val dur = mp.duration.toLong()
                                if (dur > 0) totalDurationMs = dur
                                if (movie.lastPlaybackPositionMs > 1000L && movie.lastPlaybackPositionMs < dur) {
                                    seekTo(movie.lastPlaybackPositionMs.toInt())
                                }
                                mp.setOnInfoListener { _, what, _ ->
                                    when (what) {
                                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> isBuffering = true
                                        MediaPlayer.MEDIA_INFO_BUFFERING_END,
                                        MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> isBuffering = false
                                    }
                                    false
                                }
                                if (isPlaying) start()
                            }
                            setOnErrorListener { _, _, _ ->
                                isBuffering = false
                                hasStreamError = true
                                true
                            }
                            setOnCompletionListener {
                                isPlaying = false
                                showControls = true
                            }
                        }
                    },
                    update = { view ->
                        videoViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Buffering Spinner Overlay
            if (isBuffering && !hasStreamError) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianVoid.copy(alpha = 0.55f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "INITIALIZING 4K STREAM...",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
            }

            // Custom Jetpack Compose Player Controls HUD
            androidx.compose.animation.AnimatedVisibility(
                visible = showControls || !isPlaying || hasStreamError,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianVoid.copy(alpha = 0.8f),
                                    ObsidianVoid.copy(alpha = 0.35f),
                                    ObsidianVoid.copy(alpha = 0.88f)
                                )
                            )
                        )
                ) {
                    // Top Player Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianVoid.copy(alpha = 0.65f))
                                    .testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CinemaWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CinemaWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = movie.audioFormat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Speed Toggle Pill
                            Surface(
                                color = ObsidianCard.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable {
                                    speedIndex = (speedIndex + 1) % speeds.size
                                    val newSpeed = speeds[speedIndex]
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            mediaPlayerRef?.let { mp ->
                                                mp.playbackParams = mp.playbackParams.setSpeed(newSpeed)
                                            }
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Playback Speed",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${speeds[speedIndex]}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaWhite
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    try {
                                        val vol = if (isMuted) 0f else 1f
                                        mediaPlayerRef?.setVolume(vol, vol)
                                    } catch (_: Exception) {
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianCard.copy(alpha = 0.85f))
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Mute toggle",
                                    tint = if (isMuted) CrimsonPulse else CinemaWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Center Transport Controls (-10s, Play/Pause, +10s)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        IconButton(
                            onClick = {
                                val nextPos = (currentPosMs - 10_000L).coerceAtLeast(0L)
                                currentPosMs = nextPos
                                try {
                                    videoViewRef?.seekTo(nextPos.toInt())
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ObsidianVoid.copy(alpha = 0.65f))
                                .border(1.dp, ObsidianBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10 seconds",
                                tint = CinemaWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (hasStreamError) {
                                    hasStreamError = false
                                    isBuffering = true
                                    videoViewRef?.setVideoURI(Uri.parse(movie.videoStreamUrl))
                                    videoViewRef?.start()
                                    isPlaying = true
                                } else {
                                    isPlaying = !isPlaying
                                    try {
                                        if (isPlaying) videoViewRef?.start() else videoViewRef?.pause()
                                    } catch (_: Exception) {
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(ElectricCyan, AuroraViolet)
                                    )
                                )
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = when {
                                    hasStreamError -> Icons.Default.Replay
                                    isPlaying -> Icons.Default.Pause
                                    else -> Icons.Default.PlayArrow
                                },
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = ObsidianVoid,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextPos = (currentPosMs + 10_000L).coerceAtMost(totalDurationMs)
                                currentPosMs = nextPos
                                try {
                                    videoViewRef?.seekTo(nextPos.toInt())
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ObsidianVoid.copy(alpha = 0.65f))
                                .border(1.dp, ObsidianBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10 seconds",
                                tint = CinemaWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Bottom Seek Bar & Timecode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        val sliderFraction = if (totalDurationMs > 0L) {
                            (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Slider(
                            value = sliderFraction,
                            onValueChange = { fraction ->
                                val targetMs = (fraction * totalDurationMs).toLong()
                                currentPosMs = targetMs
                                try {
                                    videoViewRef?.seekTo(targetMs.toInt())
                                } catch (_: Exception) {
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = ObsidianBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${formatTimeMs(currentPosMs)} / ${formatTimeMs(totalDurationMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaSilver
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hd,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (hasStreamError) "PREVIEW MODE (TAP PLAY TO RETRY STREAM)" else "DIRECT CLOUD STREAM ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasStreamError) SolarGold else EmeraldMatrix
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Movie Detail Header (Poster + Metadata + Action Buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .height(148.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    MovieArtwork(
                        artworkUrl = movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = CinemaWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = movie.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${movie.releaseYear} • ${movie.durationMinutes} min • ${movie.audioFormat}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaSilver
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Action Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        currentPosMs = 0L
                        isPlaying = true
                        try {
                            videoViewRef?.seekTo(0)
                            videoViewRef?.start()
                        } catch (_: Exception) {
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESTART FILM",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = { onToggleWatchlist(movie) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (movie.isWatchlisted) SolarGold else ObsidianBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ObsidianCard,
                        contentColor = if (movie.isWatchlisted) SolarGold else CinemaWhite
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("detail_watchlist_button")
                ) {
                    Icon(
                        imageVector = if (movie.isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (movie.isWatchlisted) "Saved" else "Watchlist",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                IconButton(
                    onClick = onEditInStudio,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianCard)
                        .border(1.dp, AuroraViolet.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .testTag("detail_edit_studio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit in Cloud Studio",
                        tint = AuroraViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Storyline & Cast Card
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "STORYLINE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CinemaWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DIRECTOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = movie.director,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "STARRING CAST",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = movie.castMembers,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaSilver
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Cloud Stream Source Card
            Surface(
                color = ObsidianCard.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = EmeraldMatrix,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Source: ${movie.cloudSource}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaWhite
                        )
                        Text(
                            text = movie.videoStreamUrl,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = CinemaMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // More Like This Carousel
            if (similarMovies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "MORE LIKE THIS",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrimsonPulse
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(similarMovies, key = { "sim_${it.id}" }) { sim ->
                        Surface(
                            color = ObsidianCard,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onSelectSimilarMovie(sim) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(175.dp)
                                ) {
                                    MovieArtwork(
                                        artworkUrl = sim.posterUrl,
                                        contentDescription = sim.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    RatingPill(
                                        rating = sim.rating,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = sim.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaWhite,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = sim.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
