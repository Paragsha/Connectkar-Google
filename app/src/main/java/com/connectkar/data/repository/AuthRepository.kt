package com.connectkar.data.repository

import android.app.Activity
import com.connectkar.auth.AuthUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the centralized authentication state of the application.
 */
sealed interface AuthState {
    object Unauthenticated : AuthState
    object Authenticating : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}

/**
 * Represents phone number OTP verification stages.
 */
sealed interface PhoneVerificationState {
    object Idle : PhoneVerificationState
    object SendingCode : PhoneVerificationState
    data class CodeSent(val verificationId: String) : PhoneVerificationState
    data class AutoVerified(val user: AuthUser) : PhoneVerificationState
    data class Error(val message: String) : PhoneVerificationState
}

/**
 * Clean repository abstraction for authentication logic and state flows.
 * UI components must consume this repository or ViewModels rather than accessing vendor auth SDKs directly.
 */
interface AuthRepository {
    /**
     * Hot StateFlow of the centralized authentication state.
     */
    val authState: StateFlow<AuthState>

    /**
     * Hot StateFlow of phone OTP verification states.
     */
    val phoneVerificationState: StateFlow<PhoneVerificationState>

    /**
     * Returns the currently authenticated user, or null if unauthenticated.
     */
    val currentAuthUser: AuthUser?

    /**
     * Signs in with email and password.
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthUser>

    /**
     * Creates a new user account with email and password, and optionally a display name.
     */
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String = ""
    ): Result<AuthUser>

    /**
     * Verifies an OTP code against a phone verification ID.
     */
    suspend fun signInWithPhoneCredential(verificationId: String, smsCode: String): Result<AuthUser>

    /**
     * Signs out the current user and clears session state.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Sends a password reset email to the specified registered resident email address.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)

    /**
     * Initiates phone number OTP verification.
     */
    fun startPhoneNumberVerification(
        activity: Activity?,
        phoneNumber: String
    )

    /**
     * Resets the phone verification state to Idle.
     */
    fun resetPhoneVerificationState()
}
