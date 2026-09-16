package com.connectkar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.primaryPhotoUrl
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentRentalsScreen(
    recentListings: List<ListingEntity>,
    selectedSociety: String,
    onBack: () -> Unit,
    onListingClick: (ListingEntity) -> Unit,
    onBottomNavClick: (String) -> Unit = {},
    activeTab: String = "home",
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categoryFilterOptions = listOf("ALL", "PROPERTY", "VEHICLE", "HOUSEHOLD_ITEM")

    val filteredListings = remember(recentListings, searchQuery, selectedCategoryFilter) {
        recentListings.filter { listing ->
            val matchesCategory = selectedCategoryFilter == "ALL" || listing.type.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    listing.title.contains(searchQuery, ignoreCase = true) ||
                    listing.description.contains(searchQuery, ignoreCase = true) ||
                    listing.category.contains(searchQuery, ignoreCase = true) ||
                    listing.society.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recent Rentals",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandSlate
                        )
                        Text(
                            text = if (selectedSociety.isBlank() || selectedSociety == "All Societies") {
                                "All Township Societies"
                            } else {
                                selectedSociety
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = ConciergeBrandNavy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("recent_rentals_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandSlate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = activeTab,
                onTabSelected = onBottomNavClick
            )
        },
        containerColor = BrandBackground,
        modifier = modifier.testTag("recent_rentals_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recent rentals by title, item, flat...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("recent_rentals_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergeBrandNavy,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryFilterOptions.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val chipLabel = when (cat) {
                        "ALL" -> "All"
                        "PROPERTY" -> "Properties"
                        "VEHICLE" -> "Vehicles"
                        "HOUSEHOLD_ITEM" -> "Household"
                        else -> cat
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = {
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ConciergeBrandNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("recent_rentals_filter_${cat.lowercase()}")
                    )
                }
            }

            // Results List or Empty State
            if (filteredListings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "No recent rentals",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent rentals found",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try refining your search terms" else "New rental listings will show up here",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredListings, key = { it.id }) { listing ->
                        RecentRentalListItem(
                            listing = listing,
                            onClick = { onListingClick(listing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRentalListItem(
    listing: ListingEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recent_rental_item_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val photoUrl = listing.primaryPhotoUrl
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = listing.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, shape = RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val fallbackIcon = when (listing.type) {
                        "PROPERTY" -> Icons.Default.Apartment
                        "VEHICLE" -> Icons.Default.DirectionsCar
                        "HOUSEHOLD_ITEM" -> Icons.Default.Handyman
                        else -> Icons.Default.Category
                    }
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category badge + Society
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (typeLabel, badgeColor) = when (listing.type) {
                        "PROPERTY" -> "PROPERTY" to Color(0xFF003FB1)
                        "VEHICLE" -> "VEHICLE" to Color(0xFF00796B)
                        "HOUSEHOLD_ITEM" -> "HOUSEHOLD" to Color(0xFFB45309)
                        else -> listing.type to Color.Gray
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = typeLabel,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = listing.society.ifBlank { "Township" },
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (listing.description.isNotBlank()) {
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val formattedPrice = if (listing.price % 1.0 == 0.0) {
                    "₹%,d".format(listing.price.toLong())
                } else {
                    "₹%,.2f".format(listing.price)
                }
                val period = if (listing.type == "PROPERTY") " /month" else " /day"
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formattedPrice,
                        color = Color(0xFF003FB1),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = period,
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                    )
                }
            }
        }
    }
}
