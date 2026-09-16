package com.connectkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.repository.TownshipRepository
import com.connectkar.ui.*
import com.connectkar.ui.theme.MyApplicationTheme

import com.connectkar.ui.mealhub.ChefOnboardingScreen
import com.connectkar.ui.mealhub.ChefPortalDashboardScreen
import com.connectkar.ui.mealhub.ChefPublicProfileScreen
import com.connectkar.ui.mealhub.MealCheckoutScreen
import com.connectkar.ui.mealhub.MealDiscoverScreen
import com.connectkar.ui.mealhub.MyMealsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get centralized repository from application context
        val app = application as? ConnectKarApplication ?: (applicationContext as ConnectKarApplication)
        val repository = app.repository
        
        setContent {
            MyApplicationTheme {
                val viewModel: TownshipViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = TownshipViewModel.Factory(repository)
                )

                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                val filteredListings by viewModel.filteredListings.collectAsStateWithLifecycle()
                val exploreListings by viewModel.exploreListings.collectAsStateWithLifecycle()
                val selectedSociety by viewModel.selectedSociety.collectAsStateWithLifecycle()
                val isExploreMode by viewModel.isExploreMode.collectAsStateWithLifecycle()
                val exploredSocieties by viewModel.exploredSocieties.collectAsStateWithLifecycle()
                val syncState by viewModel.syncState.collectAsStateWithLifecycle()
                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                val mealListings by viewModel.mealListingsForSociety.collectAsStateWithLifecycle()
                val operationsState by viewModel.operationsState.collectAsStateWithLifecycle()

                // MealHub state
                val currentChefProfile by viewModel.currentChefProfile.collectAsStateWithLifecycle()
                val allChefs by viewModel.allChefsForSociety.collectAsStateWithLifecycle()
                val menuItems by viewModel.menuItemsForSociety.collectAsStateWithLifecycle()
                val myOrders by viewModel.myMealOrders.collectAsStateWithLifecycle()
                val chefOrders by viewModel.chefIncomingOrders.collectAsStateWithLifecycle()
                val chefMenu by viewModel.chefMenuItems.collectAsStateWithLifecycle()
                val mySubs by viewModel.myMealSubscriptions.collectAsStateWithLifecycle()

                val navController = rememberNavController()

                // Automatically navigate based on user session status safely
                LaunchedEffect(currentUser) {
                    try {
                        if (currentUser != null) {
                            if (navController.currentDestination?.route != "dashboard") {
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        } else {
                            if (navController.currentDestination?.route != null && navController.currentDestination?.route != "onboarding") {
                                navController.navigate("onboarding") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Navigation error: ${e.message}")
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "onboarding"
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onRegisterSuccess = { fullName, phone, society, block, flat, avatar, floor, residentType, moveInDate, proofDocumentUri ->
                                    viewModel.register(fullName, phone, society, block, flat, avatar, floor, residentType, moveInDate, proofDocumentUri)
                                }
                            )
                        }

                        composable("dashboard") {
                            val user = currentUser
                            if (user != null) {
                                DashboardScreen(
                                    currentUser = user,
                                    selectedSociety = selectedSociety,
                                    syncState = syncState,
                                    isRefreshing = isRefreshing,
                                    mealListings = mealListings,
                                    isExploreMode = isExploreMode,
                                    exploredSocieties = exploredSocieties,
                                    onEnterExploreMode = { viewModel.enterExploreMode(it) },
                                    onExitExploreMode = { viewModel.exitExploreMode() },
                                    onSocietySelected = { viewModel.selectSociety(it) },
                                    onModuleClicked = { moduleId ->
                                        if (moduleId == "ADMIN") {
                                            navController.navigate("admin")
                                        } else if (moduleId == "CREATE_HUB") {
                                            navController.navigate("create_hub")
                                        } else if (moduleId == "MEAL") {
                                            navController.navigate("meal_discover")
                                        } else if (moduleId == "RENTALS_HUB") {
                                            navController.navigate("rentals_hub")
                                        } else {
                                            viewModel.setActiveModule(moduleId)
                                            navController.navigate("module_list/$moduleId")
                                        }
                                    },
                                    onSimulateApprove = {
                                        viewModel.simulateAdminVerificationOfCurrentUser()
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    },
                                    onRetrySync = {
                                        viewModel.triggerSync()
                                    },
                                    onRefresh = {
                                        viewModel.refresh()
                                    }
                                )
                            }
                        }

                        // --- MealHub Screens ---
                        composable("meal_discover") {
                            val user = currentUser
                            if (user != null) {
                                MealDiscoverScreen(
                                    currentUser = user,
                                    selectedSociety = selectedSociety,
                                    syncState = syncState,
                                    menuItems = menuItems,
                                    chefs = allChefs,
                                    currentChefProfile = currentChefProfile,
                                    isRefreshing = isRefreshing,
                                    onDishClicked = { dish ->
                                        navController.navigate("meal_checkout/${dish.id}")
                                    },
                                    onChefProfileClicked = { chefUid ->
                                        navController.navigate("chef_profile/$chefUid")
                                    },
                                    onChefPortalClicked = {
                                        navController.navigate("chef_portal")
                                    },
                                    onChefOnboardingClicked = {
                                        navController.navigate("chef_onboarding")
                                    },
                                    onMyMealsClicked = {
                                        navController.navigate("my_meals")
                                    },
                                    onBackClicked = {
                                        navController.popBackStack()
                                    },
                                    onRetrySync = {
                                        viewModel.triggerSync()
                                    },
                                    onRefresh = {
                                        viewModel.refresh()
                                    }
                                )
                            }
                        }

                        composable("chef_onboarding") {
                            val user = currentUser
                            if (user != null) {
                                ChefOnboardingScreen(
                                    currentUser = user,
                                    operationsState = operationsState,
                                    onSubmitChef = { story, dish, price, port, isVeg, tag, photo ->
                                        viewModel.becomeChef(story, dish, price, port, isVeg, tag, photo) {
                                            navController.navigate("chef_portal") {
                                                popUpTo("meal_discover") { inclusive = false }
                                            }
                                        }
                                    },
                                    onBackClicked = { navController.popBackStack() },
                                    onFinish = {
                                        viewModel.resetOperationsState()
                                    }
                                )
                            }
                        }

                        composable("chef_portal") {
                            val user = currentUser
                            if (user != null) {
                                ChefPortalDashboardScreen(
                                    currentUser = user,
                                    chefProfile = currentChefProfile,
                                    menuItems = chefMenu,
                                    incomingOrders = chefOrders,
                                    subscriptions = mySubs,
                                    onToggleSoldOut = { id, soldOut ->
                                        viewModel.toggleMenuItemSoldOut(id, soldOut)
                                    },
                                    onUpdateOrderStatus = { id, st ->
                                        viewModel.updateOrderStatus(id, st)
                                    },
                                    onAddNewDish = { name, desc, price, port, veg, tag, type, time, photo ->
                                        viewModel.addMenuItem(name, desc, price, port, veg, tag, type, time, photo)
                                    },
                                    onBackClicked = { navController.popBackStack() }
                                )
                            }
                        }

                        composable(
                            route = "chef_profile/{chefUid}",
                            arguments = listOf(navArgument("chefUid") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val chefUid = backStackEntry.arguments?.getString("chefUid") ?: ""
                            val user = currentUser
                            val targetChefProfile by viewModel.getChefProfile(chefUid).collectAsStateWithLifecycle(null)
                            val targetChefMenuItems by viewModel.getMenuItemsForChef(chefUid).collectAsStateWithLifecycle(emptyList())

                            ChefPublicProfileScreen(
                                chefUid = chefUid,
                                chefProfile = targetChefProfile,
                                menuItems = targetChefMenuItems,
                                currentUser = user,
                                onDishClicked = { dish ->
                                    navController.navigate("meal_checkout/${dish.id}")
                                },
                                onSubscribeClicked = { plan, meals, disc, price ->
                                    viewModel.subscribeToChef(
                                        chefUid = chefUid,
                                        chefName = if (chefUid == "chef_priya") "Priya Sharma" else "Home Chef",
                                        planType = plan,
                                        mealsPerCycle = meals,
                                        discountPercent = disc,
                                        pricePerMeal = price
                                    ) {
                                        navController.navigate("my_meals")
                                    }
                                },
                                onBackClicked = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "meal_checkout/{menuItemId}",
                            arguments = listOf(navArgument("menuItemId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val menuItemId = backStackEntry.arguments?.getInt("menuItemId") ?: 0
                            val user = currentUser
                            val itemFlow = remember(menuItemId) { viewModel.getMenuItemById(menuItemId) }
                            val targetItem by itemFlow.collectAsStateWithLifecycle(null)

                            if (user != null && targetItem != null) {
                                MealCheckoutScreen(
                                    menuItem = targetItem!!,
                                    currentUser = user,
                                    operationsState = operationsState,
                                    onPlaceOrder = { serv, window, notes, method, addons, itemTot, addTot, delFee, grandTot ->
                                        viewModel.placeMealOrder(
                                            menuItem = targetItem!!,
                                            servingSize = serv,
                                            deliveryWindow = window,
                                            dietaryNotes = notes,
                                            deliveryMethod = method,
                                            addOns = addons,
                                            itemTotal = itemTot,
                                            addOnsTotal = addTot,
                                            deliveryFee = delFee,
                                            grandTotal = grandTot
                                        ) {
                                            viewModel.resetOperationsState()
                                            navController.navigate("my_meals") {
                                                popUpTo("meal_discover") { inclusive = false }
                                            }
                                        }
                                    },
                                    onBackClicked = { navController.popBackStack() },
                                    onOrderSuccess = {
                                        viewModel.resetOperationsState()
                                    }
                                )
                            }
                        }

                        composable("my_meals") {
                            val user = currentUser
                            if (user != null) {
                                MyMealsScreen(
                                    currentUser = user,
                                    orders = myOrders,
                                    subscriptions = mySubs,
                                    onToggleSubscription = { id, st ->
                                        viewModel.toggleSubscriptionStatus(id, st)
                                    },
                                    onBackClicked = { navController.popBackStack() }
                                )
                            }
                        }

                        composable("create_hub") {
                            val user = currentUser
                            if (user != null) {
                                val createHubViewModel: CreateHubViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                    factory = CreateHubViewModel.Factory(repository, application)
                                )
                                CreateHubScreen(
                                    currentUser = user,
                                    viewModel = createHubViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCreateFlow = { categoryId ->
                                        viewModel.setActiveModule(categoryId)
                                        navController.navigate("create_listing/$categoryId") {
                                            // Pop up to dashboard so that when they finish creating and pop, they return to dashboard instead of create_hub
                                            popUpTo("dashboard") { inclusive = false }
                                        }
                                    },
                                    activeTab = "create",
                                    onBottomNavClick = { target ->
                                        when (target) {
                                            "home" -> navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = true }
                                            }
                                            "explore" -> {
                                                viewModel.setActiveModule("EXPLORE")
                                                navController.navigate("module_list/EXPLORE")
                                            }
                                            "society" -> navController.navigate("admin")
                                            "profile" -> navController.navigate("my_listings")
                                        }
                                    }
                                )
                            }
                        }

                        composable("rentals_hub") {
                            val user = currentUser
                            val categoryCounts by viewModel.rentalsCategoryCounts.collectAsStateWithLifecycle()
                            val recentListings by viewModel.rentalsRecentListings.collectAsStateWithLifecycle()
                            if (user != null) {
                                RentalsHubScreen(
                                    selectedSociety = selectedSociety,
                                    categoryCounts = categoryCounts,
                                    recentListings = recentListings,
                                    onBack = { navController.popBackStack() },
                                    onCategoryClick = { type ->
                                        viewModel.setActiveModule(type)
                                        navController.navigate("module_list/$type")
                                    },
                                    onListingClick = { listing ->
                                        viewModel.setActiveModule(listing.type)
                                        navController.navigate("module_list/${listing.type}")
                                    },
                                    onSeeAllRecent = {
                                        navController.navigate("recent_rentals")
                                    },
                                    onCreateClicked = { navController.navigate("create_hub") },
                                    onBottomNavClick = { target ->
                                        when (target) {
                                            "home" -> navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = true }
                                            }
                                            "explore" -> {
                                                viewModel.setActiveModule("EXPLORE")
                                                navController.navigate("module_list/EXPLORE")
                                            }
                                            "create" -> navController.navigate("create_hub")
                                            "society" -> navController.navigate("admin")
                                            "profile" -> navController.navigate("my_listings")
                                        }
                                    }
                                )
                            }
                        }

                        composable("recent_rentals") {
                            val user = currentUser
                            if (user != null) {
                                RecentRentalsScreen(
                                    recentListings = recentListings,
                                    selectedSociety = selectedSociety,
                                    onBack = { navController.popBackStack() },
                                    onListingClick = { listing ->
                                        viewModel.setActiveModule(listing.type)
                                        navController.navigate("module_list/${listing.type}")
                                    },
                                    onBottomNavClick = { target ->
                                        when (target) {
                                            "home" -> navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = true }
                                            }
                                            "explore" -> {
                                                viewModel.setActiveModule("EXPLORE")
                                                navController.navigate("module_list/EXPLORE")
                                            }
                                            "create" -> navController.navigate("create_hub")
                                            "society" -> navController.navigate("admin")
                                            "profile" -> navController.navigate("my_listings")
                                        }
                                    },
                                    activeTab = "home"
                                )
                            }
                        }

                        composable(
                            route = "module_list/{type}",
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val moduleType = backStackEntry.arguments?.getString("type") ?: ""
                            val user = currentUser
                            if (user != null) {
                                if (moduleType == "PROPERTY") {
                                    PropertyRentalsScreen(
                                        currentUser = user,
                                        listings = filteredListings,
                                        selectedSociety = selectedSociety,
                                        syncState = syncState,
                                        isRefreshing = isRefreshing,
                                        onBack = { navController.popBackStack() },
                                        onLikeListing = { viewModel.toggleLike(it) },
                                        onBookmarkListing = { viewModel.toggleBookmark(it) },
                                        onCreateListingClicked = {
                                            navController.navigate("create_listing/PROPERTY")
                                        },
                                        onRetrySync = {
                                            viewModel.triggerSync()
                                        },
                                        onRefresh = {
                                            viewModel.refresh()
                                        },
                                        onNavigateToSaved = {
                                            navController.navigate("saved_properties")
                                        },
                                        onNavigateToMyListings = {
                                            navController.navigate("my_listings")
                                        },
                                        activeTab = "home",
                                        onBottomNavClick = { target ->
                                            when (target) {
                                                "home" -> navController.navigate("dashboard") {
                                                    popUpTo("dashboard") { inclusive = true }
                                                }
                                                "explore" -> {
                                                    viewModel.setActiveModule("EXPLORE")
                                                    navController.navigate("module_list/EXPLORE")
                                                }
                                                "society" -> navController.navigate("admin")
                                                "profile" -> navController.navigate("my_listings")
                                            }
                                        }
                                    )
                                } else {
                                    ModuleListScreen(
                                        moduleType = moduleType,
                                        listings = if (moduleType == "EXPLORE") exploreListings else filteredListings,
                                        currentUser = user,
                                        selectedSociety = selectedSociety,
                                        syncState = syncState,
                                        isRefreshing = isRefreshing,
                                        onBack = { navController.popBackStack() },
                                        onLikeListing = { viewModel.toggleLike(it) },
                                        onBookmarkListing = { viewModel.toggleBookmark(it) },
                                        onCreateListingClicked = {
                                            if (moduleType == "EXPLORE") {
                                                navController.navigate("create_hub")
                                            } else {
                                                navController.navigate("create_listing/$moduleType")
                                            }
                                        },
                                        onRetrySync = {
                                            viewModel.triggerSync()
                                        },
                                        onRefresh = {
                                            viewModel.refresh()
                                        },
                                        activeTab = if (moduleType == "EXPLORE") "explore" else "home",
                                        onBottomNavClick = { target ->
                                            when (target) {
                                                "home" -> navController.navigate("dashboard") {
                                                    popUpTo("dashboard") { inclusive = true }
                                                }
                                                "explore" -> {
                                                    if (moduleType != "EXPLORE") {
                                                        viewModel.setActiveModule("EXPLORE")
                                                        navController.navigate("module_list/EXPLORE")
                                                    }
                                                }
                                                "society" -> navController.navigate("admin")
                                                "profile" -> navController.navigate("my_listings")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        composable(
                            route = "create_listing/{type}",
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val moduleType = backStackEntry.arguments?.getString("type") ?: ""
                            val user = currentUser
                            if (user != null) {
                                val createListingViewModel: CreateListingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                CreateListingScreen(
                                    initialType = moduleType,
                                    currentUser = user,
                                    viewModel = createListingViewModel,
                                    onBack = {
                                        viewModel.setActiveModule(moduleType)
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }

                        composable("admin") {
                            AdminScreen(
                                users = allUsers,
                                onApproveUser = { viewModel.approveUser(it) },
                                onRejectUser = { viewModel.rejectUser(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("my_listings") {
                            val user = currentUser
                            val myListings by viewModel.myPropertyListings.collectAsStateWithLifecycle()
                            if (user != null) {
                                MyListingsScreen(
                                    currentUser = user,
                                    myListings = myListings,
                                    selectedSociety = selectedSociety,
                                    syncState = syncState,
                                    onDeleteListing = { viewModel.deleteListing(it) },
                                    onBack = { navController.popBackStack() },
                                    onBottomNavClick = { target ->
                                        when (target) {
                                            "home" -> navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = true }
                                            }
                                            "explore" -> {
                                                viewModel.setActiveModule("EXPLORE")
                                                navController.navigate("module_list/EXPLORE")
                                            }
                                            "create" -> navController.navigate("create_hub")
                                            "society" -> navController.navigate("admin")
                                            "profile" -> { /* Already on my listings */ }
                                        }
                                    }
                                )
                            }
                        }

                        composable("saved_properties") {
                            val user = currentUser
                            val savedListings by viewModel.savedPropertyListings.collectAsStateWithLifecycle()
                            if (user != null) {
                                SavedPropertiesScreen(
                                    currentUser = user,
                                    savedListings = savedListings,
                                    selectedSociety = selectedSociety,
                                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                                    onBack = { navController.popBackStack() },
                                    onBottomNavClick = { target ->
                                        when (target) {
                                            "home" -> navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = true }
                                            }
                                            "explore" -> {
                                                viewModel.setActiveModule("EXPLORE")
                                                navController.navigate("module_list/EXPLORE")
                                            }
                                            "create" -> navController.navigate("create_hub")
                                            "society" -> navController.navigate("admin")
                                            "profile" -> navController.navigate("my_listings")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
