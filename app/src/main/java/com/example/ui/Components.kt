package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.data.local.ListingDetails
import com.example.data.local.photoUrls
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

import com.example.ui.components.ShimmerBox
import com.example.ui.components.shimmerEffect
import com.example.ui.theme.*

// --- Brand Colors (Unified with Digital Concierge design tokens) ---
val BrandIndigo = ConciergePrimaryContainer
val BrandIndigoLight = ConciergePrimaryFixed
val BrandGold = ConciergeTertiary
val BrandGoldLight = ConciergeSurfaceVariant
val BrandSlate = ConciergeOnBackground
val BrandBackground = ConciergeBackground
val BrandOutline = ConciergeOutlineVariant

// --- Onboarding Specific Colors ---
val BrandPrimaryBlue = ConciergeSecondary
val BrandPrimaryBlueLight = ConciergeSecondaryContainer
val BrandSuccessGreen = ConciergeHomeLiving

// --- Shared Societies List ---
val TownshipSocieties = listOf(
    "Aqualily Estate",
    "Nova Apartments",
    "Iris Court",
    "Happiness Apartments",
    "Lakewoods Apartments",
    "Sylvan County"
)

@Composable
fun AvatarImage(
    avatarIndex: Int,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val colors = listOf(
        Color(0xFF3F51B5), // Indigo
        Color(0xFFE91E63), // Pink
        Color(0xFF009688), // Teal
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF4CAF50)  // Green
    )
    val initials = listOf("AS", "RG", "SR", "MN", "AS", "PK")
    
    val colorIndex = avatarIndex.coerceIn(0, colors.lastIndex)
    val initial = initials.getOrElse(avatarIndex) { "R" }
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors[colorIndex],
                        colors[colorIndex].copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4f).sp
        )
    }
}

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    isVerified: Boolean,
    isPending: Boolean
) {
    when {
        isVerified -> {
            Row(
                modifier = modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(ConciergeVegGreenLight)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Resident",
                    tint = ConciergeVegGreenDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VERIFIED",
                    color = ConciergeVegGreenDark,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        isPending -> {
            Row(
                modifier = modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(BrandGoldLight)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Pending,
                    contentDescription = "Pending Verification",
                    tint = BrandGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PENDING VERIFICATION",
                    color = BrandGold,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> {
            Row(
                modifier = modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Unverified",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "UNVERIFIED",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SocietyBadge(society: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFFEEF2F6))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Society",
            tint = BrandSlate,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = society,
            color = BrandSlate,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SyncStatusBanner(
    syncState: SyncState,
    onRetrySync: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (syncState) {
        is SyncState.Syncing -> {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFEFF6FF),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .size(16.dp),
                        shape = CircleShape,
                        baseColor = Color(0xFF93C5FD),
                        highlightColor = Color(0xFFEFF6FF)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Synchronizing with community network...",
                        color = Color(0xFF1E40AF),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        is SyncState.Failed -> {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFFEF2F2),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Sync Error Indicator",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Firestore Sync Failed",
                                color = ConciergeNonVegRedDark,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Viewing stale/offline local data. ${syncState.message}",
                                color = ConciergeNonVegRedDark,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    if (onRetrySync != null) {
                        TextButton(
                            onClick = onRetrySync,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Sync Button",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry Sync", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        else -> {
            // Idle or Success
        }
    }
}

@Composable
fun StatusBanner(
    user: UserEntity,
    onSimulateApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (user.isPending) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandGoldLight),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Pending Status",
                        tint = Color(0xFFD48800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Verification Pending",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD48800),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Your profile is under review by ${user.society} admin. You can browse listings but cannot create posts or contact residents.",
                            color = Color(0xFF595959),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (com.example.BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSimulateApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)), // Distinct Dark Slate
                        border = BorderStroke(1.dp, Color(0xFFF97316)), // Eye-catching developer border
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("simulate_verify_button"),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Surface(
                            color = Color(0xFFF97316),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "DEV",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Simulate Verification",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Instant Self-Verify (Demo)", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ListingCard(
    listing: ListingEntity,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    isCurrentUserVerified: Boolean,
    modifier: Modifier = Modifier
) {
    var showContactInfo by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, BrandOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author Info, Timestamp, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(avatarIndex = listing.id % 6, size = 40)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = listing.authorName,
                            fontWeight = FontWeight.Bold,
                            color = BrandSlate,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BrandIndigo)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = listing.authorFlat,
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "Posted in ${listing.society}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("listing_type_badge_${listing.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (typeIcon, typeLabel) = when (listing.type) {
                            "MARKETPLACE" -> Icons.Default.ShoppingBasket to "Marketplace"
                            "PROPERTY" -> Icons.Default.Apartment to "Property"
                            "SERVICE" -> Icons.Default.Handyman to "Service"
                            "MEAL" -> Icons.Default.Restaurant to "Meal"
                            "CARPOOL" -> Icons.Default.DirectionsCar to "Carpool"
                            "VEHICLE" -> Icons.Default.DirectionsCar to "Vehicle"
                            "EVENT" -> Icons.Default.Event to "Event"
                            else -> Icons.Default.Tag to listing.type
                        }
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = typeLabel,
                            modifier = Modifier.size(12.dp),
                            tint = ConciergeBrandNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergeBrandNavy
                        )
                    }
                }

                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.testTag("bookmark_button_${listing.id}")
                ) {
                    Icon(
                        imageVector = if (listing.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (listing.isBookmarked) BrandIndigo else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Title & Description
            Text(
                text = listing.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = BrandSlate,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = listing.description,
                color = Color(0xFF475569),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            val photos = listing.photoUrls()
            if (photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    AsyncImage(
                        model = photos.first(),
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (photos.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "1/${photos.size} Photos",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Layout based on Module Type
            ModuleSpecificContent(listing)

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Footer actions: Like, Share, Contact/Connect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.testTag("like_button_${listing.id}")
                    ) {
                        Icon(
                            imageVector = if (listing.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (listing.isLikedByMe) Color.Red else Color.Gray
                        )
                    }
                    Text(
                        text = "${listing.likesCount}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isCurrentUserVerified) {
                    Button(
                        onClick = { showContactInfo = !showContactInfo },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showContactInfo) BrandSlate else BrandIndigo
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (showContactInfo) Icons.Default.Close else Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showContactInfo) "Hide Contact" else "Connect",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked: Profile verification is required to connect with this resident",
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify to Connect", color = Color.Gray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showContactInfo && isCurrentUserVerified,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(Color(0xFFEEF2F6))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandSlate)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Resident: ${listing.authorName} (${listing.authorFlat})", color = BrandSlate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Phone/WhatsApp: ${listing.contact}", color = BrandIndigo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleSpecificContent(listing: ListingEntity) {
    val details = listing.details
    when (details) {
        is com.example.data.local.ListingDetails.Marketplace -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${listing.price.toInt()}",
                    color = BrandIndigo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(BrandIndigoLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = details.category.uppercase(),
                        color = BrandIndigo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        is com.example.data.local.ListingDetails.Service -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = BrandGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = details.rating,
                        fontWeight = FontWeight.Bold,
                        color = BrandSlate,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = "Base rate: ₹${details.baseRate.toInt()}/visit",
                    color = BrandSlate,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        is com.example.data.local.ListingDetails.Carpool -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TripOrigin, contentDescription = "Origin", tint = Color.Blue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "From: ${details.origin}", style = MaterialTheme.typography.bodySmall, color = BrandSlate)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Destination", tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "To: ${details.destination}", style = MaterialTheme.typography.bodySmall, color = BrandSlate)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = details.departureTime, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = "Seats", tint = BrandIndigo, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = details.seats, style = MaterialTheme.typography.labelMedium, color = BrandIndigo, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        is com.example.data.local.ListingDetails.Property -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), MaterialTheme.shapes.extraSmall)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = details.bhk,
                        fontWeight = FontWeight.Bold,
                        color = BrandSlate,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "₹${details.rent.toInt()}/mo",
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandIndigo,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Status: ${details.status}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }
        is com.example.data.local.ListingDetails.Meal -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandSlate
                    )
                    Text(
                        text = "Delivery: ${details.deliveryInfo}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "₹${details.mealPrice.toInt()}",
                    color = BrandIndigo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        is com.example.data.local.ListingDetails.Vehicle -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(0xFFEEF2F6))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = details.plateNumber,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandSlate,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = details.vehicleModel,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location/Spot: ${details.locationSpot}",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandSlate
                )
                Text(
                    text = "Security Tag: ${details.securityTag}",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandIndigo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is com.example.data.local.ListingDetails.Event -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(BrandIndigoLight)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = details.eventLocation.ifEmpty { "Community Hall" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandSlate
                    )
                    Text(
                        text = details.timing.ifEmpty { "This Sunday" },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        else -> {}
    }
}
