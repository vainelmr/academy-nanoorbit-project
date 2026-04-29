package com.nanoorbit.groundcontrol.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteDao {
    @Query("SELECT * FROM satellites ORDER BY idSatellite")
    fun getAllSatellites(): Flow<List<SatelliteEntity>>

    @Query("SELECT * FROM satellites WHERE idSatellite = :id")
    fun getSatelliteById(id: String): Flow<SatelliteEntity?>

    @Upsert
    suspend fun upsertAll(satellites: List<SatelliteEntity>)

    @Query("DELETE FROM satellites")
    suspend fun clearAll()

    @Query("SELECT MAX(cachedAt) FROM satellites")
    fun getLatestCacheTimestamp(): Flow<Long?>
}
