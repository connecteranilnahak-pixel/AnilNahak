package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory

@Composable
fun CategoryPill(
    category: ExpenseCategory,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) category.darkCardColor else category.lightCardColor
    val borderColor = if (isSelected) category.accentColor else category.accentColor.copy(alpha = 0.25f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val textColor = if (isDark) Color(0xFFF8FAFC) else category.accentColor

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .pebbleBouncyClickable(hapticFeedback = true, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.iconEmoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
