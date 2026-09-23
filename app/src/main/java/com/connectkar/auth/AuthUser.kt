package com.connectkar.auth

/**
 * Domain model representing an authenticated user, decoupled from any vendor SDK (e.g., Firebase).
 */
data class AuthUser(
    val uid: String,
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val isAnonymous: Boolean = false,
    val isVerified: Boolean = true,
    val authProvider: String = "Firebase Authentication"
)
