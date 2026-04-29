package com.nanoorbit.groundcontrol.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FenetreDao {
    @Query("SELECT * FROM fenetres_com ORDER BY datetimeDebut")
    fun getAllFenetres(): Flow<List<FenetreEntity>>

    @Upsert
    suspend fun upsertAll(fenetres: List<FenetreEntity>)

    @Query("DELETE FROM fenetres_com")
    suspend fun clearAll()
}
