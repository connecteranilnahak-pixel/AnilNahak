package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyTaskEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SmokeLogEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.PebbleRepository
import com.example.ui.components.DonutSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class DailySmokeBar(
    val dayLabel: String,
    val stickCount: Int,
    val timestamp: Long,
    val isToday: Boolean = false
)

data class CategorySpend(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float
)

data class Rule503020(
    val monthlyBudget: Double,
    val needsTarget: Double,
    val needsActual: Double,
    val wantsTarget: Double,
    val wantsActual: Double,
    val savingsTarget: Double,
    val actualSaved: Double
)

class MainViewModel(
    private val repository: PebbleRepository
) : ViewModel() {

    // Current time ticker for live last-smoke timer
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(30_000) // Update timer every 30 seconds
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
    }

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .combine(MutableStateFlow(UserSettingsEntity())) { dbSettings, defaultSettings ->
            dbSettings ?: defaultSettings
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettingsEntity()
        )

    val todayTransactions: StateFlow<List<TransactionEntity>> = repository.getTodayTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayTotalSpent: StateFlow<Double> = todayTransactions
        .combine(userSettings) { txs, _ ->
            txs.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val todayDonutSegments: StateFlow<List<DonutSegment>> = todayTransactions
        .combine(userSettings) { txs, _ ->
            val total = txs.sumOf { it.amount }
            if (total == 0.0) {
                emptyList()
            } else {
                val groupMap = txs.groupBy { it.category }
                ExpenseCategory.entries.mapNotNull { cat ->
                    val catTotal = groupMap[cat.id]?.sumOf { it.amount } ?: 0.0
                    if (catTotal > 0.0) {
                        DonutSegment(
                            id = cat.id,
                            value = catTotal,
                            color = cat.accentColor,
                            label = cat.displayName
                        )
                    } else null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySmokeLogs: StateFlow<List<SmokeLogEntity>> = repository.getTodaySmokeLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySmokeCount: StateFlow<Int> = todaySmokeLogs
        .combine(userSettings) { logs, _ ->
            logs.sumOf { it.stickCount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val latestSmokeLog: StateFlow<SmokeLogEntity?> = repository.latestSmokeLog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val lastSmokeTimeString: StateFlow<String> = combine(
        latestSmokeLog,
        _currentTimeMillis
    ) { log, now ->
        if (log == null) {
            "No smokes logged"
        } else {
            val diffMs = (now - log.timestamp).coerceAtLeast(0)
            val minutes = (diffMs / (60 * 1000)).toInt()
            val hours = minutes / 60
            val remainingMins = minutes % 60
            when {
                minutes < 1 -> "Just now"
                hours == 0 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ${remainingMins}m ago"
                else -> "${hours / 24}d ago"
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Loading..."
    )

    val todaySmokeCost: StateFlow<Double> = combine(
        todaySmokeCount,
        userSettings
    ) { count, settings ->
        count * settings.stickPrice
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val dailyTasks: StateFlow<List<DailyTaskEntity>> = repository.getTodayTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSmokeLogs: StateFlow<List<SmokeLogEntity>> = repository.allSmokeLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Month category spends and 50/30/20 calculation
    val monthCategorySpends: StateFlow<List<CategorySpend>> = combine(
        allTransactions,
        userSettings
    ) { txs, _ ->
        val (monthStart, monthEnd) = PebbleRepository.getPast30DaysBounds()
        val monthTxs = txs.filter { it.timestamp in monthStart..monthEnd }
        val total = monthTxs.sumOf { it.amount }
        if (total == 0.0) {
            emptyList()
        } else {
            val grouped = monthTxs.groupBy { it.category }
            ExpenseCategory.entries.mapNotNull { cat ->
                val amount = grouped[cat.id]?.sumOf { it.amount } ?: 0.0
                if (amount > 0) {
                    CategorySpend(
                        category = cat,
                        totalAmount = amount,
                        percentage = ((amount / total) * 100).toFloat()
                    )
                } else null
            }.sortedByDescending { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val rule503020: StateFlow<Rule503020> = combine(
        monthCategorySpends,
        userSettings
    ) { spends, settings ->
        val budget = settings.monthlyBudget
        val needsTarget = budget * 0.50
        val wantsTarget = budget * 0.30
        val savingsTarget = budget * 0.20

        var needsActual = 0.0
        var wantsActual = 0.0

        for (item in spends) {
            when (item.category) {
                ExpenseCategory.MEDICINE,
                ExpenseCategory.TRAVEL,
                ExpenseCategory.FOOD,
                ExpenseCategory.RECHARGE -> needsActual += item.totalAmount
                ExpenseCategory.SMOKING,
                ExpenseCategory.CHAI_SNACKS -> wantsActual += item.totalAmount
            }
        }

        val totalSpent = needsActual + wantsActual
        val actualSaved = (budget - totalSpent).coerceAtLeast(0.0)

        Rule503020(
            monthlyBudget = budget,
            needsTarget = needsTarget,
            needsActual = needsActual,
            wantsTarget = wantsTarget,
            wantsActual = wantsActual,
            savingsTarget = savingsTarget,
            actualSaved = actualSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Rule503020(25000.0, 12500.0, 0.0, 7500.0, 0.0, 5000.0, 25000.0)
    )

    // 30-Day Smoking Deep Dive
    val thirtyDaySmokeBars: StateFlow<List<DailySmokeBar>> = combine(
        allSmokeLogs,
        _currentTimeMillis
    ) { logs, now ->
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val bars = mutableListOf<DailySmokeBar>()
        val dayFmt = SimpleDateFormat("dd", Locale.US)

        for (i in 29 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now - (i * 24L * 60 * 60 * 1000L)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = dayCal.timeInMillis
            val endMs = startMs + (24L * 60 * 60 * 1000L)

            val countInDay = logs.filter { it.timestamp in startMs until endMs }
                .sumOf { it.stickCount }

            bars.add(
                DailySmokeBar(
                    dayLabel = dayFmt.format(Date(startMs)),
                    stickCount = countInDay,
                    timestamp = startMs,
                    isToday = i == 0
                )
            )
        }
        bars
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val smokeThirtyDaysSummary: StateFlow<Triple<Int, Double, String>> = combine(
        thirtyDaySmokeBars,
        userSettings
    ) { bars, settings ->
        val totalSticks = bars.sumOf { it.stickCount }
        val totalBurnt = totalSticks * settings.stickPrice

        // Calculate longest smoke free streak in days
        var maxStreakDays = 0
        var currentStreak = 0
        for (bar in bars) {
            if (bar.stickCount == 0) {
                currentStreak++
                if (currentStreak > maxStreakDays) maxStreakDays = currentStreak
            } else {
                currentStreak = 0
            }
        }
        val streakText = if (maxStreakDays > 0) "$maxStreakDays days" else "18 hours"
        Triple(totalSticks, totalBurnt, streakText)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple(0, 0.0, "0 days")
    )

    // Actions
    fun logQuickSmoke() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logSingleSmoke()
            _currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    fun toggleTask(task: DailyTaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTask(task)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(tx)
        }
    }

    fun saveSettings(stickPrice: Double, monthlyBudget: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSettings(stickPrice, monthlyBudget)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            repository.seedSampleData()
            repository.seedInitialDataIfEmpty()
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportDataAsJson()
    }

    class Factory(private val repository: PebbleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
