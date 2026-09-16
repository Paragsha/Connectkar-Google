package com.connectkar.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.primaryPhotoUrl
import com.connectkar.data.local.propertyDetails
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.components.PropertyListingSkeletonCard
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyRentalsScreen(
    currentUser: UserEntity,
    listings: List<ListingEntity>,
    selectedSociety: String,
    syncState: SyncState,
    isRefreshing: Boolean = false,
    onBack: () -> Unit,
    onLikeListing: (Int) -> Unit,
    onBookmarkListing: (Int) -> Unit,
    onCreateListingClicked: () -> Unit,
    onRetrySync: () -> Unit,
    onRefresh: () -> Unit = {},
    onNavigateToSaved: () -> Unit = {},
    onNavigateToMyListings: () -> Unit = {},
    activeTab: String = "home",
    onBottomNavClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedListingForDetails by remember { mutableStateOf<ListingEntity?>(null) }
    var showFiltersSheet by remember { mutableStateOf(false) }

    // Advanced bottom sheet filters
    var bhkFilter by remember { mutableStateOf<String?>(null) }
    var furnishedFilter by remember { mutableStateOf<String?>(null) }
    var priceRange by remember { mutableStateOf(5000f..100000f) }
    var propertyTypeFilter by remember { mutableStateOf<String?>(null) }

    val hasActiveFilters = bhkFilter != null || furnishedFilter != null ||
            priceRange.start > 5000f || priceRange.endInclusive < 100000f ||
            propertyTypeFilter != null

    val filterChips = listOf("All", "2 BHK", "1 BHK", "Furnished", "Under ₹25k", "Pet Friendly")

    // Brand Palette remapped to matching Digital Concierge design tokens
    val brandCobalt = ConciergeSecondary
    val surfaceContainerLow = ConciergeSurfaceContainerLow
    val surfaceContainerLowest = ConciergeSurface
    val ghostBorderColor = ConciergeOutlineVariant.copy(alpha = 0.2f)
    val brandTeal = ConciergeHomeLiving

    // Filtering logic combining search query, horizontal chips, and bottom sheet filters
    val filteredProperties = listings.filter { listing ->
        val details = listing.propertyDetails()

        val matchesSearch = searchQuery.isBlank() ||
                listing.title.contains(searchQuery, ignoreCase = true) ||
                listing.description.contains(searchQuery, ignoreCase = true) ||
                listing.authorFlat.contains(searchQuery, ignoreCase = true) ||
                details.wingFlatNumber.contains(searchQuery, ignoreCase = true)

        val matchesChipFilter = when (selectedFilter) {
            "All" -> true
            "2 BHK" -> listing.title.contains("2 BHK", ignoreCase = true) ||
                    listing.category.contains("2 BHK", ignoreCase = true) ||
                    listing.extra2.contains("2 BHK", ignoreCase = true) ||
                    details.bhkType.contains("2 BHK", ignoreCase = true) ||
                    details.beds == 2
            "1 BHK" -> listing.title.contains("1 BHK", ignoreCase = true) ||
                    listing.category.contains("1 BHK", ignoreCase = true) ||
                    listing.extra2.contains("1 BHK", ignoreCase = true) ||
                    details.bhkType.contains("1 BHK", ignoreCase = true) ||
                    details.beds == 1
            "Furnished" -> listing.title.contains("furnished", ignoreCase = true) ||
                    listing.description.contains("furnished", ignoreCase = true) ||
                    details.furnishedStatus.contains("Furnished", ignoreCase = true)
            "Under ₹25k" -> listing.price < 25000.0
            "Pet Friendly" -> listing.description.contains("pet", ignoreCase = true) ||
                    listing.title.contains("pet", ignoreCase = true) ||
                    details.amenities.any { it.contains("pet", ignoreCase = true) }
            else -> true
        }

        val matchesBhk = if (bhkFilter == null) {
            true
        } else if (bhkFilter == "4+ BHK") {
            listing.title.contains("4 BHK", ignoreCase = true) ||
                    listing.title.contains("5 BHK", ignoreCase = true) ||
                    listing.category.contains("4 BHK", ignoreCase = true) ||
                    details.beds >= 4
        } else {
            val filter = bhkFilter!!
            listing.title.contains(filter, ignoreCase = true) ||
                    listing.category.contains(filter, ignoreCase = true) ||
                    listing.extra2.contains(filter, ignoreCase = true) ||
                    details.bhkType.contains(filter, ignoreCase = true) ||
                    (filter.startsWith("1") && details.beds == 1) ||
                    (filter.startsWith("2") && details.beds == 2) ||
                    (filter.startsWith("3") && details.beds == 3)
        }

        val matchesFurnished = if (furnishedFilter == null) {
            true
        } else {
            details.furnishedStatus.equals(furnishedFilter, ignoreCase = true) ||
                    listing.title.contains(furnishedFilter!!, ignoreCase = true) ||
                    listing.description.contains(furnishedFilter!!, ignoreCase = true)
        }

        val matchesPrice = if (listing.price > 0.0) {
            listing.price >= priceRange.start && listing.price <= priceRange.endInclusive
        } else {
            priceRange.start <= 5000f
        }

        val matchesPropertyType = if (propertyTypeFilter == null) {
            true
        } else {
            details.propertyType.equals(propertyTypeFilter, ignoreCase = true) ||
                    listing.title.contains(propertyTypeFilter!!, ignoreCase = true) ||
                    listing.description.contains(propertyTypeFilter!!, ignoreCase = true)
        }

        matchesSearch && matchesChipFilter && matchesBhk && matchesFurnished && matchesPrice && matchesPropertyType
    }

    // Full Screen Details View
    if (selectedListingForDetails != null) {
        PropertyDetailsScreen(
            property = selectedListingForDetails!!,
            onBack = { selectedListingForDetails = null }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.isVerified) {
                FloatingActionButton(
                    onClick = onCreateListingClicked,
                    containerColor = ConciergeBrandNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 80.dp)
                        .testTag("create_property_fab"),
                    shape = RoundedCornerShape(9999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Property Listing",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = activeTab,
                onTabSelected = { target ->
                    if (target == "create") {
                        if (currentUser.isVerified) {
                            onCreateListingClicked()
                        } else {
                            Toast.makeText(context, "Resident verification is required to create listings.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onBottomNavClick(target)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FF),
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("property_rentals_pull_to_refresh")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // --- HEADER ROW ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ConnectKar",
                        fontWeight = FontWeight.Black,
                        color = ConciergeBrandNavy,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Location Chip (Society Selection)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(surfaceContainerLow)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedSociety,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 100.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bookmark / Saved Properties Shortcut
                    IconButton(
                        onClick = onNavigateToSaved,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF4FF))
                            .testTag("saved_properties_header_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Saved Properties",
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToMyListings() }
                    ) {
                        AvatarImage(avatarIndex = currentUser.avatarIndex, size = 36)
                    }
                }
            }

            // --- PAGE TITLE ---
            item {
                Text(
                    text = "Rent flats & properties",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = ConciergeBrandNavy,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .testTag("screen_title_rentals")
                )
            }

            // --- SEARCH BAR & FILTER BUTTON ROW ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search property, rentals, or residents...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* Collapse keyboard */ }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("property_search_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC6C5D4),
                            unfocusedBorderColor = Color(0xFFC6C5D4).copy(alpha = 0.5f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Filters bottom sheet trigger button
                    IconButton(
                        onClick = { showFiltersSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (hasActiveFilters) Color(0xFF003FB1) else Color(0xFFEFF4FF))
                            .testTag("property_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (hasActiveFilters) Color.White else Color(0xFF003FB1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // --- FILTER CHIPS ROW ---
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterChips) { filterName ->
                        val isSelected = filterName == selectedFilter
                        val bg = if (isSelected) Color(0xFF003FB1) else Color(0xFFEFF4FF)
                        val textColor = if (isSelected) Color.White else Color(0xFF454652)

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = bg,
                            modifier = Modifier
                                .clickable { selectedFilter = filterName }
                                .testTag("filter_chip_$filterName")
                        ) {
                            Text(
                                text = filterName,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // --- TRUST BANNER ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified safety",
                        tint = ConciergeBrandNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "All listings verified for resident safety",
                        color = Color(0xFF454652),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- SHIMMER SKELETON / EMPTY STATE / PROPERTY CARDS ---
            if (syncState is SyncState.Syncing && listings.isEmpty()) {
                items(3) {
                    PropertyListingSkeletonCard(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else if (filteredProperties.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No rentals found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConciergeBrandNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your filters or search terms.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                // --- PROPERTY CARDS LIST ---
                items(filteredProperties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onViewDetails = { selectedListingForDetails = property },
                        brandTeal = brandTeal,
                        ghostBorderColor = ghostBorderColor,
                        surfaceContainerLowest = surfaceContainerLowest
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
        }
    }

    // --- FILTERS MODAL BOTTOM SHEET ---
    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ConciergeBrandNavy
                    )
                    IconButton(
                        onClick = { showFiltersSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Filters",
                            tint = Color.Gray
                        )
                    }
                }

                // 1. BHK TYPE
                Text(
                    text = "BHK TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                val bhkOptions = listOf("1 BHK", "2 BHK", "3 BHK", "4+ BHK")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bhkOptions.forEach { opt ->
                        val isSelected = bhkFilter == opt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF003FB1) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF003FB1) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    bhkFilter = if (isSelected) null else opt
                                }
                                .testTag("filter_bhk_$opt")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 2. FURNISHED STATUS
                Text(
                    text = "FURNISHED STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                val furnishedOptions = listOf("Fully Furnished", "Semi-Furnished", "Unfurnished")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    furnishedOptions.forEach { opt ->
                        val isSelected = furnishedFilter == opt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF003FB1) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF003FB1) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    furnishedFilter = if (isSelected) null else opt
                                }
                                .testTag("filter_furnished_$opt")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 3. PRICE RANGE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRICE RANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "₹${String.format("%,d", priceRange.start.toInt())} - ₹${String.format("%,d", priceRange.endInclusive.toInt())}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003FB1)
                    )
                }

                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 5000f..100000f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF003FB1),
                        activeTrackColor = Color(0xFF003FB1),
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                        .testTag("price_range_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: ₹5,000",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Max: ₹1,00,000+",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // 4. PROPERTY TYPE
                Text(
                    text = "PROPERTY TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                val typeOptions = listOf("Apartment", "Studio", "Penthouse", "Shared")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    typeOptions.forEach { opt ->
                        val isSelected = propertyTypeFilter == opt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF003FB1) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF003FB1) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    propertyTypeFilter = if (isSelected) null else opt
                                }
                                .testTag("filter_type_$opt")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Footer Buttons (Reset All & Apply)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            bhkFilter = null
                            furnishedFilter = null
                            priceRange = 5000f..100000f
                            propertyTypeFilter = null
                            selectedFilter = "All"
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("reset_filters_button")
                    ) {
                        Text("Reset All", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showFiltersSheet = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003FB1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("apply_filters_button")
                    ) {
                        Text("Apply Filters", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyCard(
    property: ListingEntity,
    onViewDetails: () -> Unit,
    brandTeal: Color,
    ghostBorderColor: Color,
    surfaceContainerLowest: Color
) {
    val specs = getPropertySpecs(property)
    val details = property.propertyDetails()

    // Fallback image in case the database entry is missing one
    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx"
    val imageUrl = property.primaryPhotoUrl(fallbackImage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceContainerLowest),
        border = BorderStroke(0.5.dp, ghostBorderColor)
    ) {
        Column {
            // --- HERO IMAGE WITH BADGES ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlaid Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Verified Owner Badge (Top-Left)
                    val showVerifiedBadge = property.extra4 == "VERIFIED_OWNER" || property.extra4 == "SOCIETY_APPROVED"
                    if (showVerifiedBadge) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(brandTeal)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VERIFIED OWNER",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Society Approved Badge (Top-Right)
                    if (property.extra4 == "SOCIETY_APPROVED") {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ConciergeStatusAmber)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOCIETY APPROVED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Available Badge (Bottom-Left)
                val isAvailable = property.extra3 == "AVAILABLE" || property.extra3.isEmpty()
                if (isAvailable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = ConciergeVegGreenLight,
                            border = BorderStroke(1.dp, ConciergeVegGreenBorder),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "AVAILABLE",
                                color = ConciergeVegGreenDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- BELOW IMAGE TEXT CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Location line
                val locationText = details.wingFlatNumber.ifEmpty { property.authorFlat.ifEmpty { "Wing A, Flat 304" } }
                Text(
                    text = locationText.uppercase(),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Bold title
                Text(
                    text = property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ConciergeBrandNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Specs Icon row (beds, baths, sqft)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bed,
                            contentDescription = "Beds",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${specs.first} ${if (specs.first == 1) "Bed" else "Beds"}",
                            color = Color(0xFF454652),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bathtub,
                            contentDescription = "Baths",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${specs.second} ${if (specs.second == 1) "Bath" else "Baths"}",
                            color = Color(0xFF454652),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = "Size",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${specs.third} sqft",
                            color = Color(0xFF454652),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // HorizontalDivider line
                HorizontalDivider(color = Color(0xFFEFF4FF), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

                // Price and details button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format("%,.0f", property.price)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = ConciergeBrandNavy
                        )
                        Text(
                            text = "per month",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = onViewDetails,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003FB1)),
                        shape = RoundedCornerShape(9999.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("view_details_button_${property.id}")
                    ) {
                        Text(
                            text = "View details",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns a Triple representing (Beds, Baths, Sqft) based on the listing's propertyDetails()
 * falling back to string matching on title/category/extra2 only when values are zero.
 */
fun getPropertySpecs(listing: ListingEntity): Triple<Int, Int, Int> {
    val details = listing.propertyDetails()
    if (details.beds > 0 || details.baths > 0 || details.sqft > 0) {
        val beds = if (details.beds > 0) details.beds else 1
        val baths = if (details.baths > 0) details.baths else 1
        val sqft = if (details.sqft > 0) details.sqft else 1000
        return Triple(beds, baths, sqft)
    }

    val titleLower = listing.title.lowercase()
    val catLower = listing.category.lowercase()
    val extraLower = listing.extra2.lowercase()

    return when {
        titleLower.contains("3 bhk") || catLower.contains("3 bhk") || extraLower.contains("3 bhk") -> Triple(3, 3, 2400)
        titleLower.contains("1 bhk") || catLower.contains("1 bhk") || extraLower.contains("1 bhk") || titleLower.contains("studio") -> Triple(1, 1, 750)
        titleLower.contains("shared") || titleLower.contains("flat 505") -> Triple(2, 1, 1000)
        else -> Triple(2, 2, 1250) // Fallback / default 2 BHK
    }
}
