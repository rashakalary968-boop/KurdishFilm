package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CinemaDao {

    @Query("SELECT * FROM movies ORDER BY isFeatured DESC, isTrending DESC, rating DESC, updatedAt DESC")
    fun observeAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY isFeatured DESC, isTrending DESC, rating DESC")
    suspend fun getAllMoviesOnce(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: String): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: String)

    @Query("UPDATE movies SET isWatchlisted = :watchlisted WHERE id = :movieId")
    suspend fun updateWatchlistStatus(movieId: String, watchlisted: Boolean)

    @Query("UPDATE movies SET lastPlaybackPositionMs = :positionMs WHERE id = :movieId")
    suspend fun updatePlaybackPosition(movieId: String, positionMs: Long)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int

    // Access Passes
    @Query("SELECT * FROM access_passes ORDER BY expiryEpochMillis DESC")
    fun observeAllAccessPasses(): Flow<List<AccessPassEntity>>

    @Query("SELECT * FROM access_passes WHERE UPPER(code) = UPPER(:code) LIMIT 1")
    suspend fun findAccessPass(code: String): AccessPassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccessPass(pass: AccessPassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccessPasses(passes: List<AccessPassEntity>)

    @Query("DELETE FROM access_passes WHERE code = :code")
    suspend fun deleteAccessPass(code: String)

    @Query("SELECT COUNT(*) FROM access_passes")
    suspend fun getAccessPassCount(): Int

    // Cloud Config
    @Query("SELECT * FROM cloud_config WHERE id = 1 LIMIT 1")
    fun observeCloudConfig(): Flow<CloudConfigEntity?>

    @Query("SELECT * FROM cloud_config WHERE id = 1 LIMIT 1")
    suspend fun getCloudConfigOnce(): CloudConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCloudConfig(config: CloudConfigEntity)
}
