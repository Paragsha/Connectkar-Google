package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "meal_orders",
    indices = [
        Index("firestoreId"),
        Index("buyerUid"),
        Index("chefUid"),
        Index("society")
    ]
)
@JsonClass(generateAdapter = true)
data class MealOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firestoreId: String = "",
    val buyerUid: String = "",
    val buyerName: String = "",
    val buyerFlat: String = "",
    val chefUid: String = "",
    val chefName: String = "",
    val menuItemId: Int = 0,
    val dishName: String = "",
    val servingSize: Int = 1,
    val deliveryWindow: String = "12:30 PM - 1:30 PM",
    val dietaryNotes: String = "",
    val deliveryMethod: String = "SOCIETY_RUNNER", // SOCIETY_RUNNER, SELF_PICKUP
    val addOns: String = "[]", // JSON list or CSV
    val itemTotal: Double = 0.0,
    val addOnsTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "PENDING", // PENDING, ACCEPTED, PREPARING, ARRIVING, COMPLETED, REJECTED
    val society: String = "",
    val pendingSync: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun MealOrderEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "buyerUid" to buyerUid,
        "buyerName" to buyerName,
        "buyerFlat" to buyerFlat,
        "chefUid" to chefUid,
        "chefName" to chefName,
        "menuItemId" to menuItemId,
        "dishName" to dishName,
        "servingSize" to servingSize,
        "deliveryWindow" to deliveryWindow,
        "dietaryNotes" to dietaryNotes,
        "deliveryMethod" to deliveryMethod,
        "addOns" to addOns,
        "itemTotal" to itemTotal,
        "addOnsTotal" to addOnsTotal,
        "deliveryFee" to deliveryFee,
        "grandTotal" to grandTotal,
        "status" to status,
        "society" to society,
        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "clientTimestamp" to timestamp,
        "localId" to id
    )
}
