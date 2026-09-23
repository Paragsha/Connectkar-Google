package com.connectkar.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectkar.data.repository.AuthRepository
import com.connectkar.data.repository.AuthState
import com.connectkar.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN, REGISTER
}

sealed interface ResidentAuthState {
    object Idle : ResidentAuthState
    object Authenticating : ResidentAuthState
    data class Success(
        val email: String,
        val displayName: String,
        val unitNumber: String,
        val isVerified: Boolean,
        val authProvider: String
    ) : ResidentAuthState
    data class Error(val message: String) : ResidentAuthState
}

enum class PasswordStrengthLevel {
    EMPTY, WEAK, MEDIUM, STRONG
}

data class PasswordStrength(
    val level: PasswordStrengthLevel = PasswordStrengthLevel.EMPTY,
    val score: Int = 0,
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecial: Boolean = false
) {
    companion object {
        fun calculate(password: String): PasswordStrength {
            if (password.isEmpty()) return PasswordStrength()
            val hasMinLength = password.length >= 6
            val hasUppercase = password.any { it.isUpperCase() }
            val hasNumber = password.any { it.isDigit() }
            val hasSpecial = password.any { !it.isLetterOrDigit() }

            var score = 0
            if (hasMinLength) score++
            if (password.length >= 8) score++
            if (hasUppercase || hasNumber) score++
            if (hasSpecial) score++

            val level = when {
                password.length < 6 -> PasswordStrengthLevel.WEAK
                score <= 2 -> PasswordStrengthLevel.MEDIUM
                else -> PasswordStrengthLevel.STRONG
            }

            return PasswordStrength(
                level = level,
                score = score,
                hasMinLength = hasMinLength,
                hasUppercase = hasUppercase,
                hasNumber = hasNumber,
                hasSpecial = hasSpecial
            )
        }
    }
}

sealed interface ResetPasswordStatus {
    object Idle : ResetPasswordStatus
    object Sending : ResetPasswordStatus
    data class Success(val message: String) : ResetPasswordStatus
    data class Error(val message: String) : ResetPasswordStatus
}

data class AuthUiState(
    val authMode: AuthMode = AuthMode.SIGN_IN,

    // Login Form State
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val rememberResident: Boolean = true,
    val failedAttempts: Int = 0,
    val passwordStrength: PasswordStrength = PasswordStrength(),

    // Registration Form State
    val regName: String = "",
    val regUnitNumber: String = "",
    val regEmail: String = "",
    val regPassword: String = "",
    val regConfirmPassword: String = "",
    val isRegPasswordVisible: Boolean = false,
    val isRegConfirmPasswordVisible: Boolean = false,
    val regTermsAccepted: Boolean = false,
    val regNameError: String? = null,
    val regUnitError: String? = null,
    val regEmailError: String? = null,
    val regPasswordError: String? = null,
    val regConfirmPasswordError: String? = null,
    val regTermsError: String? = null,
    val regPasswordStrength: PasswordStrength = PasswordStrength(),

    // Shared Status / Dialogs
    val authState: ResidentAuthState = ResidentAuthState.Idle,
    val showResetPasswordDialog: Boolean = false,
    val resetPasswordEmail: String = "",
    val resetPasswordError: String? = null,
    val resetPasswordStatus: ResetPasswordStatus = ResetPasswordStatus.Idle
)

