package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.data.repository.TownshipRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Failed(val message: String) : SyncState()
}

class TownshipViewModel(private val repository: TownshipRepository) : ViewModel() {

    // --- State Observables ---
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

    // Combined Flow for reactive filtering
    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        _selectedSociety,
        _activeModule
    ) { listings, society, module ->
        listings.filter { listing ->
            val matchesModule = listing.type == module
            val matchesSociety = society == "All Societies" || 
                                 listing.society == society || 
                                 listing.society.isEmpty() ||
                                 society.isEmpty()
            matchesModule && matchesSociety
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        }
    }

    fun loginAsUser(user: UserEntity) {
        viewModelScope.launch {
            repository.loginAsUser(user)
            _selectedSociety.value = user.society
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun toggleLike(listingId: Int) {
        viewModelScope.launch {
            repository.toggleLikeListing(listingId)
        }
    }

    fun toggleBookmark(listingId: Int) {
        viewModelScope.launch {
            repository.toggleBookmarkListing(listingId)
        }
    }

    fun approveUser(userId: Int) {
        viewModelScope.launch {
            repository.updateVerificationStatus(userId, isVerified = true)
        }
    }

    fun rejectUser(userId: Int) {
        viewModelScope.launch {
            repository.updateVerificationStatus(userId, isVerified = false)
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
        }
    }

    fun deleteListing(listingId: Int) {
        viewModelScope.launch {
            repository.deleteListing(listingId)
        }
    }

    fun triggerSync() {
        repository.triggerSync()
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
