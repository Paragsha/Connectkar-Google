package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "menu_items",
    indices = [
        Index("firestoreId"),
        Index("chefUid"),
        Index("society")
    ]
)
@JsonClass(generateAdapter = true)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firestoreId: String = "",
    val chefUid: String = "",
    val chefName: String = "",
    val chefFlat: String = "",
    val dishName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val portionsAvailable: Int = 10,
    val portionsBooked: Int = 0,
    val isVeg: Boolean = true,
    val photoUrl: String = "",
    val cuisineTags: String = "North Indian, Home Style",
    val mealType: String = "LUNCH", // ALL, BREAKFAST, LUNCH, DINNER, SNACKS
    val deliveryWindow: String = "12:30 PM - 1:30 PM",
    val society: String = "",
    val isSoldOut: Boolean = false,
    val pendingSync: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun MenuItemEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "chefUid" to chefUid,
        "chefName" to chefName,
        "chefFlat" to chefFlat,
        "dishName" to dishName,
        "description" to description,
        "price" to price,
        "portionsAvailable" to portionsAvailable,
        "portionsBooked" to portionsBooked,
        "isVeg" to isVeg,
        "photoUrl" to photoUrl,
        "cuisineTags" to cuisineTags,
        "mealType" to mealType,
        "deliveryWindow" to deliveryWindow,
        "society" to society,
        "isSoldOut" to isSoldOut,
        "timestamp" to timestamp,
        "localId" to id
    )
}