/**
 * ViewModel managing authentication screen presentation state.
 * Interacts with the domain layer solely through [AuthRepository],
 * ensuring that UI components have no direct dependency on Firebase Auth SDK.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository.getInstance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            authRepository.authState.collect { repoAuthState ->
                when (repoAuthState) {
                    is AuthState.Authenticated -> {
                        _uiState.update { current ->
                            if (current.authState !is ResidentAuthState.Success) {
                                val unit = if (current.regUnitNumber.isNotBlank()) current.regUnitNumber else "Tower B - 402"
                                current.copy(
                                    failedAttempts = 0,
                                    authState = ResidentAuthState.Success(
                                        email = repoAuthState.user.email,
                                        displayName = repoAuthState.user.displayName,
                                        unitNumber = unit,
                                        isVerified = repoAuthState.user.isVerified,
                                        authProvider = repoAuthState.user.authProvider
                                    )
                                )
                            } else current
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update { current ->
                            if (current.authState is ResidentAuthState.Success) {
                                current.copy(authState = ResidentAuthState.Idle)
                            } else current
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                authMode = mode,
                authState = ResidentAuthState.Idle
            )
        }
    }

    // --- Sign In Handlers ---

    fun onEmailChanged(newEmail: String) {
        _uiState.update { current ->
            current.copy(
                email = newEmail,
                emailError = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        val strength = PasswordStrength.calculate(newPassword)
        _uiState.update { current ->
            current.copy(
                password = newPassword,
                passwordError = null,
                passwordStrength = strength
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleRememberResident(remember: Boolean) {
        _uiState.update { it.copy(rememberResident = remember) }
    }

    fun signInResident() {
        val current = _uiState.value
        val email = current.email.trim()
        val password = current.password

        // 1. Email format & domain authorization validation
        when (val validation = DomainValidator.validateResidentEmail(email)) {
            is DomainValidationResult.EmptyEmail -> {
                _uiState.update { it.copy(emailError = "Please enter your registered resident email.") }
                return
            }
            is DomainValidationResult.InvalidFormat -> {
                _uiState.update { it.copy(emailError = "Please enter a valid email address.") }
                return
            }
            is DomainValidationResult.UnauthorizedDomain -> {
                val allowedStr = validation.allowedDomains.joinToString(", ") { "@$it" }
                _uiState.update {
                    it.copy(
                        emailError = "Unauthorized domain '@${validation.attemptedDomain}'. Access restricted to registered resident domains: $allowedStr"
                    )
                }
                return
            }
            is DomainValidationResult.Success -> {
                // Email is valid and domain is authorized
            }
        }

        // 2. Password validation
        if (password.isEmpty()) {
            _uiState.update { it.copy(passwordError = "Please enter your password.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters.") }
            return
        }

        // 3. Initiate authentication via repository
        _uiState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                authState = ResidentAuthState.Authenticating
            )
        }

        viewModelScope.launch(dispatcher) {
            val result = authRepository.signInWithEmailAndPassword(email, password)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        failedAttempts = 0,
                        authState = ResidentAuthState.Success(
                            email = user.email,
                            displayName = user.displayName,
                            unitNumber = "Tower B - 402",
                            isVerified = user.isVerified,
                            authProvider = user.authProvider
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        failedAttempts = it.failedAttempts + 1,
                        authState = ResidentAuthState.Error(
                            error.localizedMessage ?: "Firebase Authentication failed. Please check credentials."
                        )
                    )
                }
            }
        }
    }

    // --- Registration Handlers ---

    fun onRegNameChanged(name: String) {
        _uiState.update {
            it.copy(
                regName = name,
                regNameError = null
            )
        }
    }

    fun onRegUnitChanged(unit: String) {
        _uiState.update {
            it.copy(
                regUnitNumber = unit,
                regUnitError = null
            )
        }
    }

    fun onRegEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                regEmail = email,
                regEmailError = null
            )
        }
    }

    fun onRegPasswordChanged(password: String) {
        val strength = PasswordStrength.calculate(password)
        _uiState.update {
            val confirmErr = if (it.regConfirmPassword.isNotEmpty() && password != it.regConfirmPassword) {
                "Passwords do not match."
            } else null

            it.copy(
                regPassword = password,
                regPasswordError = null,
                regConfirmPasswordError = confirmErr,
                regPasswordStrength = strength
            )
        }
    }

    fun onRegConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update {
            val confirmErr = if (confirmPassword.isNotEmpty() && confirmPassword != it.regPassword) {
                "Passwords do not match."
            } else null

            it.copy(
                regConfirmPassword = confirmPassword,
                regConfirmPasswordError = confirmErr
            )
        }
    }

    fun onToggleRegPasswordVisibility() {
        _uiState.update { it.copy(isRegPasswordVisible = !it.isRegPasswordVisible) }
    }

    fun onToggleRegConfirmPasswordVisibility() {
        _uiState.update { it.copy(isRegConfirmPasswordVisible = !it.isRegConfirmPasswordVisible) }
    }

    fun onToggleRegTerms(accepted: Boolean) {
        _uiState.update {
            it.copy(
                regTermsAccepted = accepted,
                regTermsError = if (accepted) null else it.regTermsError
            )
        }
    }

    fun registerResident() {
        val current = _uiState.value
        val name = current.regName.trim()
        val unit = current.regUnitNumber.trim()
        val email = current.regEmail.trim()
        val password = current.regPassword
        val confirmPassword = current.regConfirmPassword
        val termsAccepted = current.regTermsAccepted

        var hasError = false
        var nameErr: String? = null
        var unitErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null
        var termsErr: String? = null

        // 1. Full Name Validation
        if (name.isEmpty()) {
            nameErr = "Please enter your full name."
            hasError = true
        } else if (name.length < 2) {
            nameErr = "Name must be at least 2 characters."
            hasError = true
        }

        // 2. Unit Number Validation
        if (unit.isEmpty()) {
            unitErr = "Please enter your apartment / unit number."
            hasError = true
        }

        // 3. Email Validation with Domain Whitelist Check
        when (val validation = DomainValidator.validateResidentEmail(email)) {
            is DomainValidationResult.EmptyEmail -> {
                emailErr = "Please enter your resident email address."
                hasError = true
            }
            is DomainValidationResult.InvalidFormat -> {
                emailErr = "Please enter a valid email address."
                hasError = true
            }
            is DomainValidationResult.UnauthorizedDomain -> {
                val allowedStr = validation.allowedDomains.joinToString(", ") { "@$it" }
                emailErr = "Unauthorized domain '@${validation.attemptedDomain}'. Access restricted to: $allowedStr"
                hasError = true
            }
            is DomainValidationResult.Success -> {}
        }

        // 4. Password Validation
        if (password.isEmpty()) {
            passErr = "Please create a secure password."
            hasError = true
        } else if (password.length < 6) {
            passErr = "Password must be at least 6 characters."
            hasError = true
        }

        // 5. Confirm Password Validation
        if (confirmPassword.isEmpty()) {
            confirmErr = "Please confirm your password."
            hasError = true
        } else if (confirmPassword != password) {
            confirmErr = "Passwords do not match."
            hasError = true
        }

        // 6. Community Guidelines / Terms Agreement Validation
        if (!termsAccepted) {
            termsErr = "You must agree to Community Guidelines & Terms of Service."
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    regNameError = nameErr,
                    regUnitError = unitErr,
                    regEmailError = emailErr,
                    regPasswordError = passErr,
                    regConfirmPasswordError = confirmErr,
                    regTermsError = termsErr
                )
            }
            return
        }

        // Clear errors and transition to Authenticating
        _uiState.update {
            it.copy(
                regNameError = null,
                regUnitError = null,
                regEmailError = null,
                regPasswordError = null,
                regConfirmPasswordError = null,
                regTermsError = null,
                authState = ResidentAuthState.Authenticating
            )
        }

        viewModelScope.launch(dispatcher) {
            val result = authRepository.createUserWithEmailAndPassword(
                email = email,
                password = password,
                displayName = name
            )
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        authState = ResidentAuthState.Success(
                            email = user.email,
                            displayName = if (user.displayName.isNotBlank()) user.displayName else name,
                            unitNumber = unit,
                            isVerified = user.isVerified,
                            authProvider = user.authProvider
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        authState = ResidentAuthState.Error(
                            error.localizedMessage ?: "Registration failed. Please check details and try again."
                        )
                    )
                }
            }
        }
    }

    // --- Password Reset Handlers ---

    fun openForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showResetPasswordDialog = true,
                resetPasswordEmail = it.email.ifEmpty { it.regEmail },
                resetPasswordError = null,
                resetPasswordStatus = ResetPasswordStatus.Idle
            )
        }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showResetPasswordDialog = false,
                resetPasswordStatus = ResetPasswordStatus.Idle,
                resetPasswordError = null
            )
        }
    }

    fun onResetPasswordEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                resetPasswordEmail = newEmail,
                resetPasswordError = null
            )
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.resetPasswordEmail.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(resetPasswordError = "Please enter your registered resident email.") }
            return
        }
        when (val validation = DomainValidator.validateResidentEmail(email)) {
            is DomainValidationResult.EmptyEmail,
            is DomainValidationResult.InvalidFormat -> {
                _uiState.update { it.copy(resetPasswordError = "Please enter a valid resident email address.") }
                return
            }
            is DomainValidationResult.UnauthorizedDomain -> {
                val allowedStr = validation.allowedDomains.joinToString(", ") { "@$it" }
                _uiState.update {
                    it.copy(resetPasswordError = "Domain not authorized. Allowed: $allowedStr")
                }
                return
            }
            is DomainValidationResult.Success -> {}
        }

        _uiState.update {
            it.copy(
                resetPasswordStatus = ResetPasswordStatus.Sending,
                resetPasswordError = null
            )
        }

        viewModelScope.launch(dispatcher) {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        resetPasswordStatus = ResetPasswordStatus.Success(
                            "Password recovery link sent to $email. Please check your inbox."
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        resetPasswordStatus = ResetPasswordStatus.Error(
                            err.localizedMessage ?: "Failed to send reset link. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(dispatcher) {
            try {
                authRepository.signOut()
            } catch (_: Exception) {}
        }

        _uiState.update {
            it.copy(
                password = "",
                regPassword = "",
                regConfirmPassword = "",
                authState = ResidentAuthState.Idle,
                emailError = null,
                passwordError = null,
                regNameError = null,
                regUnitError = null,
                regEmailError = null,
                regPasswordError = null,
                regConfirmPasswordError = null,
                regTermsError = null
            )
        }
    }
}
