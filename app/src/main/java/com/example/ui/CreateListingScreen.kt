package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    initialType: String,
    currentUser: UserEntity,
    onBack: () -> Unit,
    onSubmitListing: (
        type: String,
        title: String,
        description: String,
        price: Double,
        contact: String,
        category: String,
        extra1: String,
        extra2: String,
        extra3: String,
        extra4: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // State management for all potential form fields
    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf(currentUser.phoneNumber) }
    var category by remember { mutableStateOf("") }
    
    // Extras
    var extra1 by remember { mutableStateOf("") } // e.g. origin, rent-location, meal veg/nonveg
    var extra2 by remember { mutableStateOf("") } // e.g. destination, BHK, timing
    var extra3 by remember { mutableStateOf("") } // e.g. seats, availability, preorder
    var extra4 by remember { mutableStateOf("") } // e.g. departure time, rating

    var showTypeDropdown by remember { mutableStateOf(false) }

    val listingTypes = listOf(
        Pair("MARKETPLACE", "Sell Product"),
        Pair("SERVICE", "Offer Service"),
        Pair("EVENT", "Post Event"),
        Pair("CARPOOL", "Offer Carpool"),
        Pair("PROPERTY", "Rent Property"),
        Pair("MEAL", "Sell Meal"),
        Pair("FEED", "Create Post")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Listing", fontWeight = FontWeight.Bold, color = BrandSlate) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandSlate)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        containerColor = BrandBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Option Selector Dropdown
            Text(
                text = "Listing Flow Selection",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandSlate,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentDisplayType = listingTypes.find { it.first == selectedType }?.second ?: "Select Flow"
                OutlinedTextField(
                    value = currentDisplayType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("create_listing_flow_dropdown"),
                    shape = RoundedCornerShape(28.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    listingTypes.forEach { pair ->
                        DropdownMenuItem(
                            text = { Text(pair.second) },
                            onClick = {
                                selectedType = pair.first
                                showTypeDropdown = false
                                // Reset type-specific fields
                                title = ""
                                description = ""
                                priceStr = ""
                                category = ""
                                extra1 = ""
                                extra2 = ""
                                extra3 = ""
                                extra4 = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Form Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BrandOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Form Details - ${listingTypes.find { it.first == selectedType }?.second}",
                        fontWeight = FontWeight.Bold,
                        color = BrandSlate,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text(
                            when(selectedType) {
                                "MARKETPLACE" -> "e.g. Selling solid wood dining table"
                                "SERVICE" -> "e.g. Professional plumbing & pipeline care"
                                "EVENT" -> "e.g. Monsoon football tournament 2026"
                                "CARPOOL" -> "e.g. Morning commute to Manyata Park"
                                "PROPERTY" -> "e.g. Premium 2 BHK for rent"
                                "MEAL" -> "e.g. Delicious Homemade Gujarati Thali"
                                else -> "e.g. Found lost watch in lobby"
                            }
                        ) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_title"),
                        shape = RoundedCornerShape(28.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Provide details, dimensions, specs, or guidelines...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_desc"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Tag") },
                        placeholder = { Text(
                            when(selectedType) {
                                "MARKETPLACE" -> "e.g. Furniture, Electronics, Sports"
                                "SERVICE" -> "e.g. Plumbing, Electrical, Cleaning"
                                "EVENT" -> "e.g. Sports, Welfare, Festival"
                                "CARPOOL" -> "e.g. Carpool Offer"
                                "PROPERTY" -> "e.g. Flat Rent, Item Rental"
                                "MEAL" -> "e.g. Pure Veg, Desserts, Bakery"
                                else -> "e.g. General, Alert, Announcement"
                            }
                        ) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_category"),
                        shape = RoundedCornerShape(28.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact Info
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_contact"),
                        shape = RoundedCornerShape(28.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Fields based on Type
                    when (selectedType) {
                        "MARKETPLACE" -> {
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Price (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_marketplace_price"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        "SERVICE" -> {
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Base rate / Visiting fee (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_service_rate"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = extra4,
                                onValueChange = { extra4 = it },
                                label = { Text("Your Experience / Rating (e.g. 4.9)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        "EVENT" -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Event Location / Venue (e.g. Club House)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Date & Time (e.g. Sunday, 10:00 AM)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        "CARPOOL" -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Start Location / Pickup Point") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Destination Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = extra3,
                                    onValueChange = { extra3 = it },
                                    label = { Text("Seats Empty") },
                                    placeholder = { Text("e.g. 3 seats") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = extra4,
                                    onValueChange = { extra4 = it },
                                    label = { Text("Departure Time") },
                                    placeholder = { Text("e.g. 08:30 AM") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.8f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Fuel share cost per head (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        "PROPERTY" -> {
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Specifications (e.g. 2 BHK, Teak Wood)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = extra3,
                                onValueChange = { extra3 = it },
                                label = { Text("Availability Status (e.g. Immediate)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Rent Price (₹ per month or day)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        "MEAL" -> {
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Meal Timing (e.g. Dinner, Lunch)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = extra3,
                                onValueChange = { extra3 = it },
                                label = { Text("Pre-order Notice (e.g. Order 2 hours prior)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Price per portion / plate (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val isSubmitEnabled = title.isNotBlank() && description.isNotBlank() && category.isNotBlank()

                    Button(
                        onClick = {
                            if (isSubmitEnabled) {
                                val doublePrice = priceStr.toDoubleOrNull() ?: 0.0
                                onSubmitListing(
                                    selectedType,
                                    title,
                                    description,
                                    doublePrice,
                                    contact,
                                    category,
                                    extra1,
                                    extra2,
                                    extra3,
                                    extra4
                                )
                            }
                        },
                        enabled = isSubmitEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandEmerald,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_listing_submit_button"),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Post to ${currentUser.society}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
