package com.example.ui.mealhub

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MenuItemEntity
import com.example.data.local.UserEntity
import com.example.ui.BrandGold
import com.example.ui.BrandIndigo
import com.example.ui.BrandIndigoLight
import com.example.ui.OperationsUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCheckoutScreen(
    menuItem: MenuItemEntity,
    currentUser: UserEntity,
    operationsState: OperationsUiState,
    onPlaceOrder: (
        servingSize: Int,
        deliveryWindow: String,
        dietaryNotes: String,
        deliveryMethod: String,
        addOns: String,
        itemTotal: Double,
        addOnsTotal: Double,
        deliveryFee: Double,
        grandTotal: Double
    ) -> Unit,
    onBackClicked: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val context = LocalContext.current
    var portionCount by remember { mutableIntStateOf(1) }
    var selectedWindow by remember { mutableStateOf(menuItem.deliveryWindow.ifEmpty { "12:30 PM - 1:30 PM" }) }
    var dietaryNotes by remember { mutableStateOf("") }
    var deliveryMethod by remember { mutableStateOf("DOORSTEP") } // "DOORSTEP" or "PICKUP"
    
    // Addons
    var addPhulkas by remember { mutableStateOf(false) }
    var addDessert by remember { mutableStateOf(false) }
    var addRaita by remember { mutableStateOf(false) }

    val itemTotal = menuItem.price * portionCount
    val addOnsTotal = (if (addPhulkas) 20.0 else 0.0) + (if (addDessert) 40.0 else 0.0) + (if (addRaita) 25.0 else 0.0)
    val deliveryFee = if (deliveryMethod == "DOORSTEP") 20.0 else 0.0
    val grandTotal = itemTotal + addOnsTotal + deliveryFee

    LaunchedEffect(operationsState) {
        if (operationsState is OperationsUiState.Success) {
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
            onOrderSuccess()
        } else if (operationsState is OperationsUiState.Error) {
            Toast.makeText(context, operationsState.message, Toast.LENGTH_LONG).show()
        }
    }

    val windows = listOf("12:30 PM - 1:30 PM", "1:30 PM - 2:30 PM", "7:30 PM - 8:30 PM")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meal Checkout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ConciergeOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("checkout_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConciergeOnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConciergeBackground)
            )
        },
        bottomBar = {
            Surface(
                color = ConciergeBackground,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL TO PAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ConciergeOutline
                        )
                        Text(
                            text = "₹${grandTotal.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ConciergeOnBackground
                        )
                    }

                    Button(
                        onClick = {
                            val addOnsList = mutableListOf<String>()
                            if (addPhulkas) addOnsList.add("Extra Phulkas (₹20)")
                            if (addDessert) addOnsList.add("Dessert (₹40)")
                            if (addRaita) addOnsList.add("Fresh Raita (₹25)")

                            onPlaceOrder(
                                portionCount,
                                selectedWindow,
                                dietaryNotes,
                                if (deliveryMethod == "DOORSTEP") "Doorstep Delivery" else "Self-Pickup",
                                addOnsList.joinToString(", "),
                                itemTotal,
                                addOnsTotal,
                                deliveryFee,
                                grandTotal
                            )
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("place_order_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConciergePrimaryContainer,
                            contentColor = Color.White
                        ),
                        enabled = operationsState !is OperationsUiState.Loading
                    ) {
                        if (operationsState is OperationsUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm & Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = ConciergeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dish Summary Card
            Card(
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
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        if (menuItem.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = menuItem.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(if (menuItem.isVeg) "🥗" else "🍗", fontSize = 28.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (menuItem.isVeg) ConciergeVegGreen else ConciergeNonVegRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = menuItem.dishName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ConciergeOnBackground
                            )
                        }
                        Text(
                            text = "by ${menuItem.chefName} (${menuItem.chefFlat})",
                            fontSize = 12.sp,
                            color = ConciergeOutline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "₹${menuItem.price.toInt()} per portion",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ConciergePrimaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 2. Portion Counter
            val portionsLeft = maxOf(0, menuItem.portionsAvailable - menuItem.portionsBooked)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Number of Portions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ConciergeOnBackground
                        )
                        Text(
                            text = "$portionsLeft portions available today",
                            fontSize = 11.sp,
                            color = ConciergeOutline
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledIconButton(
                            onClick = { if (portionCount > 1) portionCount-- },
                            enabled = portionCount > 1,
                            modifier = Modifier.size(36.dp).testTag("portion_minus"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = ConciergeBackground
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ConciergeOnBackground)
                        }

                        Text(
                            text = "$portionCount",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = ConciergeOnBackground,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        FilledIconButton(
                            onClick = { if (portionCount < portionsLeft) portionCount++ },
                            enabled = portionCount < portionsLeft,
                            modifier = Modifier.size(36.dp).testTag("portion_plus"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = ConciergePrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }
            }

            // 3. Delivery Method Toggle
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delivery Option",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { deliveryMethod = "DOORSTEP" }
                                .testTag("delivery_doorstep"),
                            color = if (deliveryMethod == "DOORSTEP") BrandIndigoLight else ConciergeBackground,
                            border = BorderStroke(1.dp, if (deliveryMethod == "DOORSTEP") BrandIndigo else ConciergeOutlineVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Doorstep Delivery", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ConciergeOnBackground)
                                Text("Delivered to ${currentUser.blockTower} ${currentUser.flatNumber} (+₹20)", fontSize = 10.sp, color = ConciergeOutline)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { deliveryMethod = "PICKUP" }
                                .testTag("delivery_pickup"),
                            color = if (deliveryMethod == "PICKUP") BrandIndigoLight else ConciergeBackground,
                            border = BorderStroke(1.dp, if (deliveryMethod == "PICKUP") BrandIndigo else ConciergeOutlineVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Self Pickup", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ConciergeOnBackground)
                                Text("Pick up from Chef's flat (FREE)", fontSize = 10.sp, color = ConciergeOutline)
                            }
                        }
                    }
                }
            }

            // 4. Delivery Window
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Delivery Slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        windows.forEach { window ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedWindow = window }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedWindow == window,
                                    onClick = { selectedWindow = window },
                                    colors = RadioButtonDefaults.colors(selectedColor = ConciergePrimaryContainer)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = window,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedWindow == window) FontWeight.Bold else FontWeight.Normal,
                                    color = ConciergeOnBackground
                                )
                            }
                        }
                    }
                }
            }

            // 5. Addons
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Popular Chef Add-ons",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2x Pure Ghee Phulkas (+₹20)", fontSize = 13.sp, color = ConciergeOnBackground)
                        Checkbox(checked = addPhulkas, onCheckedChange = { addPhulkas = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fresh Mint Boondi Raita (+₹25)", fontSize = 13.sp, color = ConciergeOnBackground)
                        Checkbox(checked = addRaita, onCheckedChange = { addRaita = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Warm Gulab Jamun [2 pcs] (+₹40)", fontSize = 13.sp, color = ConciergeOnBackground)
                        Checkbox(checked = addDessert, onCheckedChange = { addDessert = it })
                    }
                }
            }

            // 6. Dietary Notes
            OutlinedTextField(
                value = dietaryNotes,
                onValueChange = { dietaryNotes = it },
                label = { Text("Dietary Preferences / Spice Level Note") },
                placeholder = { Text("e.g. Mild spice, less oil, no coriander") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dietary_notes_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConciergePrimaryContainer,
                    unfocusedBorderColor = ConciergeOutlineVariant,
                    focusedContainerColor = ConciergeSurfaceContainerLow,
                    unfocusedContainerColor = ConciergeSurfaceContainerLow
                )
            )

            // 7. Bill Details Breakdown Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ConciergeSurfaceContainerLow),
                border = BorderStroke(1.dp, ConciergeOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bill Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ConciergeOnBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Item Total ($portionCount portions)", fontSize = 13.sp, color = ConciergeOutline)
                        Text("₹${itemTotal.toInt()}", fontSize = 13.sp, color = ConciergeOnBackground)
                    }
                    if (addOnsTotal > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chef Add-ons", fontSize = 13.sp, color = ConciergeOutline)
                            Text("₹${addOnsTotal.toInt()}", fontSize = 13.sp, color = ConciergeOnBackground)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Society Delivery Fee", fontSize = 13.sp, color = ConciergeOutline)
                        Text(if (deliveryFee > 0) "₹${deliveryFee.toInt()}" else "FREE", fontSize = 13.sp, color = ConciergeOnBackground)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ConciergeOutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ConciergeOnBackground)
                        Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ConciergePrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
