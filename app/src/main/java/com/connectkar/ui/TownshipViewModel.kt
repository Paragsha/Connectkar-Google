package com.connectkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.ChefProfileEntity
import com.connectkar.data.local.MenuItemEntity
import com.connectkar.data.local.MealOrderEntity
import com.connectkar.data.local.MealSubscriptionEntity
import com.connectkar.data.repository.TownshipRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Failed(val message: String) : SyncState()
}

sealed interface OperationsUiState {
    object Idle : OperationsUiState
    object Loading : OperationsUiState
    object Success : OperationsUiState
    data class Error(val message: String) : OperationsUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TownshipViewModel(private val repository: TownshipRepository) : ViewModel() {

    // --- State Observables ---
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _operationsState = MutableStateFlow<OperationsUiState>(OperationsUiState.Idle)
    val operationsState: StateFlow<OperationsUiState> = _operationsState.asStateFlow()

    fun resetOperationsState() {
        _operationsState.value = OperationsUiState.Idle
    }

    val syncState: StateFlow<SyncState> = repository.syncWorkInfo
        .map { workInfos ->
            val workInfo = workInfos.firstOrNull() ?: return@map SyncState.Idle
            when (workInfo.state) {
                androidx.work.WorkInfo.State.RUNNING -> SyncState.Syncing
                androidx.work.WorkInfo.State.ENQUEUED -> {
                    if (workInfo.runAttemptCount > 0) {
                        SyncState.Failed("Retrying sync... (attempt ${workInfo.runAttemptCount})")
                    } else {
                        SyncState.Syncing
                    }
                }
                androidx.work.WorkInfo.State.FAILED -> SyncState.Failed("Sync failed. Check connection.")
                androidx.work.WorkInfo.State.SUCCEEDED -> SyncState.Success
                else -> {
                    if (workInfo.runAttemptCount > 0) {
                        SyncState.Failed("Retrying sync... (attempt ${workInfo.runAttemptCount})")
                    } else {
                        SyncState.Idle
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState.Idle)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedSociety = MutableStateFlow("All Societies")
    val selectedSociety: StateFlow<String> = _selectedSociety.asStateFlow()

    private val _activeModule = MutableStateFlow("MARKETPLACE")
    val activeModule: StateFlow<String> = _activeModule.asStateFlow()

    // --- MealHub Observables ---
    val currentChefProfile: StateFlow<ChefProfileEntity?> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getChefProfile(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allChefsForSociety: StateFlow<List<ChefProfileEntity>> = _selectedSociety
        .flatMapLatest { society ->
            repository.getAllChefsForSociety(society)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItemsForSociety: StateFlow<List<MenuItemEntity>> = _selectedSociety
        .flatMapLatest { society ->
            repository.getMenuItemsForSociety(society)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myMealOrders: StateFlow<List<MealOrderEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getOrdersForBuyer(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chefIncomingOrders: StateFlow<List<MealOrderEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getOrdersForChef(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chefMenuItems: StateFlow<List<MenuItemEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getMenuItemsForChef(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myMealSubscriptions: StateFlow<List<MealSubscriptionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getSubscriptionsForBuyer(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive SQL-delegated Flow for filtered listings
    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        _selectedSociety,
        _activeModule
    ) { society, module ->
        society to module
    }.flatMapLatest { (society, module) ->
        repository.getListingsByTypeAndSociety(module, society)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealListingsForSociety: StateFlow<List<ListingEntity>> = _selectedSociety
        .flatMapLatest { society ->
            repository.getListingsByTypeAndSociety("MEAL", society)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exploreListings: StateFlow<List<ListingEntity>> = selectedSociety
        .flatMapLatest { society -> repository.getListingsBySociety(society) }
        .map { it.filter { listing -> !listing.isDraft } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPropertyListings: StateFlow<List<ListingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && user.uid.isNotEmpty()) {
                repository.getListingsByAuthor(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPropertyListings: StateFlow<List<ListingEntity>> = repository.getBookmarkedListingsByType("PROPERTY")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedMockData()
        }
    }

    // --- Actions ---
    fun selectSociety(society: String) {
        _selectedSociety.value = society
    }

    fun setActiveModule(module: String) {
        _activeModule.value = module
    }

    fun register(
        fullName: String,
        phoneNumber: String,
        society: String,
        blockTower: String,
        flatNumber: String,
        avatarIndex: Int,
        floor: String = "",
        residentType: String = "OWNER",
        moveInDate: String = "",
        proofDocumentUri: String = ""
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.registerUser(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    society = society,
                    blockTower = blockTower,
                    flatNumber = flatNumber,
                    avatarIndex = avatarIndex,
                    floor = floor,
                    residentType = residentType,
                    moveInDate = moveInDate,
                    proofDocumentUri = proofDocumentUri
                )
                // Auto set selected society to user's registered society
                _selectedSociety.value = society
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun loginAsUser(user: UserEntity) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.loginAsUser(user)
                _selectedSociety.value = user.society
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.logout()
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun toggleLike(listingId: Int) {
        viewModelScope.launch {
            try {
                repository.toggleLikeListing(listingId)
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Error toggling like", e)
            }
        }
    }

    fun toggleBookmark(listingId: Int) {
        viewModelScope.launch {
            try {
                repository.toggleBookmarkListing(listingId)
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Error toggling bookmark", e)
            }
        }
    }

    fun approveUser(userId: Int) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.updateVerificationStatus(userId, isVerified = true)
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to approve user")
            }
        }
    }

    fun rejectUser(userId: Int) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.updateVerificationStatus(userId, isVerified = false)
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to reject user")
            }
        }
    }

    fun createListing(
        type: String,
        title: String,
        description: String,
        price: Double,
        contact: String,
        category: String,
        extra1: String = "",
        extra2: String = "",
        extra3: String = "",
        extra4: String = ""
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                val user = currentUser.value
                val listing = ListingEntity(
                    type = type,
                    title = title,
                    description = description,
                    price = price,
                    contact = contact.ifEmpty { user?.phoneNumber ?: "9999999999" },
                    society = user?.society ?: "Sylvan County",
                    authorName = user?.fullName ?: "Resident",
                    authorFlat = "${user?.blockTower ?: ""} ${user?.flatNumber ?: ""}",
                    authorPhone = user?.phoneNumber ?: "",
                    authorUid = user?.uid ?: "",
                    category = category,
                    extra1 = extra1,
                    extra2 = extra2,
                    extra3 = extra3,
                    extra4 = extra4
                )
                repository.insertListing(listing)
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to create listing")
            }
        }
    }

    fun deleteListing(listingId: Int) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                repository.deleteListing(listingId)
                _operationsState.value = OperationsUiState.Success
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to delete listing")
            }
        }
    }

    fun triggerSync() {
        repository.triggerSync()
    }

    fun refresh(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val startTime = System.currentTimeMillis()
            val result = repository.manualSync()
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 600) {
                kotlinx.coroutines.delay(600 - elapsed)
            }
            _isRefreshing.value = false
            onComplete?.invoke(result.isSuccess)
        }
    }

