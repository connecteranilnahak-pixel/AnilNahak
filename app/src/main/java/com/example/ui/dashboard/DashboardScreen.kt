package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyTaskEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.ui.components.ConfettiSparkEffect
import com.example.ui.components.DonutChart
import com.example.ui.components.DonutSegment
import com.example.ui.components.PebbleRollingNumber
import com.example.ui.components.pebbleBouncyClickable
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CoralCard
import com.example.ui.theme.DeepMatteSlateCard
import com.example.ui.theme.MatchaMintAccent
import com.example.ui.theme.MatchaMintCard
import com.example.ui.theme.PeachAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.WarmRicePaperElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    todayTotalSpent: Double,
    donutSegments: List<DonutSegment>,
    todaySmokeCount: Int,
    lastSmokeTimeString: String,
    todaySmokeCost: Double,
    dailyTasks: List<DailyTaskEntity>,
    todayTransactions: List<TransactionEntity>,
    onUploadSlipClick: () -> Unit,
    onQuickSmokeClick: () -> Unit,
    onToggleTask: (DailyTaskEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) DeepMatteSlateCard else WarmRicePaperElevated
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Formatter for transaction times
    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PEBBLEFLOW",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralAccent
                    )
                    Text(
                        text = "Today's Flow",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor
                    )
                }

                // Retro Battery / Pulse pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MatchaMintAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "120Hz Fast",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = subtitleColor
                        )
                    }
                }
            }
        }

        // Top Pebble Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        text = "TOTAL SPENT TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Donut Ring Chart with center Mechanical Rolling Numbers
                    DonutChart(
                        segments = donutSegments,
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CoralAccent
                            )
                            PebbleRollingNumber(
                                valueText = if (todayTotalSpent % 1.0 == 0.0) todayTotalSpent.toInt().toString() else "%.1f".format(todayTotalSpent),
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "${todayTransactions.size} transactions",
                                style = MaterialTheme.typography.labelSmall,
                                color = subtitleColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Action Buttons: [📸 Share/Upload Slip] and [🚬 +1 Smoke]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onUploadSlipClick,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) SkyBlueAccent else Color(0xFF0284C7)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .pebbleBouncyClickable(onClick = onUploadSlipClick)
                                .testTag("upload_slip_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan Slip",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Slip",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onQuickSmokeClick,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralAccent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .pebbleBouncyClickable(onClick = onQuickSmokeClick)
                                .testTag("quick_smoke_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmokingRooms,
                                contentDescription = "Quick Smoke",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+1 Smoke",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Smoking Card (Special Habit Section)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF2A151C) else CoralCard
                ),
                border = BorderStroke(1.5.dp, CoralAccent.copy(alpha = 0.6f)),
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CoralAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🚬", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SMOKING MONITOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAccent
                                )
                                Text(
                                    text = "Habit & Health Tracking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtitleColor
                                )
                            }
                        }

                        // Live Smoke Timer Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoralAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CoralAccent.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Timer",
                                    tint = CoralAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = lastSmokeTimeString,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Cigarettes Today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtitleColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PebbleRollingNumber(
                                    valueText = todaySmokeCount.toString(),
                                    textStyle = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFEE2E2) else Color(0xFF881337)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "sticks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = subtitleColor
                                )
                            }
                        }

                        // Auto Cost Metric
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cost Today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtitleColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAccent
                                )
                                PebbleRollingNumber(
                                    valueText = if (todaySmokeCost % 1.0 == 0.0) todaySmokeCost.toInt().toString() else "%.1f".format(todaySmokeCost),
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFEE2E2) else Color(0xFF881337)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // The Daily 3 (Priority Tasks)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                text = "THE DAILY 3",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatchaMintAccent
                            )
                            Text(
                                text = "Today's Essential Goals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }

                        val completedCount = dailyTasks.count { it.isCompleted }
                        Text(
                            text = "$completedCount/3 Done",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (completedCount == 3) MatchaMintAccent else subtitleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    dailyTasks.take(3).forEach { task ->
                        DailyTaskRow(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Today's Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Receipts",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
                Text(
                    text = "${todayTransactions.size} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🧾", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No payments logged today yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share a GPay/PhonePe slip or tap 'Scan Slip' to log!",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                }
            }
        } else {
            items(todayTransactions, key = { it.id }) { tx ->
                val category = ExpenseCategory.fromId(tx.category)
                val catBg = if (isDark) category.darkCardColor else category.lightCardColor

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = category.iconEmoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.merchant,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = category.accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = " • ${timeFmt.format(Date(tx.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = subtitleColor
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${if (tx.amount % 1.0 == 0.0) tx.amount.toInt() else "%.1f".format(tx.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            IconButton(
                                onClick = { onDeleteTransaction(tx) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_tx_${tx.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete transaction",
                                    tint = subtitleColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTaskRow(
    task: DailyTaskEntity,
    onToggle: () -> Unit,
    isDark: Boolean
) {
    var triggerSpark by remember { mutableStateOf(false) }
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (task.isCompleted) {
                if (isDark) Color(0xFF0D281E) else MatchaMintCard
            } else {
                if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
            },
            border = BorderStroke(
                1.dp,
                if (task.isCompleted) MatchaMintAccent.copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .pebbleBouncyClickable(
                    hapticFeedback = true,
                    onClick = {
                        if (!task.isCompleted) {
                            triggerSpark = true
                        }
                        onToggle()
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) MatchaMintAccent else Color.Transparent
                        )
                        .then(
                            if (!task.isCompleted) Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .then(
                                    Modifier.background(
                                        brush = Brush.radialGradient(listOf(Color.Transparent, Color.Transparent)),
                                        shape = CircleShape
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (task.isCompleted) subtitleColor else textColor,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Celebratory confetti spark animation
        if (triggerSpark) {
            ConfettiSparkEffect(
                isTriggered = triggerSpark,
                modifier = Modifier.align(Alignment.CenterStart),
                onAnimationEnd = { triggerSpark = false }
            )
        }
    }
}
