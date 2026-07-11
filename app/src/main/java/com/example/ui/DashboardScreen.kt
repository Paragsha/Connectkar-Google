package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity

data class ModuleItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tintColor: Color,
    val bgGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserEntity,
    selectedSociety: String,
    syncState: SyncState,
    onSocietySelected: (String) -> Unit,
    onModuleClicked: (String) -> Unit,
    onSimulateApprove: () -> Unit,
    onLogout: () -> Unit,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSocietyPicker by remember { mutableStateOf(false) }
    
    val societies = listOf("All Societies") + TownshipSocieties

    val modules = listOf(
        ModuleItem(
            id = "MARKETPLACE",
            title = "Marketplace",
            subtitle = "Buy & Sell Items",
            icon = Icons.Default.ShoppingCart,
            tintColor = Color(0xFF10B981),
            bgGradient = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
        ),
        ModuleItem(
            id = "FEED",
            title = "Community Feed",
            subtitle = "Events & Neighbors",
            icon = Icons.Default.Forum,
            tintColor = Color(0xFF3B82F6),
            bgGradient = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
        ),
        ModuleItem(
            id = "CARPOOL",
            title = "Carpooling",
            subtitle = "Share Daily Rides",
            icon = Icons.Default.DirectionsCar,
            tintColor = Color(0xFF8B5CF6),
            bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
        ),
        ModuleItem(
            id = "MEAL",
            title = "Daily Meals",
            subtitle = "Home Chef Menus",
            icon = Icons.Default.Restaurant,
            tintColor = Color(0xFFEF4444),
            bgGradient = listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2))
        ),
        ModuleItem(
            id = "SERVICE",
            title = "Resident Services",
            subtitle = "Electrician, Plumber",
            icon = Icons.Default.Build,
            tintColor = Color(0xFFF59E0B),
            bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
        ),
        ModuleItem(
            id = "PROPERTY",
            title = "Rentals & Property",
            subtitle = "Home Rent & Items",
            icon = Icons.Default.HomeWork,
            tintColor = Color(0xFF06B6D4),
            bgGradient = listOf(Color(0xFFECFEFF), Color(0xFFCFFAFE))
        ),
        ModuleItem(
            id = "VEHICLE",
            title = "Vehicles Log",
            subtitle = "Resident Cars & Spots",
            icon = Icons.Default.Garage,
            tintColor = Color(0xFF64748B),
            bgGradient = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
        ),
        ModuleItem(
            id = "ADMIN",
            title = "Admin Portal",
            subtitle = "Verify Residents",
            icon = Icons.Default.SupervisorAccount,
            tintColor = Color(0xFF1E293B),
            bgGradient = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        )
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ConnectKar",
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandSlate,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Township Super App",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Dropdown for Active Society Filtering
                    Box {
                        IconButton(
                            onClick = { showSocietyPicker = !showSocietyPicker },
                            modifier = Modifier.testTag("filter_society_dropdown")
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter Society", tint = BrandSlate)
                        }
                        
                        DropdownMenu(
                            expanded = showSocietyPicker,
                            onDismissRequest = { showSocietyPicker = false }
                        ) {
                            societies.forEach { society ->
                                DropdownMenuItem(
                                    text = { Text(society) },
                                    onClick = {
                                        onSocietySelected(society)
                                        showSocietyPicker = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (society == "All Societies") Icons.Default.Language else Icons.Default.Home,
                                            contentDescription = null,
                                            tint = if (selectedSociety == society) BrandEmerald else Color.Gray
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Logout Icon
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = BrandBackground)
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
            SyncStatusBanner(syncState = syncState, onRetrySync = onRetrySync)
            
            // Profile Overview Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BrandOutline)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(avatarIndex = currentUser.avatarIndex, size = 52)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BrandSlate
                        )
                        Text(
                            text = "${currentUser.blockTower}, Flat ${currentUser.flatNumber}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SocietyBadge(society = currentUser.society)
                            Spacer(modifier = Modifier.width(8.dp))
                            VerifiedBadge(isVerified = currentUser.isVerified, isPending = currentUser.isPending)
                        }
                    }
                }
            }

            // Pending Status Banner
            StatusBanner(user = currentUser, onSimulateApprove = onSimulateApprove)

            // Category Title with filter status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Township Modules",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BrandSlate
                )
                Text(
                    text = if (selectedSociety == "All Societies") "Showing: Entire Township" else "Showing: $selectedSociety",
                    color = BrandEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Core Modules Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
                    .padding(bottom = 16.dp)
            ) {
                items(modules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable { onModuleClicked(module.id) }
                            .testTag("module_card_${module.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BrandOutline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(module.bgGradient))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = module.icon,
                                        contentDescription = module.title,
                                        tint = module.tintColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = module.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BrandSlate
                                    )
                                    Text(
                                        text = module.subtitle,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
