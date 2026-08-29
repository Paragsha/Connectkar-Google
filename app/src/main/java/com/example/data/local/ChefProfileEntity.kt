package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "chef_profiles",
    indices = [
        Index("uid", unique = true),
        Index("society")
    ]
)
@JsonClass(generateAdapter = true)
data class ChefProfileEntity(
    @PrimaryKey
    val uid: String,
    val isChef: Boolean = true,
    val chefStory: String = "",
    val mealsServedCount: Int = 0,
    val regularsCount: Int = 0,
    val ratingAvg: Double = 5.0,
    val isSocietyVouched: Boolean = true,
    val speciality: String = "",
    val society: String = "",
    val pendingSync: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun ChefProfileEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "uid" to uid,
        "isChef" to isChef,
        "chefStory" to chefStory,
        "mealsServedCount" to mealsServedCount,
        "regularsCount" to regularsCount,
        "ratingAvg" to ratingAvg,
        "isSocietyVouched" to isSocietyVouched,
        "speciality" to speciality,
        "society" to society,
        "timestamp" to timestamp
    )
}
