package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Internal Representation of a Category for picker selection
data class CreateCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHubScreen(
    currentUser: UserEntity,
    viewModel: CreateHubViewModel,
    onBack: () -> Unit,
    onNavigateToCreateFlow: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentlyUsedIds by viewModel.recentlyUsed.collectAsState()

    // Retrieve active society name dynamically (with fallback)
    val societyName = remember(currentUser.society) {
        if (currentUser.society.isBlank() || currentUser.society == "All Societies") {
            "Aqualily"
        } else {
            currentUser.society.replace(" Estate", "").replace(" Apartments", "")
        }
    }

    // Static master list of community categories to match HTML
    val allCategories = remember {
        listOf(
            CreateCategory(
                id = "MARKETPLACE",
                title = "Sell Product",
                subtitle = "Marketplace items",
                icon = Icons.Default.ShoppingBasket,
                iconBgColor = ConciergePrimaryContainer,
                iconTintColor = ConciergeOnPrimary,
                testTag = "category_marketplace_card"
            ),
            CreateCategory(
                id = "FEED",
                title = "Create Post",
                subtitle = "News & Updates",
                icon = Icons.Default.Edit,
                iconBgColor = ConciergeSecondaryContainer,
                iconTintColor = ConciergeOnSecondaryContainer,
                testTag = "category_feed_card"
            ),
            CreateCategory(
                id = "SERVICE",
                title = "Offer Service",
                subtitle = "Professional help",
                icon = Icons.Default.Handyman,
                iconBgColor = ConciergeSurfaceContainerHigh,
                iconTintColor = ConciergePrimary,
                testTag = "category_service_card"
            ),
            CreateCategory(
                id = "MEAL",
                title = "Sell Meal",
                subtitle = "Home cooking",
                icon = Icons.Default.Restaurant,
                iconBgColor = ConciergeTertiaryFixed,
                iconTintColor = ConciergeOnTertiaryFixedVariant,
                testTag = "category_meal_card"
            ),
            CreateCategory(
                id = "EVENT",
                title = "Post Event",
                subtitle = "Local gatherings",
                icon = Icons.Default.Event,
                iconBgColor = ConciergeSecondaryFixed,
                iconTintColor = ConciergeOnSecondaryFixedVariant,
                testTag = "category_event_card"
            ),
            CreateCategory(
                id = "CARPOOL",
                title = "Offer Carpool",
                subtitle = "Share a ride",
                icon = Icons.Default.Commute,
                iconBgColor = ConciergePrimaryFixed,
                iconTintColor = ConciergeOnPrimaryFixedVariant,
                testTag = "category_carpool_card"
            )
        )
    }

    // Filter categories dynamically based on query
    val filteredCategories = remember(allCategories, searchQuery) {
        if (searchQuery.isBlank()) {
            allCategories
        } else {
            allCategories.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Resolve recently used list mapped to full category models
    val resolvedRecentlyUsed = remember(recentlyUsedIds, allCategories) {
        recentlyUsedIds.mapNotNull { id ->
            allCategories.find { it.id == id }
        }
    }

    Scaffold(
        topBar = {
            // Glassmorphic / Transparent Blur Top Bar (Custom surface overlay)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = ConciergeSurface.copy(alpha = 0.85f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("create_hub_close_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Create Hub",
                            tint = ConciergePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "ConnectKar",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ConciergePrimary,
                        modifier = Modifier.testTag("create_hub_app_title")
                    )

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color.Transparent,
                        modifier = Modifier.clickable { /* Draft actions */ }
                    ) {
                        Text(
                            text = "Draft",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = ConciergeOnSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("create_hub_draft_label")
                        )
                    }
                }
            }
        },
        bottomBar = {
            // High-End Custom Bottom Navigation Bar matching Dashboard but showing active Create tab
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Home Tab (Navigates Back)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_nav_home")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Home",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Explore Tab (Goes Back & Opens Feed)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_nav_explore")
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Highlighted Create Tab in active state
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(ConciergePrimaryContainer)
                                .testTag("bottom_nav_create_active"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Active",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Society Tab (Goes Back)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_nav_society")
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Profile Tab (Goes Back)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_nav_profile")
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        containerColor = ConciergeSurface,
        modifier = modifier
    ) { innerPadding ->
        // Use a vertical grid to represent both grid cards and full width items
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Editorial Header
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "What are you bringing to $societyName?",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = ConciergePrimary,
                        lineHeight = 30.sp,
                        modifier = Modifier.testTag("create_hub_header_title")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose a category to start sharing with your community.",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = ConciergeOnSurfaceVariant,
                        modifier = Modifier.testTag("create_hub_header_subtitle")
                    )
                }
            }

            // 2. Search Field
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search categories...",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = ConciergeOutline
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ConciergeOutline,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ConciergeSurfaceContainerHigh,
                        unfocusedContainerColor = ConciergeSurfaceContainerLowest,
                        focusedBorderColor = ConciergeOutlineVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = ConciergeOutlineVariant.copy(alpha = 0.4f),
                        focusedTextColor = ConciergeOnSurface,
                        unfocusedTextColor = ConciergeOnSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("create_hub_search_input")
                )
            }

            // 3. Recently Used Row (Only display if search query is blank & there are items)
            if (searchQuery.isBlank() && resolvedRecentlyUsed.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "RECENTLY USED",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = ConciergePrimary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .testTag("recently_used_header")
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recently_used_row")
                        ) {
                            items(resolvedRecentlyUsed) { category ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(72.dp)
                                        .clickable {
                                            viewModel.selectCategory(category.id)
                                            onNavigateToCreateFlow(category.id)
                                        }
                                        .testTag("recently_used_chip_${category.id.lowercase()}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(ConciergeSurfaceContainerLow)
                                            .border(
                                                BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.4f)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = category.title,
                                            tint = ConciergePrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = category.title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = ConciergeOnSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Grid of Categories
            if (filteredCategories.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No results",
                            tint = ConciergeOutline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No categories match your search.",
                            fontSize = 14.sp,
                            color = ConciergeOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredCategories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp)
                            .clickable {
                                viewModel.selectCategory(category.id)
                                onNavigateToCreateFlow(category.id)
                            }
                            .testTag(category.testTag),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLowest),
                        border = BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(category.iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    tint = category.iconTintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = category.title,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ConciergePrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = category.subtitle,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    color = ConciergeOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 5. Rent Property (Full Width Hero card, breaking the grid)
            // Show only if search is empty or query matches "Property", "Rent", "Real Estate"
            val showPropertyHero = searchQuery.isBlank() ||
                    "Rent Property".contains(searchQuery, ignoreCase = true) ||
                    "Real Estate".contains(searchQuery, ignoreCase = true) ||
                    "apartment".contains(searchQuery, ignoreCase = true)

            if (showPropertyHero) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                viewModel.selectCategory("PROPERTY")
                                onNavigateToCreateFlow("PROPERTY")
                            }
                            .testTag("category_property_hero_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLowest),
                        border = BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left 60%: details & branding
                            Column(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight()
                                    .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = ConciergeHomeLiving.copy(alpha = 0.1f),
                                ) {
                                    Text(
                                        text = "Real Estate",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = ConciergeHomeLiving,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Rent Property",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = ConciergePrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "List your apartment or room",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = ConciergeOnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Right 40%: Premium editorial visual representation of modern property
                            Box(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                ConciergePrimaryContainer,
                                                ConciergePrimary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic premium interior image representation / abstract representation
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDf_HWFB0nQMMrY9s1s9ZyyeVimUJE-VrftD5MAiv-gi3lmtTBu1XvaBPXULHV_ps76uakOwQnPxkApE-1aN4Yri9mudUbz8T-hxNCjl5e4Tsm35RxQ1yYhZPy4EGZ_EqvFMTgWYTgmBk35vxN3z8tGSIGcg3jbrsLLaOEgZwmz0AKApoO1EWhsT7SqP4a86qrpCPVGiPrGcRZyxNDQsiTmSnDtpiApG9ZXrBNAfp4Zoa2KFTS04wgA",
                                        fallback = rememberAsyncImagePainter(model = Icons.Default.Apartment)
                                    ),
                                    contentDescription = "Property Visual",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Soft light overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color.White.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // 6. Verified Community info banner
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ConciergeSurfaceContainerLow,
                    border = BorderStroke(0.5.dp, ConciergeOutlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("verified_community_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = "Verified community",
                            tint = ConciergeSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verified Community",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ConciergePrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All listings in $societyName are verified for resident safety and trust.",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = ConciergeOnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
