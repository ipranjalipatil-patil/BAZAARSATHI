package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.VendorProfile
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar

class BazaarSaathiRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val transactionDao = db.transactionDao()
    private val expenseDao = db.expenseDao()
    private val inventoryDao = db.inventoryDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bazaar_saathi_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().firstOrNull { it.code == prefs.getString("selected_language", "en") }
            ?: AppLanguage.ENGLISH
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _vendorProfile = MutableStateFlow(loadProfileFromPrefs())
    val vendorProfile: StateFlow<VendorProfile> = _vendorProfile.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val expenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()
    val inventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventoryFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.code).apply()
        _currentLanguage.value = language
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun updateProfile(profile: VendorProfile) {
        prefs.edit()
            .putString("vendor_id", profile.id)
            .putString("vendor_name", profile.fullName)
            .putString("business_name", profile.businessName)
            .putString("business_type", profile.businessType)
            .putString("phone_or_email", profile.phoneOrEmail)
            .putBoolean("is_demo_mode", profile.isDemoMode)
            .apply()
        _vendorProfile.value = profile
    }

    private fun loadProfileFromPrefs(): VendorProfile {
        val id = prefs.getString("vendor_id", "vendor_001") ?: "vendor_001"
        val name = prefs.getString("vendor_name", "Ramesh Sharma") ?: "Ramesh Sharma"
        val bName = prefs.getString("business_name", "Shree Tea & Snacks") ?: "Shree Tea & Snacks"
        val bType = prefs.getString("business_type", "Tea Stall") ?: "Tea Stall"
        val phone = prefs.getString("phone_or_email", "9876543210") ?: "9876543210"
        val isDemo = prefs.getBoolean("is_demo_mode", true)
        return VendorProfile(
            id = id,
            fullName = name,
            businessName = bName,
            businessType = bType,
            phoneOrEmail = phone,
            isDemoMode = isDemo
        )
    }

    // --- Sales / Transactions ---
    suspend fun addTransaction(
        amount: Double,
        paymentMethod: PaymentMethod,
        product: String,
        category: String,
        description: String = "",
        date: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            vendorId = _vendorProfile.value.id,
            amount = amount,
            paymentMethod = paymentMethod.name,
            product = product,
            category = category,
            description = description,
            date = date
        )
        transactionDao.insertTransaction(entity)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
    }

    // --- Expenses ---
    suspend fun addExpense(
        amount: Double,
        category: String,
        description: String = "",
        date: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val entity = ExpenseEntity(
            vendorId = _vendorProfile.value.id,
            amount = amount,
            category = category,
            description = description,
            date = date
        )
        expenseDao.insertExpense(entity)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(id)
    }

    // --- Inventory ---
    suspend fun addInventoryItem(
        productName: String,
        category: String,
        quantity: Double,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        minimumStock: Double
    ): Long = withContext(Dispatchers.IO) {
        val item = InventoryItemEntity(
            vendorId = _vendorProfile.value.id,
            productName = productName,
            category = category,
            quantity = quantity,
            unit = unit,
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            minimumStock = minimumStock
        )
        inventoryDao.insertItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.updateItem(item)
    }

    suspend fun adjustInventoryQuantity(id: Long, delta: Double) = withContext(Dispatchers.IO) {
        inventoryDao.adjustQuantity(id, delta)
    }

    suspend fun deleteInventoryItem(id: Long) = withContext(Dispatchers.IO) {
        inventoryDao.deleteById(id)
    }

    // --- Seed Demo Data (Shree Tea & Snacks) ---
    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L

        // Clear existing demo data
        transactionDao.clearAll()
        expenseDao.clearAll()
        inventoryDao.clearAll()

        // 1. Inventory Items: Milk, Tea, Sugar, Bread, Biscuits, Bun Maska
        val demoItems = listOf(
            InventoryItemEntity(
                productName = "Fresh Buffalo Milk",
                category = "Raw Material",
                quantity = 8.0,
                unit = "litres",
                purchasePrice = 60.0,
                sellingPrice = 0.0,
                minimumStock = 12.0
            ),
            InventoryItemEntity(
                productName = "Assam CTC Tea Powder",
                category = "Raw Material",
                quantity = 4.5,
                unit = "kg",
                purchasePrice = 320.0,
                sellingPrice = 0.0,
                minimumStock = 3.0
            ),
            InventoryItemEntity(
                productName = "Refined Sugar",
                category = "Raw Material",
                quantity = 15.0,
                unit = "kg",
                purchasePrice = 42.0,
                sellingPrice = 0.0,
                minimumStock = 5.0
            ),
            InventoryItemEntity(
                productName = "Fresh White Bread",
                category = "Bakery",
                quantity = 2.0,
                unit = "packets",
                purchasePrice = 35.0,
                sellingPrice = 50.0,
                minimumStock = 5.0
            ),
            InventoryItemEntity(
                productName = "Osmania / Marie Biscuits",
                category = "Snacks",
                quantity = 24.0,
                unit = "packets",
                purchasePrice = 15.0,
                sellingPrice = 20.0,
                minimumStock = 10.0
            ),
            InventoryItemEntity(
                productName = "Paper Tea Cups (100ml)",
                category = "Packaging",
                quantity = 120.0,
                unit = "pcs",
                purchasePrice = 0.5,
                sellingPrice = 0.0,
                minimumStock = 200.0
            )
        )
        inventoryDao.insertItems(demoItems)

        // 2. Sales over last 5 days including today
        val demoSales = listOf(
            // Today
            TransactionEntity(
                amount = 500.0,
                paymentMethod = PaymentMethod.UPI.name,
                product = "Masala Chai (50 cups)",
                category = "Beverages",
                description = "Morning office crowd",
                date = now - (2 * 3600_000L)
            ),
            TransactionEntity(
                amount = 800.0,
                paymentMethod = PaymentMethod.CASH.name,
                product = "Chai & Bun Maska combo",
                category = "Snacks",
                description = "Breakfast rush",
                date = now - (4 * 3600_000L)
            ),
            TransactionEntity(
                amount = 350.0,
                paymentMethod = PaymentMethod.UPI.name,
                product = "Ginger Tea & Biscuits",
                category = "Beverages",
                description = "Afternoon customers",
                date = now - (1 * 3600_000L)
            ),
            // Yesterday
            TransactionEntity(
                amount = 1200.0,
                paymentMethod = PaymentMethod.UPI.name,
                product = "Tea & Snack bulk parcel",
                category = "Bulk Order",
                description = "Nearby bank staff order",
                date = now - oneDayMs
            ),
            TransactionEntity(
                amount = 650.0,
                paymentMethod = PaymentMethod.CASH.name,
                product = "Evening Chai & Samosa",
                category = "Snacks",
                description = "Evening market visitors",
                date = now - oneDayMs - (3 * 3600_000L)
            ),
            // 2 days ago
            TransactionEntity(
                amount = 950.0,
                paymentMethod = PaymentMethod.UPI.name,
                product = "Special Masala Chai",
                category = "Beverages",
                description = "Market sales",
                date = now - (2 * oneDayMs)
            ),
            TransactionEntity(
                amount = 450.0,
                paymentMethod = PaymentMethod.CASH.name,
                product = "Toast & Tea",
                category = "Snacks",
                description = "Morning shift",
                date = now - (2 * oneDayMs) - (5 * 3600_000L)
            ),
            // 3 days ago
            TransactionEntity(
                amount = 1100.0,
                paymentMethod = PaymentMethod.UPI.name,
                product = "Daily Tea Service",
                category = "Beverages",
                description = "Retail shops delivery",
                date = now - (3 * oneDayMs)
            ),
            // 4 days ago
            TransactionEntity(
                amount = 750.0,
                paymentMethod = PaymentMethod.CASH.name,
                product = "Chai & Biscuits",
                category = "Beverages",
                description = "Regular customers",
                date = now - (4 * oneDayMs)
            )
        )
        transactionDao.insertTransactions(demoSales)

        // 3. Expenses over last 5 days
        val demoExpenses = listOf(
            // Today
            ExpenseEntity(
                amount = 200.0,
                category = ExpenseCategory.RAW_MATERIALS.label,
                description = "Fresh milk 3L morning supply",
                date = now - (5 * 3600_000L)
            ),
            ExpenseEntity(
                amount = 100.0,
                category = ExpenseCategory.PACKAGING.label,
                description = "Disposables & carry bags",
                date = now - (3 * 3600_000L)
            ),
            // Yesterday
            ExpenseEntity(
                amount = 350.0,
                category = ExpenseCategory.RAW_MATERIALS.label,
                description = "Tea leaves 1kg packet & ginger",
                date = now - oneDayMs
            ),
            // 2 days ago
            ExpenseEntity(
                amount = 150.0,
                category = ExpenseCategory.TRANSPORTATION.label,
                description = "Auto fare for wholesale market visit",
                date = now - (2 * oneDayMs)
            ),
            // 3 days ago
            ExpenseEntity(
                amount = 300.0,
                category = ExpenseCategory.RAW_MATERIALS.label,
                description = "Refined sugar 5kg & spices",
                date = now - (3 * oneDayMs)
            )
        )
        expenseDao.insertExpenses(demoExpenses)

        updateProfile(
            _vendorProfile.value.copy(
                businessName = "Shree Tea & Snacks",
                fullName = "Ramesh Sharma",
                businessType = "Tea Stall",
                isDemoMode = true
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        expenseDao.clearAll()
        inventoryDao.clearAll()
    }
}
