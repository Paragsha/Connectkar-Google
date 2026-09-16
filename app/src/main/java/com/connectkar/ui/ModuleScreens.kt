package com.connectkar.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.components.PropertyListingSkeletonCard
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleListScreen(
    moduleType: String,
    listings: List<ListingEntity>,
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    isRefreshing: Boolean = false,
    onBack: () -> Unit,
    onLikeListing: (Int) -> Unit,
    onBookmarkListing: (Int) -> Unit,
    onCreateListingClicked: () -> Unit,
    onRetrySync: () -> Unit,
    onRefresh: () -> Unit = {},
    activeTab: String = "home",
    onBottomNavClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val moduleTitles = mapOf(
        "MARKETPLACE" to "Marketplace",
        "CARPOOL" to "Carpooling Hub",
        "MEAL" to "Daily Meals",
        "SERVICE" to "Resident Services",
        "PROPERTY" to "Rentals & Properties",
        "VEHICLE" to "Vehicles for Rent",
        "HOUSEHOLD_ITEM" to "Household Items",
        "EXPLORE" to "Explore"
    )

    val moduleSubtitles = mapOf(
        "MARKETPLACE" to "Buy & Sell items with verified residents",
        "CARPOOL" to "Share rides & commute together sustainably",
        "MEAL" to "Order home-cooked food by local resident chefs",
        "SERVICE" to "Trustworthy plumbers, electricians & helpers",
        "PROPERTY" to "Discover rental properties & item rentals",
        "VEHICLE" to "Rent bikes, cars & more from neighbours",
        "HOUSEHOLD_ITEM" to "Furniture, appliances, tools & more",
        "EXPLORE" to "All updates across your township"
    )

    val activeTitle = moduleTitles[moduleType] ?: "Module"
    val activeSubtitle = moduleSubtitles[moduleType] ?: "Explore township updates"

    // Filter listings by search query
    val filteredListings = listings.filter { listing ->
        listing.title.contains(searchQuery, ignoreCase = true) ||
        listing.description.contains(searchQuery, ignoreCase = true) ||
        listing.category.contains(searchQuery, ignoreCase = true) ||
        listing.authorName.contains(searchQuery, ignoreCase = true)
    }

    var selectedFilterOption by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Marketplace", "Property", "Service", "Meal", "Carpool", "Vehicle")

    val displayedListings = remember(filteredListings, selectedFilterOption, moduleType) {
        if (moduleType == "EXPLORE" && selectedFilterOption != "All") {
            filteredListings.filter { listing ->
                listing.type.equals(selectedFilterOption, ignoreCase = true)
            }
        } else {
            filteredListings
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeTitle,
                            fontWeight = FontWeight.Bold,
                            color = BrandSlate,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (selectedSociety == "All Societies") "Showing all of ConnectKar" else "Showing only in $selectedSociety",
                            style = MaterialTheme.typography.labelMedium,
                            color = ConciergeBrandNavy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Hub", tint = BrandSlate)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = BrandBackground)
            )
        },
        floatingActionButton = {
            if (currentUser.isVerified && moduleType != "HOUSEHOLD_ITEM") {
                FloatingActionButton(
                    onClick = onCreateListingClicked,
                    containerColor = ConciergeBrandNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_listing_fab"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Listing")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = activeTab,
                onTabSelected = { target ->
                    if (target == "create") {
                        onCreateListingClicked()
                    } else {
                        onBottomNavClick(target)
                    }
                }
            )
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
                .testTag("module_list_pull_to_refresh")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
                
                // Module Info Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandSlate,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search title, description, or author...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("module_search_input"),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ConciergeBrandNavy,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                if (moduleType == "EXPLORE") {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("explore_filter_chips_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { option ->
                            FilterChip(
                                selected = selectedFilterOption == option,
                                onClick = { selectedFilterOption = option },
                                label = {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedFilterOption == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("filter_chip_${option.lowercase()}")
                            )
                        }
                    }
                }

                if (syncState is SyncState.Syncing && listings.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(3) {
                            PropertyListingSkeletonCard(
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                } else if (displayedListings.isEmpty()) {
                    // Beautiful Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "No listings",
                                tint = Color.LightGray,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No listings found",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = BrandSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try adjusting your search terms" else "Be the first verified resident to create an entry! (Pull down to refresh)",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // Listings List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(displayedListings, key = { it.id }) { listing ->
                            ListingCard(
                                listing = listing,
                                onLike = { onLikeListing(listing.id) },
                                onBookmark = { onBookmarkListing(listing.id) },
                                isCurrentUserVerified = currentUser.isVerified
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB overlap
                        }
                    }
                }
            }
        }
    }
}
