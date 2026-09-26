package com.connectkar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reports",
    indices = [
        androidx.room.Index(value = ["status"]),
        androidx.room.Index(value = ["contentId"]),
        androidx.room.Index(value = ["reporterId"])
    ]
)
data class ReportEntity(
    @PrimaryKey val reportId: String = UUID.randomUUID().toString(),
    val contentType: String, // "MARKETPLACE", "FEED", "VEHICLE", "HOME_BUSINESS", "PROPERTY", "COMMENT"
    val contentId: String,
    val reporterId: String,
    val reason: String,
    val details: String = "",
    val status: String = "PENDING", // "PENDING", "REVIEWED", "RESOLVED", "DISMISSED"
    val timestamp: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = true
)

fun ReportEntity.toFirestoreMap(): HashMap<String, Any?> {
    return hashMapOf(
        "reportId" to reportId,
        "contentType" to contentType,
        "contentId" to contentId,
        "reporterId" to reporterId,
        "reason" to reason,
        "details" to details,
        "status" to status,
        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "clientTimestamp" to timestamp
    )
}
