package com.example.ui

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
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyRentalsScreen(
    currentUser: UserEntity,
    listings: List<ListingEntity>,
    selectedSociety: String,
    syncState: SyncState,
    onBack: () -> Unit,
    onLikeListing: (Int) -> Unit,
    onBookmarkListing: (Int) -> Unit,
    onCreateListingClicked: () -> Unit,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedListingForDetails by remember { mutableStateOf<ListingEntity?>(null) }

    val filterChips = listOf("All", "2 BHK", "1 BHK", "Furnished", "Under ₹25k", "Pet Friendly")

    // Brand Palette remapped to matching Digital Concierge design tokens
    val brandNavy = ConciergePrimaryContainer
    val brandCobalt = ConciergeSecondary
    val surfaceContainerLow = ConciergeSurfaceContainerLow
    val surfaceContainerLowest = ConciergeSurface
    val ghostBorderColor = ConciergeOutlineVariant.copy(alpha = 0.2f)
    val brandTeal = ConciergeHomeLiving

    // Filtering logic based on search query and selected filter chip
    val filteredProperties = listings.filter { listing ->
        val matchesSearch = listing.title.contains(searchQuery, ignoreCase = true) ||
                listing.description.contains(searchQuery, ignoreCase = true) ||
                listing.authorFlat.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "2 BHK" -> listing.title.contains("2 BHK", ignoreCase = true) || 
                       listing.category.contains("2 BHK", ignoreCase = true) || 
                       listing.extra2.contains("2 BHK", ignoreCase = true)
            "1 BHK" -> listing.title.contains("1 BHK", ignoreCase = true) || 
                       listing.category.contains("1 BHK", ignoreCase = true) || 
                       listing.extra2.contains("1 BHK", ignoreCase = true)
            "Furnished" -> listing.title.contains("furnished", ignoreCase = true) || 
                           listing.description.contains("furnished", ignoreCase = true)
            "Under ₹25k" -> listing.price < 25000.0
            "Pet Friendly" -> listing.description.contains("pet", ignoreCase = true) || 
                              listing.title.contains("pet", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.isVerified) {
                FloatingActionButton(
                    onClick = onCreateListingClicked,
                    containerColor = brandNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 80.dp) // Leave room for bottom navigation
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
            // High-fidelity integrated Bottom Navigation Bar matching the spec
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(0.5.dp, Color(0xFFC6C5D4).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Home Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Home",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Home", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }

                    // Explore Tab (Active / Highlights Property rentals screen)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Explore",
                            tint = brandCobalt,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Explore", fontSize = 11.sp, color = brandCobalt, fontWeight = FontWeight.Bold)
                    }

                    // Floating Spacer for visual alignment matching layout rules
                    Spacer(modifier = Modifier.width(40.dp))

                    // Society Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "Navigating to Society Board", Toast.LENGTH_SHORT).show()
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apartment,
                            contentDescription = "Society",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Society", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }

                    // Profile Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "Showing Profile Context", Toast.LENGTH_SHORT).show()
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Profile", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FF),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            tint = brandNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ConnectKar",
                        fontWeight = FontWeight.Black,
                        color = brandNavy,
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
                            tint = brandNavy,
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
                            tint = brandNavy,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Notification Bell
                    IconButton(
                        onClick = { Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF4FF))
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = brandNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
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
                    color = brandNavy,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 16.dp)
                        .testTag("screen_title_rentals")
                )
            }

            // --- SEARCH BAR ---
            item {
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* Collapse keyboard */ }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("property_search_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC6C5D4),
                        unfocusedBorderColor = Color(0xFFC6C5D4).copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
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
                        tint = brandNavy,
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

            // --- NO PROPERTIES FOUND STATE ---
            if (filteredProperties.isEmpty()) {
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
                                color = brandNavy
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
            }

            // --- PROPERTY CARDS LIST ---
            items(filteredProperties, key = { it.id }) { property ->
                PropertyCard(
                    property = property,
                    onViewDetails = { selectedListingForDetails = property },
                    brandNavy = brandNavy,
                    brandTeal = brandTeal,
                    ghostBorderColor = ghostBorderColor,
                    surfaceContainerLowest = surfaceContainerLowest
                )
                Spacer(modifier = Modifier.height(24.dp)) // Vertical gap divider matching layout rules
            }

            item {
                Spacer(modifier = Modifier.height(120.dp)) // bottom padding
            }
        }
    }

    // --- VIEW DETAILS MODAL DIALOG ---
    val detailProperty = selectedListingForDetails
    if (detailProperty != null) {
        val specs = getPropertySpecs(detailProperty)
        AlertDialog(
            onDismissRequest = { selectedListingForDetails = null },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Calling owner: ${detailProperty.contact}", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandNavy)
                ) {
                    Text("Call Owner", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedListingForDetails = null }) {
                    Text("Close", color = Color.Gray)
                }
            },
            title = {
                Text(
                    text = detailProperty.title,
                    fontWeight = FontWeight.Bold,
                    color = brandNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${specs.first} Beds", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bathtub, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${specs.second} Baths", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${specs.third} sqft", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "Location: ${detailProperty.authorFlat.ifEmpty { "Wing A, Flat 304" }}",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Text(
                        text = detailProperty.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Price:", fontWeight = FontWeight.Bold, color = brandNavy)
                        Text(
                            text = "₹${String.format("%,.0f", detailProperty.price)}/month",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = brandNavy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Posted By:", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(detailProperty.authorName, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun PropertyCard(
    property: ListingEntity,
    onViewDetails: () -> Unit,
    brandNavy: Color,
    brandTeal: Color,
    ghostBorderColor: Color,
    surfaceContainerLowest: Color
) {
    val specs = getPropertySpecs(property)

    // Fallback image in case the database entry is missing one
    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx"
    val imageUrl = property.extra1.ifEmpty { fallbackImage }

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
                        // Empty spacer to keep Society Approved badge pushed to the right
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Society Approved Badge (Top-Right)
                    if (property.extra4 == "SOCIETY_APPROVED") {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFD97706))
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
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "AVAILABLE",
                                color = Color(0xFF15803D),
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
                Text(
                    text = property.authorFlat.ifEmpty { "Wing A, Flat 304" }.uppercase(),
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
                    color = brandNavy,
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
                            color = brandNavy
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
 * Returns a Triple representing (Beds, Baths, Sqft) based on the listing properties
 */
fun getPropertySpecs(listing: ListingEntity): Triple<Int, Int, Int> {
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
