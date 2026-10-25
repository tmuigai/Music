package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplayDao {
    // Tracks
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY lastPlayedMs DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY replayCount DESC LIMIT 20")
    fun getMostPlayedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedMs > 0 ORDER BY lastPlayedMs DESC LIMIT 25")
    fun getRecentlyPlayedTracks(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFav WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: Long, isFav: Boolean)

    @Query("UPDATE tracks SET replayCount = replayCount + 1, lastPlayedMs = :playedAt WHERE id = :trackId")
    suspend fun incrementReplay(trackId: Long, playedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET customLyrics = :lyrics WHERE id = :trackId")
    suspend fun updateLyrics(trackId: Long, lyrics: String)

    // Mixtapes
    @Query("SELECT * FROM mixtapes ORDER BY createdMs DESC")
    fun getAllMixtapes(): Flow<List<MixtapeEntity>>

    @Query("SELECT * FROM mixtapes WHERE id = :id")
    suspend fun getMixtapeById(id: Long): MixtapeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMixtape(mixtape: MixtapeEntity): Long

    @Query("DELETE FROM mixtapes WHERE id = :id")
    suspend fun deleteMixtape(id: Long)

    // Listening History
    @Insert
    suspend fun insertHistory(history: ListeningHistoryEntity)

    @Query("SELECT * FROM listening_history ORDER BY playedAtMs DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<ListeningHistoryEntity>>

    @Query("SELECT COUNT(*) FROM listening_history")
    fun getTotalListenCount(): Flow<Int>

    // Recordings
    @Query("SELECT * FROM recordings ORDER BY timestampMs DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)

    // Obsession Profiles
    @Query("SELECT * FROM obsession_profiles ORDER BY createdMs DESC")
    fun getAllObsessions(): Flow<List<ObsessionProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObsession(profile: ObsessionProfileEntity): Long

    @Query("UPDATE obsession_profiles SET currentReplays = currentReplays + 1 WHERE id = :id")
    suspend fun incrementObsessionPlay(id: Long)

    @Query("DELETE FROM obsession_profiles WHERE id = :id")
    suspend fun deleteObsession(id: Long)

    // Radio Presets
    @Query("SELECT * FROM radio_presets ORDER BY freqMhz ASC")
    fun getAllRadioPresets(): Flow<List<RadioPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRadioPresets(presets: List<RadioPresetEntity>)
}
