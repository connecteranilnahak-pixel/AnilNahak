package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CoralCard
import com.example.ui.theme.CoralDarkCard
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.LavenderCard
import com.example.ui.theme.LavenderDarkCard
import com.example.ui.theme.LilacAccent
import com.example.ui.theme.LilacCard
import com.example.ui.theme.LilacDarkCard
import com.example.ui.theme.MatchaMintAccent
import com.example.ui.theme.MatchaMintCard
import com.example.ui.theme.MatchaMintDarkCard
import com.example.ui.theme.PeachAccent
import com.example.ui.theme.PeachCard
import com.example.ui.theme.PeachDarkCard
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SkyBlueCard
import com.example.ui.theme.SkyBlueDarkCard

enum class ExpenseCategory(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val lightCardColor: Color,
    val accentColor: Color,
    val darkCardColor: Color
) {
    SMOKING(
        id = "smoking",
        displayName = "Smoking",
        iconEmoji = "🚬",
        lightCardColor = CoralCard,
        accentColor = CoralAccent,
        darkCardColor = CoralDarkCard
    ),
    RECHARGE(
        id = "recharge",
        displayName = "Recharge",
        iconEmoji = "📱",
        lightCardColor = SkyBlueCard,
        accentColor = SkyBlueAccent,
        darkCardColor = SkyBlueDarkCard
    ),
    CHAI_SNACKS(
        id = "chai_snacks",
        displayName = "Chai/Snacks",
        iconEmoji = "☕",
        lightCardColor = MatchaMintCard,
        accentColor = MatchaMintAccent,
        darkCardColor = MatchaMintDarkCard
    ),
    FOOD(
        id = "food",
        displayName = "Food",
        iconEmoji = "🍛",
        lightCardColor = PeachCard,
        accentColor = PeachAccent,
        darkCardColor = PeachDarkCard
    ),
    TRAVEL(
        id = "travel",
        displayName = "Travel",
        iconEmoji = "🚕",
        lightCardColor = LavenderCard,
        accentColor = LavenderAccent,
        darkCardColor = LavenderDarkCard
    ),
    MEDICINE(
        id = "medicine",
        displayName = "Medicine",
        iconEmoji = "💊",
        lightCardColor = LilacCard,
        accentColor = LilacAccent,
        darkCardColor = LilacDarkCard
    );

    companion object {
        fun fromId(id: String): ExpenseCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FOOD
        }
    }
}
