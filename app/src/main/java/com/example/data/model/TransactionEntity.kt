package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val category: String, // e.g. "smoking", "recharge", "chai_snacks", "food", "travel", "medicine"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
