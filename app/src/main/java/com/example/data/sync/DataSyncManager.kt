package com.example.data.sync

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExchangeRateEntity
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.CompoundingFrequency
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first

/**
 * BackupDataDTO: Holds collections of every entity in the app for universal state import/export.
 */
data class BackupDataDTO(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val baseCurrency: String = "USD",
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val freelanceProjects: List<FreelanceProjectEntity> = emptyList(),
    val milestones: List<MilestoneEntity> = emptyList(),
    val exchangeRates: List<ExchangeRateEntity> = emptyList()
)

/**
 * DataSyncManager: Provides robust JSON serialization and deserialization for the entire
 * Room database state, with atomic transaction handling, foreign-key safe table clearing,
 * and OnConflictStrategy.REPLACE to prevent crashes.
 */
class DataSyncManager(private val database: AppDatabase) {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun parseDate(value: Any?): Date {
        if (value == null || value == JSONObject.NULL) return Date()
        return when (value) {
            is Number -> Date(value.toLong())
            is String -> {
                try {
                    isoDateFormat.parse(value) ?: Date()
                } catch (e: Exception) {
                    try {
                        simpleDateFormat.parse(value) ?: Date()
                    } catch (e2: Exception) {
                        try {
                            Date(value.toLong())
                        } catch (e3: Exception) {
                            Date()
                        }
                    }
                }
            }
            else -> Date()
        }
    }

    private fun parseNullableDate(value: Any?): Date? {
        if (value == null || value == JSONObject.NULL) return null
        return parseDate(value)
    }

