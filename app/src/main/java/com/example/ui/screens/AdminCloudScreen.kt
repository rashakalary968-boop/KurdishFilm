package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.AccessPassEntity
import com.example.data.local.CloudConfigEntity
import com.example.data.local.MovieEntity
import com.example.ui.components.GlowingBadge
import com.example.ui.components.MovieArtwork
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.CinemaMuted
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMatrix
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminCloudScreen(
    movies: List<MovieEntity>,
    accessPasses: List<AccessPassEntity>,
    cloudConfig: CloudConfigEntity?,
    isSyncingCloud: Boolean,
    onBackToHome: () -> Unit,
    onSaveMovie: (
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
    ) -> Unit,
    onDeleteMovie: (MovieEntity) -> Unit,
    onSaveCloudConfig: (String, String, String, String, Boolean) -> Unit,
    onPullFromCloud: () -> Unit,
    onPushToCloud: () -> Unit,
    onCreateAccessPass: (String, String, String, Int, Boolean) -> Unit,
    onDeleteAccessPass: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onBackToHome() }

    var activeSection by rememberSaveable { mutableIntStateOf(0) } // 0 = Movies, 1 = Cloud DB, 2 = Access Passes
    var showMovieModal by rememberSaveable { mutableStateOf(false) }
    var editingMovie by remember { mutableStateOf<MovieEntity?>(null) }

    // Cloud config state
    var providerType by remember(cloudConfig) { mutableStateOf(cloudConfig?.providerType ?: "FIREBASE_REST") }
    var endpointUrl by remember(cloudConfig) { mutableStateOf(cloudConfig?.endpointUrl ?: "") }
    var authEndpointUrl by remember(cloudConfig) { mutableStateOf(cloudConfig?.authPassEndpointUrl ?: "") }
    var apiKeyOrToken by remember(cloudConfig) { mutableStateOf(cloudConfig?.apiKeyOrToken ?: "") }
    var autoSync by remember(cloudConfig) { mutableStateOf(cloudConfig?.autoSyncOnStartup ?: true) }

    // New Access Pass form state
    var newPassCode by rememberSaveable { mutableStateOf("") }
    var newPassPlan by rememberSaveable { mutableStateOf("Aether VIP 4K") }
    var newPassDays by rememberSaveable { mutableStateOf("90") }
    var newPassAdmin by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GlowingBadge(
                        text = "DYNAMIC CONTENT MANAGEMENT",
                        accentColor = AuroraViolet
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cloud & Catalog Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CinemaWhite,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Manage movies, streaming URLs, Firebase/Supabase sync & access passes live.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaSilver
                    )
                }

                Button(
                    onClick = {
                        editingMovie = null
                        showMovieModal = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier.testTag("studio_add_movie_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD MOVIE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Studio Sub-navigation Tabs
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    Triple(0, "Movies (${movies.size})", Icons.Default.Movie),
                    Triple(1, "Cloud Sync", Icons.Default.CloudSync),
                    Triple(2, "Access Codes (${accessPasses.size})", Icons.Default.Key)
                )
                tabs.forEach { (idx, label, icon) ->
                    val selected = activeSection == idx
                    FilterChip(
                        selected = selected,
                        onClick = { activeSection = idx },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selected) ObsidianVoid else ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = ObsidianVoid,
                            containerColor = ObsidianCard,
                            labelColor = CinemaSilver
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("studio_tab_$idx")
                    )
                }
            }
        }

        // SECTION 0: DYNAMIC MOVIE CATALOG MANAGER
        if (activeSection == 0) {
            items(movies, key = { "admin_mov_${it.id}" }) { movie ->
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(88.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            MovieArtwork(
                                artworkUrl = movie.posterUrl,
                                contentDescription = movie.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GlowingBadge(text = movie.category, accentColor = ElectricCyan)
                                if (movie.isTrending) {
                                    GlowingBadge(text = "TRENDING", accentColor = CrimsonPulse)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaWhite,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Stream: ${movie.videoStreamUrl}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = CinemaMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                editingMovie = movie
                                showMovieModal = true
                            },
                            modifier = Modifier.testTag("edit_movie_${movie.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit movie",
                                tint = ElectricCyan
                            )
                        }

                        IconButton(
                            onClick = { onDeleteMovie(movie) },
                            modifier = Modifier.testTag("delete_movie_${movie.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete movie",
                                tint = CrimsonPulse
                            )
                        }
                    }
                }
            }
        }

        // SECTION 1: CLOUD DATABASE CONNECTOR (FIREBASE / SUPABASE / REST)
        if (activeSection == 1) {
            item {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CLOUD BACKEND CONFIGURATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cloudConfig?.lastSyncStatus ?: "Ready for Cloud Endpoint",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldMatrix
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "FIREBASE_REST" to "Firebase RTDB",
                                "SUPABASE_REST" to "Supabase",
                                "CUSTOM_JSON" to "Custom JSON"
                            ).forEach { (key, label) ->
                                val selected = providerType == key
                                FilterChip(
                                    selected = selected,
                                    onClick = { providerType = key },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AuroraViolet,
                                        selectedLabelColor = CinemaWhite,
                                        containerColor = ObsidianCard,
                                        labelColor = CinemaSilver
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = endpointUrl,
                            onValueChange = { endpointUrl = it },
                            label = { Text("Movies Cloud Endpoint URL") },
                            placeholder = {
                                Text(
                                    when (providerType) {
                                        "FIREBASE_REST" -> "https://your-project-default-rtdb.firebaseio.com/movies.json"
                                        "SUPABASE_REST" -> "https://xyzcompany.supabase.co/rest/v1/movies"
                                        else -> "https://api.example.com/v1/movies.json"
                                    }
                                )
                            },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cloud_endpoint_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = authEndpointUrl,
                            onValueChange = { authEndpointUrl = it },
                            label = { Text("Access Codes Endpoint URL (Optional)") },
                            placeholder = { Text("https://your-project-default-rtdb.firebaseio.com/access_passes.json") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = apiKeyOrToken,
                            onValueChange = { apiKeyOrToken = it },
                            label = { Text("Supabase anon/service Key or Bearer Token (Optional)") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Auto-fetch cloud movies on app launch",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaWhite
                            )
                            Switch(
                                checked = autoSync,
                                onCheckedChange = { autoSync = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObsidianVoid,
                                    checkedTrackColor = ElectricCyan
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onSaveCloudConfig(
                                    providerType,
                                    endpointUrl,
                                    authEndpointUrl,
                                    apiKeyOrToken,
                                    autoSync
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "SAVE CLOUD CONFIGURATION",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSaveCloudConfig(
                                        providerType,
                                        endpointUrl,
                                        authEndpointUrl,
                                        apiKeyOrToken,
                                        autoSync
                                    )
                                    onPullFromCloud()
                                },
                                enabled = !isSyncingCloud,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ElectricCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSyncingCloud) {
                                    CircularProgressIndicator(
                                        color = ElectricCyan,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PULL MOVIES",
                                    color = ElectricCyan,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onSaveCloudConfig(
                                        providerType,
                                        endpointUrl,
                                        authEndpointUrl,
                                        apiKeyOrToken,
                                        autoSync
                                    )
                                    onPushToCloud()
                                },
                                enabled = !isSyncingCloud,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, AuroraViolet),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = AuroraViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PUSH CATALOG",
                                    color = AuroraViolet,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: ACCESS CODES & SUBSCRIPTION EXPIRY MANAGER
        if (activeSection == 2) {
            item {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "GENERATE SUBSCRIBER ACCESS CODE",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPassCode,
                            onValueChange = { newPassCode = it.uppercase() },
                            label = { Text("New Access Code (e.g. ULTRA-4K-2027)") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_pass_code_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newPassPlan,
                                onValueChange = { newPassPlan = it },
                                label = { Text("Plan Tier") },
                                singleLine = true,
                                colors = studioTextFieldColors(),
                                modifier = Modifier.weight(1.4f)
                            )
                            OutlinedTextField(
                                value = newPassDays,
                                onValueChange = { newPassDays = it.filter { ch -> ch.isDigit() || ch == '-' } },
                                label = { Text("Valid Days") },
                                singleLine = true,
                                colors = studioTextFieldColors(),
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newPassCode.isNotBlank()) {
                                    val days = newPassDays.toIntOrNull() ?: 30
                                    onCreateAccessPass(
                                        newPassCode,
                                        newPassPlan,
                                        "Cloud Subscriber",
                                        days,
                                        newPassAdmin
                                    )
                                    newPassCode = ""
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_pass_button")
                        ) {
                            Text(
                                text = "SAVE ACCESS PASS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val now = System.currentTimeMillis()

            items(accessPasses, key = { "pass_row_${it.code}" }) { pass ->
                val isExpired = pass.expiryEpochMillis <= now
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isExpired) CrimsonPulse.copy(alpha = 0.5f) else ObsidianBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pass.code,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                color = CinemaWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pass.planName} • Expires ${dateFormat.format(Date(pass.expiryEpochMillis))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaSilver
                            )
                        }
                        GlowingBadge(
                            text = if (isExpired) "EXPIRED" else "ACTIVE",
                            accentColor = if (isExpired) CrimsonPulse else EmeraldMatrix
                        )
                        IconButton(onClick = { onDeleteAccessPass(pass.code) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete pass",
                                tint = CrimsonPulse
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMovieModal) {
        AddOrEditMovieDialog(
            initialMovie = editingMovie,
            onDismiss = { showMovieModal = false },
            onConfirm = { id, title, tagline, desc, cat, rating, year, dur, dir, cast, poster, stream, trend, feat ->
                onSaveMovie(id, title, tagline, desc, cat, rating, year, dur, dir, cast, poster, stream, trend, feat)
                showMovieModal = false
            }
        )
    }
}

@Composable
private fun AddOrEditMovieDialog(
    initialMovie: MovieEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialMovie?.title ?: "") }
    var tagline by rememberSaveable { mutableStateOf(initialMovie?.tagline ?: "") }
    var description by rememberSaveable { mutableStateOf(initialMovie?.description ?: "") }
    var category by rememberSaveable { mutableStateOf(initialMovie?.category ?: "Sci-Fi") }
    var ratingText by rememberSaveable { mutableStateOf(initialMovie?.rating?.toString() ?: "9.0") }
    var yearText by rememberSaveable { mutableStateOf(initialMovie?.releaseYear?.toString() ?: "2026") }
    var durationText by rememberSaveable { mutableStateOf(initialMovie?.durationMinutes?.toString() ?: "124") }
    var director by rememberSaveable { mutableStateOf(initialMovie?.director ?: "Aether Studios") }
    var castMembers by rememberSaveable { mutableStateOf(initialMovie?.castMembers ?: "Elena Rostova, Marcus Sterling") }
    var posterUrl by rememberSaveable { mutableStateOf(initialMovie?.posterUrl ?: "drawable:img_poster_cyber") }
    var videoStreamUrl by rememberSaveable {
        mutableStateOf(
            initialMovie?.videoStreamUrl
                ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        )
    }
    var isTrending by rememberSaveable { mutableStateOf(initialMovie?.isTrending ?: true) }
    var isFeatured by rememberSaveable { mutableStateOf(initialMovie?.isFeatured ?: false) }

    val artworkPresets = listOf(
        "Solaris Hero" to "drawable:img_hero_solaris",
        "Cyberpunk Noir" to "drawable:img_poster_cyber",
        "Chronos Monolith" to "drawable:img_poster_nebula",
        "Deep Trench" to "drawable:img_poster_abyss"
    )

    val streamPresets = listOf(
        "Tears of Steel (Sci-Fi)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        "Sintel (Fantasy)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        "Elephants Dream" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "Big Buck Bunny" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        titleContentColor = CinemaWhite,
        textContentColor = CinemaSilver,
        title = {
            Text(
                text = if (initialMovie == null) "Add Dynamic Movie" else "Edit Movie Details",
                style = MaterialTheme.typography.titleLarge,
                color = ElectricCyan
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(420.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Movie Title *") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("modal_movie_title_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Genre") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ratingText,
                            onValueChange = { ratingText = it },
                            label = { Text("Rating") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Synopsis / Description") },
                        minLines = 2,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Poster Image URL or Preset:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(artworkPresets) { (name, uri) ->
                            FilterChip(
                                selected = posterUrl == uri,
                                onClick = { posterUrl = uri },
                                label = { Text(name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = posterUrl,
                        onValueChange = { posterUrl = it },
                        label = { Text("Poster Image URL") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Streaming Video Link (MP4 / HLS):",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(streamPresets) { (name, url) ->
                            FilterChip(
                                selected = videoStreamUrl == url,
                                onClick = { videoStreamUrl = url },
                                label = { Text(name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = videoStreamUrl,
                        onValueChange = { videoStreamUrl = it },
                        label = { Text("Direct Video Stream URL") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show in Trending Carousel", color = CinemaWhite)
                        Switch(checked = isTrending, onCheckedChange = { isTrending = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            initialMovie?.id,
                            title,
                            tagline,
                            description.ifBlank { "Added via AetherCinema Dynamic Content Studio." },
                            category,
                            ratingText.toDoubleOrNull() ?: 8.8,
                            yearText.toIntOrNull() ?: 2026,
                            durationText.toIntOrNull() ?: 120,
                            director,
                            castMembers,
                            posterUrl,
                            videoStreamUrl,
                            isTrending,
                            isFeatured
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier.testTag("modal_save_movie_button")
            ) {
                Text("SAVE MOVIE", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CinemaSilver)
            }
        }
    )
}

@Composable
private fun studioTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = ObsidianBorder,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = CinemaSilver,
    cursorColor = ElectricCyan,
    focusedContainerColor = ObsidianCard,
    unfocusedContainerColor = ObsidianCard,
    focusedTextColor = CinemaWhite,
    unfocusedTextColor = CinemaWhite
)
