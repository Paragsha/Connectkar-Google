package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ConnectKarApplication
import com.example.data.local.ListingEntity
import com.example.data.local.MoshiHelper
import com.example.data.local.UserEntity
import com.example.data.repository.TownshipRepository
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class ExtendedMarketplaceDetails(
    val brand: String = "",
    val model: String = "",
    val itemAge: String = "",
    val quantity: Int = 1,
    val meetupLocation: String = "",
    val preferredDays: List<String> = emptyList(),
    val timePreference: String = "",
    val isNegotiable: Boolean = false,
    val paymentMethods: List<String> = emptyList()
)

class CreateListingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TownshipRepository = (application as ConnectKarApplication).repository

    // Navigation and steps
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Form validation and errors
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Step 1: Basic Info
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _condition = MutableStateFlow("Like New") // Default to Like New
    val condition: StateFlow<String> = _condition.asStateFlow()

    private val _isSocietyOnly = MutableStateFlow(true) // Default to Society Only
    val isSocietyOnly: StateFlow<Boolean> = _isSocietyOnly.asStateFlow()

    // Local Photo Picker URI previews before upload
    private val _selectedPhotos = MutableStateFlow<List<String>>(emptyList())
    val selectedPhotos: StateFlow<List<String>> = _selectedPhotos.asStateFlow()

    // Step 2: Marketplace Specifications (Step 2 content is Swappable per Category/Module)
    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _model = MutableStateFlow("")
    val model: StateFlow<String> = _model.asStateFlow()

    private val _itemAge = MutableStateFlow("New")
    val itemAge: StateFlow<String> = _itemAge.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _meetupLocation = MutableStateFlow("Main Gate")
    val meetupLocation: StateFlow<String> = _meetupLocation.asStateFlow()

    private val _preferredDays = MutableStateFlow<List<String>>(emptyList())
    val preferredDays: StateFlow<List<String>> = _preferredDays.asStateFlow()

    private val _timePreference = MutableStateFlow("Evening") // Morning/Evening
    val timePreference: StateFlow<String> = _timePreference.asStateFlow()

    private val _isNegotiable = MutableStateFlow(true)
    val isNegotiable: StateFlow<Boolean> = _isNegotiable.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<String>>(listOf("UPI", "Cash"))
    val paymentMethods: StateFlow<List<String>> = _paymentMethods.asStateFlow()

    // Track the active draft ID if any
    private var activeDraftId: Int = 0
    private var activeType: String = "MARKETPLACE"

    fun initForFlow(type: String, currentUser: UserEntity) {
        activeType = type
        _currentStep.value = 1
        _errorMessage.value = null

        viewModelScope.launch {
            val draft = repository.getDraftListing(type)
            if (draft != null) {
                activeDraftId = draft.id
                _title.value = draft.title
                _description.value = draft.description
                _price.value = if (draft.price > 0.0) draft.price.toString() else ""
                _category.value = draft.category
                _condition.value = draft.extra3.ifEmpty { "Like New" }
                _isSocietyOnly.value = draft.society.isNotEmpty()

                if (draft.extra1.isNotEmpty()) {
                    // Extract photos from extra1 if available (delimited by comma)
                    _selectedPhotos.value = draft.extra1.split(",").filter { it.isNotBlank() }
                } else {
                    _selectedPhotos.value = emptyList()
                }

                // Load step 2 details
                if (draft.detailsJson.isNotEmpty()) {
                    val extended = MoshiHelper.fromJson<ExtendedMarketplaceDetails>(draft.detailsJson)
                    if (extended != null) {
                        _brand.value = extended.brand
                        _model.value = extended.model
                        _itemAge.value = extended.itemAge
                        _quantity.value = extended.quantity
                        _meetupLocation.value = extended.meetupLocation
                        _preferredDays.value = extended.preferredDays
                        _timePreference.value = extended.timePreference
                        _isNegotiable.value = extended.isNegotiable
                        _paymentMethods.value = extended.paymentMethods
                    }
                }
            } else {
                // Clear state for new creation
                activeDraftId = 0
                _title.value = ""
                _description.value = ""
                _price.value = ""
                _category.value = ""
                _condition.value = "Like New"
                _isSocietyOnly.value = true
                _selectedPhotos.value = emptyList()
                _brand.value = ""
                _model.value = ""
                _itemAge.value = "New"
                _quantity.value = 1
                _meetupLocation.value = "Main Gate"
                _preferredDays.value = emptyList()
                _timePreference.value = "Evening"
                _isNegotiable.value = true
                _paymentMethods.value = listOf("UPI", "Cash")
            }
        }
    }

    // Setters
    fun setTitle(value: String) { _title.value = value }
    fun setDescription(value: String) { _description.value = value }
    fun setPrice(value: String) { _price.value = value }
    fun setCategory(value: String) { _category.value = value }
    fun setCondition(value: String) { _condition.value = value }
    fun setSocietyOnly(value: Boolean) { _isSocietyOnly.value = value }
    fun addPhoto(uri: String) { _selectedPhotos.value = _selectedPhotos.value + uri }
    fun removePhoto(uri: String) { _selectedPhotos.value = _selectedPhotos.value - uri }

    fun setBrand(value: String) { _brand.value = value }
    fun setModel(value: String) { _model.value = value }
    fun setItemAge(value: String) { _itemAge.value = value }
    fun incrementQuantity() { _quantity.value = _quantity.value + 1 }
    fun decrementQuantity() { if (_quantity.value > 1) _quantity.value = _quantity.value - 1 }
    fun setMeetupLocation(value: String) { _meetupLocation.value = value }
    fun togglePreferredDay(day: String) {
        val current = _preferredDays.value
        _preferredDays.value = if (current.contains(day)) current - day else current + day
    }
    fun setTimePreference(value: String) { _timePreference.value = value }
    fun setNegotiable(value: Boolean) { _isNegotiable.value = value }
    fun togglePaymentMethod(method: String) {
        val current = _paymentMethods.value
        _paymentMethods.value = if (current.contains(method)) current - method else current + method
    }

    fun nextStep() {
        if (_currentStep.value < 3) {
            _currentStep.value = _currentStep.value + 1
        }
    }

    fun prevStep() {
        if (_currentStep.value > 1) {
            _currentStep.value = _currentStep.value - 1
        }
    }

    fun setStep(step: Int) {
        _currentStep.value = step
    }

    // Save as Draft
    fun saveDraft(currentUser: UserEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            val draft = buildListingEntity(currentUser, isDraft = true)
            repository.saveDraftListing(draft)
            onComplete()
        }
    }

    // Publish Listing (Writes to Room and syncs to Firestore)
    fun publishListing(currentUser: UserEntity, onComplete: () -> Unit) {
        if (_title.value.isBlank()) {
            _errorMessage.value = "Title cannot be blank."
            return
        }

        viewModelScope.launch {
            // Build final published listing entity
            val listing = buildListingEntity(currentUser, isDraft = false)
            
            // Delete old local draft if existed
            if (activeDraftId != 0) {
                repository.deleteDraft(activeDraftId)
            }

            // Insert to local database and sync to Firestore
            repository.insertListing(listing)
            onComplete()
        }
    }

    private fun buildListingEntity(currentUser: UserEntity, isDraft: Boolean): ListingEntity {
        val societyVal = if (_isSocietyOnly.value) currentUser.society else ""
        val priceVal = _price.value.toDoubleOrNull() ?: 0.0

        // Serialize Step 2 details specifically for Marketplace
        val extendedDetails = ExtendedMarketplaceDetails(
            brand = _brand.value,
            model = _model.value,
            itemAge = _itemAge.value,
            quantity = _quantity.value,
            meetupLocation = _meetupLocation.value,
            preferredDays = _preferredDays.value,
            timePreference = _timePreference.value,
            isNegotiable = _isNegotiable.value,
            paymentMethods = _paymentMethods.value
        )
        val detailsJsonStr = MoshiHelper.toJson(extendedDetails)

        // Store photos comma-delimited in extra1
        val photosStr = _selectedPhotos.value.joinToString(",")

        return ListingEntity(
            id = if (isDraft) activeDraftId else 0,
            type = activeType,
            title = _title.value,
            description = _description.value,
            price = priceVal,
            contact = currentUser.phoneNumber,
            society = societyVal,
            authorName = currentUser.fullName,
            authorFlat = "${currentUser.blockTower} - ${currentUser.flatNumber}",
            authorPhone = currentUser.phoneNumber,
            authorUid = currentUser.uid,
            timestamp = System.currentTimeMillis(),
            category = _category.value,
            extra1 = photosStr, // Pass selected local URIs here for preview / rendering
            extra2 = _brand.value, // Keep fallback model variables
            extra3 = _condition.value,
            extra4 = _itemAge.value,
            detailsJson = detailsJsonStr,
            isDraft = isDraft
        )
    }
}
