package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.PebbleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PebbleFlowApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        PebbleRepository(
            transactionDao = database.transactionDao(),
            smokeLogDao = database.smokeLogDao(),
            dailyTaskDao = database.dailyTaskDao(),
            userSettingsDao = database.userSettingsDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Seed initial data if DB is empty
        applicationScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    companion object {
        lateinit var instance: PebbleFlowApplication
            private set
    }
}
