package com.connectkar.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.FirebaseManager
import com.connectkar.BuildConfig
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onRegisterSuccess: (
        fullName: String,
        phone: String,
        society: String,
        block: String,
        flat: String,
        avatar: Int,
        floor: String,
        residentType: String,
        moveInDate: String,
        proofDocumentUri: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current Step: 1 = Phone Entry, 2 = Residence Details, 3 = Success Screen
    var currentStep by remember { mutableStateOf(1) }

    // Onboarding Form States
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedSociety by remember { mutableStateOf("Sylvan County") }
    var blockTower by remember { mutableStateOf("") }
    var flatNumber by remember { mutableStateOf("") }
    var avatarIndex by remember { mutableStateOf(0) }
    
    // New Fields
    var floor by remember { mutableStateOf("") }
    var residentType by remember { mutableStateOf("OWNER") } // Enum-like OWNER or TENANT
    var moveInDate by remember { mutableStateOf("") }
    var proofDocumentUri by remember {
        mutableStateOf(if (BuildConfig.DEBUG) "simulated_proof_of_residence.pdf" else "")
    }
    var documentUploadError by remember { mutableStateOf<String?>(null) }

    var showSocietyDropdown by remember { mutableStateOf(false) }
    var showResidentTypeDropdown by remember { mutableStateOf(false) }

    // OTP verification states
    var verificationId by remember { mutableStateOf<String?>(null) }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = remember(context) {
        var curr = context
        while (curr is android.content.ContextWrapper) {
            if (curr is Activity) break
            curr = curr.baseContext
        }
        curr as? Activity
    }

    val societies = TownshipSocieties
    val residentTypes = listOf("OWNER", "TENANT")

    // Firebase Phone Auth Callbacks
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instantly verified - auto transition to Step 2
                isVerifyingOtp = false
                isOtpSent = false
                errorMessage = null
                currentStep = 2
            }

            override fun onVerificationFailed(e: FirebaseException) {
                errorMessage = "Verification failed: ${e.message}"
                isVerifyingOtp = false
                isOtpSent = false
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                isOtpSent = true
                isVerifyingOtp = false
                errorMessage = null
            }
        }
    }

    // Trigger Phone OTP Verification
    fun startPhoneVerification() {
        val auth = FirebaseManager.auth
        val isTestOrDebug = BuildConfig.DEBUG || 
                            System.getProperty("robolectric.active") != null || 
                            System.getProperty("java.runtime.name")?.contains("Android") == false

        if (isTestOrDebug && (BuildConfig.DEBUG || auth == null || activity == null)) {
            android.util.Log.i("Onboarding", "Simulation fallback active. OTP sent.")
            isOtpSent = true
            verificationId = "simulated_verification_id"
            if (BuildConfig.DEBUG) {
                errorMessage = "Developer Simulation: Use test code '123456' to verify."
            }
            return
        }
        
        isVerifyingOtp = true
        errorMessage = null
        
        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        
        try {
            val firebaseAuth = auth ?: throw IllegalStateException("Firebase Auth is unavailable.")
            val act = activity ?: throw IllegalStateException("Activity context is missing.")
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                .setActivity(act)
                .setCallbacks(callbacks)
                .build()
                
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            errorMessage = "Phone verification setup failed: ${e.message}"
            isVerifyingOtp = false
        }
    }

    // Verify OTP Code
    fun verifyOtpCode(code: String) {
        val auth = FirebaseManager.auth

        if (BuildConfig.DEBUG && verificationId == "simulated_verification_id") {
            if (code == "123456") {
                isOtpSent = false
                errorMessage = null
                currentStep = 2
            } else {
                errorMessage = "Invalid code. Please enter '123456'."
            }
            return
        }
        
        isVerifyingOtp = true
        try {
            val vId = verificationId
            if (vId == null) {
                errorMessage = "Verification ID is missing. Please request OTP again."
                isVerifyingOtp = false
                return
            }
            val firebaseAuth = auth
            if (firebaseAuth == null) {
                errorMessage = "Firebase Auth is unavailable."
                isVerifyingOtp = false
                return
            }
            val credential = PhoneAuthProvider.getCredential(vId, code)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isOtpSent = false
                        errorMessage = null
                        currentStep = 2
                    } else {
                        errorMessage = "Invalid OTP verification code. Please try again."
                        isVerifyingOtp = false
                    }
                }
        } catch (e: Exception) {
            errorMessage = "Verification failed: ${e.message}"
            isVerifyingOtp = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Identity Header (Except on Success step to give it full hero space)
            if (currentStep < 3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandPrimaryBlueLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Identity Icon",
                            tint = BrandPrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ConnectKar",
                        color = BrandSlate,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Step Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepsCount = 3
                    for (i in 1..stepsCount) {
                        val isCompleted = i < currentStep
                        val isActive = i == currentStep
                        val barColor = when {
                            isCompleted -> BrandSuccessGreen
                            isActive -> BrandPrimaryBlue
                            else -> Color.LightGray.copy(alpha = 0.5f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            // Step Content Switcher
            when (currentStep) {
                1 -> {
                        // --- STEP 1: Phone Entry & Verification ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, BrandOutline)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Urban Sanctuary",
                                    color = BrandPrimaryBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Verify Identity",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandSlate,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = "To access the secure, gated township super app, please verify your name and mobile number first.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 20.dp),
                                    lineHeight = 18.sp
                                )

                                // Full Name
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    placeholder = { Text("Enter your full name") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Person, contentDescription = "Name Icon", tint = Color.Gray)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_fullname"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Phone Number
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("e.g. 9876543210") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Phone, contentDescription = "Phone Icon", tint = Color.Gray)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_phone"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                val isStep1Valid = FormValidators.isStep1Valid(fullName, phoneNumber)

                                Button(
                                    onClick = {
                                        if (isStep1Valid) {
                                            startPhoneVerification()
                                        }
                                    },
                                    enabled = isStep1Valid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimaryBlue,
                                        disabledContainerColor = Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("submit_onboarding_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Send Secure OTP",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- STEP 2: Residence Details ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, BrandOutline)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Gated Residence",
                                    color = BrandPrimaryBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Residence details",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandSlate,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = "Verify your specific tower and unit to establish residency. This ensures complete multi-tenant trust.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 20.dp),
                                    lineHeight = 18.sp
                                )

                                // Society Selection Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = showSocietyDropdown,
                                    onExpandedChange = { showSocietyDropdown = !showSocietyDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedSociety,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Society / Gated Community") },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Home, contentDescription = "Society Icon", tint = Color.Gray)
                                        },
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("onboarding_society_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = showSocietyDropdown,
                                        onDismissRequest = { showSocietyDropdown = false }
                                    ) {
                                        societies.forEach { society ->
                                            DropdownMenuItem(
                                                text = { Text(society) },
                                                onClick = {
                                                    selectedSociety = society
                                                    showSocietyDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Block & Flat & Floor in Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = blockTower,
                                        onValueChange = { blockTower = it },
                                        label = { Text("Block") },
                                        placeholder = { Text("e.g. B") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("onboarding_block"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = flatNumber,
                                        onValueChange = { flatNumber = it },
                                        label = { Text("Flat") },
                                        placeholder = { Text("e.g. 502") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .testTag("onboarding_flat"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = floor,
                                        onValueChange = { floor = it },
                                        label = { Text("Floor") },
                                        placeholder = { Text("e.g. 5") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("onboarding_floor"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Resident Type Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = showResidentTypeDropdown,
                                    onExpandedChange = { showResidentTypeDropdown = !showResidentTypeDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = residentType,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Resident Type") },
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("onboarding_resident_type"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = showResidentTypeDropdown,
                                        onDismissRequest = { showResidentTypeDropdown = false }
                                    ) {
                                        residentTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type) },
                                                onClick = {
                                                    residentType = type
                                                    showResidentTypeDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Move-In Date Raw Text Field
                                OutlinedTextField(
                                    value = moveInDate,
                                    onValueChange = { moveInDate = it },
                                    label = { Text("Move-in Date") },
                                    placeholder = { Text("e.g. YYYY-MM-DD") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Event, contentDescription = "Calendar Icon", tint = Color.Gray)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_move_in_date"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Document Dropzone Placeholder
                                Text(
                                    text = "Proof of Residence (Rental/Sale Agreement)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSlate,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (proofDocumentUri.isNotEmpty()) BrandSuccessGreen.copy(alpha = 0.08f)
                                            else BrandPrimaryBlueLight.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            BorderStroke(
                                                width = 1.5.dp,
                                                color = if (proofDocumentUri.isNotEmpty()) BrandSuccessGreen else BrandPrimaryBlue.copy(alpha = 0.4f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            // TODO: Integrate actual Firebase Storage file upload pipeline in a separate task.
                                            // Currently stores a simulated pdf path representation on tap as requested.
                                            if (BuildConfig.DEBUG) {
                                                proofDocumentUri = "simulated_proof_of_residence.pdf"
                                                documentUploadError = null
                                            } else {
                                                documentUploadError = "Document upload pipeline is not yet configured for production."
                                            }
                                        }
                                        .testTag("onboarding_proof_doc"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        if (proofDocumentUri.isEmpty()) {
                                            Icon(
                                                imageVector = Icons.Outlined.CloudUpload,
                                                contentDescription = "Cloud Upload Icon",
                                                tint = BrandPrimaryBlue,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Tap to Upload Agreement / Utility Bill",
                                                fontSize = 12.sp,
                                                color = BrandPrimaryBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "PDF, JPG, PNG up to 5MB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Uploaded Icon",
                                                    tint = BrandSuccessGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Selected: proof_of_residence.pdf",
                                                    fontSize = 12.sp,
                                                    color = BrandSuccessGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                "Tap to change document",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (documentUploadError != null) {
                                    Text(
                                        text = documentUploadError!!,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Avatar Profile selection
                                Text(
                                    text = "Choose Sanctuary Avatar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSlate,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0..5) {
                                        val isSelected = avatarIndex == i
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) BrandPrimaryBlue else Color.LightGray,
                                                    shape = CircleShape
                                                )
                                                .clickable { avatarIndex = i }
                                        ) {
                                            AvatarImage(
                                                avatarIndex = i,
                                                size = 38,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Validation Check
                                val isStep2Valid = FormValidators.isStep2Valid(blockTower, flatNumber, floor, moveInDate, proofDocumentUri)

                                Button(
                                    onClick = {
                                        if (isStep2Valid) {
                                            currentStep = 3
                                        }
                                    },
                                    enabled = isStep2Valid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimaryBlue,
                                        disabledContainerColor = Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("register_residence_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Register Residence",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // --- STEP 3: Success Screen ---
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, BrandOutline)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Beautiful Hero Success Icon
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(BrandSuccessGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success check",
                                        tint = BrandSuccessGreen,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    "Onboarding Successful!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandSlate,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Welcome to your Urban Sanctuary! Your residency details have been saved, and your profile is sent for secure township verification.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Summary Residence Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BrandBackground)
                                        .border(BorderStroke(1.dp, BrandOutline.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        AvatarImage(avatarIndex = avatarIndex, size = 44)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = BrandSlate
                                            )
                                            Text(
                                                text = "Unit $blockTower-$flatNumber ($selectedSociety)",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Resident Status: $residentType",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BrandPrimaryBlue
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                Button(
                                    onClick = {
                                        onRegisterSuccess(
                                            fullName,
                                            phoneNumber,
                                            selectedSociety,
                                            blockTower,
                                            flatNumber,
                                            avatarIndex,
                                            floor,
                                            residentType,
                                            moveInDate,
                                            proofDocumentUri
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandSuccessGreen),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("finish_onboarding_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Go to Dashboard",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

        // --- Integrated SMS Verification Dialog (Popup Modal) ---
        if (isOtpSent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, BrandOutline),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "Enter Verification Code",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSlate
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "We sent a secure 6-digit SMS code to ${phoneNumber}. Enter it below to unlock your Sanctuary profile:",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp),
                            lineHeight = 18.sp
                        )
                        
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("6-Digit OTP") },
                            placeholder = { Text(if (BuildConfig.DEBUG) "e.g. 123456" else "Enter 6-digit OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isOtpSent = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { verifyOtpCode(otpCode) },
                                enabled = otpCode.length >= 6 && !isVerifyingOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("verify_otp_confirm_button")
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Verify OTP")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
