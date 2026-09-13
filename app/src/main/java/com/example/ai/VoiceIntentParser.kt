package com.example.ai

import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import java.util.regex.Pattern

enum class VoiceIntent {
    ADD_SALE,
    ADD_EXPENSE,
    CHECK_PROFIT,
    CHECK_REVENUE,
    CHECK_INVENTORY,
    CHECK_REPORT,
    UNKNOWN
}

data class ParsedVoiceCommand(
    val rawText: String,
    val intent: VoiceIntent,
    val amount: Double? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val category: String? = null,
    val productOrDescription: String? = null,
    val confirmationPrompt: String,
    val spokenResponse: String
)

object VoiceIntentParser {

    fun parse(text: String): ParsedVoiceCommand {
        val lower = text.trim().lowercase()

        // 1. Check queries first
        if (isCheckProfit(lower)) {
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.CHECK_PROFIT,
                confirmationPrompt = "Check today's profit?",
                spokenResponse = "Checking your net profit today..."
            )
        }

        if (isCheckRevenue(lower)) {
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.CHECK_REVENUE,
                confirmationPrompt = "Check today's total revenue?",
                spokenResponse = "Checking your total sales revenue..."
            )
        }

        if (isCheckInventory(lower)) {
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.CHECK_INVENTORY,
                confirmationPrompt = "Check low stock inventory?",
                spokenResponse = "Checking your inventory stock levels..."
            )
        }

        if (isCheckReport(lower)) {
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.CHECK_REPORT,
                confirmationPrompt = "Open business report?",
                spokenResponse = "Opening your business financial report..."
            )
        }

        // 2. Extract numeric amount
        val extractedAmount = extractAmount(lower)

        // 3. Detect payment method
        val paymentMethod = if (lower.contains("upi") || lower.contains("online") || lower.contains("gpay") ||
            lower.contains("phonepe") || lower.contains("paytm") || lower.contains("यूपीआई") || lower.contains("ऑनलाइन")
        ) {
            PaymentMethod.UPI
        } else {
            PaymentMethod.CASH
        }

        // 4. Detect whether Expense or Sale
        val isExpense = isExpensePhrase(lower)
        val isSale = isSalePhrase(lower)

        if (isExpense || (extractedAmount != null && !isSale && isExpenseItem(lower))) {
            val (category, item) = extractExpenseCategoryAndItem(lower)
            val amt = extractedAmount ?: 100.0
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.ADD_EXPENSE,
                amount = amt,
                paymentMethod = paymentMethod,
                category = category,
                productOrDescription = item,
                confirmationPrompt = "Expense: ₹${amt.toInt()} for $item ($category) today via ${paymentMethod.name}",
                spokenResponse = "Understood expense of ₹${amt.toInt()} for $item"
            )
        }

        if (isSale || extractedAmount != null) {
            val amt = extractedAmount ?: 50.0
            val item = extractSaleProduct(lower)
            return ParsedVoiceCommand(
                rawText = text,
                intent = VoiceIntent.ADD_SALE,
                amount = amt,
                paymentMethod = paymentMethod,
                category = "Daily Sales",
                productOrDescription = item,
                confirmationPrompt = "Sale: ₹${amt.toInt()} from $item today via ${paymentMethod.name}",
                spokenResponse = "Understood sale of ₹${amt.toInt()}"
            )
        }

        return ParsedVoiceCommand(
            rawText = text,
            intent = VoiceIntent.UNKNOWN,
            confirmationPrompt = "Could not identify command. Please tap or speak clearly: 'Sold chai for 500 rupees' or 'Milk expense 300 rupees'.",
            spokenResponse = "Could not understand the transaction. Please try again."
        )
    }

    private fun isCheckProfit(t: String): Boolean {
        return t.contains("profit") || t.contains("मुनाफा") || t.contains("नफा") ||
                t.contains("kitna bacha") || t.contains("कितना बचा")
    }

    private fun isCheckRevenue(t: String): Boolean {
        return t.contains("revenue") || t.contains("kamai") || t.contains("कमाई") ||
                t.contains("bikri kitni") || t.contains("vikri") || t.contains("एकूण विक्री")
    }

    private fun isCheckInventory(t: String): Boolean {
        return t.contains("stock") || t.contains("inventory") || t.contains("माल") ||
                t.contains("स्टॉक") || t.contains("सामान") || t.contains("sampat") || t.contains("संपत")
    }

    private fun isCheckReport(t: String): Boolean {
        return t.contains("report") || t.contains("रिपोर्ट") || t.contains("अहवाल") ||
                t.contains("hisab") || t.contains("हिशोब")
    }

    private fun isExpensePhrase(t: String): Boolean {
        return t.contains("kharch") || t.contains("खर्च") || t.contains("expense") ||
                t.contains("kharida") || t.contains("खरेदी") || t.contains("diye") ||
                t.contains("दिए") || t.contains("paid") || t.contains("खर्च झाले")
    }

    private fun isSalePhrase(t: String): Boolean {
        return t.contains("sale") || t.contains("sold") || t.contains("bikri") ||
                t.contains("बिक्री") || t.contains("विक्री") || t.contains("aaye") ||
                t.contains("आए") || t.contains("mil gaye") || t.contains("झाली")
    }

    private fun isExpenseItem(t: String): Boolean {
        return t.contains("milk") || t.contains("doodh") || t.contains("दूध") ||
                t.contains("tea") || t.contains("chai") || t.contains("sugar") ||
                t.contains("shakkar") || t.contains("sakhar") || t.contains("saakhar") ||
                t.contains("gas") || t.contains("petrol") || t.contains("auto") ||
                t.contains("cup") || t.contains("cups") || t.contains("bhada") || t.contains("rent")
    }

    private fun extractAmount(text: String): Double? {
        // Match numbers like 500, 1200, 350.50
        val matcher = Pattern.compile("\\d+(\\.\\d+)?").matcher(text)
        if (matcher.find()) {
            return matcher.group().toDoubleOrNull()
        }

        // Handle words in Hindi / Marathi / English
        val wordNumbers = mapOf(
            "one hundred" to 100.0, "two hundred" to 200.0, "three hundred" to 300.0,
            "four hundred" to 400.0, "five hundred" to 500.0, "six hundred" to 600.0,
            "seven hundred" to 700.0, "eight hundred" to 800.0, "nine hundred" to 900.0,
            "thousand" to 1000.0,
            "सौ" to 100.0, "दो सौ" to 200.0, "तीन सौ" to 300.0, "चार सौ" to 400.0,
            "पाँच सौ" to 500.0, "पाच सौ" to 500.0, "छह सौ" to 600.0, "सात सौ" to 700.0,
            "आठ सौ" to 800.0, "नौ सौ" to 900.0, "हज़ार" to 1000.0,
            "शंभर" to 100.0, "दोनशे" to 200.0, "तीनशे" to 300.0, "चारशे" to 400.0,
            "पाचशे" to 500.0, "सहाशे" to 600.0, "सातशे" to 700.0, "आठशे" to 800.0,
            "नऊशे" to 900.0, "हजार" to 1000.0
        )

        for ((phrase, value) in wordNumbers) {
            if (text.contains(phrase)) {
                return value
            }
        }
        return null
    }

    private fun extractExpenseCategoryAndItem(t: String): Pair<String, String> {
        return when {
            t.contains("milk") || t.contains("doodh") || t.contains("दूध") ->
                Pair(ExpenseCategory.RAW_MATERIALS.label, "Milk / Dairy")
            t.contains("tea") || t.contains("chai") || t.contains("चाय") || t.contains("चहा") ->
                Pair(ExpenseCategory.RAW_MATERIALS.label, "Tea leaves / Chai powder")
            t.contains("sugar") || t.contains("shakkar") || t.contains("साखर") ->
                Pair(ExpenseCategory.RAW_MATERIALS.label, "Sugar / Shakkar")
            t.contains("cup") || t.contains("cups") || t.contains("कप") ->
                Pair(ExpenseCategory.PACKAGING.label, "Paper Cups")
            t.contains("auto") || t.contains("petrol") || t.contains("fare") || t.contains("भाड़ा") || t.contains("वाहतूक") ->
                Pair(ExpenseCategory.TRANSPORTATION.label, "Transportation / Fare")
            t.contains("rent") || t.contains("किराया") || t.contains("भाडे") ->
                Pair(ExpenseCategory.RENT.label, "Shop / Stall Rent")
            t.contains("bijli") || t.contains("electricity") || t.contains("बिजली") || t.contains("वीज") ->
                Pair(ExpenseCategory.ELECTRICITY.label, "Electricity Bill")
            t.contains("repair") || t.contains("gas") || t.contains("cylinder") || t.contains("गैस") ->
                Pair(ExpenseCategory.MAINTENANCE.label, "Gas Cylinder / Maintenance")
            else ->
                Pair(ExpenseCategory.RAW_MATERIALS.label, "Raw Material Supplies")
        }
    }

    private fun extractSaleProduct(t: String): String {
        return when {
            t.contains("chai") || t.contains("tea") || t.contains("चाय") || t.contains("चहा") -> "Masala Chai"
            t.contains("samosa") || t.contains("समोसा") -> "Samosa"
            t.contains("bun") || t.contains("maska") || t.contains("बन मस्का") -> "Bun Maska"
            t.contains("biscuit") || t.contains("बिस्किट") -> "Biscuits"
            t.contains("vada") || t.contains("pav") || t.contains("वडा पाव") -> "Vada Pav"
            t.contains("bread") || t.contains("ब्रेड") -> "Bread"
            t.contains("snack") || t.contains("नाश्ता") -> "Snacks"
            else -> "Counter Sale"
        }
    }
}
