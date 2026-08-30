package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ListingEntity
import com.example.data.local.photoUrls
import com.example.data.local.propertyDetails
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(
    property: ListingEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val specs = getPropertySpecs(property)
    val details = property.propertyDetails()

    val brandNavy = Color(0xFF001A40)
    val brandTeal = Color(0xFF007A5A)
    val brandOrange = ConciergeStatusAmber
    val lightBg = Color(0xFFF8FAFC)
    val chipBg = Color(0xFFEFF4FF)

    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx"
    val photoList = property.photoUrls().ifEmpty { listOf(fallbackImage) }
    val pagerState = rememberPagerState(pageCount = { photoList.size })

    val rawAmenities = details.amenities.filter { it.isNotBlank() }
    val amenitiesList = if (rawAmenities.isNotEmpty()) {
        rawAmenities
    } else {
        listOf("Gymnasium", "Swimming Pool", "Reserved Parking", "Power Backup", "Clubhouse", "24/7 Security")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Property Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = brandNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("property_details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, property.title)
                                putExtra(Intent.EXTRA_TEXT, "${property.title} in ${property.society} for ₹${String.format("%,.0f", property.price)}/mo on ConnectKar")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Property"))
                        },
                        modifier = Modifier.testTag("property_details_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = brandNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${String.format("%,.0f", property.price)}/mo",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = brandNavy
                        )
                    }

                    Button(
                        onClick = {
                            val contactNumber = property.contact.ifEmpty { "9876543210" }
                            Toast.makeText(context, "Calling owner: $contactNumber", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003FB1)),
                        shape = RoundedCornerShape(9999.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("contact_owner_sticky_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contact Owner",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color.White,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = photoList[page],
                        contentDescription = "${property.title} photo ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (photoList.size > 1) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${photoList.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overlaid Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val showVerifiedBadge = property.extra4 == "VERIFIED_OWNER" || property.extra4 == "SOCIETY_APPROVED"
                    if (showVerifiedBadge) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(brandTeal)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
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
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (property.extra4 == "SOCIETY_APPROVED") {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(brandOrange)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
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

            // Main Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Flat / Wing location
                val flatLocation = details.wingFlatNumber.ifEmpty { property.authorFlat.ifEmpty { "Wing A, Flat 304" } }
                Text(
                    text = flatLocation.uppercase(),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Title
                Text(
                    text = property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = brandNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Price line
                Text(
                    text = "₹${String.format("%,.0f", property.price)} / month",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF003FB1),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 3 Spec Cards (Beds, Baths, Sqft)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecBox(
                        icon = Icons.Default.Bed,
                        label = "${specs.first} ${if (specs.first == 1) "Bed" else "Beds"}",
                        modifier = Modifier.weight(1f),
                        chipBg = chipBg,
                        brandNavy = brandNavy
                    )
                    SpecBox(
                        icon = Icons.Default.Bathtub,
                        label = "${specs.second} ${if (specs.second == 1) "Bath" else "Baths"}",
                        modifier = Modifier.weight(1f),
                        chipBg = chipBg,
                        brandNavy = brandNavy
                    )
                    SpecBox(
                        icon = Icons.Default.Straighten,
                        label = "${specs.third} sqft",
                        modifier = Modifier.weight(1f),
                        chipBg = chipBg,
                        brandNavy = brandNavy
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(bottom = 20.dp))

                // About this property
                Text(
                    text = "About this property",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = brandNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val descriptionText = property.description.ifEmpty {
                    "Experience premium living in this meticulously maintained apartment in the heart of the society. Features ample natural lighting, cross-ventilation, and modern fittings."
                }
                Text(
                    text = descriptionText,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(bottom = 20.dp))

                // Amenities
                Text(
                    text = "Amenities",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = brandNavy,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    val chunks = amenitiesList.chunked(2)
                    for (chunk in chunks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (item in chunk) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = getAmenityIcon(item),
                                        contentDescription = null,
                                        tint = Color(0xFF003FB1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item,
                                        fontSize = 13.sp,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(bottom = 20.dp))

                // Posted By / Landlord card
                Text(
                    text = "Listed by",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = brandNavy,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = lightBg,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF003FB1),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = property.authorName.ifEmpty { "Resident Owner" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = brandNavy
                                )
                                Text(
                                    text = "Verified Resident · ${property.society.ifEmpty { "Sylvan County" }}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val contactNumber = property.contact.ifEmpty { "9876543210" }
                                    Toast.makeText(context, "Calling owner: $contactNumber", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF003FB1)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF003FB1),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Contact", color = Color(0xFF003FB1), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "In-app messaging isn't available yet — v1 uses phone contact only", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0057B7)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(bottom = 20.dp))

                // Location / Map section
                Text(
                    text = property.society.ifEmpty { "Society Location" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = brandNavy,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Static Map Placeholder Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable {
                            Toast.makeText(context, "Society: ${property.society.ifEmpty { "ConnectKar Community" }}", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("static_map_placeholder")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ConciergeNonVegRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Map Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = property.society.ifEmpty { "Society Campus" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = brandNavy
                            )
                            Text(
                                text = "Interactive map view placeholder",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun SpecBox(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    chipBg: Color,
    brandNavy: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = chipBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF003FB1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = brandNavy
            )
        }
    }
}

private fun getAmenityIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("gym") || lower.contains("fitness") -> Icons.Default.FitnessCenter
        lower.contains("pool") || lower.contains("swim") -> Icons.Default.Pool
        lower.contains("park") || lower.contains("parking") -> Icons.Default.LocalParking
        lower.contains("power") || lower.contains("backup") || lower.contains("electric") -> Icons.Default.ElectricBolt
        lower.contains("club") -> Icons.Default.Apartment
        lower.contains("security") || lower.contains("guard") -> Icons.Default.Security
        lower.contains("lift") || lower.contains("elevator") -> Icons.Default.Elevator
        lower.contains("garden") -> Icons.Default.Park
        lower.contains("water") -> Icons.Default.WaterDrop
        lower.contains("wifi") || lower.contains("internet") -> Icons.Default.Wifi
        else -> Icons.Default.CheckCircle
    }
}
