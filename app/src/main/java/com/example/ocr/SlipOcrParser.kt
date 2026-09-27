package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.model.ExpenseCategory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ParsedSlipResult(
    val amount: Double,
    val merchant: String,
    val rawText: String,
    val suggestedCategory: ExpenseCategory
)

object SlipOcrParser {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Regex 1: Matches currency symbol/prefix followed by numbers, e.g. ₹150, Rs. 240.50, INR 399
    private val AMOUNT_REGEX_CURRENCY = Pattern.compile(
        """(?:₹|Rs\.?|INR)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex 2: Words followed by amounts: Paid to / Payment of / Transfer to / Paid / Debited / Sent
    private val AMOUNT_REGEX_WORDS = Pattern.compile(
        """(?:Paid\s+to|Payment\s+of|Transfer\s+to|Paid|Debited|Sent|Total\s+Amount)\s*(?:₹|Rs\.?|INR)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex 3: Standalone 2-5 digit numbers that look like rupee payment amounts (e.g. 18.00, 150, 499)
    private val STANDALONE_NUMBER_REGEX = Pattern.compile(
        """\b([0-9]{2,5}(?:\.[0-9]{1,2})?)\b"""
    )

    suspend fun parseFromUri(context: Context, uri: Uri): ParsedSlipResult = withContext(Dispatchers.IO) {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            // Fallback load bitmap downsampled
            val bitmap = decodeSampledBitmapFromUri(context, uri, 1200, 1200)
            if (bitmap != null) {
                InputImage.fromBitmap(bitmap, 0)
            } else {
                return@withContext defaultFallbackResult("Failed to open image")
            }
        }
        recognizeAndParse(image)
    }

