package com.example.ui.mealhub

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.UserEntity
import com.example.ui.BrandEmerald
import com.example.ui.BrandEmeraldLight
import com.example.ui.BrandGold
import com.example.ui.OperationsUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefOnboardingScreen(
    currentUser: UserEntity,
    operationsState: OperationsUiState,
    onSubmitChef: (
        story: String,
        dishName: String,
        price: Double,
        portions: Int,
        isVeg: Boolean,
        cuisineTags: String,
        photoUrl: String
    ) -> Unit,
    onBackClicked: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var dishName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var portionsText by remember { mutableStateOf("10") }
    var isVeg by remember { mutableStateOf(true) }
    var chefStory by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500") }

    val availableCuisines = listOf(
        "North Indian",
        "South Indian",
        "Gujarati",
        "Continental",
        "Healthy & Salads",
        "Home Thalis",
        "Desserts"
    )
    val selectedCuisines = remember { mutableStateListOf("North Indian", "Home Thalis") }

    LaunchedEffect(operationsState) {
        if (operationsState is OperationsUiState.Success) {
            Toast.makeText(context, "Kitchen Published Successfully!", Toast.LENGTH_SHORT).show()
            onFinish()
        } else if (operationsState is OperationsUiState.Error) {
            Toast.makeText(context, operationsState.message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chef Onboarding",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ConciergeOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("chef_onboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeOnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConciergeBackground)
            )
        },
        bottomBar = {
            Surface(
                color = ConciergeBackground,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            val portions = portionsText.toIntOrNull() ?: 10
                            if (dishName.isBlank()) {
                                Toast.makeText(context, "Please enter your dish name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (price <= 0.0) {
                                Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSubmitChef(
                                chefStory.ifBlank { "Passionate home chef in ${currentUser.society}" },
                                dishName,
                                price,
                                portions,
                                isVeg,
                                selectedCuisines.joinToString(", "),
                                photoUrl
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_chef_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConciergePrimaryContainer,
                            contentColor = Color.White
                        ),
                        enabled = operationsState !is OperationsUiState.Loading
                    ) {
                        if (operationsState is OperationsUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Publish Kitchen & Menu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = ConciergeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Progress & Intro
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "STEP 1 OF 1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergePrimaryContainer,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandEmeraldLight
                        ) {
                            Text(
                                text = "Verified Resident",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add Your Kitchen's Signature Dish",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "Start by offering your first meal special to residents of ${currentUser.society.ifEmpty { "your society" }}.",
                        fontSize = 13.sp,
                        color = ConciergeOutline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Photo Upload Dropzone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        // Toggle sample delicious photo
                        photoUrl = if (photoUrl.contains("photo-1546069901-ba9599a7e63c")) {
                            "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=500"
                        } else {
                            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                        }
                        Toast.makeText(context, "Dish photo updated!", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("photo_dropzone"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Dish Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tap to Change Photo",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = ConciergePrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap to upload mouth-watering dish photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ConciergeOutline
                            )
                        }
                    }
                }
            }

            // Dish Name Input
            OutlinedTextField(
                value = dishName,
                onValueChange = { dishName = it },
                label = { Text("Dish / Thali Name") },
                placeholder = { Text("e.g., Shahi Paneer Thali with Phulkas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dish_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergePrimaryContainer,
                    unfocusedBorderColor = ConciergeOutlineVariant,
                    focusedContainerColor = ConciergeSurfaceContainerLow,
                    unfocusedContainerColor = ConciergeSurfaceContainerLow
                ),
                singleLine = true
            )

            // Price and Portions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price per Meal (₹)") },
                    placeholder = { Text("240") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("price_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ConciergePrimaryContainer,
                        unfocusedBorderColor = ConciergeOutlineVariant,
                        focusedContainerColor = ConciergeSurfaceContainerLow,
                        unfocusedContainerColor = ConciergeSurfaceContainerLow
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = portionsText,
                    onValueChange = { portionsText = it },
                    label = { Text("Portions Limit") },
                    placeholder = { Text("10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("portions_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ConciergePrimaryContainer,
                        unfocusedBorderColor = ConciergeOutlineVariant,
                        focusedContainerColor = ConciergeSurfaceContainerLow,
                        unfocusedContainerColor = ConciergeSurfaceContainerLow
                    ),
                    singleLine = true
                )
            }

            // Veg / Non-Veg Toggle
            Column {
                Text(
                    text = "Dietary Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isVeg = true }
                            .testTag("veg_toggle"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isVeg) ConciergeVegGreenLight else ConciergeSurfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (isVeg) ConciergeVegGreen else ConciergeOutlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ConciergeVegGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pure Vegetarian",
                                fontSize = 13.sp,
                                fontWeight = if (isVeg) FontWeight.Bold else FontWeight.Normal,
                                color = if (isVeg) ConciergeVegGreenMedium else ConciergeOnBackground
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isVeg = false }
                            .testTag("non_veg_toggle"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (!isVeg) ConciergeNonVegRedLight else ConciergeSurfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (!isVeg) ConciergeNonVegRed else ConciergeOutlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ConciergeNonVegRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Non-Vegetarian",
                                fontSize = 13.sp,
                                fontWeight = if (!isVeg) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isVeg) ConciergeNonVegRedDark else ConciergeOnBackground
                            )
                        }
                    }
                }
            }

            // Cuisine Filter Chips
            Column {
                Text(
                    text = "Cuisine Tags",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableCuisines) { tag ->
                        val selected = selectedCuisines.contains(tag)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) selectedCuisines.remove(tag) else selectedCuisines.add(tag)
                            },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ConciergePrimaryContainer,
                                selectedLabelColor = Color.White,
                                containerColor = ConciergeSurfaceContainerLow,
                                labelColor = ConciergeOnBackground
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            // Chef Bio / Story
            OutlinedTextField(
                value = chefStory,
                onValueChange = { chefStory = it },
                label = { Text("Your Chef Story & Kitchen Hygiene Note") },
                placeholder = { Text("Share what makes your home cooking special, e.g., 'Made with cold-pressed oils, zero preservatives, and fresh greens.'") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("chef_story_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergePrimaryContainer,
                    unfocusedBorderColor = ConciergeOutlineVariant,
                    focusedContainerColor = ConciergeSurfaceContainerLow,
                    unfocusedContainerColor = ConciergeSurfaceContainerLow
                ),
                maxLines = 4
            )

            // Pro Tip Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeStatusAmberLight),
                border = BorderStroke(1.dp, ConciergeStatusAmberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ConciergeStatusAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Community Chef Tip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ConciergeStatusAmberDark
                        )
                        Text(
                            text = "Neighbors love knowing your prep schedule. Keep portions capped so you can cook comfortably with pristine hygiene standards.",
                            fontSize = 12.sp,
                            color = ConciergeStatusAmberMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
