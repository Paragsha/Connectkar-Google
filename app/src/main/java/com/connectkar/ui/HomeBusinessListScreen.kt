package com.connectkar.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.components.PropertyListingSkeletonCard
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBusinessListScreen(
    listings: List<ListingEntity>,
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    isRefreshing: Boolean = false,
    hasPostedListings: Boolean = false,
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
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Bakers", "Tutors", "Handmade & Crafts", "Services", "Food & Meals")

    // Pulse animation logic: user is verified but hasn't created a listing yet
    val shouldPulse = currentUser.isVerified && !hasPostedListings
    val infiniteTransition = rememberInfiniteTransition(label = "home_biz_fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_pulse_scale"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fab_halo_scale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fab_halo_alpha"
    )
    val currentFabScale = if (shouldPulse) pulseScale else 1f

    // Filter listings by search query and category chip
    val filteredListings = listings.filter { listing ->
        val matchesQuery = searchQuery.isEmpty() ||
            listing.title.contains(searchQuery, ignoreCase = true) ||
            listing.description.contains(searchQuery, ignoreCase = true) ||
            listing.authorName.contains(searchQuery, ignoreCase = true) ||
            listing.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "All" ||
            listing.category.contains(selectedCategory, ignoreCase = true) ||
            (selectedCategory == "Bakers" && (listing.category.contains("bake", ignoreCase = true) || listing.title.contains("bak", ignoreCase = true))) ||
            (selectedCategory == "Tutors" && (listing.category.contains("tutor", ignoreCase = true) || listing.title.contains("tutor", ignoreCase = true))) ||
            (selectedCategory == "Handmade & Crafts" && (listing.category.contains("craft", ignoreCase = true) || listing.category.contains("handmade", ignoreCase = true))) ||
            (selectedCategory == "Services" && listing.category.contains("service", ignoreCase = true)) ||
            (selectedCategory == "Food & Meals" && (listing.category.contains("food", ignoreCase = true) || listing.category.contains("meal", ignoreCase = true)))

        matchesQuery && matchesCategory
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.isVerified) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    if (shouldPulse) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    scaleX = haloScale
                                    scaleY = haloScale
                                    alpha = haloAlpha
                                }
                                .background(
                                    color = ConciergePrimaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .testTag("home_biz_pulse_halo")
                        )
                    }
                    FloatingActionButton(
                        onClick = onCreateListingClicked,
                        containerColor = ConciergePrimaryContainer,
                        contentColor = Color.White,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = currentFabScale
                                scaleY = currentFabScale
                            }
                            .testTag("home_biz_fab"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Business Listing")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
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
        containerColor = ConciergeSurface,
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_biz_pull_to_refresh")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Header row
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
                                tint = ConciergePrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ConnectKar",
                            fontWeight = FontWeight.Black,
                            color = ConciergePrimaryContainer,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .width(1.dp)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ConciergeSurfaceContainerLow)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ConciergePrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedSociety,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BrandSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                        }
                    }
                }

                // Sync status banner if syncing or error
                item {
                    SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
                }

                // Screen title and subtitle
                item {
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                        Text(
                            text = "Home Businesses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = ConciergePrimaryContainer,
                            modifier = Modifier.testTag("screen_title_home_businesses")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Discover talented bakers, tutors, crafters & creators in your community",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Search Input
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search businesses, crafts, tutors...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* Done */ }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("home_biz_search_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ConciergePrimaryContainer,
                            unfocusedBorderColor = Color(0xFFC6C5D4).copy(alpha = 0.6f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Category Chips Row
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("home_biz_category_chips"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            val bg = if (isSelected) ConciergePrimaryContainer else ConciergeSurfaceContainerLow
                            val textColor = if (isSelected) Color.White else Color(0xFF454652)

                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = bg,
                                modifier = Modifier
                                    .clickable { selectedCategory = cat }
                                    .testTag("chip_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Trust Banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ConciergeSurfaceContainerLow)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified residents",
                            tint = ConciergePrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All businesses run by verified township residents",
                            color = Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Listings list / Skeleton / Empty state
                if (syncState is SyncState.Syncing && listings.isEmpty()) {
                    items(3) {
                        PropertyListingSkeletonCard(
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                } else if (filteredListings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No home businesses found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ConciergePrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Try adjusting your search terms" else "Be the first resident to showcase your home business!",
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredListings, key = { it.id }) { listing ->
                        ListingCard(
                            listing = listing,
                            onLike = { onLikeListing(listing.id) },
                            onBookmark = { onBookmarkListing(listing.id) },
                            isCurrentUserVerified = currentUser.isVerified,
                            modifier = Modifier.testTag("listing_card_${listing.id}")
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
