package com.connectkar.ui

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.primaryPhotoUrl
import com.connectkar.ui.SyncState
import com.connectkar.ui.components.ConnectKarBottomBar
import com.connectkar.ui.components.MyListingItemSkeleton
import com.connectkar.ui.theme.*

@Composable
fun MyListingsScreen(
    currentUser: UserEntity,
    myListings: List<ListingEntity>,
    selectedSociety: String,
    onDeleteListing: (Int) -> Unit,
    onBack: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    syncState: SyncState = SyncState.Idle,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Active") } // "Active", "Draft", "Expired"
    var selectedListingForDetails by remember { mutableStateOf<ListingEntity?>(null) }
    var listingToDelete by remember { mutableStateOf<ListingEntity?>(null) }

    val brandActiveBlue = Color(0xFF003FB1)
    val lightBg = Color(0xFFF8FAFC)

    val filteredListings = remember(selectedTab, myListings) {
        when (selectedTab) {
            "Active" -> myListings.filter { !it.isDraft }
            "Draft" -> myListings.filter { it.isDraft }
            "Expired" -> emptyList() // No expiry column exists in schema; render empty state
            else -> myListings
        }
    }

    if (selectedListingForDetails != null) {
        PropertyDetailsScreen(
            property = selectedListingForDetails!!,
            onBack = { selectedListingForDetails = null }
        )
        return
    }

    if (listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = {
                Text(
                    text = "Delete Listing",
                    fontWeight = FontWeight.Bold,
                    color = ConciergeBrandNavy
                )
            },
            text = {
                Text("Are you sure you want to delete \"${listingToDelete?.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = listingToDelete?.id ?: 0
                        listingToDelete = null
                        onDeleteListing(id)
                        Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConciergeNonVegRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        bottomBar = {
            ConnectKarBottomBar(
                activeTab = "profile",
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
            // Header: ConnectKar Logo & Bell
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ConciergeBrandNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ConnectKar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = ConciergeBrandNavy
                        )
                    }

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
                                tint = ConciergeBrandNavy,
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
                }
            }

            // Title: "My Listings"
            item {
                Text(
                    text = "My Listings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = ConciergeBrandNavy,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .testTag("my_listings_title")
                )
            }

            // 3-Segment Tab Bar (Active / Draft / Expired)
            item {
                val tabs = listOf("Active", "Draft", "Expired")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tabs.forEach { tabName ->
                        val isSelected = selectedTab == tabName
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (isSelected) brandActiveBlue else Color(0xFFEFF4FF),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tabName }
                                .testTag("my_listings_tab_$tabName")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabName,
                                    color = if (isSelected) Color.White else Color(0xFF454652),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Shimmer Skeleton loading state during initial fetch / syncing
            if ((syncState is SyncState.Syncing || isLoading) && filteredListings.isEmpty()) {
                items(3) {
                    MyListingItemSkeleton(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else if (filteredListings.isEmpty()) {
                // Empty State
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == "Expired") Icons.Default.History else Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (selectedTab) {
                                    "Active" -> "No active listings"
                                    "Draft" -> "No drafts saved"
                                    "Expired" -> "No expired listings"
                                    else -> "No listings found"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConciergeBrandNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (selectedTab) {
                                    "Active" -> "Create your first rental listing to see it here."
                                    "Draft" -> "Draft listings will appear here when you save in-progress creations."
                                    "Expired" -> "Past listings that reached their expiry duration will show here."
                                    else -> ""
                                },
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                // Listings List
                items(filteredListings, key = { it.id }) { listing ->
                    MyListingCard(
                        listing = listing,
                        onViewDetails = { selectedListingForDetails = listing },
                        onEdit = {
                            Toast.makeText(context, "Editing existing listings isn't available yet", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            listingToDelete = listing
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MyListingCard(
    listing: ListingEntity,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx"
    val imageUrl = listing.primaryPhotoUrl(fallbackImage)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("my_listing_card_${listing.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Status Pill Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ConciergeBrandNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (listing.isDraft) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = ConciergeStatusAmberBadgeBg
                        ) {
                            Text(
                                text = "Draft",
                                color = ConciergeStatusAmberMedium,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = ConciergeVegGreenLight
                        ) {
                            Text(
                                text = "Active",
                                color = ConciergeVegGreenDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                Text(
                    text = "₹${String.format("%,.0f", listing.price)}/mo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF003FB1)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: Edit and Delete buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF4FF))
                            .clickable { onEdit() }
                            .testTag("edit_listing_button_${listing.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Listing",
                            tint = Color(0xFF003FB1),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ConciergeNonVegRedLight)
                            .clickable { onDelete() }
                            .testTag("delete_listing_button_${listing.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Listing",
                            tint = ConciergeNonVegRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
