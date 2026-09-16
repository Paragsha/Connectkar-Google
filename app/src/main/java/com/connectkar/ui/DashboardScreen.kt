package com.connectkar.ui

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.primaryPhotoUrl
import com.connectkar.ui.theme.*
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale

data class BentoPillar(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bgBrush: Brush,
    val iconColor: Color,
    val textColor: Color,
    val outlineColor: Color = Color.Transparent,
    val ghostIcon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ChefMeal(
    val chefName: String,
    val location: String,
    val dishName: String,
    val price: Int,
    val isVeg: Boolean,
    val contactPhone: String,
    val emoji: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    isRefreshing: Boolean = false,
    mealListings: List<ListingEntity> = emptyList(),
    isExploreMode: Boolean = false,
    exploredSocieties: List<String> = emptyList(),
    onEnterExploreMode: (String) -> Unit = {},
    onExitExploreMode: () -> Unit = {},
    onSocietySelected: (String) -> Unit,
    onModuleClicked: (String) -> Unit,
    onSimulateApprove: () -> Unit,
    onLogout: () -> Unit,
    onRetrySync: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSocietyDropdown by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("home") } // "home", "explore", "society", "profile"
    
    // Dialog / popup states
    var showCreateChoiceDialog by remember { mutableStateOf(false) }
    var showProfileDetailsDialog by remember { mutableStateOf(false) }
    var selectedMealForOrder by remember { mutableStateOf<ChefMeal?>(null) }
    var selectedListingMealForOrder by remember { mutableStateOf<ListingEntity?>(null) }
    var societyToConfirmExplore by remember { mutableStateOf<String?>(null) }
    var isBannerDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSociety, isExploreMode) {
        if (isExploreMode) {
            isBannerDismissed = false
        }
    }

    val societies = listOf("All Societies") + TownshipSocieties

    // High-end design palette (Consolidated to Concierge Design Tokens)
    val brandGreen = ConciergeHomeLiving
    val colorGrayLight = ConciergeSurfaceContainerLow

    // Bento grid pillars
    val pillars = listOf(
        BentoPillar(
            id = "MARKETPLACE",
            title = "Buy & Sell\nItems",
            subtitle = "Buy & Sell Items",
            icon = Icons.Default.Storefront,
            bgBrush = Brush.linearGradient(listOf(ConciergeBrandNavy, Color(0xFF283593))),
            iconColor = Color.White,
            textColor = Color.White,
            ghostIcon = Icons.Default.ShoppingBag
        ),
        BentoPillar(
            id = "PROPERTY",
            title = "Rentals",
            subtitle = "Flats, PGs & Parking",
            icon = Icons.Default.VpnKey,
            bgBrush = Brush.linearGradient(listOf(Color(0xFF1976D2), Color(0xFF0D47A1))),
            iconColor = Color.White,
            textColor = Color.White,
            ghostIcon = Icons.Default.Apartment
        ),
        BentoPillar(
            id = "CARPOOL",
            title = "Carpool",
            subtitle = "Offer · Find a Ride",
            icon = Icons.Default.DirectionsCar,
            bgBrush = Brush.linearGradient(listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB))),
            iconColor = Color.White,
            textColor = Color.White,
            ghostIcon = Icons.Default.AltRoute
        ),
        BentoPillar(
            id = "MEAL",
            title = "Share Daily\nMeals",
            subtitle = "Share Daily Meals",
            icon = Icons.Default.Restaurant,
            bgBrush = Brush.linearGradient(listOf(Color(0xFFD84315), Color(0xFFBF360C))),
            iconColor = Color.White,
            textColor = Color.White,
            ghostIcon = Icons.Default.DinnerDining
        )
    )

    // Horizontal Scroll Meals
    val chefMeals = listOf(
        ChefMeal(
            chefName = "Priya S.",
            location = "Wing A, Flat 304",
            dishName = "Spicy Paneer Salad Bowl",
            price = 249,
            isVeg = true,
            contactPhone = "9876543210",
            emoji = "🥗",
            gradientColors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
        ),
        ChefMeal(
            chefName = "Vikram M.",
            location = "Wing C, Flat 902",
            dishName = "Nawabi Chicken Biryani",
            price = 380,
            isVeg = false,
            contactPhone = "9123456789",
            emoji = "🍲",
            gradientColors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
        )
    )

    // Retrieve user's first name
    val userFirstName = remember(currentUser.fullName) {
        currentUser.fullName.split(" ").firstOrNull() ?: currentUser.fullName
    }

    Scaffold(
        topBar = {
            // High-End sticky Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ConnectKar",
                            fontWeight = FontWeight.Black,
                            color = ConciergeBrandNavy,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        // Vertical divider
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(Color.LightGray)
                                .padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Society Selection Dropdown clickable
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .clickable { showSocietyDropdown = !showSocietyDropdown }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("filter_society_dropdown")
                        ) {
                            Text(
                                text = selectedSociety,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = BrandSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Choose Society",
                                tint = ConciergeBrandNavy,
                                modifier = Modifier.size(18.dp)
                            )

                            DropdownMenu(
                                expanded = showSocietyDropdown,
                                onDismissRequest = { showSocietyDropdown = false }
                            ) {
                                societies.forEach { society ->
                                    DropdownMenuItem(
                                        text = { Text(society, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium) },
                                        onClick = {
                                            if (society == "All Societies" || society == currentUser.society) {
                                                if (isExploreMode) {
                                                    onExitExploreMode()
                                                }
                                                onSocietySelected(society)
                                            } else if (exploredSocieties.contains(society)) {
                                                onEnterExploreMode(society)
                                            } else {
                                                societyToConfirmExplore = society
                                            }
                                            showSocietyDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (society == "All Societies") Icons.Default.Language else Icons.Default.Home,
                                                contentDescription = null,
                                                tint = if (selectedSociety == society) brandGreen else Color.Gray
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Top Bar Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { /* Simulated Notifications */ },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colorGrayLight)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = BrandSlate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { showProfileDetailsDialog = true }
                        ) {
                            AvatarImage(avatarIndex = currentUser.avatarIndex, size = 36)
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Glassmorphic Custom Bottom Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = BorderStroke(0.5.dp, BrandOutline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Home Tab
                    IconButton(
                        onClick = { activeTab = "home" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (activeTab == "home") Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = if (activeTab == "home") ConciergeBrandNavy else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Home",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == "home") ConciergeBrandNavy else Color.Gray
                            )
                        }
                    }

                    // Explore (Feed) Tab
                    IconButton(
                        onClick = { onModuleClicked("EXPLORE") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Explore,
                                contentDescription = "Explore",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Explore",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Create (+) Floating Pillar Button
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isExploreMode) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ConciergeBrandNavy)
                                    .clickable {
                                        if (currentUser.isVerified) {
                                            onModuleClicked("CREATE_HUB")
                                        } else {
                                            Toast.makeText(context, "Resident verification is required to create listings.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("create_pillar_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Posting",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Society (Admin) Tab
                    IconButton(
                        onClick = { onModuleClicked("ADMIN") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Groups,
                                contentDescription = "Society",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Society",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Profile Tab
                    IconButton(
                        onClick = { showProfileDetailsDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Profile",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        containerColor = BrandBackground,
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dashboard_pull_to_refresh")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Sync status banner
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
                }

                // Status review banner for unverified/pending
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    StatusBanner(user = currentUser, onSimulateApprove = onSimulateApprove)
                }

                // 1. HERO SECTION (Editorial Welcome)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                // Verified Resident Chip/Badge
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (currentUser.isVerified) brandGreen else Color(0xFFD48800),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentUser.isVerified) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = "Status Icon",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentUser.isVerified) "VERIFIED RESIDENT" else "PENDING VERIFICATION",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Greeting Title
                Text(
                    text = "Welcome Home,",
                    fontWeight = FontWeight.Normal,
                    color = BrandSlate,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = userFirstName,
                    fontWeight = FontWeight.ExtraBold,
                    color = ConciergeBrandNavy,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Greeting Subtitle
                Text(
                    text = "It's a beautiful morning at The Urban Sanctuary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. CORE PILLARS (Bento-ish Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pillar 1: Marketplace
                    BentoCard(
                        pillar = pillars[0],
                        onClick = { onModuleClicked(pillars[0].id) },
                        modifier = Modifier.weight(1f)
                    )
                    // Pillar 2: Property
                    BentoCard(
                        pillar = pillars[1],
                        onClick = { onModuleClicked(pillars[1].id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pillar 3: Household Rent / Service
                    BentoCard(
                        pillar = pillars[2],
                        onClick = { onModuleClicked(pillars[2].id) },
                        modifier = Modifier.weight(1f)
                    )
                    // Pillar 4: Meal Sharing
                    BentoCard(
                        pillar = pillars[3],
                        onClick = { onModuleClicked(pillars[3].id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Explore Mode Banner (persistent, dismissible)
            if (isExploreMode && !isBannerDismissed) {
                Surface(
                    color = Color(0xFFE8EAF6),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("explore_mode_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Exploring",
                                tint = Color(0xFF1A237E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Exploring $selectedSociety — read only",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A237E)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = onExitExploreMode,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Exit",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF1A237E)
                                )
                            }
                            IconButton(
                                onClick = { isBannerDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. FRESH TODAY SECTION (Horizontal list of local resident chef menu items)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Fresh Today",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandSlate
                        )
                        Text(
                            text = "IN YOUR BUILDING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }
                    TextButton(
                        onClick = { onModuleClicked("MEAL") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "See All",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = ConciergeBrandNavy
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "See All",
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (mealListings.isNotEmpty()) {
                        items(mealListings) { mealListing ->
                            FreshTodayCard(
                                listing = mealListing,
                                isReadOnly = isExploreMode,
                                onOrderClicked = { selectedListingMealForOrder = mealListing }
                            )
                        }
                    } else {
                        items(chefMeals) { meal ->
                            ChefMealItem(
                                meal = meal,
                                isReadOnly = isExploreMode,
                                onOrderClicked = { selectedMealForOrder = meal }
                            )
                        }
                    }
                }
            }

            // 4. TRENDING SECTION (Local spotlight and community alerts)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Trending in $selectedSociety",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandSlate,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Spotlight Deal Card (Bicycle)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModuleClicked("MARKETPLACE") },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, BrandOutline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Image representation
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚲", style = MaterialTheme.typography.displaySmall)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOP DEAL",
                                    color = ConciergeBrandNavy,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "2h ago",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Specialized Allez Road Bike",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = BrandSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Excellent condition, size L.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹45,000",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ConciergeBrandNavy
                                )
                                Text(
                                    text = "View Details",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ConciergeBrandNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Community Warning Alert Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
                    border = BorderStroke(1.dp, Color(0xFFFFCDCD))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert icon",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Gate 2 Maintenance Notice",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gate 2 closed for 2 hours (11:00 AM - 1:00 PM) due to flooring maintenance. Please use Gate 1.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
        }
    }

    // --- Interactive Popups & Dialogs ---

    // 1. Chef Order Dialog
    selectedMealForOrder?.let { meal ->
        AlertDialog(
            onDismissRequest = { selectedMealForOrder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(meal.emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Place Meal Order", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = meal.dishName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ConciergeBrandNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chef: ${meal.chefName} (${meal.location})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Price: ₹${meal.price} / order",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = brandGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connect with the resident chef directly via Phone/WhatsApp to finalize details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = colorGrayLight,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone icon", tint = ConciergeBrandNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = meal.contactPhone,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = ConciergeBrandNavy
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedMealForOrder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ConciergeBrandNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 1b. Real Listing Chef Order Dialog
    selectedListingMealForOrder?.let { listing ->
        val obj = if (listing.detailsJson.isNotEmpty()) {
            com.connectkar.data.local.MoshiHelper.fromJson<com.connectkar.data.local.MealDetailsJson>(listing.detailsJson)
        } else {
            null
        }
        val price = obj?.mealPrice ?: listing.price
        val deliveryInfo = obj?.deliveryInfo ?: listing.extra3
        val isVeg = listing.extra2 == "VEG" || listing.category.contains("Veg", ignoreCase = true)

        AlertDialog(
            onDismissRequest = { selectedListingMealForOrder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isVeg) "🥗" else "🥩", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Place Meal Order", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = listing.title,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ConciergeBrandNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chef: ${listing.authorName} (${listing.authorFlat})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    if (deliveryInfo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delivery Info: $deliveryInfo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Price: ₹${price.toInt()} / order",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = brandGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connect with the resident chef directly via Phone/WhatsApp to finalize details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = colorGrayLight,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone icon", tint = ConciergeBrandNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = listing.contact,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = ConciergeBrandNavy
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedListingMealForOrder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ConciergeBrandNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 2. Profile Details Dialog
    if (showProfileDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarImage(avatarIndex = currentUser.avatarIndex, size = 40)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Resident Profile", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currentUser.fullName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandSlate
                    )
                    Text(
                        text = "Phone: ${currentUser.phoneNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Society: ${currentUser.society}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${currentUser.blockTower}, Flat ${currentUser.flatNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verified Document proof:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "simulated_proof_of_residence.pdf",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = brandGreen
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showProfileDetailsDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDetailsDialog = false }) {
                    Text("Close", color = ConciergeBrandNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 3. Create Listing Choice Dialog
    if (showCreateChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateChoiceDialog = false },
            title = {
                Text("Select Community Pillar", fontWeight = FontWeight.Black, color = ConciergeBrandNavy)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Where would you like to post an update or offer?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    CreateChoiceItem(
                        title = "🏪 Post on Marketplace",
                        description = "Sell furniture, fitness gear, electronics, etc.",
                        onClick = {
                            showCreateChoiceDialog = false
                            onModuleClicked("MARKETPLACE")
                        }
                    )
                    CreateChoiceItem(
                        title = "🏢 List Rental / Property",
                        description = "Share flat listings, parking spots, space rentals.",
                        onClick = {
                            showCreateChoiceDialog = false
                            onModuleClicked("PROPERTY")
                        }
                    )
                    CreateChoiceItem(
                        title = "🍳 Share Daily Meals (Chef Menu)",
                        description = "Offer delicious home-cooked meals to your neighbors.",
                        onClick = {
                            showCreateChoiceDialog = false
                            onModuleClicked("MEAL")
                        }
                    )
                    CreateChoiceItem(
                        title = "🔧 Offer Resident Service",
                        description = "Services like plumbing, electrician, item rentals.",
                        onClick = {
                            showCreateChoiceDialog = false
                            onModuleClicked("SERVICE")
                        }
                    )
                    CreateChoiceItem(
                        title = "🚗 Offer Carpool / Ride",
                        description = "Commute together with neighborhood carpools.",
                        onClick = {
                            showCreateChoiceDialog = false
                            onModuleClicked("CARPOOL")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreateChoiceDialog = false }) {
                    Text("Cancel", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 4. Explore Society Confirmation Dialog
    societyToConfirmExplore?.let { society ->
        AlertDialog(
            onDismissRequest = { societyToConfirmExplore = null },
            title = {
                Text("Explore $society?", fontWeight = FontWeight.Bold, color = ConciergeBrandNavy)
            },
            text = {
                Text(
                    "You will enter read-only explore mode for $society. You can browse listings and community posts without contacting residents or creating posts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = society
                        societyToConfirmExplore = null
                        onEnterExploreMode(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConciergeBrandNavy)
                ) {
                    Text("Explore")
                }
            },
            dismissButton = {
                TextButton(onClick = { societyToConfirmExplore = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun BentoCard(
    pillar: BentoPillar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() }
            .testTag("module_card_${pillar.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, BrandOutline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pillar.bgBrush)
                .padding(16.dp)
        ) {
            // Ghost background icon
            Icon(
                imageVector = pillar.ghostIcon,
                contentDescription = null,
                tint = pillar.iconColor.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 16.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Circle Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = pillar.icon,
                        contentDescription = pillar.title,
                        tint = pillar.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Title & Subtitle
                Column {
                    Text(
                        text = pillar.title,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = pillar.textColor,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pillar.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = pillar.textColor.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChefMealItem(
    meal: ChefMeal,
    onOrderClicked: () -> Unit,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, BrandOutline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual top box instead of a slow network image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.linearGradient(meal.gradientColors))
                    .padding(12.dp)
            ) {
                Text(
                    text = meal.emoji,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Veg/Non-veg Pill Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (meal.isVeg) "🥗 VEG MEAL" else "🥩 NON-VEG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (meal.isVeg) ConciergeVegGreenDark else ConciergeNonVegRed
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Chef profile small row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(meal.chefName.take(1), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${meal.chefName} · ${meal.location}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = meal.dishName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${meal.price}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1A237E)
                    )
                    if (!isReadOnly) {
                        Button(
                            onClick = onOrderClicked,
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Order Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "Read Only",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChoiceItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@Composable
fun FreshTodayCard(
    listing: ListingEntity,
    onOrderClicked: () -> Unit,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val obj = if (listing.detailsJson.isNotEmpty()) {
        com.connectkar.data.local.MoshiHelper.fromJson<com.connectkar.data.local.MealDetailsJson>(listing.detailsJson)
    } else {
        null
    }
    val price = obj?.mealPrice ?: listing.price
    val deliveryInfo = obj?.deliveryInfo ?: listing.extra3
    val isVeg = listing.extra2 == "VEG" || listing.category.contains("Veg", ignoreCase = true)
    val imageUrl = listing.primaryPhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500" }

    Card(
        modifier = modifier
            .width(260.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = listing.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = rememberAsyncImagePainter(
                            model = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500" // default fallback
                        ),
                        placeholder = rememberAsyncImagePainter(
                            model = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🥗",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                // Veg/Non-veg Pill Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isVeg) "🥗 VEG MEAL" else "🥩 NON-VEG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isVeg) ConciergeVegGreenDark else ConciergeNonVegRed
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Chef profile small row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(listing.authorName.take(1), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${listing.authorName} · ${listing.authorFlat}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = ConciergeOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (deliveryInfo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Delivery: $deliveryInfo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${price.toInt()}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = ConciergePrimaryContainer
                    )
                    if (!isReadOnly) {
                        Button(
                            onClick = onOrderClicked,
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = ButtonDefaults.buttonColors(containerColor = ConciergePrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Order Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "Read Only",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

