package com.connectkar.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        val testDispatcher = UnconfinedTestDispatcher()
        viewModel = AuthViewModel(dispatcher = testDispatcher)
    }

    @Test
    fun initialState_isIdle() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertTrue(state.rememberResident)
        assertEquals(ResidentAuthState.Idle, state.authState)
    }

    @Test
    fun signIn_withEmptyEmail_showsEmailError() {
        viewModel.signInResident()
        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertEquals(ResidentAuthState.Idle, state.authState)
    }

    @Test
    fun signIn_withUnauthorizedDomain_showsDomainRestrictionError() {
        viewModel.onEmailChanged("resident@gmail.com")
        viewModel.onPasswordChanged("SecretPassword123")
        viewModel.signInResident()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError!!.contains("Unauthorized domain '@gmail.com'"))
        assertEquals(ResidentAuthState.Idle, state.authState)
    }

    @Test
    fun signIn_withEmptyPassword_showsPasswordError() {
        viewModel.onEmailChanged("resident@connectkar.com")
        viewModel.signInResident()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(ResidentAuthState.Idle, state.authState)
    }

    @Test
    fun signIn_withAuthorizedDomain_authenticatesSuccessfully() {
        viewModel.onEmailChanged("sarah.connor@society.org")
        viewModel.onPasswordChanged("SecureResidentPass")
        viewModel.signInResident()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertTrue(state.authState is ResidentAuthState.Success)

        val success = state.authState as ResidentAuthState.Success
        assertEquals("sarah.connor@society.org", success.email)
        assertTrue(success.isVerified)
    }

    @Test
    fun signOut_resetsStateToIdle() {
        viewModel.onEmailChanged("resident@connectkar.com")
        viewModel.onPasswordChanged("MySecretPass")
        viewModel.signInResident()

        assertTrue(viewModel.uiState.value.authState is ResidentAuthState.Success)

        viewModel.signOut()

        val state = viewModel.uiState.value
        assertEquals(ResidentAuthState.Idle, state.authState)
        assertEquals("", state.password)
    }
}
