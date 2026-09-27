package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DonutSmall
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.DonutSmall
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.components.pebbleBouncyClickable
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.quickcapture.QuickCaptureContent
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DeepMatteSlate
import com.example.ui.theme.PebbleFlowTheme
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.WarmRicePaper
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuickCaptureUiState
import com.example.ui.viewmodel.QuickCaptureViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(PebbleFlowApplication.instance.repository)
    }

    private val quickCaptureViewModel: QuickCaptureViewModel by viewModels {
        QuickCaptureViewModel.Factory(PebbleFlowApplication.instance.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PebbleFlowTheme {
                MainAppScaffold(
                    mainViewModel = mainViewModel,
                    quickCaptureViewModel = quickCaptureViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    mainViewModel: MainViewModel,
    quickCaptureViewModel: QuickCaptureViewModel
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }
    var showQuickCaptureSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            quickCaptureViewModel.processSlipImage(context, uri)
            showQuickCaptureSheet = true
        }
    }

    val isDark = isSystemInDarkTheme()
    val navBg = if (isDark) DeepMatteSlate else WarmRicePaper
    val activeColor = if (currentTab == 0) CoralAccent else if (currentTab == 1) SkyBlueAccent else Color(0xFF059669)

    // Collect States from MainViewModel
    val todayTotalSpent by mainViewModel.todayTotalSpent.collectAsState()
    val donutSegments by mainViewModel.todayDonutSegments.collectAsState()
    val todaySmokeCount by mainViewModel.todaySmokeCount.collectAsState()
    val lastSmokeTimeString by mainViewModel.lastSmokeTimeString.collectAsState()
    val todaySmokeCost by mainViewModel.todaySmokeCost.collectAsState()
    val dailyTasks by mainViewModel.dailyTasks.collectAsState()
    val todayTransactions by mainViewModel.todayTransactions.collectAsState()

    val categorySpends by mainViewModel.monthCategorySpends.collectAsState()
    val rule503020 by mainViewModel.rule503020.collectAsState()
    val thirtyDaySmokeBars by mainViewModel.thirtyDaySmokeBars.collectAsState()
    val smokeSummary by mainViewModel.smokeThirtyDaysSummary.collectAsState()

    val userSettings by mainViewModel.userSettings.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = navBg,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("pebble_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Filled.Today else Icons.Outlined.Today,
                            contentDescription = "Today"
                        )
                    },
                    label = {
                        Text(
                            text = "Today",
                            fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralAccent,
                        selectedTextColor = CoralAccent,
                        indicatorColor = CoralAccent.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Filled.DonutSmall else Icons.Outlined.DonutSmall,
                            contentDescription = "Analytics"
                        )
                    },
                    label = {
                        Text(
                            text = "Trends",
                            fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkyBlueAccent,
                        selectedTextColor = SkyBlueAccent,
                        indicatorColor = SkyBlueAccent.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_trends")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text(
                            text = "Preferences",
                            fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF059669),
                        selectedTextColor = Color(0xFF059669),
                        indicatorColor = Color(0xFF059669).copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    todayTotalSpent = todayTotalSpent,
                    donutSegments = donutSegments,
                    todaySmokeCount = todaySmokeCount,
                    lastSmokeTimeString = lastSmokeTimeString,
                    todaySmokeCost = todaySmokeCost,
                    dailyTasks = dailyTasks,
                    todayTransactions = todayTransactions,
                    onUploadSlipClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onQuickSmokeClick = {
                        mainViewModel.logQuickSmoke()
                    },
                    onToggleTask = { task ->
                        mainViewModel.toggleTask(task)
                    },
                    onDeleteTransaction = { tx ->
                        mainViewModel.deleteTransaction(tx)
                    }
                )
                1 -> AnalyticsScreen(
                    categorySpends = categorySpends,
                    rule503020 = rule503020,
                    thirtyDaySmokeBars = thirtyDaySmokeBars,
                    smokeSummary = smokeSummary
                )
                2 -> SettingsScreen(
                    userSettings = userSettings,
                    onSaveSettings = { stickPrice, budget ->
                        mainViewModel.saveSettings(stickPrice, budget)
                    },
                    onClearAllData = {
                        mainViewModel.clearAllData()
                    },
                    onResetSampleData = {
                        mainViewModel.resetToSampleData()
                    },
                    onExportJson = {
                        mainViewModel.getExportJson()
                    }
                )
            }

            // In-app Quick Capture Bottom Sheet overlay when a slip is scanned
            AnimatedVisibility(
                visible = showQuickCaptureSheet,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val ocrUiState by quickCaptureViewModel.uiState.collectAsState()
                QuickCaptureContent(
                    uiState = ocrUiState,
                    onCategoryClick = { category ->
                        quickCaptureViewModel.saveAndFinish(category) {
                            showQuickCaptureSheet = false
                        }
                    },
                    onDismiss = {
                        showQuickCaptureSheet = false
                    },
                    onUpdateAmount = { newAmt ->
                        quickCaptureViewModel.setAmount(newAmt)
                    },
                    onUpdateMerchant = { newMerch ->
                        quickCaptureViewModel.setMerchant(newMerch)
                    }
                )
            }
        }
    }
}
