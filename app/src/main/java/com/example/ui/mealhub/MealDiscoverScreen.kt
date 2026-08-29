package com.example.ui.mealhub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChefProfileEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.UserEntity
import com.example.ui.AvatarImage
import com.example.ui.BrandEmerald
import com.example.ui.BrandEmeraldLight
import com.example.ui.BrandGold
import com.example.ui.BrandGoldLight
import com.example.ui.BrandSlate
import com.example.ui.SyncState
import com.example.ui.SyncStatusBanner
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDiscoverScreen(
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    menuItems: List<MenuItemEntity>,
    chefs: List<ChefProfileEntity>,
    currentChefProfile: ChefProfileEntity?,
    onDishClicked: (MenuItemEntity) -> Unit,
    onChefProfileClicked: (String) -> Unit,
    onChefPortalClicked: () -> Unit,
    onChefOnboardingClicked: () -> Unit,
    onMyMealsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onRetrySync: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf(
        "ALL" to "All Meals",
        "LUNCH" to "Lunch Specials",
        "DINNER" to "Dinner",
        "BREAKFAST" to "Breakfast",
        "HEALTHY" to "Healthy & Bowls"
    )

    val filteredItems = remember(menuItems, selectedCategory) {
        if (selectedCategory == "ALL") {
            menuItems
        } else if (selectedCategory == "HEALTHY") {
            menuItems.filter { it.cuisineTags.contains("Healthy", ignoreCase = true) || it.cuisineTags.contains("Salad", ignoreCase = true) }
        } else {
            menuItems.filter { it.mealType.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val firstName = currentUser.fullName.split(" ").firstOrNull() ?: "Neighbor"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MealHub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ConciergeOnBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFECE5)
                            ) {
                                Text(
                                    text = "Home Cooked",
                                    color = Color(0xFFE65100),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (selectedSociety.isEmpty() || selectedSociety == "All Societies") "All Societies" else selectedSociety,
                            fontSize = 12.sp,
                            color = ConciergeOutline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("meal_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeOnBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onMyMealsClicked,
                        modifier = Modifier.testTag("my_meals_nav_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = BrandEmerald) {
                                    Text("Orders")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "My Meals & Orders",
                                tint = ConciergePrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ConciergeBackground
                )
            )
        },
        containerColor = ConciergeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 0. Sync Status Banner
            item {
                SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
            }

            // 1. Greeting & Hero Spotlight
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Hello, $firstName 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "Fresh home-cooked food prepared right here by your verified neighbors.",
                        fontSize = 14.sp,
                        color = ConciergeOutline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 2. Chef Portal / Become a Chef CTA Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ConciergePrimaryContainer,
                                    Color(0xFF2E3D49)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OutdoorGrill,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentChefProfile != null) "CHEF KITCHEN ACTIVE" else "NEIGHBORHOOD CHEFS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentChefProfile != null) {
                                    "Manage your active menu & incoming resident orders"
                                } else {
                                    "Love cooking? Serve meals to your society & earn"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (currentChefProfile != null) {
                                        onChefPortalClicked()
                                    } else {
                                        onChefOnboardingClicked()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentChefProfile != null) BrandEmeraldLight else Color(0xFFFFD54F),
                                    contentColor = Color(0xFF1E293B)
                                ),
                                modifier = Modifier.testTag(
                                    if (currentChefProfile != null) "chef_portal_button" else "become_chef_button"
                                )
                            ) {
                                Icon(
                                    imageVector = if (currentChefProfile != null) Icons.Default.Dashboard else Icons.Default.AddBusiness,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentChefProfile != null) "Chef Dashboard" else "Become a Chef",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = key },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ConciergePrimaryContainer,
                                selectedLabelColor = Color.White,
                                containerColor = ConciergeSurfaceContainerLow,
                                labelColor = ConciergeOnBackground
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else ConciergeOutlineVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("filter_chip_$key")
                        )
                    }
                }
            }

            // 4. Featured Today's Specials Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Kitchen Specials",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "${filteredItems.size} dishes ready",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }
            }

            // 5. Horizontal Dish Carousel
            item {
                if (filteredItems.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = ConciergeOutline,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No meals listed under this filter yet",
                                fontWeight = FontWeight.Medium,
                                color = ConciergeOutline,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredItems) { dish ->
                            DishCard(
                                dish = dish,
                                onOrder = { onDishClicked(dish) },
                                onChefClick = { onChefProfileClicked(dish.chefUid) }
                            )
                        }
                    }
                }
            }

            // 6. Tiffin Subscription Promo Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🍱", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Daily Tiffin Plans",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF14532D)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF16A34A)
                                ) {
                                    Text(
                                        text = "15% OFF",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Subscribe weekly or monthly with your verified neighbor chef. Fresh, hot, and on-time.",
                                fontSize = 12.sp,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // 7. Community Chefs Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Verified Community Chefs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "Vouched by residents of ${if (selectedSociety.isEmpty() || selectedSociety == "All Societies") "the township" else selectedSociety}",
                        fontSize = 12.sp,
                        color = ConciergeOutline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // 8. Chef Cards Grid / List
            items(chefs) { chef ->
                ChefSummaryCard(
                    chef = chef,
                    onClick = { onChefProfileClicked(chef.uid) }
                )
            }
        }
    }
}

@Composable
fun DishCard(
    dish: MenuItemEntity,
    onOrder: () -> Unit,
    onChefClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column {
            // Dish Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                if (dish.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = dish.photoUrl,
                        contentDescription = dish.dishName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (dish.isVeg) "🥗" else "🍗",
                            fontSize = 48.sp
                        )
                    }
                }

                // Veg / Non-Veg indicator pill
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (dish.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dish.isVeg) "VEG" else "NON-VEG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dish.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }

                // Portions Left Badge
                if (dish.portionsAvailable > 0 && !dish.isSoldOut) {
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "${dish.portionsAvailable} left",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDC2626)
                    ) {
                        Text(
                            text = "SOLD OUT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Dish Content Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = dish.dishName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ConciergeOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Chef & Flat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onChefClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = ConciergeOutline,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${dish.chefName} • ${dish.chefFlat}",
                        fontSize = 11.sp,
                        color = ConciergeOutline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delivery Window
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ConciergePrimaryContainer,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dish.deliveryWindow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ConciergePrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price and Order Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRICE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergeOutline
                        )
                        Text(
                            text = "₹${dish.price.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ConciergeOnBackground
                        )
                    }

                    Button(
                        onClick = onOrder,
                        enabled = !dish.isSoldOut,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConciergePrimaryContainer,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("order_dish_${dish.id}")
                    ) {
                        Text(
                            text = if (dish.isSoldOut) "Sold Out" else "Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChefSummaryCard(
    chef: ChefProfileEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(avatarIndex = 0, size = 52)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (chef.uid == "chef_priya") "Priya Sharma" else if (chef.uid == "chef_vikram") "Vikram Malhotra" else "Chef Neighbor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeOnBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandEmeraldLight
                    ) {
                        Text(
                            text = "★ ${chef.ratingAvg}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergePrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = chef.speciality.ifEmpty { "Home Kitchen Specialist" },
                    fontSize = 12.sp,
                    color = ConciergeOutline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chef.mealsServedCount} meals served • ${chef.regularsCount} regulars",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ConciergePrimaryContainer
                    )
                }
            }

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ConciergePrimaryContainer),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("view_chef_${chef.uid}")
            ) {
                Text(
                    text = "View",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergePrimaryContainer
                )
            }
        }
    }
}