    suspend fun parseFromBitmap(bitmap: Bitmap): ParsedSlipResult = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizeAndParse(image)
    }

    private suspend fun recognizeAndParse(image: InputImage): ParsedSlipResult = suspendCancellableCoroutine { cont ->
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val parsed = parseExtractedText(fullText)
                if (cont.isActive) cont.resume(parsed)
            }
            .addOnFailureListener { exc ->
                if (cont.isActive) cont.resume(defaultFallbackResult("OCR failed: ${exc.localizedMessage}"))
            }
    }

    fun parseExtractedText(text: String): ParsedSlipResult {
        if (text.isBlank()) {
            return defaultFallbackResult("Empty text detected")
        }

        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Amount Extraction
        var detectedAmount: Double? = null

        // Try currency pattern first
        val matcher1 = AMOUNT_REGEX_CURRENCY.matcher(text)
        while (matcher1.find()) {
            val rawAmt = matcher1.group(1)?.replace(",", "")
            val parsedAmt = rawAmt?.toDoubleOrNull()
            if (parsedAmt != null && parsedAmt in 1.0..1000000.0) {
                detectedAmount = parsedAmt
                break
            }
        }

        // Try words pattern next
        if (detectedAmount == null) {
            val matcher2 = AMOUNT_REGEX_WORDS.matcher(text)
            while (matcher2.find()) {
                val rawAmt = matcher2.group(1)?.replace(",", "")
                val parsedAmt = rawAmt?.toDoubleOrNull()
                if (parsedAmt != null && parsedAmt in 1.0..1000000.0) {
                    detectedAmount = parsedAmt
                    break
                }
            }
        }

        // Try searching large digits in lines
        if (detectedAmount == null) {
            for (line in lines) {
                if (line.contains("₹") || line.contains("Rs") || line.contains("INR")) {
                    val m = STANDALONE_NUMBER_REGEX.matcher(line)
                    if (m.find()) {
                        val num = m.group(1)?.toDoubleOrNull()
                        if (num != null && num in 1.0..500000.0) {
                            detectedAmount = num
                            break
                        }
                    }
                }
            }
        }

        // 2. Merchant Extraction
        var detectedMerchant = "Merchant"

        // Search for patterns: "Paid to <Name>", "To: <Name>", or text preceding "Paid to"
        val paidToLineIdx = lines.indexOfFirst {
            it.contains("Paid to", ignoreCase = true) ||
            it.contains("Transfer to", ignoreCase = true) ||
            it.contains("To:", ignoreCase = true)
        }

        if (paidToLineIdx != -1) {
            val line = lines[paidToLineIdx]
            val cleaned = line.replace("Paid to", "", ignoreCase = true)
                .replace("Transfer to", "", ignoreCase = true)
                .replace("To:", "", ignoreCase = true)
                .trim()
            if (cleaned.length >= 2 && !cleaned.startsWith("₹")) {
                detectedMerchant = cleaned
            } else if (paidToLineIdx + 1 < lines.size) {
                val nextLine = lines[paidToLineIdx + 1].trim()
                if (!nextLine.contains("₹") && !nextLine.matches(Regex("""[0-9.,]+"""))) {
                    detectedMerchant = nextLine
                }
            }
        } else {
            // Find top non-header line that looks like a name
            val candidate = lines.firstOrNull { l ->
                l.length in 3..35 &&
                !l.contains("PhonePe", ignoreCase = true) &&
                !l.contains("Google Pay", ignoreCase = true) &&
                !l.contains("Paytm", ignoreCase = true) &&
                !l.contains("UPI", ignoreCase = true) &&
                !l.contains("Transaction", ignoreCase = true) &&
                !l.contains("Success", ignoreCase = true) &&
                !l.contains("Completed", ignoreCase = true) &&
                !l.contains("Banking", ignoreCase = true) &&
                !l.contains("Payment", ignoreCase = true) &&
                !l.contains("₹")
            }
            if (candidate != null) {
                detectedMerchant = candidate
            }
        }

        // 3. Category inference based on keywords
        val lowerText = (text + " " + detectedMerchant).lowercase()
        val suggestedCat = when {
            lowerText.contains("smoke") || lowerText.contains("cigarette") || lowerText.contains("paan") ||
                    lowerText.contains("sutta") || lowerText.contains("tobacco") -> ExpenseCategory.SMOKING

            lowerText.contains("airtel") || lowerText.contains("jio") || lowerText.contains("vi prepaid") ||
                    lowerText.contains("recharge") || lowerText.contains("broadband") || lowerText.contains("telecom") -> ExpenseCategory.RECHARGE

            lowerText.contains("chai") || lowerText.contains("tea") || lowerText.contains("snack") ||
                    lowerText.contains("bakery") || lowerText.contains("biscuit") || lowerText.contains("coffee") -> ExpenseCategory.CHAI_SNACKS

            lowerText.contains("pharmacy") || lowerText.contains("apollo") || lowerText.contains("med") ||
                    lowerText.contains("chemist") || lowerText.contains("hospital") || lowerText.contains("clinic") ||
                    lowerText.contains("dr.") || lowerText.contains("health") -> ExpenseCategory.MEDICINE

            lowerText.contains("uber") || lowerText.contains("ola") || lowerText.contains("rapido") ||
                    lowerText.contains("metro") || lowerText.contains("fuel") || lowerText.contains("petrol") ||
                    lowerText.contains("diesel") || lowerText.contains("indianoil") || lowerText.contains("hp") ||
                    lowerText.contains("rail") || lowerText.contains("toll") -> ExpenseCategory.TRAVEL

            else -> ExpenseCategory.FOOD
        }

        return ParsedSlipResult(
            amount = detectedAmount ?: 150.0,
            merchant = detectedMerchant.take(35),
            rawText = text,
            suggestedCategory = suggestedCat
        )
    }

    private fun defaultFallbackResult(raw: String): ParsedSlipResult {
        return ParsedSlipResult(
            amount = 150.0,
            merchant = "Quick Payment",
            rawText = raw,
            suggestedCategory = ExpenseCategory.FOOD
        )
    }

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
