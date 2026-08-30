package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.House
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ConciergeBrandNavy

/**
 * Friendly, minimal illustration for empty listing states (My Listings, Saved Properties, etc.)
 */
@Composable
fun NoListingsEmptyState(
    title: String,
    subtitle: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "empty_listings_state"
) {
    val context = LocalContext.current
    val drawableResId = rememberDrawableId(context, "img_no_listings")

    val brandBlue = Color(0xFF003FB1)
    val softBlueBg = Color(0xFFEFF4FF)
    val mutedGray = Color(0xFF64748B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (drawableResId != 0) {
            // Display generated PNG if available
            androidx.compose.foundation.Image(
                painter = painterResource(id = drawableResId),
                contentDescription = title,
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        } else {
            // Friendly minimal Compose illustration
            FriendlyNoListingsIllustration(
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ConciergeBrandNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = mutedGray,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun rememberDrawableId(context: android.content.Context, name: String): Int {
    return androidx.compose.runtime.remember(name) {
        try {
            context.resources.getIdentifier(name, "drawable", context.packageName)
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * Minimalist geometric illustration with gentle pastels: a cozy house silhouette,
 * soft rounded glow backdrop, and an inquisitive search lens.
 */
@Composable
fun FriendlyNoListingsIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Soft gradient backdrop circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2.1f

            // Ambient background halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0EAFF), Color(0xFFF1F5F9), Color.Transparent),
                    center = center,
                    radius = radius * 1.15f
                ),
                radius = radius,
                center = center
            )

            // Inner soft pill
            drawRoundRect(
                color = Color(0xFFDCE7FE),
                topLeft = Offset(size.width * 0.18f, size.height * 0.22f),
                size = Size(size.width * 0.64f, size.height * 0.60f),
                cornerRadius = CornerRadius(28f, 28f)
            )

            // Stylized Minimal House Outline
            val houseLeft = size.width * 0.28f
            val houseRight = size.width * 0.72f
            val roofPeak = Offset(size.width * 0.50f, size.height * 0.32f)
            val eavesY = size.height * 0.48f
            val floorY = size.height * 0.74f

            // House Roof Path
            val roofPath = Path().apply {
                moveTo(roofPeak.x, roofPeak.y)
                lineTo(houseRight, eavesY)
                lineTo(houseLeft, eavesY)
                close()
            }
            drawPath(
                path = roofPath,
                color = Color(0xFF003FB1)
            )

            // House Body
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(houseLeft + 8f, eavesY),
                size = Size(houseRight - houseLeft - 16f, floorY - eavesY),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // House Door / Window
            val doorWidth = 24f
            val doorHeight = 36f
            drawRoundRect(
                color = Color(0xFF93C5FD),
                topLeft = Offset((size.width - doorWidth) / 2f, floorY - doorHeight),
                size = Size(doorWidth, doorHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Little chimney
            drawRoundRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(houseLeft + 14f, size.height * 0.34f),
                size = Size(16f, 24f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Subtle Magnifying Glass / Discovery Ring
            val lensCenter = Offset(size.width * 0.70f, size.height * 0.64f)
            val lensRadius = size.width * 0.16f

            drawCircle(
                color = Color.White,
                radius = lensRadius,
                center = lensCenter
            )
            drawCircle(
                color = ConciergeBrandNavy,
                radius = lensRadius,
                center = lensCenter,
                style = Stroke(width = 6f)
            )
            // Lens handle
            drawLine(
                color = ConciergeBrandNavy,
                start = Offset(lensCenter.x + lensRadius * 0.7f, lensCenter.y + lensRadius * 0.7f),
                end = Offset(lensCenter.x + lensRadius * 1.35f, lensCenter.y + lensRadius * 1.35f),
                strokeWidth = 7f
            )

            // Small discovery sparkle in lens
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 5f,
                center = Offset(lensCenter.x - 6f, lensCenter.y - 6f)
            )
        }
    }
}
