package com.example.ui.mealhub

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.BrandGold
import com.example.ui.BrandGoldLight
import com.example.ui.BrandSlate
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefPublicProfileScreen(
    chefUid: String,
    chefProfile: ChefProfileEntity?,
    menuItems: List<MenuItemEntity>,
    currentUser: UserEntity?,
    onDishClicked: (MenuItemEntity) -> Unit,
    onSubscribeClicked: (planType: String, mealsPerCycle: Int, discount: Int, pricePerMeal: Double) -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("WEEKLY") } // WEEKLY or MONTHLY

    val chefName = if (chefUid == "chef_priya") "Priya Sharma" else if (chefUid == "chef_vikram") "Vikram Malhotra" else "Home Chef"
    val chefFlat = if (chefUid == "chef_priya") "Wing A, Flat 304" else if (chefUid == "chef_vikram") "Wing C, Flat 902" else "Tower B"
    val basePrice = menuItems.firstOrNull()?.price ?: 220.0
    val discountPercent = if (selectedPlan == "WEEKLY") 10 else 18
    val discountedPricePerMeal = (basePrice * (100 - discountPercent) / 100.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chef Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ConciergeOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("chef_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeOnBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Shared kitchen link copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = ConciergePrimaryContainer)
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Chef Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                    border = BorderStroke(1.dp, ConciergeOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarImage(avatarIndex = 0, size = 76)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chefName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ConciergeOnBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ConciergeVegGreenLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = ConciergeVegGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Vouched Chef",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ConciergeVegGreenDark
                                    )
                                }
                            }
                        }
                        Text(
                            text = "$chefFlat • ${chefProfile?.society ?: "Sylvan County"}",
                            fontSize = 13.sp,
                            color = ConciergeOutline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = chefProfile?.speciality ?: "North Indian & Satvik Thalis",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergePrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ConciergeBackground)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "★ ${chefProfile?.ratingAvg ?: 4.9}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ConciergeOnBackground
                                )
                                Text("Rating", fontSize = 11.sp, color = ConciergeOutline)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(ConciergeOutlineVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${chefProfile?.mealsServedCount ?: 140}+",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ConciergeOnBackground
                                )
                                Text("Meals Served", fontSize = 11.sp, color = ConciergeOutline)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(ConciergeOutlineVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${chefProfile?.regularsCount ?: 25}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ConciergeOnBackground
                                )
                                Text("Regulars", fontSize = 11.sp, color = ConciergeOutline)
                            }
                        }
                    }
                }
            }

            // 2. Chef Story Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                    border = BorderStroke(1.dp, ConciergeOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = ConciergePrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "About My Kitchen",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ConciergeOnBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = chefProfile?.chefStory ?: "Cooking has always been a way to bring community together. Every meal is cooked fresh daily in small batches with cold-pressed oils, zero additives, and hygienic practices.",
                            fontSize = 13.sp,
                            color = ConciergeOnBackground,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // 3. Tiffin Subscription Plan Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ConciergeVegGreenContainer),
                    border = BorderStroke(1.dp, ConciergeVegGreenBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🍱", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Subscribe to Daily Tiffin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ConciergeVegGreenDark
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ConciergeVegGreen
                            ) {
                                Text(
                                    text = "Save $discountPercent%",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Plan toggle buttons (Weekly vs Monthly)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPlan = "WEEKLY" },
                                color = if (selectedPlan == "WEEKLY") ConciergeVegGreenLight else Color.White,
                                border = BorderStroke(1.dp, if (selectedPlan == "WEEKLY") ConciergeVegGreen else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Weekly (5 Days)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ConciergeVegGreenDark)
                                    Text("₹${discountedPricePerMeal.toInt()} / meal", fontSize = 11.sp, color = ConciergeVegGreenMedium)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPlan = "MONTHLY" },
                                color = if (selectedPlan == "MONTHLY") ConciergeVegGreenLight else Color.White,
                                border = BorderStroke(1.dp, if (selectedPlan == "MONTHLY") ConciergeVegGreen else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Monthly (20 Days)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ConciergeVegGreenDark)
                                    Text("₹${discountedPricePerMeal.toInt()} / meal", fontSize = 11.sp, color = ConciergeVegGreenMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val days = if (selectedPlan == "WEEKLY") 5 else 20
                                onSubscribeClicked(selectedPlan, days, discountPercent, discountedPricePerMeal)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("subscribe_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ConciergeVegGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Start $selectedPlan Subscription",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 4. Active Dishes Section
            item {
                Text(
                    text = "Dishes from this Kitchen (${menuItems.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnBackground
                )
            }

            items(menuItems) { dish ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(70.dp)
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
                                    Text(if (dish.isVeg) "🥗" else "🍗", fontSize = 28.sp)
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dish.dishName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ConciergeOnBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                text = "$portionsLeft portions left today",
                                fontSize = 11.sp,
                                color = ConciergePrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val portionsLeft = maxOf(0, dish.portionsAvailable - dish.portionsBooked)
                        Button(
                            onClick = { onDishClicked(dish) },
                            enabled = !dish.isSoldOut && portionsLeft > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ConciergePrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("order_dish_${dish.id}")
                        ) {
                            Text(
                                text = if (dish.isSoldOut || portionsLeft <= 0) "Sold Out" else "Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 5. Resident Reviews
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                    border = BorderStroke(1.dp, ConciergeOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Resident Feedback",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ConciergeOnBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"Priya's thalis are the highlight of our workday. Tastes exactly like home food, super hygienic packaging!\"",
                            fontSize = 12.sp,
                            color = ConciergeOutline,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "— Rohit Gupta, Wing B Flat 505",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergePrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
