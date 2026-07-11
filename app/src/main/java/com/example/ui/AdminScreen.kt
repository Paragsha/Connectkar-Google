package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    users: List<UserEntity>,
    onApproveUser: (Int) -> Unit,
    onRejectUser: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Pending Verification, 1 = Verified Residents

    val pendingUsers = users.filter { it.isPending }
    val verifiedUsers = users.filter { it.isVerified && !it.isPending }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Township Admin Desk", fontWeight = FontWeight.Bold, color = BrandSlate) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandSlate)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        containerColor = BrandBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BrandGoldLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pending Review", fontSize = 12.sp, color = Color(0xFFD48800), fontWeight = FontWeight.Medium)
                        Text("${pendingUsers.size} Users", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD48800))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BrandEmeraldLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Residents", fontSize = 12.sp, color = BrandEmerald, fontWeight = FontWeight.Medium)
                        Text("${verifiedUsers.size} Verified", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                    }
                }
            }

            // Tab Rows
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                divider = { Divider(color = Color(0xFFE2E8F0)) },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = if (activeTab == 0) BrandGold else BrandEmerald
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pending Verification", fontWeight = FontWeight.Bold)
                            if (pendingUsers.isNotEmpty()) {
                                Badge(
                                    containerColor = BrandGold,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text("${pendingUsers.size}", color = Color.White)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Verified Residents", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                0 -> {
                    if (pendingUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("All caught up!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandSlate)
                                Text("No pending verification requests.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingUsers, key = { it.id }) { user ->
                                AdminUserRequestCard(
                                    user = user,
                                    onApprove = { onApproveUser(user.id) },
                                    onReject = { onRejectUser(user.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(30.dp)) }
                        }
                    }
                }
                1 -> {
                    if (verifiedUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No verified residents yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandSlate)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(verifiedUsers, key = { it.id }) { user ->
                                VerifiedResidentCard(user = user, onRevoke = { onRejectUser(user.id) })
                            }
                            item { Spacer(modifier = Modifier.height(30.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserRequestCard(
    user: UserEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BrandOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(avatarIndex = user.avatarIndex, size = 48)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandSlate)
                    Text("Phone: ${user.phoneNumber}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address Info Details Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SOCIETY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(user.society, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandSlate)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("BLOCK / FLAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("${user.blockTower} / ${user.flatNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandSlate)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Approve & Reject Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reject_user_button_${user.id}"),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("approve_user_button_${user.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Approve Resident", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VerifiedResidentCard(
    user: UserEntity,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BrandOutline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(avatarIndex = user.avatarIndex, size = 36)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)
                Text("${user.society} • ${user.blockTower}, ${user.flatNumber}", fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = onRevoke) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Revoke Verification", tint = Color.Red)
            }
        }
    }
}
