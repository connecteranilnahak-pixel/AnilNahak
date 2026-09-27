package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dateKey = :dateKey ORDER BY orderIndex ASC")
    fun getTasksForDate(dateKey: String): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DailyTaskEntity>)

    @Update
    suspend fun updateTask(task: DailyTaskEntity)

    @Delete
    suspend fun deleteTask(task: DailyTaskEntity)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAll()
}
