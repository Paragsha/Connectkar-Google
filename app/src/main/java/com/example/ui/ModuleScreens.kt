package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleListScreen(
    moduleType: String,
    listings: List<ListingEntity>,
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    onBack: () -> Unit,
    onLikeListing: (Int) -> Unit,
    onBookmarkListing: (Int) -> Unit,
    onCreateListingClicked: () -> Unit,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val moduleTitles = mapOf(
        "MARKETPLACE" to "Marketplace",
        "FEED" to "Community Feed",
        "CARPOOL" to "Carpooling Hub",
        "MEAL" to "Daily Meals",
        "SERVICE" to "Resident Services",
        "PROPERTY" to "Rentals & Properties",
        "VEHICLE" to "Vehicles Log"
    )

    val moduleSubtitles = mapOf(
        "MARKETPLACE" to "Buy & Sell items with verified residents",
        "FEED" to "Stay updated on neighborhood posts & events",
        "CARPOOL" to "Share rides & commute together sustainably",
        "MEAL" to "Order home-cooked food by local resident chefs",
        "SERVICE" to "Trustworthy plumbers, electricians & helpers",
        "PROPERTY" to "Discover rental properties & item rentals",
        "VEHICLE" to "View vehicle entry logs & safety details"
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

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeTitle,
                            fontWeight = FontWeight.Bold,
                            color = BrandSlate,
                            fontSize = 20.sp
                        )
                        Text(
                            text = if (selectedSociety == "All Societies") "Showing all of ConnectKar" else "Showing only in $selectedSociety",
                            fontSize = 11.sp,
                            color = BrandEmerald,
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
            if (currentUser.isVerified && moduleType != "VEHICLE") {
                FloatingActionButton(
                    onClick = onCreateListingClicked,
                    containerColor = BrandEmerald,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_listing_fab"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Listing")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = BrandBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
            
            // Module Info Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp)
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
                        fontSize = 12.sp,
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandEmerald,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            if (filteredListings.isEmpty()) {
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
                            fontSize = 18.sp,
                            color = BrandSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search terms" else "Be the first verified resident to create an entry!",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
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
                    items(filteredListings, key = { it.id }) { listing ->
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
