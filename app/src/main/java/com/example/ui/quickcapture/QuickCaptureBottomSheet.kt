package com.example.ui.quickcapture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.ui.components.CategoryPill
import com.example.ui.components.PebbleRollingNumber
import com.example.ui.components.pebbleBouncyClickable
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DeepMatteSlate
import com.example.ui.theme.MatchaMintAccent
import com.example.ui.theme.WarmRicePaper
import com.example.ui.viewmodel.QuickCaptureUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCaptureContent(
    uiState: QuickCaptureUiState,
    onCategoryClick: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit,
    onUpdateAmount: (Double) -> Unit = {},
    onUpdateMerchant: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) DeepMatteSlate else WarmRicePaper
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Full screen overlay with translucent backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Sheet Card
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = sheetBg,
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercept click inside sheet
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .clip(CircleShape)
                        .background(subtitleColor.copy(alpha = 0.35f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Header with status indicator & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState is QuickCaptureUiState.Success) MatchaMintAccent else CoralAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState is QuickCaptureUiState.Analyzing) "SCANNING RECEIPT..." else "ONE-TAP LOG",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtitleColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quick_capture_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close sheet",
                            tint = subtitleColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (uiState) {
                    is QuickCaptureUiState.Analyzing -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = CoralAccent,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Running ML Kit OCR Engine on Dispatchers.IO...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtitleColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is QuickCaptureUiState.Success -> {
                        var isEditingAmount by remember { mutableStateOf(false) }
                        var isEditingMerchant by remember { mutableStateOf(false) }
                        var tempAmountText by remember(uiState.amount) {
                            mutableStateOf(if (uiState.amount % 1.0 == 0.0) uiState.amount.toInt().toString() else uiState.amount.toString())
                        }
                        var tempMerchantText by remember(uiState.merchant) { mutableStateOf(uiState.merchant) }
                        val focusManager = LocalFocusManager.current
                        val amountFocus = remember { FocusRequester() }
                        val merchantFocus = remember { FocusRequester() }

                        // Top: Detected amount displayed in massive bold font with small edit icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            if (isEditingAmount) {
                                BasicTextField(
                                    value = tempAmountText,
                                    onValueChange = { tempAmountText = it },
                                    textStyle = MaterialTheme.typography.displayLarge.copy(
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = SolidColor(CoralAccent),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            isEditingAmount = false
                                            val amt = tempAmountText.toDoubleOrNull() ?: uiState.amount
                                            onUpdateAmount(amt)
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    modifier = Modifier
                                        .focusRequester(amountFocus)
                                        .testTag("amount_edit_input")
                                )
                                IconButton(
                                    onClick = {
                                        isEditingAmount = false
                                        val amt = tempAmountText.toDoubleOrNull() ?: uiState.amount
                                        onUpdateAmount(amt)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = MatchaMintAccent)
                                }
                            } else {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Light,
                                    color = CoralAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                PebbleRollingNumber(
                                    valueText = tempAmountText,
                                    textStyle = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textColor
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isEditingAmount = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("edit_amount_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Amount",
                                        tint = subtitleColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Subtitle: "Paid to: [Merchant Name]" with editable capability
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            if (isEditingMerchant) {
                                BasicTextField(
                                    value = tempMerchantText,
                                    onValueChange = { tempMerchantText = it },
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = textColor,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = SolidColor(CoralAccent),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            isEditingMerchant = false
                                            onUpdateMerchant(tempMerchantText)
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    modifier = Modifier
                                        .focusRequester(merchantFocus)
                                        .testTag("merchant_edit_input")
                                )
                                IconButton(
                                    onClick = {
                                        isEditingMerchant = false
                                        onUpdateMerchant(tempMerchantText)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = MatchaMintAccent)
                                }
                            } else {
                                Text(
                                    text = "Paid to: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = subtitleColor
                                )
                                Text(
                                    text = uiState.merchant,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { isEditingMerchant = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Merchant",
                                        tint = subtitleColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category Grid: 6 pastel rounded pills
                        Text(
                            text = "Tap a category to log instantly (Auto-closes in 300ms)",
                            style = MaterialTheme.typography.labelMedium,
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = 3
                        ) {
                            ExpenseCategory.entries.forEach { category ->
                                CategoryPill(
                                    category = category,
                                    isSelected = category == uiState.selectedCategory,
                                    onClick = {
                                        onCategoryClick(category)
                                    },
                                    modifier = Modifier.testTag("pill_${category.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    is QuickCaptureUiState.Saved -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MatchaMintAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Saved",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Transaction Saved!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    is QuickCaptureUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoralAccent
                            )
                        }
                    }

                    QuickCaptureUiState.Idle -> {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
