package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SmokeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmokeLogDao {
    @Query("SELECT * FROM smoke_logs ORDER BY timestamp DESC")
    fun getAllSmokeLogs(): Flow<List<SmokeLogEntity>>

    @Query("SELECT * FROM smoke_logs WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp ASC")
    fun getSmokeLogsBetween(startTime: Long, endTime: Long): Flow<List<SmokeLogEntity>>

    @Query("SELECT * FROM smoke_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSmokeLog(): Flow<SmokeLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmokeLog(smokeLog: SmokeLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smokeLogs: List<SmokeLogEntity>)

    @Delete
    suspend fun deleteSmokeLog(smokeLog: SmokeLogEntity)

    @Query("DELETE FROM smoke_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM smoke_logs")
    suspend fun getCount(): Int
}
