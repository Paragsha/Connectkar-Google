package com.connectkar.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.homeBusinessDetails
import com.connectkar.data.local.primaryPhotoUrl
import com.connectkar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishSuccessSheet(
    listing: ListingEntity,
    isConfirmedLive: Boolean,
    onViewListing: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val bizDetails = listing.homeBusinessDetails()
    val photoUrl = listing.primaryPhotoUrl

    // Build the share text
    val flatLocation = if (bizDetails.operatesFromFlat.isNotBlank()) {
        bizDetails.operatesFromFlat
    } else {
        listing.authorFlat
    }
    val locationSnippet = if (flatLocation.isNotBlank() && listing.society.isNotBlank()) {
        " at $flatLocation, ${listing.society}"
    } else if (listing.society.isNotBlank()) {
        " in ${listing.society}"
    } else ""

    val shareText = "Check out ${listing.title} on ConnectKar!$locationSnippet. Find it under Home Businesses in the township app."

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("publish_success_sheet"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        )

        // Bottom sheet surface without merged semantics
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Dismiss handle / close button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("publish_success_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ConciergeOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Success Icon (Large mint circle with checkmark)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ConciergeVegGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Published successfully",
                    tint = ConciergeVegGreen,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Headline & Subtitle
            Text(
                text = "Listing Published!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ConciergeOnSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("publish_success_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your business is now visible to verified residents of ${listing.society.ifBlank { "your society" }}.",
                style = MaterialTheme.typography.bodyMedium,
                color = ConciergeOnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Status Badge (D1: LIVE only if confirmed)
            if (isConfirmedLive) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = ConciergeVegGreenLight,
                    border = BorderStroke(1.dp, ConciergeVegGreenBorder),
                    modifier = Modifier.testTag("publish_success_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ConciergeVegGreen)
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ConciergeVegGreenDark,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = ConciergeStatusAmberLight,
                    border = BorderStroke(1.dp, ConciergeStatusAmberBorder),
                    modifier = Modifier.testTag("publish_success_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ConciergeStatusAmber)
                        )
                        Text(
                            text = "SAVED • SYNCING TO CLOUD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergeStatusAmberDark,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Listing Preview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_success_preview_card"),
                shape = RoundedCornerShape(16.dp),
                color = ConciergeSurfaceContainerLowest,
                border = BorderStroke(1.dp, ConciergeOutlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Thumbnail or fallback storefront icon
                    if (photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = listing.title,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ConciergeOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ConciergeSurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = ConciergePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ConciergeOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (listing.category.isNotBlank()) {
                            Text(
                                text = listing.category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = ConciergePrimary
                            )
                        }

                        val locationDisplay = if (flatLocation.isNotBlank() && listing.society.isNotBlank()) {
                            "$flatLocation • ${listing.society}"
                        } else if (listing.society.isNotBlank()) {
                            listing.society
                        } else {
                            flatLocation
                        }

                        if (locationDisplay.isNotBlank()) {
                            Text(
                                text = locationDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                color = ConciergeOnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Share section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share With Neighbors",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ConciergeOnSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Share Tiles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Option 1: WhatsApp
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("publish_success_share_whatsapp")
                        .clickable {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                setPackage("com.whatsapp")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to generic chooser
                                val chooser = Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    },
                                    "Share via"
                                )
                                context.startActivity(chooser)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = ConciergeSurfaceContainerLow,
                    border = BorderStroke(1.dp, ConciergeOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp",
                            tint = ConciergeVegGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergeOnSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Share Option 2: Society Buzz (D2: Disabled, "Coming soon")
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("publish_success_share_society_buzz"),
                    shape = RoundedCornerShape(12.dp),
                    color = ConciergeSurfaceContainerLowest,
                    border = BorderStroke(1.dp, ConciergeOutlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Society Buzz",
                            tint = ConciergeOutlineVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Buzz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergeOutlineVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Coming soon",
                            fontSize = 9.sp,
                            color = ConciergeOutlineVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Share Option 3: Copy Link (D3: Copies share text to clipboard)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("publish_success_share_copy_link")
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("ConnectKar Business", shareText)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Listing details copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = ConciergeSurfaceContainerLow,
                    border = BorderStroke(1.dp, ConciergeOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = ConciergePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Copy Text",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergeOnSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 7. View Listing Primary Button
            Button(
                onClick = onViewListing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConciergePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_success_view_listing_button")
            ) {
                Text(
                    text = "View Listing",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 8. Done Text Button (Dismisses to home_business_list)
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("publish_success_done_button")
            ) {
                Text(
                    text = "Done",
                    color = ConciergeOnSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
}
