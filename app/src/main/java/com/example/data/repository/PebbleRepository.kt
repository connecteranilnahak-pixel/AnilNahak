package com.example.data.repository

import com.example.data.local.DailyTaskDao
import com.example.data.local.SmokeLogDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserSettingsDao
import com.example.data.model.DailyTaskEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SmokeLogEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PebbleRepository(
    private val transactionDao: TransactionDao,
    private val smokeLogDao: SmokeLogDao,
    private val dailyTaskDao: DailyTaskDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allSmokeLogs: Flow<List<SmokeLogEntity>> = smokeLogDao.getAllSmokeLogs()
    val latestSmokeLog: Flow<SmokeLogEntity?> = smokeLogDao.getLatestSmokeLog()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettings()

    fun getTodayTransactions(): Flow<List<TransactionEntity>> {
        val (start, end) = getDayBounds(System.currentTimeMillis())
        return transactionDao.getTransactionsBetween(start, end)
    }

    fun getTransactionsForMonth(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startTime, endTime)
    }

    fun getTodaySmokeLogs(): Flow<List<SmokeLogEntity>> {
        val (start, end) = getDayBounds(System.currentTimeMillis())
        return smokeLogDao.getSmokeLogsBetween(start, end)
    }

    fun getSmokeLogsForMonth(startTime: Long, endTime: Long): Flow<List<SmokeLogEntity>> {
        return smokeLogDao.getSmokeLogsBetween(startTime, endTime)
    }

    fun getTodayTasks(dateKey: String = getTodayDateKey()): Flow<List<DailyTaskEntity>> {
        return dailyTaskDao.getTasksForDate(dateKey)
    }

    suspend fun insertTransaction(
        amount: Double,
        merchant: String,
        category: String,
        timestamp: Long = System.currentTimeMillis(),
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            amount = amount,
            merchant = merchant,
            category = category,
            timestamp = timestamp,
            note = note
        )
        val id = transactionDao.insertTransaction(entity)

        // If category is smoking, simultaneously register a smoke log
        if (category.equals(ExpenseCategory.SMOKING.id, ignoreCase = true)) {
            val settings = userSettingsDao.getSettings().firstOrNull() ?: UserSettingsEntity()
            val stickPrice = settings.stickPrice.coerceAtLeast(1.0)
            val sticksEstimated = (amount / stickPrice).toInt().coerceAtLeast(1)
            smokeLogDao.insertSmokeLog(
                SmokeLogEntity(
                    timestamp = timestamp,
                    stickCount = sticksEstimated,
                    cost = amount
                )
            )
        }
        id
    }

    suspend fun logSingleSmoke(timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val settings = userSettingsDao.getSettings().firstOrNull() ?: UserSettingsEntity()
        val stickPrice = settings.stickPrice
        smokeLogDao.insertSmokeLog(
            SmokeLogEntity(
                timestamp = timestamp,
                stickCount = 1,
                cost = stickPrice
            )
        )
        // Also log as an expense in transactions
        transactionDao.insertTransaction(
            TransactionEntity(
                amount = stickPrice,
                merchant = "Pebble Quick Smoke",
                category = ExpenseCategory.SMOKING.id,
                timestamp = timestamp,
                note = "1 Stick"
            )
        )
    }

    suspend fun deleteTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(tx)
    }

    suspend fun toggleTask(task: DailyTaskEntity) = withContext(Dispatchers.IO) {
        dailyTaskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun updateTaskTitle(task: DailyTaskEntity, newTitle: String) = withContext(Dispatchers.IO) {
        dailyTaskDao.updateTask(task.copy(title = newTitle))
    }

    suspend fun updateSettings(stickPrice: Double, monthlyBudget: Double) = withContext(Dispatchers.IO) {
        userSettingsDao.insertOrUpdate(
            UserSettingsEntity(
                id = 1,
                stickPrice = stickPrice,
                monthlyBudget = monthlyBudget
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        smokeLogDao.clearAll()
        dailyTaskDao.clearAll()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val txCount = transactionDao.getCount()
        if (txCount == 0) {
            seedSampleData()
        }

        // Ensure default settings exist
        val currentSettings = userSettingsDao.getSettings().firstOrNull()
        if (currentSettings == null) {
            userSettingsDao.insertOrUpdate(UserSettingsEntity())
        }

        // Ensure today has the Daily 3 tasks
        val todayKey = getTodayDateKey()
        val existingTasks = dailyTaskDao.getTasksForDate(todayKey).firstOrNull()
        if (existingTasks.isNullOrEmpty()) {
            dailyTaskDao.insertAll(
                listOf(
                    DailyTaskEntity(
                        title = "Keep daily smoking under 4 sticks",
                        isCompleted = false,
                        dateKey = todayKey,
                        orderIndex = 0
                    ),
                    DailyTaskEntity(
                        title = "Log all UPI/screenshot receipts instantly",
                        isCompleted = true,
                        dateKey = todayKey,
                        orderIndex = 1
                    ),
                    DailyTaskEntity(
                        title = "Stay within ₹850 today's spend target",
                        isCompleted = false,
                        dateKey = todayKey,
                        orderIndex = 2
                    )
                )
            )
        }
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        // Today's transactions
        val sampleTransactions = mutableListOf<TransactionEntity>()
        val sampleSmokes = mutableListOf<SmokeLogEntity>()

        // Today's transactions
        sampleTransactions.add(
            TransactionEntity(
                amount = 36.0,
                merchant = "Chai Corner Paan Shop",
                category = ExpenseCategory.SMOKING.id,
                timestamp = now - (2 * 60 * 60 * 1000L),
                note = "2 sticks"
            )
        )
        sampleSmokes.add(
            SmokeLogEntity(
                timestamp = now - (2 * 60 * 60 * 1000L),
                stickCount = 2,
                cost = 36.0
            )
        )

        sampleTransactions.add(
            TransactionEntity(
                amount = 40.0,
                merchant = "Sharma Tea Stall",
                category = ExpenseCategory.CHAI_SNACKS.id,
                timestamp = now - (3 * 60 * 60 * 1000L),
                note = "Chai + Samosa"
            )
        )

        sampleTransactions.add(
            TransactionEntity(
                amount = 240.0,
                merchant = "Uber India Mobility",
                category = ExpenseCategory.TRAVEL.id,
                timestamp = now - (5 * 60 * 60 * 1000L),
                note = "Cab to workspace"
            )
        )

        sampleTransactions.add(
            TransactionEntity(
                amount = 399.0,
                merchant = "Airtel Prepaid Recharge",
                category = ExpenseCategory.RECHARGE.id,
                timestamp = now - (1 * dayMillis),
                note = "28-day data pack"
            )
        )

        sampleTransactions.add(
            TransactionEntity(
                amount = 540.0,
                merchant = "Green Bowl Healthy Eatery",
                category = ExpenseCategory.FOOD.id,
                timestamp = now - (1 * dayMillis + 3 * 3600000L),
                note = "Lunch meal"
            )
        )

        sampleTransactions.add(
            TransactionEntity(
                amount = 1250.0,
                merchant = "Apollo Pharmacy Ltd",
                category = ExpenseCategory.MEDICINE.id,
                timestamp = now - (3 * dayMillis),
                note = "Monthly vitamins & allergy tabs"
            )
        )

        // Seed 30-day smoking trend data
        for (i in 1..28) {
            val pastTime = now - (i * dayMillis) - (i * 123456L % 36000000L)
            // Stagger stick count between 1 and 6
            val sticks = when {
                i % 7 == 0 -> 1 // Sunday cutdown
                i % 4 == 0 -> 2
                i % 3 == 0 -> 3
                i % 2 == 0 -> 4
                else -> 5
            }
            sampleSmokes.add(
                SmokeLogEntity(
                    timestamp = pastTime,
                    stickCount = sticks,
                    cost = sticks * 18.0
                )
            )

            if (i % 2 == 0) {
                sampleTransactions.add(
                    TransactionEntity(
                        amount = sticks * 18.0,
                        merchant = "Local Kiosk",
                        category = ExpenseCategory.SMOKING.id,
                        timestamp = pastTime,
                        note = "$sticks sticks"
                    )
                )
            }

            if (i % 3 == 0) {
                sampleTransactions.add(
                    TransactionEntity(
                        amount = (120 + (i * 35) % 450).toDouble(),
                        merchant = if (i % 2 == 0) "Swiggy Order" else "Metro Rail Smartcard",
                        category = if (i % 2 == 0) ExpenseCategory.FOOD.id else ExpenseCategory.TRAVEL.id,
                        timestamp = pastTime + 7200000L
                    )
                )
            }
        }

        transactionDao.insertAll(sampleTransactions)
        smokeLogDao.insertAll(sampleSmokes)
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val txs = transactionDao.getAllTransactions().firstOrNull() ?: emptyList()
        val smokes = smokeLogDao.getAllSmokeLogs().firstOrNull() ?: emptyList()
        val settings = userSettingsDao.getSettings().firstOrNull() ?: UserSettingsEntity()

        buildString {
            append("{\n")
            append("  \"exportedAt\": \"${Date()}\",\n")
            append("  \"settings\": {\n")
            append("    \"stickPrice\": ${settings.stickPrice},\n")
            append("    \"monthlyBudget\": ${settings.monthlyBudget}\n")
            append("  },\n")
            append("  \"transactions\": [\n")
            txs.forEachIndexed { index, tx ->
                append("    {\n")
                append("      \"id\": ${tx.id},\n")
                append("      \"amount\": ${tx.amount},\n")
                append("      \"merchant\": \"${tx.merchant.replace("\"", "\\\"")}\",\n")
                append("      \"category\": \"${tx.category}\",\n")
                append("      \"timestamp\": ${tx.timestamp},\n")
                append("      \"note\": \"${tx.note.replace("\"", "\\\"")}\"\n")
                append("    }${if (index < txs.size - 1) "," else ""}\n")
            }
            append("  ],\n")
            append("  \"smokeLogs\": [\n")
            smokes.forEachIndexed { index, s ->
                append("    {\n")
                append("      \"id\": ${s.id},\n")
                append("      \"timestamp\": ${s.timestamp},\n")
                append("      \"stickCount\": ${s.stickCount},\n")
                append("      \"cost\": ${s.cost}\n")
                append("    }${if (index < smokes.size - 1) "," else ""}\n")
            }
            append("  ]\n")
            append("}")
        }
    }

    companion object {
        fun getDayBounds(timestamp: Long): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val end = cal.timeInMillis
            return Pair(start, end)
        }

        fun getMonthBounds(timestamp: Long): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            return Pair(start, end)
        }

        fun getPast30DaysBounds(timestamp: Long = System.currentTimeMillis()): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val end = cal.timeInMillis
            val start = end - (30L * 24 * 60 * 60 * 1000L)
            return Pair(start, end)
        }

        fun getTodayDateKey(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }
    }
}
