package com.example.data.remote

import com.example.data.local.AccessPassEntity
import com.example.data.local.CloudConfigEntity
import com.example.data.local.MovieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CloudSyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val message: String,
    val movies: List<MovieEntity> = emptyList()
)

class CloudSyncService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun pullMoviesFromCloud(config: CloudConfigEntity): CloudSyncResult = withContext(Dispatchers.IO) {
        val rawUrl = config.endpointUrl.trim()
        if (rawUrl.isEmpty()) {
            return@withContext CloudSyncResult(
                success = false,
                syncedCount = 0,
                message = "Please enter a valid Firebase, Supabase, or JSON endpoint URL."
            )
        }

        val finalUrl = if (config.providerType == "FIREBASE_REST" && !rawUrl.endsWith(".json") && !rawUrl.contains(".json?")) {
            "${rawUrl.trimEnd('/')}.json"
        } else {
            rawUrl
        }

        try {
            val requestBuilder = Request.Builder().url(finalUrl).get()
            if (config.apiKeyOrToken.isNotBlank()) {
                when (config.providerType) {
                    "SUPABASE_REST" -> {
                        requestBuilder.addHeader("apikey", config.apiKeyOrToken.trim())
                        requestBuilder.addHeader("Authorization", "Bearer ${config.apiKeyOrToken.trim()}")
                    }
                    else -> {
                        requestBuilder.addHeader("Authorization", "Bearer ${config.apiKeyOrToken.trim()}")
                    }
                }
            }

            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext CloudSyncResult(
                        success = false,
                        syncedCount = 0,
                        message = "Cloud server returned HTTP ${response.code}: ${response.message}"
                    )
                }

                val bodyString = response.body?.string()?.trim().orEmpty()
                if (bodyString.isEmpty() || bodyString == "null") {
                    return@withContext CloudSyncResult(
                        success = true,
                        syncedCount = 0,
                        message = "Connected to cloud database (currently empty)."
                    )
                }

