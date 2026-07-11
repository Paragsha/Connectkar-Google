package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String = "", // Firebase Auth User UID
    val fullName: String,
    val phoneNumber: String,
    val society: String, // e.g. "Sylvan County"
    val blockTower: String,
    val flatNumber: String,
    val avatarIndex: Int = 0, // Maps to a set of predefined local avatars
    val isVerified: Boolean = false,
    val isPending: Boolean = true,
    val isCurrent: Boolean = false,
    val role: String = "RESIDENT", // "RESIDENT" or "ADMIN"
    val pendingSync: Boolean = false,
    val floor: String = "",
    val residentType: String = "OWNER",
    val moveInDate: String = "",
    val proofDocumentUri: String = ""
)

fun UserEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "uid" to uid,
        "fullName" to fullName,
        "phoneNumber" to phoneNumber,
        "society" to society,
        "blockTower" to blockTower,
        "flatNumber" to flatNumber,
        "avatarIndex" to avatarIndex,
        "isVerified" to isVerified,
        "isPending" to isPending,
        "isCurrent" to isCurrent,
        "role" to role,
        "floor" to floor,
        "residentType" to residentType,
        "moveInDate" to moveInDate,
        "proofDocumentUri" to proofDocumentUri
    )
}
