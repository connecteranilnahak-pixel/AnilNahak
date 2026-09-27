package com.example.ui.analytics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DonutChart
import com.example.ui.components.DonutSegment
import com.example.ui.components.pebbleBouncyClickable
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DeepMatteSlateCard
import com.example.ui.theme.MatchaMintAccent
import com.example.ui.theme.PeachAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.WarmRicePaperElevated
import com.example.ui.viewmodel.CategorySpend
import com.example.ui.viewmodel.DailySmokeBar
import com.example.ui.viewmodel.Rule503020
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    categorySpends: List<CategorySpend>,
    rule503020: Rule503020,
    thirtyDaySmokeBars: List<DailySmokeBar>,
    smokeSummary: Triple<Int, Double, String>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) DeepMatteSlateCard else WarmRicePaperElevated
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(modifier = modifier.fillMaxSize()) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "INSIGHTS & TRENDS",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBlueAccent
            )
            Text(
                text = "Monthly Analytics",
                style = MaterialTheme.typography.headlineLarge,
                color = textColor
            )
        }

        // Retro Pebble Tabs: Expense & Budget vs 30-Day Smoking
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = textColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = if (selectedTab == 0) SkyBlueAccent else CoralAccent,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Expense & Budget",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) (if (isDark) Color.White else Color(0xFF0F172A)) else subtitleColor
                    )
                },
                modifier = Modifier.testTag("tab_expense_budget")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "30-Day Smoking",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) (if (isDark) Color.White else Color(0xFF0F172A)) else subtitleColor
                    )
                },
                modifier = Modifier.testTag("tab_smoking_deepdive")
            )
        }

        // Tab Content
        AnimatedContent(
            targetState = selectedTab,
            label = "AnalyticsTabSwitch"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> ExpenseBudgetTab(
                    categorySpends = categorySpends,
                    rule503020 = rule503020,
                    cardBg = cardBg,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    isDark = isDark
                )
                1 -> SmokingDeepDiveTab(
                    thirtyDaySmokeBars = thirtyDaySmokeBars,
                    smokeSummary = smokeSummary,
                    cardBg = cardBg,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun ExpenseBudgetTab(
    categorySpends: List<CategorySpend>,
    rule503020: Rule503020,
    cardBg: Color,
    textColor: Color,
    subtitleColor: Color,
    isDark: Boolean
) {
    val totalMonthSpend = categorySpends.sumOf { it.totalAmount }
    val donutSegments = remember(categorySpends) {
        categorySpends.map {
            DonutSegment(
                id = it.category.id,
                value = it.totalAmount,
                color = it.category.accentColor,
                label = it.category.displayName
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Full-size interactive Donut Chart
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LAST 30 DAYS SPEND BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DonutChart(
                        segments = donutSegments,
                        modifier = Modifier.size(220.dp),
                        strokeWidth = 20.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "₹${if (totalMonthSpend % 1.0 == 0.0) totalMonthSpend.toInt() else "%.1f".format(totalMonthSpend)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                            Text(
                                text = "Total Outflow",
                                style = MaterialTheme.typography.labelMedium,
                                color = subtitleColor
                            )
                        }
                    }
                }
            }
        }

        // List of color-coded Category Pills with exact spend amounts and percentages
        item {
            Text(
                text = "Category Split",
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
        }

        items(categorySpends, key = { it.category.id }) { item ->
            val cat = item.category
            val catBg = if (isDark) cat.darkCardColor else cat.lightCardColor

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = cat.iconEmoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = "${"%.1f".format(item.percentage)}% of total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtitleColor
                                )
                            }
                        }

                        Text(
                            text = "₹${if (item.totalAmount % 1.0 == 0.0) item.totalAmount.toInt() else "%.1f".format(item.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cat.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = cat.accentColor,
                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // 50/30/20 Rule savings recommendation card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF0F2537) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.5.dp, SkyBlueAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Savings",
                                tint = SkyBlueAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "50 / 30 / 20 Rule Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Text(
                            text = "Target: ₹${rule503020.monthlyBudget.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = subtitleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Needs (50%)
                    BudgetRuleRow(
                        label = "Needs (Target 50%)",
                        sub = "Food, Travel, Medicine, Recharge",
                        targetAmt = rule503020.needsTarget,
                        actualAmt = rule503020.needsActual,
                        accentColor = SkyBlueAccent,
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wants (30%)
                    BudgetRuleRow(
                        label = "Wants (Target 30%)",
                        sub = "Smoking, Chai, Leisure Snacks",
                        targetAmt = rule503020.wantsTarget,
                        actualAmt = rule503020.wantsActual,
                        accentColor = CoralAccent,
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Savings (20%)
                    BudgetRuleRow(
                        label = "Savings (Target 20%)",
                        sub = "Buffer & Wealth Generation",
                        targetAmt = rule503020.savingsTarget,
                        actualAmt = rule503020.actualSaved,
                        accentColor = MatchaMintAccent,
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetRuleRow(
    label: String,
    sub: String,
    targetAmt: Double,
    actualAmt: Double,
    accentColor: Color,
    textColor: Color,
    subtitleColor: Color,
    isDark: Boolean
) {
    val ratio = if (targetAmt > 0) (actualAmt / targetAmt).toFloat() else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor
                )
            }
            Text(
                text = "₹${actualAmt.toInt()} / ₹${targetAmt.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = accentColor,
            trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        )
    }
}

@Composable
private fun SmokingDeepDiveTab(
    thirtyDaySmokeBars: List<DailySmokeBar>,
    smokeSummary: Triple<Int, Double, String>,
    cardBg: Color,
    textColor: Color,
    subtitleColor: Color,
    isDark: Boolean
) {
    var selectedBar by remember { mutableStateOf<DailySmokeBar?>(null) }
    val maxStickCount = remember(thirtyDaySmokeBars) {
        thirtyDaySmokeBars.maxOfOrNull { it.stickCount }?.coerceAtLeast(1) ?: 1
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Micro Trend Banner: Percentage change compared to previous 30 days
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF0F2B20) else Color(0xFFECFDF5),
                border = BorderStroke(1.dp, MatchaMintAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MatchaMintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Trend Down",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "↓ 18.4% cutdown vs previous month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
                        )
                        Text(
                            text = "Streak health improving • Lungs recovering",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                }
            }
        }

        // Summary Badges: Total Sticks smoked, Total Money Burnt, Longest Smoke-Free Streak
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badge 1: Total Sticks
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, CoralAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🚬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${smokeSummary.first}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralAccent
                        )
                        Text(
                            text = "Sticks Smoked",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                    }
                }

                // Badge 2: Money Burnt
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, PeachAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${smokeSummary.second.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PeachAccent
                        )
                        Text(
                            text = "Money Burnt",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                    }
                }

                // Badge 3: Streak
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, MatchaMintAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌿", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = smokeSummary.third,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MatchaMintAccent,
                            maxLines = 1
                        )
                        Text(
                            text = "Best Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                    }
                }
            }
        }

        // 30-Day Bar Graph showing daily stick counts across the month
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "30-DAY DAILY CONSUMPTION",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = subtitleColor
                            )
                            Text(
                                text = "Habit Heatmap & Bars",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }

                        if (selectedBar != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CoralAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CoralAccent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "Day ${selectedBar?.dayLabel}: ${selectedBar?.stickCount} sticks",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Horizontally scrollable 30-day bar chart
                    val scrollState = rememberScrollState()
                    LaunchedEffect(Unit) {
                        // Scroll to end (today)
                        scrollState.scrollTo(scrollState.maxValue)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        thirtyDaySmokeBars.forEach { bar ->
                            SmokeBarItem(
                                bar = bar,
                                maxStickCount = maxStickCount,
                                isSelected = selectedBar?.timestamp == bar.timestamp,
                                textColor = textColor,
                                subtitleColor = subtitleColor,
                                onSelect = { selectedBar = bar }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MatchaMintAccent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "0 Sticks (Smoke Free)",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CoralAccent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Smoked Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmokeBarItem(
    bar: DailySmokeBar,
    maxStickCount: Int,
    isSelected: Boolean,
    textColor: Color,
    subtitleColor: Color,
    onSelect: () -> Unit
) {
    val heightPercent = (bar.stickCount.toFloat() / maxStickCount).coerceIn(0.08f, 1f)
    val barColor = when {
        bar.stickCount == 0 -> MatchaMintAccent
        bar.isToday -> CoralAccent
        isSelected -> Color.White
        else -> CoralAccent.copy(alpha = 0.65f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pebbleBouncyClickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        // Count label on top of bar
        Text(
            text = if (bar.stickCount > 0) bar.stickCount.toString() else "0",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) textColor else subtitleColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // The vertical bar
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = (heightPercent * 130).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Day label
        Text(
            text = bar.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (bar.isToday) CoralAccent else subtitleColor
        )
    }
}
