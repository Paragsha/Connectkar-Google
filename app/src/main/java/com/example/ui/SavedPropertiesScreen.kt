package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.data.local.propertyDetails
import com.example.ui.components.ConnectKarBottomBar

@Composable
fun SavedPropertiesScreen(
    currentUser: UserEntity,
    savedListings: List<ListingEntity>,
    selectedSociety: String,
    onToggleBookmark: (Int) -> Unit,
    onBack: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedListingForDetails by remember { mutableStateOf<ListingEntity?>(null) }

    val brandNavy = Color(0xFF001A40)
    val lightBg = Color(0xFFF8FAFC)

    if (selectedListingForDetails != null) {
        PropertyDetailsScreen(
            property = selectedListingForDetails!!,
            onBack = { selectedListingForDetails = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = "explore",
                onTabSelected = onBottomNavClick
            )
        },
        containerColor = lightBg,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Header: Back Button & Bell
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF4FF))
                            .testTag("saved_properties_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF4FF))
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = brandNavy,
                                modifier = Modifier.size(20.dp)
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
                }
            }

            // Title: "Saved Properties"
            item {
                Text(
                    text = "Saved Properties",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = brandNavy,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 20.dp)
                        .testTag("saved_properties_title")
                )
            }

            // Empty State
            if (savedListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No saved properties yet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bookmark rental listings to quickly compare and access them here.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Saved Properties List
            items(savedListings, key = { it.id }) { property ->
                SavedPropertyCard(
                    property = property,
                    onViewDetails = { selectedListingForDetails = property },
                    onToggleBookmark = {
                        onToggleBookmark(property.id)
                        Toast.makeText(context, "Removed from saved properties", Toast.LENGTH_SHORT).show()
                    },
                    brandNavy = brandNavy
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SavedPropertyCard(
    property: ListingEntity,
    onViewDetails: () -> Unit,
    onToggleBookmark: () -> Unit,
    brandNavy: Color
) {
    val specs = getPropertySpecs(property)
    val details = property.propertyDetails()

    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx"
    val imageUrl = property.extra1.ifEmpty { fallbackImage }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("saved_property_card_${property.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Hero Image with Overlaid Badges & Bookmark Heart
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

                // Top badges row (Verified Owner / Society Approved on left, Bookmark heart on right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left badges
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val showVerifiedBadge = property.extra4 == "VERIFIED_OWNER" || property.extra4 == "SOCIETY_APPROVED"
                        if (showVerifiedBadge) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF007A5A)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            }
                        }

                        if (property.extra4 == "SOCIETY_APPROVED") {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = Color(0xFFD97706)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                    }

                    // Bookmark heart button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onToggleBookmark() }
                            .testTag("saved_property_heart_${property.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Remove from saved",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Below Image Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Location line & Available badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val locationText = details.wingFlatNumber.ifEmpty { property.authorFlat.ifEmpty { "Wing A, Flat 304" } }
                    Text(
                        text = locationText.uppercase(),
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    val isAvailable = property.extra3 == "AVAILABLE" || property.extra3.isEmpty()
                    if (isAvailable) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "AVAILABLE",
                                color = Color(0xFF15803D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Title
                Text(
                    text = property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
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

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

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
                            fontSize = 20.sp,
                            color = brandNavy
                        )
                        Text(
                            text = "per month",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    OutlinedButton(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, Color(0xFF003FB1)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("saved_property_view_details_${property.id}")
                    ) {
                        Text(
                            text = "View details",
                            color = Color(0xFF003FB1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
