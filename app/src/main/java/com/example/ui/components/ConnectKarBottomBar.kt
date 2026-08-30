package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ConciergeBrandNavy

@Composable
fun ConnectKarBottomBar(
    activeTab: String, // "home", "explore", "create", "society", "profile"
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF003FB1)
    val inactiveGray = Color.Gray

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home Tab
            val isHome = activeTab == "home"
            IconButton(
                onClick = { onTabSelected("home") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (isHome) activeBlue else inactiveGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Home",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHome) activeBlue else inactiveGray
                    )
                }
            }

            // Explore Tab
            val isExplore = activeTab == "explore"
            IconButton(
                onClick = { onTabSelected("explore") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isExplore) Icons.Filled.Explore else Icons.Outlined.Explore,
                        contentDescription = "Explore",
                        tint = if (isExplore) activeBlue else inactiveGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Explore",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExplore) activeBlue else inactiveGray
                    )
                }
            }

            // Create (+) Center Pillar
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(ConciergeBrandNavy)
                        .clickable { onTabSelected("create") }
                        .testTag("create_pillar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Posting",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Society Tab
            val isSociety = activeTab == "society"
            IconButton(
                onClick = { onTabSelected("society") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isSociety) Icons.Filled.Groups else Icons.Outlined.Groups,
                        contentDescription = "Society",
                        tint = if (isSociety) activeBlue else inactiveGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Society",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSociety) activeBlue else inactiveGray
                    )
                }
            }

            // Profile Tab
            val isProfile = activeTab == "profile"
            IconButton(
                onClick = { onTabSelected("profile") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = if (isProfile) activeBlue else inactiveGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Profile",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfile) activeBlue else inactiveGray
                    )
                }
            }
        }
    }
}
