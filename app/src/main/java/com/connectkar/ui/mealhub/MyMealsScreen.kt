package com.connectkar.ui.mealhub

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.local.MealOrderEntity
import com.connectkar.data.local.MealSubscriptionEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.BrandGold
import com.connectkar.ui.BrandGoldLight
import com.connectkar.ui.BrandSlate
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMealsScreen(
    currentUser: UserEntity,
    orders: List<MealOrderEntity>,
    subscriptions: List<MealSubscriptionEntity>,
    onToggleSubscription: (subId: Int, newStatus: String) -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Orders", "History", "My Tiffins")

    val activeOrders = remember(orders) {
        orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    }
    val pastOrders = remember(orders) {
        orders.filter { it.status == "DELIVERED" || it.status == "CANCELLED" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Meals & Tiffins",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ConciergeOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("my_meals_back_button")
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
        containerColor = ConciergeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tab Row
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
                            modifier = Modifier.testTag("meals_tab_$index")
                        )
                    }
                }
            }

            // Tab 0: Active Orders
            if (selectedTab == 0) {
                if (activeOrders.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.DeliveryDining,
                            title = "No Active Meal Orders",
                            subtitle = "Dishes you order from neighborhood chefs will be tracked here in real-time."
                        )
                    }
                } else {
                    items(activeOrders) { order ->
                        BuyerActiveOrderCard(order = order)
                    }
                }
            }

            // Tab 1: Past Orders
            if (selectedTab == 1) {
                if (pastOrders.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.History,
                            title = "No Past Orders",
                            subtitle = "Completed meal orders will appear here for easy reordering."
                        )
                    }
                } else {
                    items(pastOrders) { order ->
                        BuyerPastOrderCard(order = order)
                    }
                }
            }

            // Tab 2: My Tiffins
            if (selectedTab == 2) {
                if (subscriptions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.CalendarMonth,
                            title = "No Active Subscriptions",
                            subtitle = "Subscribe to daily home tiffins with neighborhood chefs to save up to 18%."
                        )
                    }
                } else {
                    items(subscriptions) { sub ->
                        BuyerSubscriptionCard(
                            sub = sub,
                            onToggle = {
                                val nextStatus = if (sub.status == "ACTIVE") "PAUSED" else "ACTIVE"
                                onToggleSubscription(sub.id, nextStatus)
                                Toast.makeText(context, "Subscription $nextStatus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerActiveOrderCard(order: MealOrderEntity) {
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
                        text = order.dishName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "Chef ${order.chefName} • ${order.deliveryMethod}",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        "PENDING" -> BrandGoldLight
                        "PREPARING" -> Color(0xFFE0F2FE)
                        "READY" -> ConciergeVegGreenLight
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (order.status) {
                            "PENDING" -> BrandGold
                            "PREPARING" -> Color(0xFF0284C7)
                            "READY" -> ConciergeVegGreenDark
                            else -> ConciergeOutline
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Tracker
            OrderProgressTracker(status = order.status)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = ConciergeOutlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.servingSize} portion(s) • Window: ${order.deliveryWindow}",
                        fontSize = 12.sp,
                        color = ConciergeOnBackground
                    )
                    if (order.addOns.isNotBlank()) {
                        Text(
                            text = "+ ${order.addOns}",
                            fontSize = 11.sp,
                            color = ConciergeOutline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Text(
                    text = "₹${order.grandTotal.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ConciergePrimaryContainer
                )
            }
        }
    }
}

@Composable
fun OrderProgressTracker(status: String) {
    val steps = listOf("Received", "Preparing", "Ready / Out", "Delivered")
    val currentStep = when (status) {
        "PENDING" -> 0
        "PREPARING" -> 1
        "READY" -> 2
        "DELIVERED" -> 3
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index <= currentStep
            val isCurrent = index == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) ConciergePrimaryContainer else Color(0xFFCBD5E1)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepName,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) ConciergeOnBackground else ConciergeOutline
                )
            }
        }
    }
}

@Composable
fun BuyerPastOrderCard(order: MealOrderEntity) {
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
                        text = order.dishName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "from Chef ${order.chefName}",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }

                Text(
                    text = "₹${order.grandTotal.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ConciergeOnBackground
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${order.servingSize} portion(s) • Delivered ✓",
                fontSize = 12.sp,
                color = ConciergeVegGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BuyerSubscriptionCard(
    sub: MealSubscriptionEntity,
    onToggle: () -> Unit
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
                        text = "Chef ${sub.chefName} Tiffin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeOnBackground
                    )
                    Text(
                        text = "${sub.planType} Plan • ₹${sub.pricePerMeal.toInt()}/meal",
                        fontSize = 12.sp,
                        color = ConciergeOutline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sub.status == "ACTIVE") ConciergeVegGreenLight else ConciergeStatusAmberBadgeBg
                ) {
                    Text(
                        text = sub.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (sub.status == "ACTIVE") ConciergeVegGreenDark else ConciergeStatusAmberDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${sub.daysRemaining} meals remaining • Renews on ${sub.renewalDate}",
                fontSize = 12.sp,
                color = ConciergeOnBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onToggle,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("pause_sub_${sub.id}")
            ) {
                Text(
                    text = if (sub.status == "ACTIVE") "Pause Subscription" else "Resume Subscription",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
