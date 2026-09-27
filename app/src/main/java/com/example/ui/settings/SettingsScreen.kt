package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSettingsEntity
import com.example.ui.components.pebbleBouncyClickable
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DeepMatteSlateCard
import com.example.ui.theme.MatchaMintAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.WarmRicePaperElevated
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    onSaveSettings: (Double, Double) -> Unit,
    onClearAllData: () -> Unit,
    onResetSampleData: () -> Unit,
    onExportJson: suspend () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) DeepMatteSlateCard else WarmRicePaperElevated
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var stickPriceText by remember(userSettings.stickPrice) {
        mutableStateOf(userSettings.stickPrice.toString())
    }
    var monthlyBudgetText by remember(userSettings.monthlyBudget) {
        mutableStateOf(userSettings.monthlyBudget.toInt().toString())
    }

    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PREFERENCES & DATA",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatchaMintAccent
                )
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textColor
                )
            }
        }

        // Section 1: Financial & Habit Baselines
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Preferences",
                            tint = SkyBlueAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tracking Baselines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Default stick price
                    OutlinedTextField(
                        value = stickPriceText,
                        onValueChange = { stickPriceText = it },
                        label = { Text("Default Price per Stick (₹)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SmokingRooms,
                                contentDescription = "Stick Price",
                                tint = CoralAccent
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            focusedLabelColor = CoralAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_stick_price")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Monthly Expense Budget Goal
                    OutlinedTextField(
                        value = monthlyBudgetText,
                        onValueChange = { monthlyBudgetText = it },
                        label = { Text("Monthly Expense Budget Goal (₹)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Budget Goal",
                                tint = MatchaMintAccent
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatchaMintAccent,
                            focusedLabelColor = MatchaMintAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_monthly_budget")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val price = stickPriceText.toDoubleOrNull() ?: userSettings.stickPrice
                            val budget = monthlyBudgetText.toDoubleOrNull() ?: userSettings.monthlyBudget
                            onSaveSettings(price, budget)
                            Toast.makeText(context, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pebbleBouncyClickable {
                                val price = stickPriceText.toDoubleOrNull() ?: userSettings.stickPrice
                                val budget = monthlyBudgetText.toDoubleOrNull() ?: userSettings.monthlyBudget
                                onSaveSettings(price, budget)
                                Toast.makeText(context, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("save_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Preferences",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 2: Data & Export
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
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export JSON button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = onExportJson()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_SUBJECT, "PebbleFlow Data Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export PebbleFlow Database"))
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SkyBlueAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pebbleBouncyClickable {
                                scope.launch {
                                    val json = onExportJson()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "PebbleFlow Data Backup")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export PebbleFlow Database"))
                                }
                            }
                            .testTag("export_data_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export JSON",
                            tint = SkyBlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Database as JSON",
                            color = SkyBlueAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reset to Sample Data
                    OutlinedButton(
                        onClick = {
                            onResetSampleData()
                            Toast.makeText(context, "Populated sample transactions and habits!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MatchaMintAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pebbleBouncyClickable {
                                onResetSampleData()
                                Toast.makeText(context, "Populated sample transactions and habits!", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("reset_sample_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Sample Data",
                            tint = MatchaMintAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset with Sample Data",
                            color = MatchaMintAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clear All Data
                    Button(
                        onClick = { showClearDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralAccent.copy(alpha = 0.15f),
                            contentColor = CoralAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pebbleBouncyClickable { showClearDialog = true }
                            .testTag("clear_all_data_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear All",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clear All Local Data",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 3: App Specs & Performance Info
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
                    Text(
                        text = "Device Performance & Physics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 120Hz AMOLED Refresh Engine\n• Mechanical Odometer Rolls (Spring.StiffnessMediumLow)\n• Room DB on CoroutineScope(Dispatchers.IO)\n• On-device Google ML Kit OCR Engine\n• Haptic LocalHapticFeedback feedback on pills & actions",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear all PebbleFlow data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will wipe all transactions, daily tasks, and smoke logs from your local Room database. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                        Toast.makeText(context, "All data wiped.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
