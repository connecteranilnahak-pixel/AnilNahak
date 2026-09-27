package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val stickPrice: Double = 18.0,
    val monthlyBudget: Double = 25000.0,
    val currencySymbol: String = "₹"
)
