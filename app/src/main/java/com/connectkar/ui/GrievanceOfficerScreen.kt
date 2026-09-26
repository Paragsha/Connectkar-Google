package com.connectkar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectkar.ui.theme.*

// Compliance Placeholders - literal placeholders per IT Rules 2021 specification
const val CONTACT_NAME_PLACEHOLDER = "CONTACT_NAME_PLACEHOLDER"
const val CONTACT_EMAIL_PLACEHOLDER = "CONTACT_EMAIL_PLACEHOLDER"
const val CONTACT_PHONE_PLACEHOLDER = "CONTACT_PHONE_PLACEHOLDER"
const val CONTACT_ADDRESS_PLACEHOLDER = "CONTACT_ADDRESS_PLACEHOLDER"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrievanceOfficerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("grievance_officer_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Grievance Officer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("grievance_officer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statutory Notice Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grievance_statutory_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "IT Rules, 2021 Compliance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "In accordance with the Information Technology (Intermediary Guidelines and Digital Media Ethics Code) Rules, 2021, ConnectKar has appointed a designated Grievance Officer for resident complaint redressal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E40AF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Contact Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grievance_contact_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Designated Grievance Officer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    GrievanceInfoRow(
                        icon = Icons.Default.Person,
                        label = "Officer Name",
                        value = CONTACT_NAME_PLACEHOLDER,
                        tag = "grievance_contact_name"
                    )

                    GrievanceInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = CONTACT_EMAIL_PLACEHOLDER,
                        tag = "grievance_contact_email"
                    )

                    GrievanceInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Contact Phone",
                        value = CONTACT_PHONE_PLACEHOLDER,
                        tag = "grievance_contact_phone"
                    )

                    GrievanceInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Physical Postal Address",
                        value = CONTACT_ADDRESS_PLACEHOLDER,
                        tag = "grievance_contact_address"
                    )
                }
            }

            // Statutory Response Timelines Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grievance_timelines_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Statutory Response Timelines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    TimelineItem(
                        timeBadge = "24 Hours",
                        badgeColor = Color(0xFF3B82F6),
                        title = "Acknowledgment Receipt",
                        description = "We acknowledge receipt of any grievance or complaint within 24 hours of submission."
                    )

                    TimelineItem(
                        timeBadge = "36 Hours",
                        badgeColor = Color(0xFFDC2626),
                        title = "Action on Flagged / Objectionable Content",
                        description = "Removal or disabling of access to content violating IT Rules or containing prohibited material within 36 hours of receipt."
                    )

                    TimelineItem(
                        timeBadge = "15 Days",
                        badgeColor = Color(0xFF059669),
                        title = "General Grievance Resolution",
                        description = "Complete disposal and resolution of all other resident complaints and appeals within 15 days."
                    )
                }
            }

            // Grievance Filing Procedure Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grievance_procedure_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "How to File a Grievance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "1. For objectionable posts or listings, use the 'Report Content' option on the item.\n2. For privacy, data, or platform concerns, write to the Grievance Officer with:\n   • Your registered township & unit number\n   • Clear description of the grievance\n   • Relevant screenshots or listing IDs\n   • Reference to applicable rule violation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GrievanceInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun TimelineItem(
    timeBadge: String,
    badgeColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
        ) {
            Text(
                text = timeBadge,
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                lineHeight = 17.sp
            )
        }
    }
}
