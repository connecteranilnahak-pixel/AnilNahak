package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseCategory
import com.example.ocr.SlipOcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PebbleFlow", appName)
    }

    @Test
    fun `ocr regex parser extracts amount and merchant accurately`() {
        val mockSlipText = """
            Google Pay
            Payment Completed
            Paid to
            Sharma Tea Stall
            ₹ 150.00
            UPI Transaction ID: 412356789012
        """.trimIndent()

        val parsed = SlipOcrParser.parseExtractedText(mockSlipText)
        assertEquals(150.0, parsed.amount, 0.01)
        assertEquals("Sharma Tea Stall", parsed.merchant)
        assertEquals(ExpenseCategory.CHAI_SNACKS, parsed.suggestedCategory)
    }

    @Test
    fun `ocr regex parser detects smoking keyword and amount`() {
        val mockSmokingSlip = """
            PhonePe
            Paid to
            Paan & Smoke Corner
            INR 36
            Txn ID: T240927001
        """.trimIndent()

        val parsed = SlipOcrParser.parseExtractedText(mockSmokingSlip)
        assertEquals(36.0, parsed.amount, 0.01)
        assertEquals(ExpenseCategory.SMOKING, parsed.suggestedCategory)
    }
}
