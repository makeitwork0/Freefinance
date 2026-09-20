package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DebtDao
import com.example.data.local.dao.ExchangeRateDao
import com.example.data.local.dao.FreelanceDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExchangeRateEntity
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.AccountBalanceSummary
import com.example.data.local.model.BudgetProgress
import com.example.data.local.model.CategoryExpense
import com.example.data.local.model.DebtStatus
import com.example.data.local.model.ProjectWithMilestones
import com.example.data.local.model.RecurrenceRule
import com.example.data.local.model.TransactionStatus
import com.example.data.local.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * FinanceRepository: Single source of truth abstracting Room DAOs for Accounts, Transactions,
 * Categories, Budgets, Debts, Freelance Projects, Quick Pic Receipts, and Tangible Assets.
 */
class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val debtDao: DebtDao,
    private val freelanceDao: FreelanceDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val receiptDao: ReceiptDao,
    private val assetDao: AssetDao
) {

    // --- Assets & Valuables ---
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()
    val includedAssets: Flow<List<AssetEntity>> = assetDao.getIncludedAssets()
    val totalAssetsValue: Flow<Double?> = assetDao.getTotalAssetsValue()

    suspend fun insertAsset(asset: AssetEntity): Long = assetDao.insertAsset(asset)
    suspend fun updateAsset(asset: AssetEntity) = assetDao.updateAsset(asset)
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset)
    suspend fun deleteAssetById(id: Long) = assetDao.deleteAssetById(id)
    suspend fun getAssetById(id: Long): AssetEntity? = assetDao.getAssetById(id)
    fun getAssetsByCategory(category: String): Flow<List<AssetEntity>> = assetDao.getAssetsByCategory(category)

    // --- Quick Pic Receipts Inbox ---
    val pendingReceipts: Flow<List<ReceiptEntity>> = receiptDao.getPendingReceipts()
    val pendingReceiptCount: Flow<Int> = receiptDao.getPendingReceiptCount()
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()

    suspend fun insertReceipt(receipt: ReceiptEntity): Long = receiptDao.insertReceipt(receipt)

    suspend fun getReceiptByIdImmediate(id: Long): ReceiptEntity? = receiptDao.getReceiptByIdImmediate(id)

    suspend fun updateReceipt(receipt: ReceiptEntity) = receiptDao.updateReceipt(receipt)

    suspend fun deleteReceipt(receipt: ReceiptEntity) = receiptDao.deleteReceipt(receipt)

    suspend fun deleteReceiptById(id: Long) = receiptDao.deleteReceiptById(id)

    suspend fun discardReceipt(id: Long) {
        val r = receiptDao.getReceiptByIdImmediate(id) ?: return
        receiptDao.updateReceipt(r.copy(status = "DISCARDED"))
    }

    /**
     * Convert an inbox receipt into a ledger transaction (Income or Expense).
     */
    suspend fun convertReceiptToTransaction(
        receiptId: Long,
        accountId: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        date: Date,
        note: String?
    ): Long {
        val tx = TransactionEntity(
            accountId = accountId,
            type = type.displayName,
            amount = amount,
            categoryId = categoryId,
            date = date,
            status = TransactionStatus.CLEARED.displayName,
            recurrenceRule = RecurrenceRule.NONE.displayName,
            labels = listOf("Receipt"),
            note = note
        )
        val txId = transactionDao.insertTransaction(tx)

        // Adjust account balance
        applyBalanceChangesForClearedTransaction(tx)

        // Update receipt to CONVERTED
        val receipt = receiptDao.getReceiptByIdImmediate(receiptId)
        if (receipt != null) {
            receiptDao.updateReceipt(
                receipt.copy(
                    status = "CONVERTED",
                    convertedTransactionId = txId,
                    suggestedAmount = amount,
                    suggestedType = type.displayName
                )
            )
        }

        return txId
    }

    // --- Exchange Rate Streams & Operations ---
    val allExchangeRates: Flow<List<ExchangeRateEntity>> = exchangeRateDao.getAllRates()

    suspend fun insertExchangeRate(rate: ExchangeRateEntity): Long = exchangeRateDao.insertRate(rate)

    suspend fun deleteExchangeRate(id: Long) = exchangeRateDao.deleteRate(id)

    // --- Account Streams ---
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    val totalClearedNetWorth: Flow<Double> = accountDao.getTotalClearedNetWorth()

    val accountBalanceSummaries: Flow<List<AccountBalanceSummary>> =
        accountDao.getAccountBalanceSummaries()

    fun getAccountById(id: Long): Flow<AccountEntity?> = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun insertCategories(categories: List<CategoryEntity>) = categoryDao.insertCategories(categories)

    // --- Transaction Streams ---
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    val categoryExpenses: Flow<List<CategoryExpense>> = transactionDao.getCategoryExpenses()

    val recurringTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecurringTransactions()

    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccount(accountId)

    fun getPlannedAndRecurringTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getPlannedAndRecurringTransactions()

    suspend fun stopRecurrence(transactionId: Long) = transactionDao.stopRecurrence(transactionId)

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)

    suspend fun insertTransaction(transaction: TransactionEntity): Long = transactionDao.insertTransaction(transaction)

    suspend fun addRecurringTransaction(
        amount: Double,
        type: TransactionType,
        accountId: Long,
        categoryId: Long? = null,
        recurrenceRule: RecurrenceRule,
        startDate: Date = Date(),
        note: String? = null
    ): Long {
        val transaction = TransactionEntity(
            amount = amount,
            type = type.displayName,
            date = startDate,
            status = TransactionStatus.PLANNED.displayName,
            accountId = accountId,
            categoryId = categoryId,
            recurrenceRule = recurrenceRule.displayName,
            note = note
        )
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun addQuickTransaction(
        amount: Double,
        type: TransactionType,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        labels: List<String> = emptyList(),
        note: String? = null
    ): Long {
        val transaction = TransactionEntity(
            amount = amount,
            type = type.displayName,
            date = Date(),
            status = TransactionStatus.CLEARED.displayName,
            accountId = accountId,
            toAccountId = toAccountId,
            categoryId = categoryId,
            labels = labels,
            note = note,
            recurrenceRule = RecurrenceRule.NONE.displayName
        )

        val txId = transactionDao.insertTransaction(transaction)
        applyBalanceChangesForClearedTransaction(transaction)
        return txId
    }

    private suspend fun applyBalanceChangesForClearedTransaction(transaction: TransactionEntity) {
        val sourceAccount = accountDao.getAccountByIdImmediate(transaction.accountId) ?: return

        when (transaction.type) {
            TransactionType.INCOME.displayName -> {
                accountDao.updateCurrentBalance(
                    sourceAccount.id,
                    sourceAccount.currentBalance + transaction.amount
                )
            }
            TransactionType.EXPENSE.displayName -> {
                accountDao.updateCurrentBalance(
                    sourceAccount.id,
                    sourceAccount.currentBalance - transaction.amount
                )
            }
            TransactionType.TRANSFER.displayName -> {
                accountDao.updateCurrentBalance(
                    sourceAccount.id,
                    sourceAccount.currentBalance - transaction.amount
                )
                transaction.toAccountId?.let { destinationId ->
                    val destAccount = accountDao.getAccountByIdImmediate(destinationId)
                    if (destAccount != null) {
                        accountDao.updateCurrentBalance(
                            destAccount.id,
                            destAccount.currentBalance + transaction.amount
                        )
                    }
                }
            }
        }
    }

    // --- Category Streams ---
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val rootCategories: Flow<List<CategoryEntity>> = categoryDao.getRootCategories()

    fun getSubcategories(parentId: Long): Flow<List<CategoryEntity>> = categoryDao.getSubcategories(parentId)

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // --- Budget Streams ---
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    val budgetProgressList: Flow<List<BudgetProgress>> = budgetDao.getBudgetProgressByCategory()

    fun getBudgetProgressById(budgetId: Long): Flow<BudgetProgress?> =
        budgetDao.getBudgetProgressById(budgetId)

    suspend fun insertBudget(budget: BudgetEntity): Long = budgetDao.insertBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    // --- Debt Streams & Operations ---
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    val lentDebts: Flow<List<DebtEntity>> = debtDao.getDebtsByTypeAndStatus("Lent", "Active")

    val borrowedDebts: Flow<List<DebtEntity>> = debtDao.getDebtsByTypeAndStatus("Borrowed", "Active")

    val topReceivables: Flow<List<DebtEntity>> = debtDao.getTopReceivables(3)

    val totalActiveLent: Flow<Double> = debtDao.getTotalActiveLent()

    val totalActiveBorrowed: Flow<Double> = debtDao.getTotalActiveBorrowed()

    fun getDebtsByType(type: String): Flow<List<DebtEntity>> = debtDao.getDebtsByType(type)

    suspend fun getDebtByIdImmediate(id: Long): DebtEntity? = debtDao.getDebtByIdImmediate(id)

    suspend fun insertDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) = debtDao.updateDebt(debt)

    suspend fun deleteDebt(debt: DebtEntity) = debtDao.deleteDebt(debt)

    suspend fun settleDebt(debtId: Long) {
        debtDao.updateRemainingAndStatus(debtId, 0.0, DebtStatus.SETTLED.displayName)
    }

    suspend fun payDebtPortion(debtId: Long, paymentAmount: Double) {
        val debt = debtDao.getDebtByIdImmediate(debtId) ?: return
        val newRemaining = (debt.remainingAmount - paymentAmount).coerceAtLeast(0.0)
        val newStatus = if (newRemaining <= 0.0) DebtStatus.SETTLED.displayName else DebtStatus.ACTIVE.displayName
        debtDao.updateRemainingAndStatus(debtId, newRemaining, newStatus)
    }

    // --- Freelance Commission Streams & Operations ---
    val activeProjectsWithMilestones: Flow<List<ProjectWithMilestones>> =
        freelanceDao.getActiveProjectsWithMilestones()

    val allProjectsWithMilestones: Flow<List<ProjectWithMilestones>> =
        freelanceDao.getAllProjectsWithMilestones()

    val pendingMilestones: Flow<List<MilestoneEntity>> = freelanceDao.getPendingMilestones()

    suspend fun insertProject(project: FreelanceProjectEntity): Long =
        freelanceDao.insertProject(project)

    suspend fun updateProject(project: FreelanceProjectEntity) =
        freelanceDao.updateProject(project)

    suspend fun deleteProject(project: FreelanceProjectEntity) =
        freelanceDao.deleteProject(project)

    suspend fun insertMilestone(milestone: MilestoneEntity): Long =
        freelanceDao.insertMilestone(milestone)

    suspend fun deleteMilestone(milestone: MilestoneEntity) =
        freelanceDao.deleteMilestone(milestone)

    /**
     * Mark Milestone as Paid:
     * 1. Logs an Income transaction in the ledger for the chosen account.
     * 2. Credits the target account's current balance.
     * 3. Updates milestone status to "Paid".
     */
    suspend fun markMilestoneAsPaid(milestoneId: Long, accountId: Long) {
        val milestone = freelanceDao.getMilestoneById(milestoneId) ?: return
        if (milestone.status.equals("Paid", ignoreCase = true)) return

        // 1. Log Income transaction
        addQuickTransaction(
            amount = milestone.amount,
            type = TransactionType.INCOME,
            accountId = accountId,
            labels = listOf("Freelance", "Milestone"),
            note = "Freelance Milestone: ${milestone.title}"
        )

        // 2. Mark milestone as paid
        freelanceDao.updateMilestoneStatus(milestoneId, "Paid", Date())
    }
}