    // Quick switch helper for simulation / testing
    fun simulateAdminVerificationOfCurrentUser() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateVerificationStatus(user.id, isVerified = true)
            }
        }
    }

    // --- MealHub Actions ---
    fun getChefProfile(uid: String): Flow<ChefProfileEntity?> = repository.getChefProfile(uid)

    fun getMenuItemsForChef(chefUid: String): Flow<List<MenuItemEntity>> = repository.getMenuItemsForChef(chefUid)

    fun getMenuItemById(id: Int): Flow<MenuItemEntity?> = repository.getMenuItemById(id)

    fun becomeChef(
        story: String,
        dishName: String,
        price: Double,
        portions: Int,
        isVeg: Boolean,
        cuisineTags: String,
        photoUrl: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                val user = currentUser.value ?: return@launch
                val chefProfile = ChefProfileEntity(
                    uid = user.uid,
                    isChef = true,
                    chefStory = story,
                    mealsServedCount = 0,
                    regularsCount = 0,
                    ratingAvg = 5.0,
                    isSocietyVouched = true,
                    speciality = cuisineTags,
                    society = user.society
                )
                val initialItem = MenuItemEntity(
                    chefUid = user.uid,
                    chefName = user.fullName,
                    chefFlat = "${user.blockTower} ${user.flatNumber}".trim(),
                    dishName = dishName,
                    description = story,
                    price = price,
                    portionsAvailable = portions,
                    portionsBooked = 0,
                    isVeg = isVeg,
                    photoUrl = photoUrl,
                    cuisineTags = cuisineTags,
                    mealType = "LUNCH",
                    deliveryWindow = "12:30 PM - 1:30 PM",
                    society = user.society,
                    isSoldOut = false
                )
                repository.becomeChef(chefProfile, initialItem)
                _operationsState.value = OperationsUiState.Success
                onComplete()
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to become a chef")
            }
        }
    }

    fun addMenuItem(
        dishName: String,
        description: String,
        price: Double,
        portions: Int,
        isVeg: Boolean,
        cuisineTags: String,
        mealType: String,
        deliveryWindow: String,
        photoUrl: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                val user = currentUser.value ?: return@launch
                val item = MenuItemEntity(
                    chefUid = user.uid,
                    chefName = user.fullName,
                    chefFlat = "${user.blockTower} ${user.flatNumber}".trim(),
                    dishName = dishName,
                    description = description,
                    price = price,
                    portionsAvailable = portions,
                    portionsBooked = 0,
                    isVeg = isVeg,
                    photoUrl = photoUrl,
                    cuisineTags = cuisineTags,
                    mealType = mealType,
                    deliveryWindow = deliveryWindow,
                    society = user.society,
                    isSoldOut = false
                )
                repository.createMenuItem(item)
                _operationsState.value = OperationsUiState.Success
                onComplete()
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to add menu item")
            }
        }
    }

    fun toggleMenuItemSoldOut(itemId: Int, isSoldOut: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleMenuItemSoldOut(itemId, isSoldOut)
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to update item")
            }
        }
    }

    fun placeMealOrder(
        menuItem: MenuItemEntity,
        servingSize: Int,
        deliveryWindow: String,
        dietaryNotes: String,
        deliveryMethod: String,
        addOns: String,
        itemTotal: Double,
        addOnsTotal: Double,
        deliveryFee: Double,
        grandTotal: Double,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                val user = currentUser.value ?: return@launch
                val order = MealOrderEntity(
                    buyerUid = user.uid,
                    buyerName = user.fullName,
                    buyerFlat = "${user.blockTower} ${user.flatNumber}".trim(),
                    chefUid = menuItem.chefUid,
                    chefName = menuItem.chefName,
                    menuItemId = menuItem.id,
                    dishName = menuItem.dishName,
                    servingSize = servingSize,
                    deliveryWindow = deliveryWindow,
                    dietaryNotes = dietaryNotes,
                    deliveryMethod = deliveryMethod,
                    addOns = addOns,
                    itemTotal = itemTotal,
                    addOnsTotal = addOnsTotal,
                    deliveryFee = deliveryFee,
                    grandTotal = grandTotal,
                    status = "PENDING",
                    society = user.society
                )
                repository.createMealOrder(order)
                _operationsState.value = OperationsUiState.Success
                onComplete()
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to place meal order")
            }
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, newStatus)
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to update order status")
            }
        }
    }

    fun subscribeToChef(
        chefUid: String,
        chefName: String,
        planType: String,
        mealsPerCycle: Int,
        discountPercent: Int,
        pricePerMeal: Double,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationsState.value = OperationsUiState.Loading
            try {
                val user = currentUser.value ?: return@launch
                val sub = MealSubscriptionEntity(
                    buyerUid = user.uid,
                    chefUid = chefUid,
                    chefName = chefName,
                    planType = planType,
                    mealsPerCycle = mealsPerCycle,
                    discountPercent = discountPercent,
                    daysRemaining = mealsPerCycle,
                    renewalDate = "Next Monday",
                    status = "ACTIVE",
                    pricePerMeal = pricePerMeal,
                    society = user.society
                )
                repository.createMealSubscription(sub)
                _operationsState.value = OperationsUiState.Success
                onComplete()
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to subscribe")
            }
        }
    }

    fun toggleSubscriptionStatus(subId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.toggleSubscriptionStatus(subId, newStatus)
            } catch (e: Exception) {
                _operationsState.value = OperationsUiState.Error(e.message ?: "Failed to update subscription")
            }
        }
    }

    // Factory Class
    class Factory(private val repository: TownshipRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TownshipViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TownshipViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
