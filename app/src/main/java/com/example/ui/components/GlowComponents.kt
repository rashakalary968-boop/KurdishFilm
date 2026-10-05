package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold
import java.util.Locale

@Composable
fun MovieArtwork(
    artworkUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableResId = when {
        artworkUrl.contains("img_hero_solaris") -> R.drawable.img_hero_solaris
        artworkUrl.contains("img_poster_cyber") -> R.drawable.img_poster_cyber
        artworkUrl.contains("img_poster_nebula") -> R.drawable.img_poster_nebula
        artworkUrl.contains("img_poster_abyss") -> R.drawable.img_poster_abyss
        artworkUrl.contains("img_app_icon") -> R.drawable.img_app_icon
        else -> null
    }

    if (drawableResId != null) {
        Image(
            painter = painterResource(id = drawableResId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else if (artworkUrl.startsWith("http://") || artworkUrl.startsWith("https://")) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    colors = listOf(ObsidianElevated, ObsidianCard, ObsidianVoid)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = null,
                tint = ElectricCyan.copy(alpha = 0.35f),
                modifier = Modifier.size(42.dp)
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Image(
            painter = painterResource(id = R.drawable.img_poster_cyber),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun RatingPill(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianVoid.copy(alpha = 0.82f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = SolarGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format(Locale.US, "%.1f", rating),
                style = MaterialTheme.typography.labelSmall,
                color = CinemaWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GlowingBadge(
    text: String,
    accentColor: Color = ElectricCyan,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accentColor.copy(alpha = 0.14f))
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PulsingDotIndicator(
    color: Color = ElectricCyan,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
