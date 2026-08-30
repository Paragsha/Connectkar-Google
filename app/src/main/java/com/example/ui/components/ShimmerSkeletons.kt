package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Animated Shimmer Effect Modifier
 * Produces a sleek, continuous left-to-right highlight gradient sweep
 * for skeleton loading placeholder states.
 */
fun Modifier.shimmerEffect(
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF8FAFC)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -350f,
        targetValue = 1250f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, translateAnim),
            end = Offset(translateAnim + 350f, translateAnim + 350f)
        )
    )
}

/**
 * Reusable clipped container with applied shimmer gradient
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF8FAFC)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect(baseColor, highlightColor)
    )
}

/**
 * Shimmer Skeleton for "My Listings" cards
 */
@Composable
fun MyListingItemSkeleton(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Skeleton
            ShimmerBox(
                modifier = Modifier.size(86.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header Row (Title & Status Pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(18.dp)
                            .width(48.dp),
                        shape = RoundedCornerShape(9999.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price line
                ShimmerBox(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.35f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons skeleton
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape
                    )
                    ShimmerBox(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Meal Discover Dish Cards (horizontal carousel)
 */
@Composable
fun DishCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column {
            // Dish Image Skeleton
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(0.dp)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.65f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(36.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ShimmerBox(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        ShimmerBox(
                            modifier = Modifier
                                .height(10.dp)
                                .width(30.dp),
                            shape = RoundedCornerShape(3.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerBox(
                            modifier = Modifier
                                .height(16.dp)
                                .width(50.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    ShimmerBox(
                        modifier = Modifier
                            .height(34.dp)
                            .width(76.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Community Chef Cards (vertical list)
 */
@Composable
fun ChefSummaryCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(52.dp),
                shape = CircleShape
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(42.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ShimmerBox(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(48.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Property & Item Rental Cards
 */
@Composable
fun PropertyListingSkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(0.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(18.dp)
                            .fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(18.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(9999.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ShimmerBox(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.4f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(20.dp)
                            .width(80.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(36.dp)
                            .width(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Admin Verification / Resident Cards
 */
@Composable
fun AdminUserItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.35f),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address info block skeleton
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Shimmer Skeleton for Dashboard "Fresh Today" cards
 */
@Composable
fun FreshTodayCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(0.dp)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(36.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ShimmerBox(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.45f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(18.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(32.dp)
                            .width(72.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Incoming/Active Meal Orders (Chef Portal & My Meals)
 */
@Composable
fun ChefOrderCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(16.dp)
                        .width(90.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(20.dp)
                        .width(70.dp),
                    shape = RoundedCornerShape(9999.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ShimmerBox(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.7f),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ShimmerBox(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.5f),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(18.dp)
                        .width(80.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(36.dp)
                        .width(110.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

/**
 * Shimmer Skeleton for Chef Menu Items (Chef Portal & Public Profile)
 */
@Composable
fun ChefMenuItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(14.dp)
                            .width(36.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ShimmerBox(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(16.dp)
                            .width(55.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(24.dp)
                            .width(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Chef Public Profile Header
 */
@Composable
fun ChefHeroProfileSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShimmerBox(
                modifier = Modifier.size(76.dp),
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .height(20.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier
                    .height(14.dp)
                    .width(180.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            ShimmerBox(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerBox(
                            modifier = Modifier
                                .height(16.dp)
                                .width(36.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerBox(
                            modifier = Modifier
                                .height(12.dp)
                                .width(50.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shimmer Skeleton for Subscriptions (Chef Portal & My Meals)
 */
@Composable
fun ChefSubscriptionSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
        border = BorderStroke(1.dp, ConciergeOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(18.dp)
                        .width(110.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(20.dp)
                        .width(60.dp),
                    shape = RoundedCornerShape(9999.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ShimmerBox(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(18.dp)
                        .width(90.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(34.dp)
                        .width(90.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Shimmer Skeleton for Property Details Screen
 */
@Composable
fun PropertyDetailsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(0.dp)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(0.7f),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ShimmerBox(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.45f),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

