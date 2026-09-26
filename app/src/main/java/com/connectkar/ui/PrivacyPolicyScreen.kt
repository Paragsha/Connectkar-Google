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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    onNavigateToGrievanceOfficer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("privacy_policy_back_button")
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
            // Header Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ConnectKar Resident Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Commitment to data minimization, resident security & statutory compliance under IT Rules 2021 & DPDP Act.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Section 1: What is Collected
            PolicySectionCard(
                icon = Icons.Default.Info,
                title = "1. Information We Collect",
                tag = "policy_section_collection"
            ) {
                PolicyBulletItem(
                    title = "Resident Profile Data",
                    description = "Full name, mobile phone number, assigned society, tower/wing, flat/unit number, floor level, and resident type (Owner / Tenant)."
                )
                PolicyBulletItem(
                    title = "Proof of Residence Verification",
                    description = "Electricity bill, maintenance receipt, or tenancy agreement uploaded to establish genuine resident identity before admin approval."
                )
                PolicyBulletItem(
                    title = "User Generated Content (UGC)",
                    description = "Marketplace items, community feed discussions, vehicle pooling, and home business listings voluntarily created by residents."
                )
                PolicyBulletItem(
                    title = "Interactions & Content Reports",
                    description = "Bookmarks, likes, and content violation reports submitted for community safety."
                )
            }

            // Section 2: Why it is Collected
            PolicySectionCard(
                icon = Icons.Default.Security,
                title = "2. Why We Collect Your Data",
                tag = "policy_section_purpose"
            ) {
                PolicyBulletItem(
                    title = "Gated Community Security",
                    description = "To ensure only verified residents living within the specific residential society can access the private community hub."
                )
                PolicyBulletItem(
                    title = "Enabling Resident Services",
                    description = "To facilitate direct resident-to-resident marketplace trades, home-cooked meal ordering, and vehicle carpool coordination."
                )
                PolicyBulletItem(
                    title = "Safety & IT Rules Compliance",
                    description = "To prevent spam, fraudulent postings, harassment, or unauthorized access, and fulfill statutory content moderation obligations under IT Rules 2021."
                )
            }

            // Section 3: Data Rights & Request Process
            PolicySectionCard(
                icon = Icons.Default.Lock,
                title = "3. Data Rights & Request Process",
                tag = "policy_section_rights"
            ) {
                Text(
                    text = "Residents have full control over their personal data in accordance with digital privacy principles:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                PolicyBulletItem(
                    title = "Right to Access & Portability",
                    description = "Request a complete copy of your registered profile information and active listings at any time."
                )
                PolicyBulletItem(
                    title = "Right to Correction / Rectification",
                    description = "Update flat numbers, phone contacts, or inaccurate listing details directly in the app or via the support desk."
                )
                PolicyBulletItem(
                    title = "Right to Erasure / Account Deletion",
                    description = "Upon moving out or requesting account closure, residents can request immediate purge of proof documents, profile data, and listings."
                )
                PolicyBulletItem(
                    title = "Submitting a Request",
                    description = "Send your data access or erasure request to our Grievance Officer with your registered phone number and society unit."
                )
            }

            // Section 4: Grievance Officer Contact & Link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_policy_grievance_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Grievance Officer & Redressal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                    }

                    Text(
                        text = "For any grievances, content moderation disputes, or data protection inquiries, you can reach out directly to our statutory Grievance Officer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF166534),
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onNavigateToGrievanceOfficer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("privacy_policy_grievance_button")
                    ) {
                        Text(
                            text = "View Grievance Officer Details",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    title: String,
    tag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            content()
        }
    }
}

@Composable
private fun PolicyBulletItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )
    }
}
