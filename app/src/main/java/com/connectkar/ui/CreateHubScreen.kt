package com.connectkar.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.theme.*

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

data class CreateSection(
    val title: String,
    val subLabel: String,
    val headerTestTag: String,
    val categories: List<CreateCategory>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHubScreen(
    currentUser: UserEntity,
    viewModel: CreateHubViewModel,
    onBack: () -> Unit,
    onNavigateToCreateFlow: (type: String) -> Unit,
    onBottomNavClick: (String) -> Unit = {},
    activeTab: String = "create",
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

    // Top suggested category (Sell Meal)
    val suggestedCategory = remember {
        CreateCategory(
            id = "MEAL",
            title = "Sell Meal",
            subtitle = "Home cooking",
            icon = Icons.Default.Restaurant,
            iconBgColor = Color(0xFFFEF3C7),
            iconTintColor = Color(0xFFD97706),
            testTag = "category_meal_card"
        )
    }

    // Grouped category sections matching mock design
    val sections = remember {
        listOf(
            CreateSection(
                title = "Buy and sell",
                subLabel = "Marketplace",
                headerTestTag = "section_header_buy_and_sell",
                categories = listOf(
                    CreateCategory(
                        id = "MARKETPLACE",
                        title = "Sell product",
                        subtitle = "Marketplace items",
                        icon = Icons.Default.ShoppingBasket,
                        iconBgColor = ConciergePrimaryContainer,
                        iconTintColor = ConciergeOnPrimary,
                        testTag = "category_marketplace_card"
                    ),
                    CreateCategory(
                        id = "PROPERTY",
                        title = "Rent property",
                        subtitle = "List flat or room",
                        icon = Icons.Default.Apartment,
                        iconBgColor = ConciergeHomeLiving.copy(alpha = 0.15f),
                        iconTintColor = ConciergeHomeLiving,
                        testTag = "category_property_hero_card"
                    ),
                    CreateCategory(
                        id = "HOME_BUSINESS",
                        title = "Home Business",
                        subtitle = "Bakers, tutors & crafts",
                        icon = Icons.Default.Storefront,
                        iconBgColor = ConciergePrimaryContainer.copy(alpha = 0.15f),
                        iconTintColor = ConciergePrimaryContainer,
                        testTag = "category_home_business_card"
                    )
                )
            ),
            CreateSection(
                title = "Community",
                subLabel = "Estate buzz",
                headerTestTag = "section_header_community",
                categories = listOf(
                    CreateCategory(
                        id = "COMMUNITY_POST",
                        title = "Create post",
                        subtitle = "News & updates",
                        icon = Icons.Default.Article,
                        iconBgColor = ConciergeSurfaceContainerHigh,
                        iconTintColor = ConciergePrimary,
                        testTag = "category_community_post_card"
                    ),
                    CreateCategory(
                        id = "EVENT",
                        title = "Post event",
                        subtitle = "Local gatherings",
                        icon = Icons.Default.Event,
                        iconBgColor = ConciergeSecondaryFixed,
                        iconTintColor = ConciergeOnSecondaryFixedVariant,
                        testTag = "category_event_card"
                    )
                )
            ),
            CreateSection(
                title = "Services and sharing",
                subLabel = "Neighbor help",
                headerTestTag = "section_header_services",
                categories = listOf(
                    CreateCategory(
                        id = "SERVICE",
                        title = "Offer service",
                        subtitle = "Professional help",
                        icon = Icons.Default.Handyman,
                        iconBgColor = ConciergeSurfaceContainerHigh,
                        iconTintColor = ConciergePrimary,
                        testTag = "category_service_card"
                    ),
                    CreateCategory(
                        id = "CARPOOL",
                        title = "Offer carpool",
                        subtitle = "Share a ride",
                        icon = Icons.Default.Commute,
                        iconBgColor = ConciergePrimaryFixed,
                        iconTintColor = ConciergeOnPrimaryFixedVariant,
                        testTag = "category_carpool_card"
                    )
                )
            )
        )
    }

    // Flat list of all categories
    val allCategories = remember(sections, suggestedCategory) {
        listOf(suggestedCategory) + sections.flatMap { it.categories }
    }

    // Suggested card visibility based on search query
    val showSuggested = remember(searchQuery, suggestedCategory) {
        if (searchQuery.isBlank()) {
            true
        } else {
            suggestedCategory.title.contains(searchQuery, ignoreCase = true) ||
            suggestedCategory.subtitle.contains(searchQuery, ignoreCase = true) ||
            "Suggested".contains(searchQuery, ignoreCase = true) ||
            "Meal".contains(searchQuery, ignoreCase = true) ||
            "Food".contains(searchQuery, ignoreCase = true) ||
            "Lunch".contains(searchQuery, ignoreCase = true)
        }
    }

    // Filtered sections based on query
    val filteredSections = remember(sections, searchQuery) {
        if (searchQuery.isBlank()) {
            sections
        } else {
            sections.map { section ->
                section.copy(
                    categories = section.categories.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
                    }
                )
            }
        }
    }

    // Resolve recently used list mapped to full category models
    val resolvedRecentlyUsed = remember(recentlyUsedIds, allCategories) {
        recentlyUsedIds.mapNotNull { id ->
            allCategories.find { it.id.equals(id, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
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
                        text = "Create post",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ConciergePrimary,
                        modifier = Modifier.testTag("create_hub_app_title")
                    )

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .testTag("create_hub_draft_label")
                            .clickable { /* Draft actions */ }
                    ) {
                        Text(
                            text = "Drafts",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = ConciergeOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = activeTab,
                onTabSelected = { target ->
                    if (target == "create") {
                        // Already in create hub
                    } else {
                        onBottomNavClick(target)
                    }
                }
            )
        },
        containerColor = ConciergeSurface,
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("create_hub_grid"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Editorial Header
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
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

            // 3. Top Suggested Card (Sell Meal)
            if (showSuggested) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCategory(suggestedCategory.id)
                                onNavigateToCreateFlow(suggestedCategory.id)
                            }
                            .testTag(suggestedCategory.testTag),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = suggestedCategory.icon,
                                    contentDescription = suggestedCategory.title,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier
                                        .padding(bottom = 4.dp)
                                        .testTag("create_hub_suggested_badge")
                                ) {
                                    Text(
                                        text = "Suggested · Lunch time",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = suggestedCategory.title,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ConciergePrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = suggestedCategory.subtitle,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    color = ConciergeOnSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Navigate to Sell Meal",
                                tint = ConciergeOutline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 4. Recently Used Row (Only display if search query is blank & there are items)
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

            // 5. Sections with Header & 2-column grid cards
            if (filteredSections.all { it.categories.isEmpty() } && !showSuggested) {
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
                filteredSections.forEach { section ->
                    if (section.categories.isNotEmpty()) {
                        // Section Header
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = section.title,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ConciergePrimary,
                                    modifier = Modifier.testTag(section.headerTestTag)
                                )
                                Text(
                                    text = section.subLabel,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    color = ConciergeOnSurfaceVariant
                                )
                            }
                        }

                        // Categories in 2-column grid
                        items(section.categories) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(136.dp)
                                    .clickable {
                                        viewModel.selectCategory(category.id)
                                        onNavigateToCreateFlow(category.id)
                                    }
                                    .testTag(category.testTag),
                                shape = RoundedCornerShape(20.dp),
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
                }
            }

            // 6. Verified resident safety banner (slim rounded pill per mock)
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp)
                        .testTag("verified_community_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GppGood,
                            contentDescription = "Verified resident safety",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All listings verified for resident safety",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
