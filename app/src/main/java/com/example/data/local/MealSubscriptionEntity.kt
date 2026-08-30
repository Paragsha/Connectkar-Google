package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "meal_subscriptions",
    indices = [
        Index("firestoreId"),
        Index("buyerUid"),
        Index("chefUid"),
        Index("society")
    ]
)
@JsonClass(generateAdapter = true)
data class MealSubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firestoreId: String = "",
    val buyerUid: String = "",
    val chefUid: String = "",
    val chefName: String = "",
    val planType: String = "WEEKLY", // WEEKLY, MONTHLY
    val mealsPerCycle: Int = 7,
    val discountPercent: Int = 15,
    val daysRemaining: Int = 7,
    val renewalDate: String = "Next Monday",
    val status: String = "ACTIVE", // ACTIVE, PAUSED
    val pricePerMeal: Double = 120.0,
    val society: String = "",
    val pendingSync: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun MealSubscriptionEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "buyerUid" to buyerUid,
        "chefUid" to chefUid,
        "chefName" to chefName,
        "planType" to planType,
        "mealsPerCycle" to mealsPerCycle,
        "discountPercent" to discountPercent,
        "daysRemaining" to daysRemaining,
        "renewalDate" to renewalDate,
        "status" to status,
        "pricePerMeal" to pricePerMeal,
        "society" to society,
        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "clientTimestamp" to timestamp,
        "localId" to id
    )
}
