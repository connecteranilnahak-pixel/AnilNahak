package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyTaskEntity
import com.example.data.model.SmokeLogEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingsEntity

@Database(
    entities = [
        TransactionEntity::class,
        SmokeLogEntity::class,
        DailyTaskEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun smokeLogDao(): SmokeLogDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pebbleflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
