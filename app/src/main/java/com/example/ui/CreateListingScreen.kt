package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.UserEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    initialType: String,
    currentUser: UserEntity,
    viewModel: CreateListingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentStep by viewModel.currentStep.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Initialize state once for this flow
    LaunchedEffect(initialType) {
        viewModel.initForFlow(initialType, currentUser)
    }

    // Auto-save draft on system back or back click
    val handleBackAndSave = {
        viewModel.saveDraft(currentUser) {
            Toast.makeText(context, "Progress saved as draft", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ConnectKar",
                            fontWeight = FontWeight.Bold,
                            color = ConciergePrimary,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBackAndSave) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ConciergeOnSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveDraft(currentUser) {
                                Toast.makeText(context, "Draft Saved", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Draft", color = ConciergePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConciergeSurface)
            )
        },
        containerColor = ConciergeSurface,
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = Color.White,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ConciergeOutlineVariant)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                1 -> {
                                    val titleVal = viewModel.title.value
                                    val catVal = viewModel.category.value
                                    if (titleVal.isBlank()) {
                                        Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                                    } else if (catVal.isBlank()) {
                                        Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.nextStep()
                                    }
                                }
                                2 -> viewModel.nextStep()
                                3 -> {
                                    viewModel.publishListing(currentUser) {
                                        Toast.makeText(context, "Listing published successfully!", Toast.LENGTH_LONG).show()
                                        onBack()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConciergePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp)
                            .testTag(if (currentStep == 3) "create_listing_submit_button" else "next_step_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (currentStep) {
                                    1 -> "Continue"
                                    2 -> "Review"
                                    else -> "Publish Listing"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = if (currentStep == 3) Icons.Default.Send else Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ConciergeSurface)
        ) {
            // STEP PROGRESS BAR
            StepProgressBar(currentStep = currentStep)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> Step1BasicInfo(viewModel = viewModel, moduleType = initialType)
                    2 -> Step2Details(viewModel = viewModel, moduleType = initialType)
                    3 -> Step3Review(viewModel = viewModel, currentUser = currentUser)
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(currentStep: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ConciergeSurfaceContainerLow)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP $currentStep OF 3",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergePrimary,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = when (currentStep) {
                    1 -> "Basic Info"
                    2 -> "Logistics & Preferences"
                    else -> "Review & Publish"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ConciergeOnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (step in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (step <= currentStep) ConciergePrimary else ConciergeSurfaceContainerHighest
                        )
                )
            }
        }
    }
}

