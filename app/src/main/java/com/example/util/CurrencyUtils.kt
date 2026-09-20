package com.example.util

import com.example.data.local.entity.ExchangeRateEntity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val defaultRateToUsd: Double // 1 Unit of this currency = X USD
)

object CurrencyUtils {

    // Explicit high-fidelity symbol mappings to guarantee exact currency symbols across all Android phone locales
    private val KNOWN_SYMBOLS = mapOf(
        "PHP" to "₱",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CAD" to "CA$",
        "AUD" to "A$",
        "CHF" to "CHF",
        "CNY" to "¥",
        "INR" to "₹",
        "SGD" to "S$",
        "NZD" to "NZ$",
        "BRL" to "R$",
        "MXN" to "Mex$",
        "KRW" to "₩",
        "HKD" to "HK$",
        "THB" to "฿",
        "VND" to "₫",
        "IDR" to "Rp",
        "MYR" to "RM",
        "TRY" to "₺",
        "RUB" to "₽",
        "SEK" to "kr",
        "NOK" to "kr",
        "DKK" to "kr",
        "PLN" to "zł",
        "ZAR" to "R",
        "AED" to "AED",
        "SAR" to "SAR"
    )

    val SUPPORTED_CURRENCIES: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "US Dollar", "$", 1.0),
        CurrencyInfo("EUR", "Euro", "€", 1.087),
        CurrencyInfo("GBP", "British Pound", "£", 1.265),
        CurrencyInfo("JPY", "Japanese Yen", "¥", 0.00645),
        CurrencyInfo("CAD", "Canadian Dollar", "CA$", 0.725),
        CurrencyInfo("AUD", "Australian Dollar", "A$", 0.654),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", 1.111),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", 0.138),
        CurrencyInfo("INR", "Indian Rupee", "₹", 0.012),
        CurrencyInfo("PHP", "Philippine Peso", "₱", 0.0172),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", 0.741),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", 0.605),
        CurrencyInfo("BRL", "Brazilian Real", "R$", 0.182),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$", 0.054)
    )

    private val currencyMap: Map<String, CurrencyInfo> =
        SUPPORTED_CURRENCIES.associateBy { it.code.uppercase() }

    /**
     * Resolves the exact currency symbol using java.util.Currency with reliable fallback.
     * Guaranteed to return ₱ for PHP, $ for USD, € for EUR, etc.
     */
    fun getSymbol(currencyCode: String): String {
        val code = currencyCode.uppercase().trim()
        val known = KNOWN_SYMBOLS[code]
        if (known != null) return known

        return try {
            val currency = Currency.getInstance(code)
            val sym = currency.getSymbol(Locale.getDefault())
            if (sym.isNotBlank() && sym != code) sym else currency.symbol
        } catch (e: Exception) {
            currencyMap[code]?.symbol ?: "$"
        }
    }

    fun getCurrencyName(currencyCode: String): String {
        val code = currencyCode.uppercase().trim()
        return try {
            val currency = Currency.getInstance(code)
            val displayName = currency.getDisplayName(Locale.getDefault())
            if (displayName.isNotBlank() && displayName != code) displayName else (currencyMap[code]?.name ?: code)
        } catch (e: Exception) {
            currencyMap[code]?.name ?: currencyCode
        }
    }

    /**
     * Formats an amount with the exact currency symbol and decimal precision from java.util.Currency.
     * Supports masking with asterisks (e.g., ₱**** or $**** or ****) when hideMoney is enabled.
     * E.g. 1250.50 with PHP -> "₱1,250.50" (or "₱****" if hidden)
     * E.g. 1250.50 with USD -> "$1,250.50" (or "$****" if hidden)
     * E.g. -200 with EUR -> "-€200.00"
     */
    fun formatCurrency(
        amount: Double,
        currencyCode: String = "USD",
        hideMoney: Boolean = false,
        showSymbol: Boolean = true,
        includeCode: Boolean = false
    ): String {
        val code = currencyCode.uppercase().trim()
        val symbol = if (showSymbol) getSymbol(code) else ""
        if (hideMoney) {
            val mask = "****"
            return when {
                includeCode && symbol.isNotEmpty() -> "$symbol$mask $code"
                includeCode -> "$mask $code"
                symbol.isNotEmpty() -> "$symbol$mask"
                else -> mask
            }
        }

        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)

        val fractionDigits = try {
            val currency = Currency.getInstance(code)
            val digits = currency.defaultFractionDigits
            if (digits >= 0) digits else 2
        } catch (e: Exception) {
            if (code == "JPY" || code == "KRW") 0 else 2
        }

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        val pattern = if (fractionDigits == 0) {
            "#,##0"
        } else {
            "#,##0." + "0".repeat(fractionDigits)
        }

        val formatter = DecimalFormat(pattern, symbols)
        val formattedNumber = formatter.format(absAmount)

        val prefix = if (isNegative) "-$symbol" else symbol
        return if (includeCode) {
            "$prefix$formattedNumber $code"
        } else {
            "$prefix$formattedNumber"
        }
    }

    /**
     * Formats an amount compactly (e.g., $1.2k, $450, or $**** if hidden).
     */
    fun formatCompact(
        amount: Double,
        currencyCode: String = "USD",
        hideMoney: Boolean = false
    ): String {
        val symbol = getSymbol(currencyCode)
        if (hideMoney) {
            return if (symbol.isNotEmpty()) "$symbol****" else "****"
        }
        val absAmount = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            absAmount >= 1_000_000 -> String.format(Locale.US, "%s%s%.1fM", sign, symbol, absAmount / 1_000_000)
            absAmount >= 1_000 -> String.format(Locale.US, "%s%s%.1fk", sign, symbol, absAmount / 1_000)
            else -> String.format(Locale.US, "%s%s%.0f", sign, symbol, absAmount)
        }
    }

    /**
     * Converts an amount from [fromCurrency] to [toCurrency] using database exchange rates,
     * falling back to standard market conversion rates.
     */
    fun convert(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        exchangeRates: List<ExchangeRateEntity> = emptyList()
    ): Double {
        val from = fromCurrency.uppercase().trim()
        val to = toCurrency.uppercase().trim()

        if (from == to) return amount

        // 1. Check direct database match: from -> to
        val directMatch = exchangeRates.firstOrNull {
            it.fromCurrency.equals(from, ignoreCase = true) && it.toCurrency.equals(to, ignoreCase = true)
        }
        if (directMatch != null && directMatch.rate > 0) {
            return amount * directMatch.rate
        }

        // 2. Check inverse database match: to -> from
        val inverseMatch = exchangeRates.firstOrNull {
            it.fromCurrency.equals(to, ignoreCase = true) && it.toCurrency.equals(from, ignoreCase = true)
        }
        if (inverseMatch != null && inverseMatch.rate > 0) {
            return amount / inverseMatch.rate
        }

        // 3. Fall back to standard benchmark market rates (bridging via USD)
        val fromInfo = currencyMap[from]
        val toInfo = currencyMap[to]

        val fromToUsdRate = fromInfo?.defaultRateToUsd ?: 1.0
        val toToUsdRate = toInfo?.defaultRateToUsd ?: 1.0

        if (toToUsdRate <= 0) return amount

        // amount * (from in USD) / (to in USD)
        val amountInUsd = amount * fromToUsdRate
        return amountInUsd / toToUsdRate
    }
}

