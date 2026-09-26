package com.connectkar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.auth.AuthMode
import com.connectkar.auth.AuthUiState
import com.connectkar.auth.AuthViewModel
import com.connectkar.auth.DomainValidator
import com.connectkar.auth.PasswordStrength
import com.connectkar.auth.PasswordStrengthLevel
import com.connectkar.auth.ResetPasswordStatus
import com.connectkar.auth.ResidentAuthState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.TownshipSocieties
import com.connectkar.ui.TownshipViewModel
import com.connectkar.ui.DashboardScreen

@Composable
fun FirebaseAuthScreen(
    viewModel: AuthViewModel,
    townshipViewModel: TownshipViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("firebase_auth_screen"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val authState = uiState.authState) {
                is ResidentAuthState.Success -> {
                    if (townshipViewModel != null) {
                        val currentUser by townshipViewModel.currentUser.collectAsState()
                        val selectedSociety by townshipViewModel.selectedSociety.collectAsState()
                        val syncState by townshipViewModel.syncState.collectAsState()
                        val isRefreshing by townshipViewModel.isRefreshing.collectAsState()
                        val mealListings by townshipViewModel.mealListingsForSociety.collectAsState()
                        val isExploreMode by townshipViewModel.isExploreMode.collectAsState()
                        val exploredSocieties by townshipViewModel.exploredSocieties.collectAsState()

                        val activeUser = currentUser ?: UserEntity(
                            uid = "",
                            fullName = authState.displayName.ifBlank { "Resident" },
                            phoneNumber = "",
                            society = selectedSociety.ifBlank { TownshipSocieties.first() },
                            blockTower = "",
                            flatNumber = authState.unitNumber,
                            avatarIndex = 0,
                            isVerified = authState.isVerified,
                            isPending = !authState.isVerified,
                            isCurrent = true,
                            role = "RESIDENT"
                        )

                        DashboardScreen(
                            currentUser = activeUser,
                            selectedSociety = selectedSociety,
                            syncState = syncState,
                            isRefreshing = isRefreshing,
                            mealListings = mealListings,
                            isExploreMode = isExploreMode,
                            exploredSocieties = exploredSocieties,
                            onEnterExploreMode = { townshipViewModel.enterExploreMode(it) },
                            onExitExploreMode = { townshipViewModel.exitExploreMode() },
                            onSocietySelected = { townshipViewModel.selectSociety(it) },
                            onModuleClicked = { townshipViewModel.setActiveModule(it) },
                            onSimulateApprove = { townshipViewModel.simulateAdminVerificationOfCurrentUser() },
                            onLogout = {
                                townshipViewModel.logout()
                                viewModel.signOut()
                            },
                            onRetrySync = { townshipViewModel.triggerSync() },
                            onRefresh = { townshipViewModel.refresh() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ResidentVerifiedDashboard(
                            state = authState,
                            onSignOut = { viewModel.signOut() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (uiState.authMode) {
                            AuthMode.SIGN_IN -> {
                                ResidentLoginForm(
                                    uiState = uiState,
                                    onEmailChanged = viewModel::onEmailChanged,
                                    onPasswordChanged = viewModel::onPasswordChanged,
                                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                                    onToggleRemember = viewModel::onToggleRememberResident,
                                    onSignIn = viewModel::signInResident,
                                    onForgotPasswordClick = viewModel::openForgotPasswordDialog,
                                    onSwitchToRegister = { viewModel.setAuthMode(AuthMode.REGISTER) },
                                    onTabSelected = { mode -> viewModel.setAuthMode(mode) },
                                    onDomainPillClicked = { domain ->
                                        val currentLocal = uiState.email.substringBefore("@")
                                        val newEmail = if (currentLocal.isNotEmpty() && !uiState.email.contains("@")) {
                                            "$currentLocal@$domain"
                                        } else if (uiState.email.contains("@")) {
                                            "$currentLocal@$domain"
                                        } else {
                                            "resident@$domain"
                                        }
                                        viewModel.onEmailChanged(newEmail)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            AuthMode.REGISTER -> {
                                ResidentRegistrationForm(
                                    uiState = uiState,
                                    onNameChanged = viewModel::onRegNameChanged,
                                    onUnitChanged = viewModel::onRegUnitChanged,
                                    onSocietyChanged = viewModel::onRegSocietyChanged,
                                    onToggleIsAdult = viewModel::onToggleRegIsAdult,
                                    onEmailChanged = viewModel::onRegEmailChanged,
                                    onPasswordChanged = viewModel::onRegPasswordChanged,
                                    onConfirmPasswordChanged = viewModel::onRegConfirmPasswordChanged,
                                    onTogglePasswordVisibility = viewModel::onToggleRegPasswordVisibility,
                                    onToggleConfirmPasswordVisibility = viewModel::onToggleRegConfirmPasswordVisibility,
                                    onToggleTerms = viewModel::onToggleRegTerms,
                                    onRegister = viewModel::registerResident,
                                    onSwitchToLogin = { viewModel.setAuthMode(AuthMode.SIGN_IN) },
                                    onTabSelected = { mode -> viewModel.setAuthMode(mode) },
                                    onDomainPillClicked = { domain ->
                                        val currentLocal = uiState.regEmail.substringBefore("@")
                                        val newEmail = if (currentLocal.isNotEmpty() && !uiState.regEmail.contains("@")) {
                                            "$currentLocal@$domain"
                                        } else if (uiState.regEmail.contains("@")) {
                                            "$currentLocal@$domain"
                                        } else {
                                            "newresident@$domain"
                                        }
                                        viewModel.onRegEmailChanged(newEmail)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Forgot Password Modal Dialog
            if (uiState.showResetPasswordDialog) {
                ForgotPasswordDialog(
                    email = uiState.resetPasswordEmail,
                    onEmailChanged = viewModel::onResetPasswordEmailChanged,
                    status = uiState.resetPasswordStatus,
                    error = uiState.resetPasswordError,
                    onSend = viewModel::sendPasswordReset,
                    onDismiss = viewModel::dismissForgotPasswordDialog
                )
            }
        }
    }
}

@Composable
private fun AuthTabs(
    currentMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = if (currentMode == AuthMode.SIGN_IN) 0 else 1

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedIndex == 0,
                onClick = { onModeSelected(AuthMode.SIGN_IN) },
                modifier = Modifier.testTag("auth_tab_signin"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign In",
                            fontWeight = if (selectedIndex == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            )
            Tab(
                selected = selectedIndex == 1,
                onClick = { onModeSelected(AuthMode.REGISTER) },
                modifier = Modifier.testTag("auth_tab_register"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Register",
                            fontWeight = if (selectedIndex == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResidentLoginForm(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleRemember: (Boolean) -> Unit,
    onSignIn: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSwitchToRegister: () -> Unit,
    onTabSelected: (AuthMode) -> Unit,
    onDomainPillClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAuthenticating = uiState.authState is ResidentAuthState.Authenticating
    val isDomainValid = DomainValidator.isAuthorized(uiState.email)

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Decorative Header with Security Accent
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = "ConnectKar Community Portal",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ConnectKar",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("app_title")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Resident Portal Authentication",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Secure sign-in for registered township residents & owners",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Mode Switching Tabs
        AuthTabs(
            currentMode = AuthMode.SIGN_IN,
            onModeSelected = onTabSelected
        )

        // Error Banner
        if (uiState.authState is ResidentAuthState.Error) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("auth_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = (uiState.authState as ResidentAuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Rate Limiting / Brute-Force Alert Shield
        if (uiState.failedAttempts >= 3) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("rate_limit_warning")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Active",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security Shield Active: ${uiState.failedAttempts} unverified sign-in attempts detected. Credentials are cryptographically protected via Firebase Auth.",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Email Field Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resident Email",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isDomainValid) {
                        Text(
                            text = "Domain Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    placeholder = { Text("e.g. resident@connectkar.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Email",
                            tint = if (uiState.emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (isDomainValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid domain",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("email_valid_indicator")
                            )
                        } else if (uiState.emailError != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error indicator",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = uiState.emailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                AnimatedVisibility(
                    visible = uiState.emailError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.emailError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("email_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Authorized Domains Suggestion Section
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Domain Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Authorized Resident Domains",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("domain_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DomainValidator.AUTHORIZED_RESIDENT_DOMAINS.forEach { domain ->
                        val isCurrentDomain = uiState.email.endsWith("@$domain")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrentDomain) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrentDomain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDomainPillClicked(domain) }
                                .testTag("domain_pill_$domain")
                        ) {
                            Text(
                                text = "@$domain",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentDomain) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field Header with Forgot Password trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onForgotPasswordClick() }
                            .padding(vertical = 4.dp)
                            .testTag("forgot_password_button")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    placeholder = { Text("Enter your account password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = if (uiState.passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.testTag("toggle_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = uiState.passwordError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSignIn() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                AnimatedVisibility(
                    visible = uiState.passwordError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.passwordError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("password_error_text")
                        )
                    }
                }

                // Password Strength Indicator
                if (uiState.password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(
                        strength = uiState.passwordStrength,
                        modifier = Modifier.testTag("password_strength_bar")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.rememberResident,
                        onCheckedChange = onToggleRemember,
                        modifier = Modifier.testTag("remember_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Trust this resident device for 30 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = onSignIn,
                    enabled = !isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sign_in_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("sign_in_progress")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Verifying Credentials…")
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In as Resident",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch to Register Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New resident in the community? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create an Account",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onSwitchToRegister() }
                            .padding(4.dp)
                            .testTag("switch_to_register_button")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Footprint & Badges
        SecurityBadgesFooter()
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ResidentRegistrationForm(
    uiState: AuthUiState,
    onNameChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onSocietyChanged: (String) -> Unit,
    onToggleIsAdult: (Boolean) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onToggleTerms: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onTabSelected: (AuthMode) -> Unit,
    onDomainPillClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAuthenticating = uiState.authState is ResidentAuthState.Authenticating
    val isDomainValid = DomainValidator.isAuthorized(uiState.regEmail)

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Decorative Header
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = "New Resident Registration",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Create Resident Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("reg_title")
        )

        Text(
            text = "Register with your verified email and community unit to access amenities, billing, and security",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Mode Switching Tabs
        AuthTabs(
            currentMode = AuthMode.REGISTER,
            onModeSelected = onTabSelected
        )

        // Error Banner
        if (uiState.authState is ResidentAuthState.Error) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("reg_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = (uiState.authState as ResidentAuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Registration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 1. Full Name
                Text(
                    text = "Full Name",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.regName,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_name_input"),
                    placeholder = { Text("e.g. Alice Smith") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Full Name",
                            tint = if (uiState.regNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.regNameError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                AnimatedVisibility(
                    visible = uiState.regNameError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regNameError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_name_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Society / Gated Community Dropdown
                Text(
                    text = "Society / Gated Community",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                var showSocietyDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showSocietyDropdown,
                    onExpandedChange = { showSocietyDropdown = !showSocietyDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.regSociety,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("reg_society_input"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = "Society",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSocietyDropdown)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showSocietyDropdown,
                        onDismissRequest = { showSocietyDropdown = false }
                    ) {
                        TownshipSocieties.forEach { society ->
                            DropdownMenuItem(
                                text = { Text(society) },
                                onClick = {
                                    onSocietyChanged(society)
                                    showSocietyDropdown = false
                                },
                                modifier = Modifier.testTag("reg_society_option_$society")
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = uiState.regSocietyError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regSocietyError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_society_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Unit / Flat Number
                Text(
                    text = "Apartment / Unit Number",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.regUnitNumber,
                    onValueChange = onUnitChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_unit_input"),
                    placeholder = { Text("e.g. Tower B - 402 or Flat 204") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Unit Number",
                            tint = if (uiState.regUnitError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.regUnitError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                AnimatedVisibility(
                    visible = uiState.regUnitError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regUnitError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_unit_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Resident Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resident Email",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isDomainValid) {
                        Text(
                            text = "Authorized Domain",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.regEmail,
                    onValueChange = onEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_email_input"),
                    placeholder = { Text("e.g. resident@connectkar.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Email",
                            tint = if (uiState.regEmailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (isDomainValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid domain",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("reg_email_valid_indicator")
                            )
                        } else if (uiState.regEmailError != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error indicator",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = uiState.regEmailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                AnimatedVisibility(
                    visible = uiState.regEmailError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regEmailError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_email_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Domain Whitelist Chips
                Text(
                    text = "Authorized Community Domains:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_domain_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DomainValidator.AUTHORIZED_RESIDENT_DOMAINS.forEach { domain ->
                        val isCurrentDomain = uiState.regEmail.endsWith("@$domain")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrentDomain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrentDomain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDomainPillClicked(domain) }
                                .testTag("reg_domain_pill_$domain")
                        ) {
                            Text(
                                text = "@$domain",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentDomain) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Password
                Text(
                    text = "Create Password",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.regPassword,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_password_input"),
                    placeholder = { Text("At least 6 characters") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = if (uiState.regPasswordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.testTag("reg_toggle_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (uiState.isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.isRegPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (uiState.isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = uiState.regPasswordError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                AnimatedVisibility(
                    visible = uiState.regPasswordError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regPasswordError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_password_error_text")
                        )
                    }
                }

                // Real-time Strength Meter for registration
                if (uiState.regPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(
                        strength = uiState.regPasswordStrength,
                        modifier = Modifier.testTag("reg_password_strength_bar")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Confirm Password
                Text(
                    text = "Confirm Password",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.regConfirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_confirm_password_input"),
                    placeholder = { Text("Re-type password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password",
                            tint = if (uiState.regConfirmPasswordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onToggleConfirmPasswordVisibility,
                            modifier = Modifier.testTag("reg_toggle_confirm_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (uiState.isRegConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.isRegConfirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (uiState.isRegConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = uiState.regConfirmPasswordError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onRegister() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                AnimatedVisibility(
                    visible = uiState.regConfirmPasswordError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regConfirmPasswordError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 4.dp)
                                .testTag("reg_confirm_password_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 18+ Age Gate Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleIsAdult(!uiState.regIsAdult) }
                        .testTag("reg_age_gate_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.regIsAdult,
                        onCheckedChange = onToggleIsAdult,
                        modifier = Modifier.testTag("reg_is_adult_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I confirm I am 18 years of age or older",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = uiState.regIsAdultError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regIsAdultError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 2.dp, start = 8.dp)
                                .testTag("reg_is_adult_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Community Guidelines Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.regTermsAccepted,
                        onCheckedChange = onToggleTerms,
                        modifier = Modifier.testTag("reg_terms_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "I agree to ConnectKar Community Guidelines & Terms of Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = uiState.regTermsError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.regTermsError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 2.dp, start = 8.dp)
                                .testTag("reg_terms_error_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = onRegister,
                    enabled = !isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("reg_progress_indicator")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Creating Resident Account…")
                    } else {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Resident Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch to Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onSwitchToLogin() }
                            .padding(4.dp)
                            .testTag("switch_to_login_button")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Badges Footer
        SecurityBadgesFooter()
    }
}

@Composable
private fun SecurityBadgesFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Protected by Firebase Auth & Resident Domain Whitelist",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecurityFeatureBadge(label = "256-Bit SSL/TLS")
            SecurityFeatureBadge(label = "OAuth 2.0 / Firebase")
            SecurityFeatureBadge(label = "Encrypted Store")
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val progress = when (strength.level) {
        PasswordStrengthLevel.EMPTY -> 0f
        PasswordStrengthLevel.WEAK -> 0.33f
        PasswordStrengthLevel.MEDIUM -> 0.66f
        PasswordStrengthLevel.STRONG -> 1f
    }

    val color = when (strength.level) {
        PasswordStrengthLevel.EMPTY -> Color.Gray
        PasswordStrengthLevel.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrengthLevel.MEDIUM -> Color(0xFFF59E0B) // Amber
        PasswordStrengthLevel.STRONG -> Color(0xFF10B981) // Emerald Green
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "strengthProgress")
    val animatedColor by animateColorAsState(targetValue = color, label = "strengthColor")

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strength.level.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StrengthCheckCriterion(label = "6+ chars", met = strength.hasMinLength)
            StrengthCheckCriterion(label = "Uppercase", met = strength.hasUppercase)
            StrengthCheckCriterion(label = "Number", met = strength.hasNumber)
            StrengthCheckCriterion(label = "Symbol", met = strength.hasSpecial)
        }
    }
}

@Composable
private fun StrengthCheckCriterion(label: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (met) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (met) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SecurityFeatureBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChanged: (String) -> Unit,
    status: ResetPasswordStatus,
    error: String?,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("reset_password_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Reset Resident Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your registered township resident email to receive a secure Firebase recovery link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_password_input"),
                    placeholder = { Text("resident@connectkar.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                when (status) {
                    is ResetPasswordStatus.Sending -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatching password reset email…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is ResetPasswordStatus.Success -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }
                    is ResetPasswordStatus.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = status.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ResetPasswordStatus.Idle -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = status !is ResetPasswordStatus.Sending,
                modifier = Modifier.testTag("send_reset_button")
            ) {
                Text("Send Reset Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ResidentVerifiedDashboard(
    state: ResidentAuthState.Success,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(24.dp)
            .testTag("verified_resident_dashboard"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Verified Resident",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = state.displayName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("resident_display_name")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ResidentDetailRow(label = "Resident Email", value = state.email)
                Spacer(modifier = Modifier.height(8.dp))
                ResidentDetailRow(label = "Assigned Residence", value = state.unitNumber)
                Spacer(modifier = Modifier.height(8.dp))
                ResidentDetailRow(label = "Authentication Provider", value = state.authProvider)
                Spacer(modifier = Modifier.height(8.dp))
                ResidentDetailRow(
                    label = "Security Verification",
                    value = if (state.isVerified) "Verified Community Resident" else "Pending Verification"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sign_out_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out from Resident Session",
                color = MaterialTheme.colorScheme.onError,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResidentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