// ==================== STEP 1: BASIC INFO ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1BasicInfo(
    viewModel: CreateListingViewModel,
    moduleType: String
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val category by viewModel.category.collectAsState()
    val condition by viewModel.condition.collectAsState()
    val isSocietyOnly by viewModel.isSocietyOnly.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()

    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Defined lists of categories per module type
    val categoriesList = remember(moduleType) {
        when (moduleType) {
            "MARKETPLACE" -> listOf("Electronics", "Home & Living", "Vehicles", "Fashion", "Sports & Outdoors", "Books & Education")
            "SERVICE" -> listOf("Plumbing", "Electrical", "Cleaning", "Tuition & Coaching", "Baby Sitting", "Gardening")
            "EVENT" -> listOf("Sports", "Festival", "Welfare", "Cultural", "General Gathering")
            "CARPOOL" -> listOf("Daily Commute", "Intercity Ride", "Weekend Getaway", "Airport Run")
            "PROPERTY" -> listOf("1 BHK Rent", "2 BHK Rent", "3 BHK Rent", "PG / Shared Accommodation", "Commercial Space")
            "MEAL" -> listOf("Breakfast", "Lunch Veg", "Lunch Non-Veg", "Dinner Veg", "Dinner Non-Veg", "Home Bakery", "Desserts")
            "VEHICLE" -> listOf("Two Wheeler", "Hatchback", "Sedan", "SUV", "Luxury / Premium")
            else -> listOf("General Discussion", "Alert / Safety", "Society Notice", "Complaints", "Lost & Found")
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addPhoto(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // PHOTOS SECTION
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "PRODUCT PHOTOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnSurfaceVariant,
                letterSpacing = 1.sp
            )

            if (selectedPhotos.isEmpty()) {
                // Empty state photo upload box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ConciergeSurfaceContainerLow)
                        .border(1.dp, ConciergeOutlineVariant, RoundedCornerShape(16.dp))
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = ConciergePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "Upload Photos",
                            fontWeight = FontWeight.Bold,
                            color = ConciergeOnSurface
                        )
                        Text(
                            "High-quality images increase trust by 80%",
                            fontSize = 11.sp,
                            color = ConciergeOnSurfaceVariant
                        )
                    }
                }
            } else {
                // Horizontal scroll list of chosen images with add tile
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(selectedPhotos) { photoUri ->
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ConciergeOutlineVariant, RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button overlays
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { viewModel.removePhoto(photoUri) }
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Small Add Button in Carousel
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ConciergeSurfaceContainerLow)
                                .border(1.dp, ConciergeOutlineVariant, RoundedCornerShape(12.dp))
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add More",
                                tint = ConciergePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // TITLE FIELD (WITH LIVE CHAR COUNTER)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TITLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${title.length}/60",
                    fontSize = 11.sp,
                    color = if (title.length > 60) Color.Red else ConciergeOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.setTitle(it) },
                placeholder = { Text("e.g. Designer Lounge Chair") },
                singleLine = true,
                isError = title.length > 60,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergePrimary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }

        // PRICE & CATEGORY DROPDOWN ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price Field (₹)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PRICE (₹)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { viewModel.setPrice(it) },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ConciergePrimary,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // Category Field
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                            .testTag("create_category_dropdown"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ConciergePrimary,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            disabledBorderColor = ConciergeOutlineVariant
                        ),
                        enabled = false // Disable direct text input so click handles dropdown
                    )
                    // Invisible box overlay to catch click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCategoryDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categoriesList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = ConciergeOnSurface) },
                                onClick = {
                                    viewModel.setCategory(cat)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // CONDITION PILLS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CONDITION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnSurfaceVariant,
                letterSpacing = 1.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("New", "Like New", "Good", "Fair").forEach { condOption ->
                    val isSelected = condition == condOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) ConciergePrimary else Color.White
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ConciergePrimary else ConciergeOutlineVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setCondition(condOption) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = condOption,
                            color = if (isSelected) Color.White else ConciergeOnSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // DESCRIPTION FIELD
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "DESCRIPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnSurfaceVariant,
                letterSpacing = 1.sp
            )
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.setDescription(it) },
                placeholder = { Text("Tell the society more about what you're listing...") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_desc"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergePrimary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }

        // PRIVACY SETTINGS CARD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Privacy Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ConciergeOnSurface
                        )
                        Text(
                            "Control who can view your listing",
                            fontSize = 11.sp,
                            color = ConciergeOnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSocietyOnly,
                        onCheckedChange = { viewModel.setSocietyOnly(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ConciergePrimary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Society Only Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSocietyOnly) ConciergePrimary.copy(alpha = 0.08f) else ConciergeSurfaceContainerHighest
                            )
                            .border(
                                1.dp,
                                if (isSocietyOnly) ConciergePrimary else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSocietyOnly(true) }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isSocietyOnly) ConciergePrimary else ConciergeOutline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "SOCIETY ONLY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSocietyOnly) ConciergePrimary else ConciergeOutline
                                    )
                                }
                                if (isSocietyOnly) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ConciergePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                "Verified members only. High trust, faster sales.",
                                fontSize = 10.sp,
                                color = ConciergeOnSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // Public Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isSocietyOnly) ConciergePrimary.copy(alpha = 0.08f) else ConciergeSurfaceContainerHighest
                            )
                            .border(
                                1.dp,
                                if (!isSocietyOnly) ConciergePrimary else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSocietyOnly(false) }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (!isSocietyOnly) ConciergePrimary else ConciergeOutline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "PUBLIC",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isSocietyOnly) ConciergePrimary else ConciergeOutline
                                    )
                                }
                                if (!isSocietyOnly) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ConciergePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                "Visible to everyone. Wider reach, varied township.",
                                fontSize = 10.sp,
                                color = ConciergeOnSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==================== STEP 2: SWAPPABLE DETAILS ====================
