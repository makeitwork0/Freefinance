package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converter.Converters
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

/**
 * AppDatabase: Foundational Room database engine for the personal finance tracker.
 * Pre-populates default structural categories (zero mock transactions).
 */
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        FreelanceProjectEntity::class,
        MilestoneEntity::class,
        ExchangeRateEntity::class,
        ReceiptEntity::class,
        AssetEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun freelanceDao(): FreelanceDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun assetDao(): AssetDao

    companion object {
        const val DATABASE_NAME = "finance_tracker_db"

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `receipts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `image_path` TEXT NOT NULL,
                        `captured_at` INTEGER NOT NULL,
                        `note` TEXT,
                        `suggested_amount` REAL,
                        `suggested_type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `converted_transaction_id` INTEGER
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `assets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `estimated_value` REAL NOT NULL,
                        `purchase_price` REAL NOT NULL DEFAULT 0.0,
                        `purchase_date` INTEGER,
                        `currency_code` TEXT NOT NULL DEFAULT 'USD',
                        `notes` TEXT,
                        `icon_name` TEXT NOT NULL DEFAULT 'ic_asset',
                        `color` INTEGER NOT NULL DEFAULT -16738120,
                        `include_in_net_worth` INTEGER NOT NULL DEFAULT 1,
                        `created_at` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `accounts` ADD COLUMN `linked_app_package` TEXT DEFAULT NULL")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate structural default categories (No mock transactions or accounts)
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Food & Dining', 'ic_food', 'Expense', -13447886)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Transport', 'ic_transport', 'Expense', -14579213)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Housing & Rent', 'ic_home', 'Expense', -6543440)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Utilities & Bills', 'ic_utilities', 'Expense', -26624)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Entertainment', 'ic_entertainment', 'Expense', -1499549)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Healthcare', 'ic_health', 'Expense', -16728876)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Salary', 'ic_salary', 'Income', -11751600)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Freelance', 'ic_freelance', 'Income', -16738120)")
                            db.execSQL("INSERT INTO categories (name, icon_res_id, type, color) VALUES ('Investments', 'ic_investment', 'Income', -12627531)")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
