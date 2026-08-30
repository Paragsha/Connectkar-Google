package com.example.ui.mealhub

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.ChefProfileEntity
import com.example.data.local.MealOrderEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.UserEntity
import com.example.ui.BrandEmerald
import com.example.ui.BrandEmeraldLight
import com.example.ui.BrandGold
import com.example.ui.BrandGoldLight
import com.example.ui.BrandSlate
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefPortalDashboardScreen(
    currentUser: UserEntity,
    chefProfile: ChefProfileEntity?,
    menuItems: List<MenuItemEntity>,
    incomingOrders: List<MealOrderEntity>,
    subscriptions: List<MealSubscriptionEntity>,
    onToggleSoldOut: (itemId: Int, isSoldOut: Boolean) -> Unit,
    onUpdateOrderStatus: (orderId: Int, newStatus: String) -> Unit,
    onAddNewDish: (
        dishName: String,
        description: String,
        price: Double,
        portions: Int,
        isVeg: Boolean,
        cuisineTags: String,
        mealType: String,
        deliveryWindow: String,
        photoUrl: String
    ) -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Orders (${incomingOrders.size})", "My Menu (${menuItems.size})", "Subscribers (${subscriptions.size})")

    var showAddDishSheet by remember { mutableStateOf(false) }

    val totalRevenue = incomingOrders.filter { it.status == "DELIVERED" || it.status == "READY" || it.status == "PREPARING" }.sumOf { it.grandTotal }
    val totalPortionsServed = incomingOrders.filter { it.status == "DELIVERED" }.sumOf { it.servingSize }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chef Portal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ConciergeOnBackground
                        )
                        Text(
                            text = "${currentUser.fullName} • ${currentUser.society}",
                            fontSize = 12.sp,
                            color = ConciergeOutline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("chef_portal_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeOnBackground
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandEmeraldLight,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BrandEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KITCHEN OPEN",
                                color = BrandEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConciergeBackground)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDishSheet = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Dish", fontWeight = FontWeight.Bold) },
                    containerColor = ConciergePrimaryContainer,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_new_dish_fab")
                )
            }
        },
        containerColor = ConciergeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Stats Bento Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Revenue Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ConciergePrimaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "EST. REVENUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${totalRevenue.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${incomingOrders.size} total orders",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Rating & Meals Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                        border = BorderStroke(1.dp, ConciergeOutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "KITCHEN STATS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConciergeOutline,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "★ ${chefProfile?.ratingAvg ?: 5.0}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ConciergeOnBackground
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalPortionsServed meals completed",
                                fontSize = 11.sp,
                                color = ConciergePrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. Tab Navigation
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ConciergeSurfaceContainerLow,
                    contentColor = ConciergePrimaryContainer,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("chef_tab_$index")
                        )
                    }
                }
            }

            // Tab 0: Incoming Orders
            if (selectedTab == 0) {
                if (incomingOrders.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.Receipt,
                            title = "No Incoming Orders Yet",
                            subtitle = "When neighbors order dishes from your menu, they will appear here with prep notes."
                        )
                    }
                } else {
                    items(incomingOrders) { order ->
                        ChefOrderCard(
                            order = order,
                            onStatusUpdate = { newStatus ->
                                onUpdateOrderStatus(order.id, newStatus)
                                Toast.makeText(context, "Order marked as $newStatus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Tab 1: Menu Items
            if (selectedTab == 1) {
                if (menuItems.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.RestaurantMenu,
                            title = "No Menu Items Added",
                            subtitle = "Tap '+ Add Dish' below to publish your first signature meal."
                        )
                    }
                } else {
                    items(menuItems) { dish ->
                        ChefMenuItemCard(
                            dish = dish,
                            onToggleSoldOut = { isSoldOut ->
                                onToggleSoldOut(dish.id, isSoldOut)
                                Toast.makeText(
                                    context,
                                    if (isSoldOut) "Marked as Sold Out" else "Dish is now Available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            // Tab 2: Subscribers
            if (selectedTab == 2) {
                if (subscriptions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.People,
                            title = "No Tiffin Subscribers Yet",
                            subtitle = "Residents can subscribe weekly or monthly to your daily kitchen menu."
                        )
                    }
                } else {
                    items(subscriptions) { sub ->
                        ChefSubscriptionCard(sub = sub)
                    }
                }
            }
        }
    }

    // Add Dish Bottom Sheet
    if (showAddDishSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddDishSheet = false },
            containerColor = ConciergeBackground
        ) {
            AddDishSheetContent(
                onDismiss = { showAddDishSheet = false },
                onAddDish = { name, desc, price, port, isVeg, tag, mealType, time, photo ->
                    onAddNewDish(name, desc, price, port, isVeg, tag, mealType, time, photo)
                    showAddDishSheet = false
                    Toast.makeText(context, "New dish added to kitchen!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ChefOrderCard(
    order: MealOrderEntity,
    onStatusUpdate: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.buyerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "Flat: ${order.buyerFlat} • ${order.deliveryMethod}",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        "PENDING" -> BrandGoldLight
                        "PREPARING" -> Color(0xFFE0F2FE)
                        "READY" -> BrandEmeraldLight
                        "DELIVERED" -> Color(0xFFF1F5F9)
                        else -> ConciergeSurfaceContainerLow
                    }
                ) {
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (order.status) {
                            "PENDING" -> BrandGold
                            "PREPARING" -> Color(0xFF0284C7)
                            "READY" -> BrandEmerald
                            "DELIVERED" -> Color(0xFF64748B)
                            else -> ConciergeOutline
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = ConciergeOutlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${order.servingSize}x ${order.dishName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    if (order.dietaryNotes.isNotBlank()) {
                        Text(
                            text = "Notes: ${order.dietaryNotes}",
                            fontSize = 12.sp,
                            color = ConciergeStatusAmber,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = "Window: ${order.deliveryWindow}",
                        fontSize = 11.sp,
                        color = ConciergeOutline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "₹${order.grandTotal.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ConciergeOnBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (order.status) {
                    "PENDING" -> {
                        Button(
                            onClick = { onStatusUpdate("PREPARING") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ConciergePrimaryContainer),
                            modifier = Modifier.testTag("update_order_${order.id}")
                        ) {
                            Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Accept & Start Prep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "PREPARING" -> {
                        Button(
                            onClick = { onStatusUpdate("READY") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ConciergeVegGreen),
                            modifier = Modifier.testTag("update_order_${order.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Ready / Dispatched", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "READY" -> {
                        OutlinedButton(
                            onClick = { onStatusUpdate("DELIVERED") },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandEmerald),
                            modifier = Modifier.testTag("update_order_${order.id}")
                        ) {
                            Text("Confirm Delivered", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                        }
                    }
                    else -> {
                        Text(
                            text = "Order Complete ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergeVegGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChefMenuItemCard(
    dish: MenuItemEntity,
    onToggleSoldOut: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                if (dish.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = dish.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (dish.isVeg) "🥗" else "🍗", fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (dish.isVeg) ConciergeVegGreen else ConciergeNonVegRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dish.dishName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                }
                Text(
                    text = "₹${dish.price.toInt()} • ${dish.deliveryWindow}",
                    fontSize = 12.sp,
                    color = ConciergeOutline,
                    modifier = Modifier.padding(top = 2.dp)
                )
                val portionsLeft = maxOf(0, dish.portionsAvailable - dish.portionsBooked)
                Text(
                    text = "$portionsLeft of ${dish.portionsAvailable} left (${dish.portionsBooked} booked)",
                    fontSize = 11.sp,
                    color = ConciergePrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = !dish.isSoldOut,
                    onCheckedChange = { isAvailable -> onToggleSoldOut(!isAvailable) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ConciergePrimaryContainer
                    ),
                    modifier = Modifier.testTag("toggle_soldout_${dish.id}")
                )
                Text(
                    text = if (dish.isSoldOut) "Sold Out" else "Available",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dish.isSoldOut) ConciergeNonVegRed else ConciergeVegGreen
                )
            }
        }
    }
}

@Composable
fun ChefSubscriptionCard(sub: MealSubscriptionEntity) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subscriber Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "${sub.planType} Plan • ${sub.daysRemaining} days remaining",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandEmeraldLight
                ) {
                    Text(
                        text = sub.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = BrandEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ConciergeOutline,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ConciergeOutline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AddDishSheetContent(
    onDismiss: () -> Unit,
    onAddDish: (
        dishName: String,
        description: String,
        price: Double,
        portions: Int,
        isVeg: Boolean,
        cuisineTags: String,
        mealType: String,
        deliveryWindow: String,
        photoUrl: String
    ) -> Unit
) {
    var dishName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var portionsText by remember { mutableStateOf("10") }
    var isVeg by remember { mutableStateOf(true) }
    var mealType by remember { mutableStateOf("LUNCH") }
    var deliveryWindow by remember { mutableStateOf("12:30 PM - 1:30 PM") }
    var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Add New Dish to Kitchen",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ConciergeOnBackground
        )

        OutlinedTextField(
            value = dishName,
            onValueChange = { dishName = it },
            label = { Text("Dish Name") },
            modifier = Modifier.fillMaxWidth().testTag("sheet_dish_name_input"),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Ingredients") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = portionsText,
                onValueChange = { portionsText = it },
                label = { Text("Portions") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = isVeg,
                onClick = { isVeg = true },
                label = { Text("Pure Veg") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isVeg,
                onClick = { isVeg = false },
                label = { Text("Non-Veg") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val price = priceText.toDoubleOrNull() ?: 150.0
                val port = portionsText.toIntOrNull() ?: 10
                if (dishName.isNotBlank()) {
                    onAddDish(dishName, description, price, port, isVeg, "Home Cooked", mealType, deliveryWindow, photoUrl)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sheet_submit_dish_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ConciergePrimaryContainer)
        ) {
            Text("Add to Active Menu", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