    /**
     * Serializes entire local Room database into a single standard JSON payload.
     */
    suspend fun exportStateToJson(baseCurrency: String = "USD"): String {
        val accounts = database.accountDao().getAllAccounts().first()
        val categories = database.categoryDao().getAllCategories().first()
        val transactions = database.transactionDao().getAllTransactions().first()
        val budgets = database.budgetDao().getAllBudgets().first()
        val debts = database.debtDao().getAllDebts().first()
        val projects = database.freelanceDao().getAllProjects().first()
        val milestones = database.freelanceDao().getAllMilestones().first()
        val exchangeRates = database.exchangeRateDao().getAllRates().first()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("baseCurrency", baseCurrency)

        // 1. Categories
        val catArray = JSONArray()
        categories.forEach { cat ->
            val obj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("color", cat.color)
                put("iconResId", cat.iconResId)
                put("type", cat.type)
                if (cat.parentCategoryId != null) put("parentCategoryId", cat.parentCategoryId)
            }
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // 2. Accounts
        val accArray = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject().apply {
                put("id", acc.id)
                put("name", acc.name)
                put("type", acc.type)
                put("initialBalance", acc.initialBalance)
                put("currentBalance", acc.currentBalance)
                put("color", acc.color)
                put("interestRatePa", acc.interestRatePa.toDouble())
                put("compoundingFrequency", acc.compoundingFrequency.name)
                put("currencyCode", acc.currencyCode)
                put("isLocked", acc.isLocked)
                if (acc.lockedUntil != null) {
                    put("lockedUntil", isoDateFormat.format(acc.lockedUntil))
                }
            }
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        // 3. Transactions
        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("accountId", tx.accountId)
                if (tx.toAccountId != null) put("toAccountId", tx.toAccountId)
                put("type", tx.type)
                put("amount", tx.amount)
                if (tx.categoryId != null) put("categoryId", tx.categoryId)
                put("date", isoDateFormat.format(tx.date))
                put("status", tx.status)
                put("recurrenceRule", tx.recurrenceRule)
                val labelsArr = JSONArray()
                tx.labels.forEach { labelsArr.put(it) }
                put("labels", labelsArr)
                if (tx.note != null) put("note", tx.note)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // 4. Budgets
        val budgetArray = JSONArray()
        budgets.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("categoryId", b.categoryId)
                put("amountLimit", b.amountLimit)
                put("period", b.period)
                put("startDate", isoDateFormat.format(b.startDate))
                put("endDate", isoDateFormat.format(b.endDate))
            }
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        // 5. Debts
        val debtArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("personName", d.personName)
                put("type", d.type)
                put("totalAmount", d.totalAmount)
                put("remainingAmount", d.remainingAmount)
                if (d.dueDate != null) put("dueDate", isoDateFormat.format(d.dueDate))
                put("status", d.status)
                if (d.notes != null) put("notes", d.notes)
                put("createdAt", isoDateFormat.format(d.createdAt))
            }
            debtArray.put(obj)
        }
        root.put("debts", debtArray)

        // 6. Freelance Projects & Milestones
        val projArray = JSONArray()
        projects.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("projectName", p.projectName)
                put("client", p.client)
                put("totalExpectedFee", p.totalExpectedFee)
                put("status", p.status)
                put("createdAt", isoDateFormat.format(p.createdAt))
            }
            projArray.put(obj)
        }
        root.put("freelanceProjects", projArray)

        val milestoneArray = JSONArray()
        milestones.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("projectId", m.projectId)
                put("title", m.title)
                put("amount", m.amount)
                put("expectedDate", isoDateFormat.format(m.expectedDate))
                put("status", m.status)
                if (m.paidAt != null) put("paidAt", isoDateFormat.format(m.paidAt))
            }
            milestoneArray.put(obj)
        }
        root.put("milestones", milestoneArray)

        // 7. Exchange Rates
        val rateArray = JSONArray()
        exchangeRates.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("fromCurrency", r.fromCurrency)
                put("toCurrency", r.toCurrency)
                put("rate", r.rate)
                put("lastUpdated", r.lastUpdated)
            }
            rateArray.put(obj)
        }
        root.put("exchangeRates", rateArray)

        return root.toString(2)
    }

    /**
     * Parses the given JSON string into [BackupDataDTO] safely with comprehensive try-catch.
     */
    fun parseJsonToDTO(jsonString: String): BackupDataDTO {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
        val baseCurrency = root.optString("baseCurrency", "USD")

        // 1. Categories
        val categories = mutableListOf<CategoryEntity>()
        val catArray = root.optJSONArray("categories")
        if (catArray != null) {
            for (i in 0 until catArray.length()) {
                try {
                    val obj = catArray.getJSONObject(i)
                    categories.add(
                        CategoryEntity(
                            id = obj.optLong("id", 0),
                            name = obj.optString("name", "Category"),
                            iconResId = obj.optString("iconResId", "ic_category"),
                            type = obj.optString("type", "Expense"),
                            color = obj.optInt("color", 0xFF2196F3.toInt()),
                            parentCategoryId = if (obj.has("parentCategoryId") && !obj.isNull("parentCategoryId")) {
                                obj.getLong("parentCategoryId")
                            } else null
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 2. Accounts
        val accounts = mutableListOf<AccountEntity>()
        val accArray = root.optJSONArray("accounts")
        if (accArray != null) {
            for (i in 0 until accArray.length()) {
                try {
                    val obj = accArray.getJSONObject(i)
                    val compStr = obj.optString("compoundingFrequency", "NONE")
                    val compFreq = try {
                        CompoundingFrequency.valueOf(compStr.uppercase())
                    } catch (e: Exception) {
                        CompoundingFrequency.NONE
                    }
                    val isLocked = obj.optBoolean("isLocked", false) || obj.has("lockedUntil")
                    val lockedUntilDate = parseNullableDate(obj.opt("lockedUntil"))
                    accounts.add(
                        AccountEntity(
                            id = obj.optLong("id", 0),
                            name = obj.optString("name", "Account"),
                            type = obj.optString("type", "Bank"),
                            initialBalance = obj.optDouble("initialBalance", 0.0),
                            currentBalance = obj.optDouble("currentBalance", obj.optDouble("initialBalance", 0.0)),
                            color = obj.optInt("color", 0xFF4CAF50.toInt()),
                            interestRatePa = obj.optDouble("interestRatePa", 0.0).toFloat(),
                            compoundingFrequency = compFreq,
                            currencyCode = obj.optString("currencyCode", "USD").uppercase(),
                            isLocked = isLocked,
                            lockedUntil = lockedUntilDate
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 3. Transactions
        val transactions = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions")
        if (txArray != null) {
            for (i in 0 until txArray.length()) {
                try {
                    val obj = txArray.getJSONObject(i)
                    val labelsList = mutableListOf<String>()
                    val labelsArr = obj.optJSONArray("labels")
                    if (labelsArr != null) {
                        for (j in 0 until labelsArr.length()) {
                            labelsList.add(labelsArr.getString(j))
                        }
                    }
                    transactions.add(
                        TransactionEntity(
                            id = obj.optLong("id", 0),
                            accountId = obj.optLong("accountId", 1),
                            toAccountId = if (obj.has("toAccountId") && !obj.isNull("toAccountId")) obj.getLong("toAccountId") else null,
                            type = obj.optString("type", "Expense"),
                            amount = obj.optDouble("amount", 0.0),
                            categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.getLong("categoryId") else null,
                            date = parseDate(obj.opt("date")),
                            status = obj.optString("status", "Cleared"),
                            recurrenceRule = obj.optString("recurrenceRule", "None"),
                            labels = labelsList,
                            note = if (obj.has("note") && !obj.isNull("note")) obj.getString("note") else null
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 4. Budgets
        val budgets = mutableListOf<BudgetEntity>()
        val budgetArray = root.optJSONArray("budgets")
        if (budgetArray != null) {
            for (i in 0 until budgetArray.length()) {
                try {
                    val obj = budgetArray.getJSONObject(i)
                    budgets.add(
                        BudgetEntity(
                            id = obj.optLong("id", 0),
                            categoryId = obj.optLong("categoryId", 1),
                            amountLimit = obj.optDouble("amountLimit", 0.0),
                            period = obj.optString("period", "Monthly"),
                            startDate = parseDate(obj.opt("startDate")),
                            endDate = parseDate(obj.opt("endDate"))
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 5. Debts
        val debts = mutableListOf<DebtEntity>()
        val debtArray = root.optJSONArray("debts")
        if (debtArray != null) {
            for (i in 0 until debtArray.length()) {
                try {
                    val obj = debtArray.getJSONObject(i)
                    debts.add(
                        DebtEntity(
                            id = obj.optLong("id", 0),
                            personName = obj.optString("personName", "Unnamed"),
                            type = obj.optString("type", "Lent"),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            remainingAmount = obj.optDouble("remainingAmount", obj.optDouble("totalAmount", 0.0)),
                            dueDate = parseNullableDate(obj.opt("dueDate")),
                            status = obj.optString("status", "Active"),
                            notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                            createdAt = parseDate(obj.opt("createdAt"))
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 6. Freelance Projects & Milestones
        val projects = mutableListOf<FreelanceProjectEntity>()
        val projectArray = root.optJSONArray("freelanceProjects")
        if (projectArray != null) {
            for (i in 0 until projectArray.length()) {
                try {
                    val obj = projectArray.getJSONObject(i)
                    projects.add(
                        FreelanceProjectEntity(
                            id = obj.optLong("id", 0),
                            projectName = obj.optString("projectName", "Project"),
                            client = obj.optString("client", "Client"),
                            totalExpectedFee = obj.optDouble("totalExpectedFee", 0.0),
                            status = obj.optString("status", "Active"),
                            createdAt = parseDate(obj.opt("createdAt"))
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        val milestones = mutableListOf<MilestoneEntity>()
        val milestoneArray = root.optJSONArray("milestones")
        if (milestoneArray != null) {
            for (i in 0 until milestoneArray.length()) {
                try {
                    val obj = milestoneArray.getJSONObject(i)
                    milestones.add(
                        MilestoneEntity(
                            id = obj.optLong("id", 0),
                            projectId = obj.optLong("projectId", 1),
                            title = obj.optString("title", "Milestone"),
                            amount = obj.optDouble("amount", 0.0),
                            expectedDate = parseDate(obj.opt("expectedDate")),
                            status = obj.optString("status", "Pending"),
                            paidAt = parseNullableDate(obj.opt("paidAt"))
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        // 7. Exchange Rates
        val exchangeRates = mutableListOf<ExchangeRateEntity>()
        val rateArray = root.optJSONArray("exchangeRates")
        if (rateArray != null) {
            for (i in 0 until rateArray.length()) {
                try {
                    val obj = rateArray.getJSONObject(i)
                    exchangeRates.add(
                        ExchangeRateEntity(
                            id = obj.optLong("id", 0),
                            fromCurrency = obj.optString("fromCurrency", "USD").uppercase(),
                            toCurrency = obj.optString("toCurrency", "USD").uppercase(),
                            rate = obj.optDouble("rate", 1.0),
                            lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                        )
                    )
                } catch (e: Exception) {
                    // skip malformed item
                }
            }
        }

        return BackupDataDTO(
            version = version,
            exportedAt = exportedAt,
            baseCurrency = baseCurrency,
            accounts = accounts,
            transactions = transactions,
            categories = categories,
            budgets = budgets,
            debts = debts,
            freelanceProjects = projects,
            milestones = milestones,
            exchangeRates = exchangeRates
        )
    }

    /**
     * Imports the entire application state from a JSON string inside an atomic Room transaction.
     * Disables foreign key constraints temporarily, clears tables cleanly, and inserts all entities.
     * Returns the total count of inserted entities or a failure Result.
     */
    suspend fun importStateFromJson(jsonString: String): Result<Int> {
        return try {
            if (jsonString.isBlank()) {
                return Result.failure(IllegalArgumentException("Import JSON cannot be empty"))
            }

            val dto = parseJsonToDTO(jsonString)

            database.withTransaction {
                val db = database.openHelper.writableDatabase
                // Temporarily disable foreign key checks to avoid constraint exceptions during mass wipe & batch load
                db.execSQL("PRAGMA foreign_keys = OFF")

                try {
                    // Delete all table contents
                    db.execSQL("DELETE FROM transactions")
                    db.execSQL("DELETE FROM milestones")
                    db.execSQL("DELETE FROM budgets")
                    db.execSQL("DELETE FROM freelance_projects")
                    db.execSQL("DELETE FROM debts")
                    db.execSQL("DELETE FROM exchange_rates")
                    db.execSQL("DELETE FROM accounts")
                    db.execSQL("DELETE FROM categories")

                    // Insert parent entities first, followed by dependent children
                    if (dto.categories.isNotEmpty()) {
                        database.categoryDao().insertCategories(dto.categories)
                    }

                    if (dto.accounts.isNotEmpty()) {
                        database.accountDao().insertAccounts(dto.accounts)
                    }

                    if (dto.exchangeRates.isNotEmpty()) {
                        database.exchangeRateDao().insertRates(dto.exchangeRates)
                    }

                    if (dto.freelanceProjects.isNotEmpty()) {
                        database.freelanceDao().insertProjects(dto.freelanceProjects)
                    }

                    if (dto.milestones.isNotEmpty()) {
                        database.freelanceDao().insertMilestones(dto.milestones)
                    }

                    if (dto.budgets.isNotEmpty()) {
                        database.budgetDao().insertBudgets(dto.budgets)
                    }

                    if (dto.debts.isNotEmpty()) {
                        database.debtDao().insertDebts(dto.debts)
                    }

                    if (dto.transactions.isNotEmpty()) {
                        database.transactionDao().insertTransactions(dto.transactions)
                    }
                } finally {
                    // Always re-enable foreign keys
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            }

            val totalImported = dto.categories.size + dto.accounts.size + dto.transactions.size +
                    dto.budgets.size + dto.debts.size + dto.freelanceProjects.size +
                    dto.milestones.size + dto.exchangeRates.size

            Result.success(totalImported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
