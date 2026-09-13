package com.example.data.model

data class VendorProfile(
    val id: String = "vendor_001",
    val fullName: String = "Ramesh Sharma",
    val businessName: String = "Shree Tea & Snacks",
    val businessType: String = "Tea Stall",
    val phoneOrEmail: String = "9876543210",
    val languageCode: String = "en",
    val isDemoMode: Boolean = true,
    val token: String = "demo_jwt_token_bazaar_saathi"
)

enum class BusinessType(val displayName: String) {
    TEA_STALL("Tea Stall"),
    FOOD_STALL("Food Stall"),
    FRUIT_VENDOR("Fruit Vendor"),
    VEGETABLE_VENDOR("Vegetable Vendor"),
    GROCERY("Grocery"),
    CLOTHING("Clothing"),
    ELECTRONICS("Electronics"),
    OTHER("Other")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