@Composable
fun Step2Details(
    viewModel: CreateListingViewModel,
    moduleType: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Swappable flow based on category/module type
        when (moduleType) {
            "MARKETPLACE" -> MarketplaceStep2Details(viewModel)
            "VEHICLE" -> VehicleStep2Details(viewModel)
            "PROPERTY" -> PropertyStep2Details(viewModel)
            else -> GeneralStep2Details(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceStep2Details(viewModel: CreateListingViewModel) {
    val brand by viewModel.brand.collectAsState()
    val model by viewModel.model.collectAsState()
    val itemAge by viewModel.itemAge.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val meetupLocation by viewModel.meetupLocation.collectAsState()
    val preferredDays by viewModel.preferredDays.collectAsState()
    val timePreference by viewModel.timePreference.collectAsState()
    val isNegotiable by viewModel.isNegotiable.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    var showAgeDropdown by remember { mutableStateOf(false) }

    // DYNAMIC REAL DATES GENERATION
    val realCalendarDays = remember {
        val list = mutableListOf<Pair<String, String>>() // Day Abbreviation (MON), Day Number (12)
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfNum = SimpleDateFormat("d", Locale.getDefault())
        val cal = Calendar.getInstance()
        for (i in 0 until 4) {
            list.add(Pair(sdfDay.format(cal.time).uppercase(), sdfNum.format(cal.time)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Text(
        text = "ITEM SPECIFICATIONS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ConciergePrimary,
        letterSpacing = 1.sp
    )

    // Brand and Model
    OutlinedTextField(
        value = brand,
        onValueChange = { viewModel.setBrand(it) },
        label = { Text("Brand Name") },
        placeholder = { Text("e.g. Samsung, Apple, Sony") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ConciergePrimary,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )

    OutlinedTextField(
        value = model,
        onValueChange = { viewModel.setModel(it) },
        label = { Text("Model Reference") },
        placeholder = { Text("e.g. Galaxy S24 Ultra") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ConciergePrimary,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item Age Dropdown
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Item Age",
                fontSize = 11.sp,
                color = ConciergeOnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = itemAge,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAgeDropdown = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ConciergePrimary,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        disabledBorderColor = ConciergeOutlineVariant
                    ),
                    enabled = false
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showAgeDropdown = true }
                )
                DropdownMenu(
                    expanded = showAgeDropdown,
                    onDismissRequest = { showAgeDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    listOf("New", "< 1 Year", "1-3 Years", "3+ Years").forEach { ageOpt ->
                        DropdownMenuItem(
                            text = { Text(ageOpt, color = ConciergeOnSurface) },
                            onClick = {
                                viewModel.setItemAge(ageOpt)
                                showAgeDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Quantity Stepper (Shown only for Marketplace / Meal)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Quantity",
                fontSize = 11.sp,
                color = ConciergeOnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConciergeSurfaceContainerLow)
                    .border(1.dp, ConciergeOutlineVariant, RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.decrementQuantity() }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = ConciergePrimary)
                }
                Text(
                    text = quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ConciergeOnSurface
                )
                IconButton(onClick = { viewModel.incrementQuantity() }) {
                    Icon(Icons.Default.Add, contentDescription = "Increment", tint = ConciergePrimary)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "PICKUP & EXCHANGE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ConciergeSecondary,
        letterSpacing = 1.sp
    )

    // Meetup location
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Suggested Meetup Location",
            fontSize = 11.sp,
            color = ConciergeOnSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Main Gate", "Clubhouse", "Block Lobby").forEach { loc ->
                val isSelected = meetupLocation == loc
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ConciergePrimary.copy(alpha = 0.1f) else Color.White)
                        .border(
                            1.dp,
                            if (isSelected) ConciergePrimary else ConciergeOutlineVariant,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setMeetupLocation(loc) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (loc == "Main Gate") Icons.Default.LocationOn else Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = if (isSelected) ConciergePrimary else ConciergeOutline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            loc,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ConciergePrimary else ConciergeOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Preferred days binding to real calendar dates
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Preferred Days",
            fontSize = 11.sp,
            color = ConciergeOnSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            realCalendarDays.forEach { (dayName, dayNum) ->
                val dayId = "$dayName $dayNum"
                val isSelected = preferredDays.contains(dayId)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) ConciergePrimary else Color.White)
                        .border(
                            1.dp,
                            if (isSelected) ConciergePrimary else ConciergeOutlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.togglePreferredDay(dayId) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else ConciergeOutline
                        )
                        Text(
                            dayNum,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else ConciergeOnSurface
                        )
                    }
                }
            }
        }
    }

    // Time Preference Morning vs Evening
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Time Preference",
            fontSize = 11.sp,
            color = ConciergeOnSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Morning" to Icons.Default.WbSunny, "Evening" to Icons.Default.NightsStay).forEach { (time, icon) ->
                val isSelected = timePreference == time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ConciergePrimary.copy(alpha = 0.08f) else Color.White)
                        .border(
                            2.dp,
                            if (isSelected) ConciergePrimary else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setTimePreference(time) }
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) ConciergePrimary else ConciergeOutline
                        )
                        Text(
                            time,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ConciergePrimary else ConciergeOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Price & Payment Options Card
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Price & Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ConciergePrimary
            )

            // Negotiable Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = ConciergePrimary)
                    Text("Price is Negotiable", fontWeight = FontWeight.Medium, color = ConciergeOnSurface)
                }
                Switch(
                    checked = isNegotiable,
                    onCheckedChange = { viewModel.setNegotiable(it) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = ConciergePrimary
                    )
                )
            }

            // Accepted Methods
            Text(
                "Accepted Methods",
                fontSize = 11.sp,
                color = ConciergeOnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ConnectPay (Placeholder/Future feature - marked as coming soon)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(1.dp, ConciergeOutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ConciergeSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ConciergeOutline, modifier = Modifier.size(16.dp))
                        }
                        Text("ConnectPay", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ConciergeOutline)
                        Box(
                            modifier = Modifier
                                .background(ConciergePrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("SOON", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ConciergePrimary)
                        }
                    }
                }

                // UPI (Active)
                val isUpiSelected = paymentMethods.contains("UPI")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUpiSelected) ConciergePrimary.copy(alpha = 0.08f) else Color.White)
                        .border(
                            1.dp,
                            if (isUpiSelected) ConciergePrimary else ConciergeOutlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.togglePaymentMethod("UPI") }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isUpiSelected) ConciergePrimary else ConciergeSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = if (isUpiSelected) Color.White else ConciergeOnSurface
                            )
                        }
                        Text("UPI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ConciergeOnSurface)
                    }
                }

                // Cash (Active)
                val isCashSelected = paymentMethods.contains("Cash")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCashSelected) ConciergePrimary.copy(alpha = 0.08f) else Color.White)
                        .border(
                            1.dp,
                            if (isCashSelected) ConciergePrimary else ConciergeOutlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.togglePaymentMethod("Cash") }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCashSelected) ConciergePrimary else ConciergeSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = if (isCashSelected) Color.White else ConciergeOnSurface
                            )
                        }
                        Text("Cash", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ConciergeOnSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleStep2Details(viewModel: CreateListingViewModel) {
    val brand by viewModel.brand.collectAsState()
    val model by viewModel.model.collectAsState()
    val meetupLocation by viewModel.meetupLocation.collectAsState()
    val isNegotiable by viewModel.isNegotiable.collectAsState()

    Text(
        text = "VEHICLE SPECIFICATIONS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ConciergePrimary,
        letterSpacing = 1.sp
    )

    OutlinedTextField(
        value = brand,
        onValueChange = { viewModel.setBrand(it) },
        label = { Text("Vehicle Make/Brand") },
        placeholder = { Text("e.g. Honda, Suzuki, Toyota") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = model,
        onValueChange = { viewModel.setModel(it) },
        label = { Text("Vehicle Model & Plate Number") },
        placeholder = { Text("e.g. Civic - MH-12-AB-1234") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = meetupLocation,
        onValueChange = { viewModel.setMeetupLocation(it) },
        label = { Text("Suggested Pickup Spot / Parking Bay") },
        placeholder = { Text("e.g. Block C Parking Spot 45") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null, tint = ConciergePrimary)
            Text("Ride Cost is Negotiable", fontWeight = FontWeight.Medium, color = ConciergeOnSurface)
        }
        Switch(
            checked = isNegotiable,
            onCheckedChange = { viewModel.setNegotiable(it) },
            colors = SwitchDefaults.colors(checkedTrackColor = ConciergePrimary)
        )
    }
}

@Composable
fun PropertyStep2Details(viewModel: CreateListingViewModel) {
    val brand by viewModel.brand.collectAsState()
    val model by viewModel.model.collectAsState()
    val meetupLocation by viewModel.meetupLocation.collectAsState()
    val isNegotiable by viewModel.isNegotiable.collectAsState()

    Text(
        text = "PROPERTY SPECIFICATIONS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ConciergePrimary,
        letterSpacing = 1.sp
    )

    OutlinedTextField(
        value = brand,
        onValueChange = { viewModel.setBrand(it) },
        label = { Text("Configuration / BHK Type") },
        placeholder = { Text("e.g. 2 BHK, Fully Furnished") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = model,
        onValueChange = { viewModel.setModel(it) },
        label = { Text("Key Handover / Security Details") },
        placeholder = { Text("e.g. Deposit ₹50,000, 1 month notice") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = meetupLocation,
        onValueChange = { viewModel.setMeetupLocation(it) },
        label = { Text("Viewing Location / Flat Number") },
        placeholder = { Text("e.g. Flat C-903, Block C") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null, tint = ConciergePrimary)
            Text("Rent Price is Negotiable", fontWeight = FontWeight.Medium, color = ConciergeOnSurface)
        }
        Switch(
            checked = isNegotiable,
            onCheckedChange = { viewModel.setNegotiable(it) },
            colors = SwitchDefaults.colors(checkedTrackColor = ConciergePrimary)
        )
    }
}

@Composable
fun GeneralStep2Details(viewModel: CreateListingViewModel) {
    val brand by viewModel.brand.collectAsState()
    val meetupLocation by viewModel.meetupLocation.collectAsState()
    val isNegotiable by viewModel.isNegotiable.collectAsState()

    Text(
        text = "LOGISTICS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ConciergePrimary,
        letterSpacing = 1.sp
    )

    OutlinedTextField(
        value = brand,
        onValueChange = { viewModel.setBrand(it) },
        label = { Text("Timings / Validity Details") },
        placeholder = { Text("e.g. Available daily from 5PM to 8PM") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = meetupLocation,
        onValueChange = { viewModel.setMeetupLocation(it) },
        label = { Text("Meetup/Action Location") },
        placeholder = { Text("e.g. Clubhouse Lounge, Block Lobby") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null, tint = ConciergePrimary)
            Text("Price terms are negotiable", fontWeight = FontWeight.Medium, color = ConciergeOnSurface)
        }
        Switch(
            checked = isNegotiable,
            onCheckedChange = { viewModel.setNegotiable(it) },
            colors = SwitchDefaults.colors(checkedTrackColor = ConciergePrimary)
        )
    }
}


// ==================== STEP 3: REVIEW & PUBLISH ====================
@Composable
fun Step3Review(
    viewModel: CreateListingViewModel,
    currentUser: UserEntity
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val category by viewModel.category.collectAsState()
    val condition by viewModel.condition.collectAsState()
    val isSocietyOnly by viewModel.isSocietyOnly.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()

    val brand by viewModel.brand.collectAsState()
    val model by viewModel.model.collectAsState()
    val meetupLocation by viewModel.meetupLocation.collectAsState()
    val preferredDays by viewModel.preferredDays.collectAsState()
    val timePreference by viewModel.timePreference.collectAsState()
    val isNegotiable by viewModel.isNegotiable.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Review & Publish",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnSurface
            )
            Text(
                text = "Ensure all details are correct before taking your listing live to the society community.",
                fontSize = 13.sp,
                color = ConciergeOnSurfaceVariant
            )
        }

        // HERO IMAGE PREVIEW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ConciergeSurfaceContainerLow)
                .border(1.dp, ConciergeOutlineVariant, RoundedCornerShape(20.dp))
        ) {
            if (selectedPhotos.isNotEmpty()) {
                AsyncImage(
                    model = selectedPhotos.first(),
                    contentDescription = "Main Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = ConciergeOutline, modifier = Modifier.size(48.dp))
                        Text("No Photo Uploaded", fontWeight = FontWeight.Bold, color = ConciergeOutline)
                    }
                }
            }

            // Floating Badges on Image
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(ConciergeSecondary.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(condition, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                if (isNegotiable) {
                    Box(
                        modifier = Modifier
                            .background(ConciergeSecondary.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Negotiable", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Edit floating action trigger
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
                    .clickable { viewModel.setStep(1) }
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Step 1", tint = ConciergePrimary, modifier = Modifier.size(16.dp))
            }
        }

        // DETAILED INFORMATION CARD (Read-only render of same state)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
            border = BorderStroke(1.dp, ConciergeOutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "DETAILED INFORMATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ConciergePrimary,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ConciergeOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Divider(color = ConciergeOutlineVariant.copy(alpha = 0.4f))

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Title", fontSize = 11.sp, color = ConciergeOnSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(title, fontWeight = FontWeight.Bold, color = ConciergePrimary, fontSize = 14.sp)
                }

                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Price", fontSize = 11.sp, color = ConciergeOnSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text("₹ $price", fontWeight = FontWeight.ExtraBold, color = ConciergePrimary, fontSize = 18.sp)
                }

                // Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category", fontSize = 11.sp, color = ConciergeOnSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(category, fontWeight = FontWeight.Medium, color = ConciergeOnSurface, fontSize = 14.sp)
                }

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Description", fontSize = 11.sp, color = ConciergeOnSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(
                        text = description.ifEmpty { "No description provided." },
                        color = ConciergeOnSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // LISTING VISIBILITY CARD (Loads verified resident's actual society)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
            border = BorderStroke(1.dp, ConciergeOutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "LISTING VISIBILITY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = ConciergePrimary,
                    letterSpacing = 1.sp
                )

                // Visibility Detail Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ConciergePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ConciergePrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isSocietyOnly) "Resident Verified" else "Public Access",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ConciergeOnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .background(ConciergeSecondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SOCIETY-APPROVED", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = ConciergeSecondary)
                            }
                        }
                        Text(
                            text = if (isSocietyOnly) "Only verified residents of ${currentUser.society} can see this." else "Visible to all township residents.",
                            fontSize = 11.sp,
                            color = ConciergeOnSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Location Box (Loads real currentUser.society)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ConciergeVehicles.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ConciergeVehicles, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = currentUser.society.ifBlank { "My Estate" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ConciergeOnSurface
                        )
                        Text(
                            text = "Meetup at designated spot: $meetupLocation",
                            fontSize = 11.sp,
                            color = ConciergeOnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Guidelines Notice
        Text(
            text = "By publishing, you agree to ConnectKar's Community Guidelines and Privacy Policy.",
            fontSize = 11.sp,
            color = ConciergeOnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            lineHeight = 15.sp
        )
    }
}
