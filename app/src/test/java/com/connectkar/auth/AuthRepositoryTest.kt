package com.connectkar.auth

import com.connectkar.data.repository.AuthRepository
import com.connectkar.data.repository.AuthState
import com.connectkar.data.repository.FirebaseAuthRepository
import com.connectkar.data.repository.PhoneVerificationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        FirebaseAuthRepository.resetInstanceForTesting()
        authRepository = FirebaseAuthRepository(
            context = null,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        FirebaseAuthRepository.resetInstanceForTesting()
    }

    @Test
    fun initialState_isUnauthenticated() {
        assertEquals(AuthState.Unauthenticated, authRepository.authState.value)
        assertEquals(PhoneVerificationState.Idle, authRepository.phoneVerificationState.value)
        assertNull(authRepository.currentAuthUser)
    }

    @Test
    fun signInWithEmailAndPassword_inTestMode_updatesCentralizedAuthState() = runTest(testDispatcher) {
        val email = "resident@connectkar.com"
        val result = authRepository.signInWithEmailAndPassword(email, "SecurePassword123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(email, user?.email)
        assertTrue(user?.isVerified == true)

        val state = authRepository.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals(user?.uid, (state as AuthState.Authenticated).user.uid)
        assertEquals(user?.uid, authRepository.currentAuthUser?.uid)
    }

    @Test
    fun createUserWithEmailAndPassword_inTestMode_updatesCentralizedAuthState() = runTest(testDispatcher) {
        val email = "newresident@resident.community"
        val displayName = "Jane Doe"
        val result = authRepository.createUserWithEmailAndPassword(email, "SecurePassword123", displayName)

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(email, user?.email)
        assertEquals(displayName, user?.displayName)
        assertTrue(user?.isVerified == true)

        val state = authRepository.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals(user?.uid, (state as AuthState.Authenticated).user.uid)
        assertEquals(user?.uid, authRepository.currentAuthUser?.uid)
    }

    @Test
    fun signOut_clearsCentralizedStateAndSession() = runTest(testDispatcher) {
        authRepository.signInWithEmailAndPassword("resident@connectkar.com", "Password123")
        assertTrue(authRepository.authState.value is AuthState.Authenticated)

        val signOutResult = authRepository.signOut()
        assertTrue(signOutResult.isSuccess)

        assertEquals(AuthState.Unauthenticated, authRepository.authState.value)
        assertEquals(PhoneVerificationState.Idle, authRepository.phoneVerificationState.value)
        assertNull(authRepository.currentAuthUser)
    }

    @Test
    fun phoneVerification_completeFlow_inTestMode() = runTest(testDispatcher) {
        // 1. Initiate phone verification
        authRepository.startPhoneNumberVerification(null, "9876543210")
        val stateAfterSend = authRepository.phoneVerificationState.value
        assertTrue(stateAfterSend is PhoneVerificationState.CodeSent)
        val verificationId = (stateAfterSend as PhoneVerificationState.CodeSent).verificationId
        assertEquals("simulated_verification_id", verificationId)

        // 2. Attempt verification with invalid code
        val invalidResult = authRepository.signInWithPhoneCredential(verificationId, "000000")
        assertTrue(invalidResult.isFailure)
        assertTrue(authRepository.phoneVerificationState.value is PhoneVerificationState.Error)

        // 3. Verify with valid debug simulation code
        val validResult = authRepository.signInWithPhoneCredential(verificationId, "123456")
        assertTrue(validResult.isSuccess)
        val verifiedUser = validResult.getOrNull()
        assertNotNull(verifiedUser)
        assertTrue(verifiedUser?.isVerified == true)

        // 4. Verify central state transitions
        assertTrue(authRepository.phoneVerificationState.value is PhoneVerificationState.AutoVerified)
        assertTrue(authRepository.authState.value is AuthState.Authenticated)
        assertEquals(verifiedUser?.uid, authRepository.currentAuthUser?.uid)

        // 5. Reset phone state to Idle
        authRepository.resetPhoneVerificationState()
        assertEquals(PhoneVerificationState.Idle, authRepository.phoneVerificationState.value)
    }
}