                val parsedMovies = parseFlexibleMoviesJson(bodyString)
                CloudSyncResult(
                    success = true,
                    syncedCount = parsedMovies.size,
                    message = "Synced ${parsedMovies.size} movies from ${config.providerType} cloud.",
                    movies = parsedMovies
                )
            }
        } catch (e: Exception) {
            CloudSyncResult(
                success = false,
                syncedCount = 0,
                message = "Cloud sync error: ${e.localizedMessage ?: "Network unreachable"}"
            )
        }
    }

    suspend fun pushCatalogToCloud(config: CloudConfigEntity, movies: List<MovieEntity>): CloudSyncResult =
        withContext(Dispatchers.IO) {
            val rawUrl = config.endpointUrl.trim()
            if (rawUrl.isEmpty()) {
                return@withContext CloudSyncResult(
                    success = false,
                    syncedCount = 0,
                    message = "Configure a Firebase or REST endpoint URL before pushing."
                )
            }

            val finalUrl = if (config.providerType == "FIREBASE_REST" && !rawUrl.endsWith(".json") && !rawUrl.contains(".json?")) {
                "${rawUrl.trimEnd('/')}.json"
            } else {
                rawUrl
            }

            try {
                val jsonArray = JSONArray()
                movies.forEach { movie ->
                    val obj = JSONObject().apply {
                        put("id", movie.id)
                        put("title", movie.title)
                        put("tagline", movie.tagline)
                        put("description", movie.description)
                        put("category", movie.category)
                        put("rating", movie.rating)
                        put("releaseYear", movie.releaseYear)
                        put("durationMinutes", movie.durationMinutes)
                        put("director", movie.director)
                        put("castMembers", movie.castMembers)
                        put("posterUrl", movie.posterUrl)
                        put("backdropUrl", movie.backdropUrl)
                        put("videoStreamUrl", movie.videoStreamUrl)
                        put("isTrending", movie.isTrending)
                        put("isFeatured", movie.isFeatured)
                        put("audioFormat", movie.audioFormat)
                    }
                    jsonArray.put(obj)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonArray.toString().toRequestBody(mediaType)

                val requestBuilder = Request.Builder().url(finalUrl)
                if (config.providerType == "FIREBASE_REST") {
                    requestBuilder.put(body)
                } else {
                    requestBuilder.post(body)
                }

                if (config.apiKeyOrToken.isNotBlank()) {
                    when (config.providerType) {
                        "SUPABASE_REST" -> {
                            requestBuilder.addHeader("apikey", config.apiKeyOrToken.trim())
                            requestBuilder.addHeader("Authorization", "Bearer ${config.apiKeyOrToken.trim()}")
                            requestBuilder.addHeader("Prefer", "resolution=merge-duplicates")
                        }
                        else -> {
                            requestBuilder.addHeader("Authorization", "Bearer ${config.apiKeyOrToken.trim()}")
                        }
                    }
                }

                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext CloudSyncResult(
                            success = false,
                            syncedCount = 0,
                            message = "Cloud push failed with HTTP ${response.code}: ${response.message}"
                        )
                    }
                    CloudSyncResult(
                        success = true,
                        syncedCount = movies.size,
                        message = "Published ${movies.size} movies to ${config.providerType} cloud."
                    )
                }
            } catch (e: Exception) {
                CloudSyncResult(
                    success = false,
                    syncedCount = 0,
                    message = "Cloud push failed: ${e.localizedMessage ?: "Connection error"}"
                )
            }
        }

    suspend fun validateCodeRemotely(
        config: CloudConfigEntity,
        enteredCode: String
    ): AccessPassEntity? = withContext(Dispatchers.IO) {
        val rawUrl = config.authPassEndpointUrl.trim()
        if (rawUrl.isEmpty()) return@withContext null

        try {
            val requestBuilder = Request.Builder().url(rawUrl).get()
            if (config.apiKeyOrToken.isNotBlank()) {
                requestBuilder.addHeader("apikey", config.apiKeyOrToken.trim())
                requestBuilder.addHeader("Authorization", "Bearer ${config.apiKeyOrToken.trim()}")
            }
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string()?.trim().orEmpty()
                val passes = parseFlexiblePassesJson(body)
                passes.firstOrNull { it.code.equals(enteredCode.trim(), ignoreCase = true) }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFlexibleMoviesJson(rawJson: String): List<MovieEntity> {
        val result = mutableListOf<MovieEntity>()
        if (rawJson.startsWith("[")) {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                parseMovieObject(obj)?.let { result.add(it) }
            }
        } else if (rawJson.startsWith("{")) {
            val root = JSONObject(rawJson)
            if (root.has("movies") && root.opt("movies") is JSONArray) {
                val array = root.getJSONArray("movies")
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parseMovieObject(obj)?.let { result.add(it) }
                }
            } else {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val child = root.optJSONObject(key) ?: continue
                    parseMovieObject(child, fallbackId = key)?.let { result.add(it) }
                }
            }
        }
        return result
    }

    private fun parseMovieObject(obj: JSONObject, fallbackId: String? = null): MovieEntity? {
        val title = obj.optString("title", "").trim()
        if (title.isEmpty()) return null
        val id = obj.optString("id", fallbackId ?: UUID.randomUUID().toString())
        val poster = obj.optString("posterUrl", obj.optString("poster", "drawable:img_poster_cyber"))
        val backdrop = obj.optString("backdropUrl", obj.optString("backdrop", poster))
        val streamUrl = obj.optString(
            "videoStreamUrl",
            obj.optString(
                "streamUrl",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            )
        )
        return MovieEntity(
            id = id,
            title = title,
            tagline = obj.optString("tagline", "Streaming in 4K HDR on AetherCinema"),
            description = obj.optString("description", "Added dynamically via Cloud Database Sync."),
            category = obj.optString("category", "Sci-Fi"),
            rating = obj.optDouble("rating", 8.5),
            releaseYear = obj.optInt("releaseYear", 2026),
            durationMinutes = obj.optInt("durationMinutes", 118),
            director = obj.optString("director", "Aether Studios"),
            castMembers = obj.optString("castMembers", "Ensemble Cast"),
            posterUrl = poster,
            backdropUrl = backdrop,
            videoStreamUrl = streamUrl,
            isTrending = obj.optBoolean("isTrending", true),
            isFeatured = obj.optBoolean("isFeatured", false),
            audioFormat = obj.optString("audioFormat", "Dolby Atmos • 4K HDR"),
            cloudSource = "Cloud Synced"
        )
    }

    private fun parseFlexiblePassesJson(rawJson: String): List<AccessPassEntity> {
        val list = mutableListOf<AccessPassEntity>()
        if (rawJson.startsWith("[")) {
            val arr = JSONArray(rawJson)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val code = obj.optString("code", "").trim()
                if (code.isNotEmpty()) {
                    val durationDays = obj.optLong("durationDays", 30L)
                    val validityMs = obj.optLong("validityDurationMs", durationDays * 24L * 3600_000L)
                    list.add(
                        AccessPassEntity(
                            code = code.uppercase(),
                            planName = obj.optString("planName", "KurdishFlim Bot Pass"),
                            holderName = obj.optString("holderName", "Telegram Subscriber"),
                            validityDurationMs = validityMs,
                            activatedAtEpochMillis = obj.optLong("activatedAtEpochMillis", 0L),
                            expiryEpochMillis = obj.optLong(
                                "expiryEpochMillis",
                                System.currentTimeMillis() + validityMs
                            ),
                            maxResolution = obj.optString("maxResolution", "4K HDR10+"),
                            botSource = obj.optString("botSource", "@KurdishFlim_4bot"),
                            isAdmin = obj.optBoolean("isAdmin", false),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }
        }
        return list
    }
}
