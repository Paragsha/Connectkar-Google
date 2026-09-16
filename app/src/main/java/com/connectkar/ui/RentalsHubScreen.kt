package com.connectkar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun RentalsHubScreen(
    selectedSociety: String,
    categoryCounts: Map<String, Int>, // keys: "PROPERTY", "VEHICLE", "HOUSEHOLD_ITEM"
    recentListings: List<ListingEntity>,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit, // passes "PROPERTY" | "VEHICLE" | "HOUSEHOLD_ITEM"
    onListingClick: (ListingEntity) -> Unit,
    onSeeAllRecent: () -> Unit,
    onCreateClicked: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    activeTab: String = "home",
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rentals",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrandSlate
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("rentals_hub_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandSlate
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // TODO: wire to search
                        },
                        modifier = Modifier.testTag("rentals_hub_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
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
                onTabSelected = { target ->
                    if (target == "create") {
                        onCreateClicked()
                    } else {
                        onBottomNavClick(target)
                    }
                }
            )
        },
        containerColor = BrandBackground,
        modifier = modifier.testTag("rentals_hub_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Eyebrow label + Society pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFIED TOWNSHIP HUB",
                    color = ConciergeBrandNavy,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    val displaySociety = if (selectedSociety.isBlank() || selectedSociety == "All Societies") {
                        "TOWNSHIP"
                    } else {
                        selectedSociety
                    }
                    Text(
                        text = displaySociety.uppercase(),
                        color = Color(0xFF00796B),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .widthIn(max = 140.dp)
                    )
                }
            }

            // Subtext line
            Text(
                text = "Browse verified listings directly from neighbours.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Three stacked Category Cards
            RentalsCategoryCard(
                id = "PROPERTY",
                title = "Property",
                subtitle = "Flats, PGs & rooms for rent",
                count = categoryCounts["PROPERTY"] ?: 0,
                icon = Icons.Default.Apartment,
                ghostIcon = Icons.Default.Apartment,
                bgBrush = Brush.linearGradient(listOf(Color(0xFF1E60E2), Color(0xFF1345AF))),
                onClick = { onCategoryClick("PROPERTY") },
                modifier = Modifier.padding(bottom = 14.dp)
            )

            RentalsCategoryCard(
                id = "VEHICLE",
                title = "Vehicle",
                subtitle = "Cars, bikes & cycles from neighbours",
                count = categoryCounts["VEHICLE"] ?: 0,
                icon = Icons.Default.DirectionsCar,
                ghostIcon = Icons.Default.DirectionsCar,
                bgBrush = Brush.linearGradient(listOf(Color(0xFF00897B), Color(0xFF00695C))),
                onClick = { onCategoryClick("VEHICLE") },
                modifier = Modifier.padding(bottom = 14.dp)
            )

            RentalsCategoryCard(
                id = "HOUSEHOLD_ITEM",
                title = "Household Item",
                subtitle = "Furniture, appliances, tools & more",
                count = categoryCounts["HOUSEHOLD_ITEM"] ?: 0,
                icon = Icons.Default.Handyman,
                ghostIcon = Icons.Default.Build,
                bgBrush = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF3730A3))),
                onClick = { onCategoryClick("HOUSEHOLD_ITEM") },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // "Recently Listed" section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recently Listed",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandSlate
                    )
                    Text(
                        text = "IN YOUR TOWNSHIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // TODO: dedicated recents screen
                            onSeeAllRecent()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("rentals_see_all_recent")
                ) {
                    Text(
                        text = "See all",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF003FB1)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "See all",
                        tint = Color(0xFF003FB1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Horizontal strip of compact listing cards
            if (recentListings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent rentals in this society yet.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(recentListings, key = { it.id }) { listing ->
                        RentalsCompactListingCard(
                            listing = listing,
                            onClick = { onListingClick(listing) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RentalsCategoryCard(
    id: String,
    title: String,
    subtitle: String,
    count: Int,
    icon: ImageVector,
    ghostIcon: ImageVector,
    bgBrush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp)
            .clickable { onClick() }
            .testTag("rentals_category_card_$id"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Faint oversized ghost icon bottom-right
            Icon(
                imageVector = ghostIcon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Icon container + count pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "listings",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Bottom title & subtitle
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RentalsCompactListingCard(
    listing: ListingEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("recent_listing_card_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Photo with category badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                val photoUrl = listing.primaryPhotoUrl
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val fallbackIcon = when (listing.type) {
                            "PROPERTY" -> Icons.Default.Apartment
                            "VEHICLE" -> Icons.Default.DirectionsCar
                            "HOUSEHOLD_ITEM" -> Icons.Default.Handyman
                            else -> Icons.Default.Category
                        }
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Category badge overlay
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        val (typeIcon, typeLabel) = when (listing.type) {
                            "PROPERTY" -> Icons.Default.Apartment to "PROPERTY"
                            "VEHICLE" -> Icons.Default.DirectionsCar to "VEHICLE"
                            "HOUSEHOLD_ITEM" -> Icons.Default.Handyman to "HOUSEHOLD"
                            else -> Icons.Default.Tag to listing.type
                        }
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = typeLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Text metadata
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    val formattedPrice = if (listing.price % 1.0 == 0.0) {
                        "₹%,d".format(listing.price.toLong())
                    } else {
                        "₹%,.2f".format(listing.price)
                    }
                    Text(
                        text = formattedPrice,
                        color = Color(0xFF003FB1),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val period = if (listing.type == "PROPERTY") " /mo" else " /day"
                    Text(
                        text = period,
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = listing.society.ifBlank { "Verified Township" },
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
