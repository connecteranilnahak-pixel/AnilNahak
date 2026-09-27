package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dateKey: String = "", // e.g. "2026-09-27"
    val orderIndex: Int = 0
)
