package com.connectkar.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_listing_interactions",
    primaryKeys = ["userId", "listingId"],
    indices = [
        Index(value = ["listingId"]),
        Index(value = ["userId"])
    ]
)
data class UserListingInteractionEntity(
    val userId: String,
    val listingId: Int,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)
