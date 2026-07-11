package com.example

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
import com.example.data.local.AppDatabase
import com.example.data.repository.TownshipRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "connectkar_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .build()

        // Initialize Repository and ViewModel Factory
        val repository = TownshipRepository(database.appDao(), applicationContext)
        
        setContent {
            MyApplicationTheme {
                val viewModel: TownshipViewModel by viewModels {
                    TownshipViewModel.Factory(repository)
                }

                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                val filteredListings by viewModel.filteredListings.collectAsStateWithLifecycle()
                val selectedSociety by viewModel.selectedSociety.collectAsStateWithLifecycle()
                val syncState by viewModel.syncState.collectAsStateWithLifecycle()

                val navController = rememberNavController()

                // Automatically navigate based on user session status
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        navController.navigate("dashboard") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo(0) { inclusive = true }
                        }
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
                                    onSocietySelected = { viewModel.selectSociety(it) },
                                    onModuleClicked = { moduleId ->
                                        if (moduleId == "ADMIN") {
                                            navController.navigate("admin")
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
                                    }
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
                                ModuleListScreen(
                                    moduleType = moduleType,
                                    listings = filteredListings,
                                    currentUser = user,
                                    selectedSociety = selectedSociety,
                                    syncState = syncState,
                                    onBack = { navController.popBackStack() },
                                    onLikeListing = { viewModel.toggleLike(it) },
                                    onBookmarkListing = { viewModel.toggleBookmark(it) },
                                    onCreateListingClicked = {
                                        navController.navigate("create_listing/$moduleType")
                                    },
                                    onRetrySync = {
                                        viewModel.triggerSync()
                                    }
                                )
                            }
                        }

                        composable(
                            route = "create_listing/{type}",
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val moduleType = backStackEntry.arguments?.getString("type") ?: ""
                            val user = currentUser
                            if (user != null) {
                                CreateListingScreen(
                                    initialType = moduleType,
                                    currentUser = user,
                                    onBack = { navController.popBackStack() },
                                    onSubmitListing = { type, title, description, price, contact, category, extra1, extra2, extra3, extra4 ->
                                        viewModel.createListing(
                                            type = type,
                                            title = title,
                                            description = description,
                                            price = price,
                                            contact = contact,
                                            category = category,
                                            extra1 = extra1,
                                            extra2 = extra2,
                                            extra3 = extra3,
                                            extra4 = extra4
                                        )
                                        // Return to listings list
                                        viewModel.setActiveModule(type)
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
                    }
                }
            }
        }
    }
}
