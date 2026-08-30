package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ConnectKarApplication
import com.example.data.local.ExtendedMarketplaceDetails
import com.example.data.local.ListingEntity
import com.example.data.local.MoshiHelper
import com.example.data.local.UserEntity
import com.example.data.local.photoUrls
import com.example.data.repository.TownshipRepository
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Local Photo Picker URI previews before upload (used in create-flow UI)
    private val _selectedPhotos = MutableStateFlow<List<String>>(emptyList())
    val selectedPhotos: StateFlow<List<String>> = _selectedPhotos.asStateFlow()

    // Uploaded Firebase Storage download URLs (localUri -> downloadUrl)
    private val _uploadedPhotoUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val uploadedPhotoUrls: StateFlow<Map<String, String>> = _uploadedPhotoUrls.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    // Unique storage folder key for listing photos
    private var storageListingKey: String = "listing_${System.currentTimeMillis()}"

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

    // Property Specific StateFlows
    private val _wingFlatNumber = MutableStateFlow("")
    val wingFlatNumber: StateFlow<String> = _wingFlatNumber.asStateFlow()

    private val _bhkType = MutableStateFlow("")
    val bhkType: StateFlow<String> = _bhkType.asStateFlow()

    private val _furnishedStatus = MutableStateFlow("")
    val furnishedStatus: StateFlow<String> = _furnishedStatus.asStateFlow()

    private val _propertyType = MutableStateFlow("")
    val propertyType: StateFlow<String> = _propertyType.asStateFlow()

    private val _beds = MutableStateFlow(0)
    val beds: StateFlow<Int> = _beds.asStateFlow()

    private val _baths = MutableStateFlow(0)
    val baths: StateFlow<Int> = _baths.asStateFlow()

    private val _sqft = MutableStateFlow(0)
    val sqft: StateFlow<Int> = _sqft.asStateFlow()

    private val _amenities = MutableStateFlow<List<String>>(emptyList())
    val amenities: StateFlow<List<String>> = _amenities.asStateFlow()

    private val _isAvailable = MutableStateFlow(true)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _verificationRequested = MutableStateFlow(false)
    val verificationRequested: StateFlow<Boolean> = _verificationRequested.asStateFlow()

    // Track the active draft ID if any
    private var activeDraftId: Int = 0
    private var activeType: String = "MARKETPLACE"

    fun initForFlow(type: String, currentUser: UserEntity) {
        activeType = type
        _currentStep.value = 1
        _errorMessage.value = null
        storageListingKey = "listing_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"

        viewModelScope.launch {
            val draft = repository.getDraftListing(type)
            if (draft != null) {
                activeDraftId = draft.id
                _title.value = draft.title
                _description.value = draft.description
                _price.value = if (draft.price > 0.0) draft.price.toString() else ""
                _category.value = draft.category
                _condition.value = draft.extra3.ifEmpty { "Like New" }
                _isSocietyOnly.value = !draft.isPublic

                if (draft.extra1.isNotEmpty()) {
                    // Extract photos from extra1 if available (JSON array or comma-delimited)
                    val loadedPhotos = draft.photoUrls()
                    _selectedPhotos.value = loadedPhotos
                    val urlMap = mutableMapOf<String, String>()
                    for (photo in loadedPhotos) {
                        urlMap[photo] = photo
                    }
                    _uploadedPhotoUrls.value = urlMap
                } else {
                    _selectedPhotos.value = emptyList()
                    _uploadedPhotoUrls.value = emptyMap()
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
                        // Property fields
                        _wingFlatNumber.value = extended.wingFlatNumber
                        _bhkType.value = extended.bhkType
                        _furnishedStatus.value = extended.furnishedStatus
                        _propertyType.value = extended.propertyType
                        _beds.value = extended.beds
                        _baths.value = extended.baths
                        _sqft.value = extended.sqft
                        _amenities.value = extended.amenities
                        _isAvailable.value = extended.isAvailable
                        _verificationRequested.value = extended.verificationRequested
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
                _uploadedPhotoUrls.value = emptyMap()
                _brand.value = ""
                _model.value = ""
                _itemAge.value = "New"
                _quantity.value = 1
                _meetupLocation.value = "Main Gate"
                _preferredDays.value = emptyList()
                _timePreference.value = "Evening"
                _isNegotiable.value = true
                _paymentMethods.value = listOf("UPI", "Cash")
                // Property defaults
                _wingFlatNumber.value = ""
                _bhkType.value = ""
                _furnishedStatus.value = ""
                _propertyType.value = ""
                _beds.value = 0
                _baths.value = 0
                _sqft.value = 0
                _amenities.value = emptyList()
                _isAvailable.value = true
                _verificationRequested.value = false
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
    
    fun addPhoto(uri: String) {
        if (uri.isBlank()) return
        if (!_selectedPhotos.value.contains(uri)) {
            _selectedPhotos.value = _selectedPhotos.value + uri
        }

        // If it's already a remote HTTP/HTTPS URL, map directly
        if (uri.startsWith("http://", ignoreCase = true) || uri.startsWith("https://", ignoreCase = true)) {
            _uploadedPhotoUrls.value = _uploadedPhotoUrls.value + (uri to uri)
            return
        }

        // Upload picked URI's bytes to Firebase Storage
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            val photoIndex = _selectedPhotos.value.indexOf(uri) + 1
            val downloadUrl = com.example.data.FirebaseManager.uploadListingImage(
                context = getApplication(),
                uriString = uri,
                listingId = storageListingKey,
                n = if (photoIndex > 0) photoIndex else (_uploadedPhotoUrls.value.size + 1)
            )
            if (downloadUrl != null) {
                _uploadedPhotoUrls.value = _uploadedPhotoUrls.value + (uri to downloadUrl)
            }
            _isUploadingPhoto.value = false
        }
    }

    fun removePhoto(uri: String) {
        _selectedPhotos.value = _selectedPhotos.value - uri
        _uploadedPhotoUrls.value = _uploadedPhotoUrls.value - uri
    }

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

    // Property Setters
    fun setWingFlatNumber(value: String) { _wingFlatNumber.value = value }
    fun setBhkType(value: String) { _bhkType.value = value }
    fun setFurnishedStatus(value: String) { _furnishedStatus.value = value }
    fun setPropertyType(value: String) { _propertyType.value = value }
    fun setBeds(value: Int) { _beds.value = value }
    fun incrementBeds() { _beds.value = _beds.value + 1 }
    fun decrementBeds() { if (_beds.value > 0) _beds.value = _beds.value - 1 }
    fun setBaths(value: Int) { _baths.value = value }
    fun incrementBaths() { _baths.value = _baths.value + 1 }
    fun decrementBaths() { if (_baths.value > 0) _baths.value = _baths.value - 1 }
    fun setSqft(value: Int) { _sqft.value = value }
    fun toggleAmenity(amenity: String) {
        val current = _amenities.value
        _amenities.value = if (current.contains(amenity)) current - amenity else current + amenity
    }
    fun setAvailable(value: Boolean) { _isAvailable.value = value }
    fun setVerificationRequested(value: Boolean) { _verificationRequested.value = value }

    fun validateCurrentStep(): String? {
        return when (_currentStep.value) {
            1 -> FormValidators.getStep1ValidationError(
                type = activeType,
                title = _title.value,
                category = _category.value,
                price = _price.value,
                description = _description.value,
                wingFlatNumber = _wingFlatNumber.value,
                bhkType = _bhkType.value,
                propertyType = _propertyType.value
            )
            2 -> FormValidators.getStep2ValidationError(
                type = activeType,
                price = _price.value,
                wingFlatNumber = _wingFlatNumber.value,
                propertyType = _propertyType.value,
                bhkType = _bhkType.value
            )
            else -> null
        }
    }

    fun nextStep(): Boolean {
        val error = validateCurrentStep()
        if (error != null) {
            _errorMessage.value = error
            return false
        }
        _errorMessage.value = null
        if (_currentStep.value < 3) {
            _currentStep.value = _currentStep.value + 1
        }
        return true
    }

    fun prevStep() {
        _errorMessage.value = null
        if (_currentStep.value > 1) {
            _currentStep.value = _currentStep.value - 1
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setStep(step: Int) {
        _errorMessage.value = null
        _currentStep.value = step
    }

    // Save as Draft
    fun saveDraft(currentUser: UserEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            val draft = buildListingEntity(currentUser, isDraft = true)
            val generatedId = repository.saveDraftListing(draft)
            if (activeDraftId == 0 && generatedId > 0) {
                activeDraftId = generatedId.toInt()
            }
            onComplete()
        }
    }

    // Publish Listing (Writes to Room and syncs to Firestore)
    fun publishListing(currentUser: UserEntity, onComplete: () -> Unit) {
        val validationError = FormValidators.getPublishValidationError(
            type = activeType,
            title = _title.value,
            category = _category.value,
            price = _price.value,
            description = _description.value,
            wingFlatNumber = _wingFlatNumber.value,
            bhkType = _bhkType.value,
            propertyType = _propertyType.value
        )
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        _errorMessage.value = null

        viewModelScope.launch {
            // Ensure any picked URIs without completed upload finish uploading to Firebase Storage
            val currentMap = _uploadedPhotoUrls.value.toMutableMap()
            for ((index, uri) in _selectedPhotos.value.withIndex()) {
                if (!currentMap.containsKey(uri) && !uri.startsWith("http://", ignoreCase = true) && !uri.startsWith("https://", ignoreCase = true)) {
                    val downloadUrl = com.example.data.FirebaseManager.uploadListingImage(
                        context = getApplication(),
                        uriString = uri,
                        listingId = storageListingKey,
                        n = index + 1
                    )
                    if (downloadUrl != null) {
                        currentMap[uri] = downloadUrl
                    }
                }
            }
            _uploadedPhotoUrls.value = currentMap

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
        val priceVal = _price.value.toDoubleOrNull() ?: 0.0
        val isProperty = activeType == "PROPERTY"

        // Serialize Step 2 details specifically for Marketplace & Property
        val extendedDetails = ExtendedMarketplaceDetails(
            brand = _brand.value,
            model = _model.value,
            itemAge = _itemAge.value,
            quantity = _quantity.value,
            meetupLocation = _meetupLocation.value,
            preferredDays = _preferredDays.value,
            timePreference = _timePreference.value,
            isNegotiable = _isNegotiable.value,
            paymentMethods = _paymentMethods.value,
            // Property fields
            wingFlatNumber = _wingFlatNumber.value,
            bhkType = _bhkType.value,
            furnishedStatus = _furnishedStatus.value,
            propertyType = _propertyType.value,
            beds = _beds.value,
            baths = _baths.value,
            sqft = _sqft.value,
            amenities = _amenities.value,
            isAvailable = _isAvailable.value,
            verificationRequested = _verificationRequested.value
        )
        val detailsJsonStr = MoshiHelper.toJson(extendedDetails)

        // Store resolved Firebase Storage download URLs in extra1 as JSON array (with comma fallback compatibility)
        val resolvedPhotos = _selectedPhotos.value.mapNotNull { uri ->
            val downloadUrl = _uploadedPhotoUrls.value[uri]
            if (downloadUrl != null) {
                downloadUrl
            } else if (uri.startsWith("http://", ignoreCase = true) || uri.startsWith("https://", ignoreCase = true)) {
                uri
            } else if (isDraft) {
                uri // Keep local URI only in local draft
            } else {
                null
            }
        }
        val photosStr = if (resolvedPhotos.isNotEmpty()) MoshiHelper.toJsonStringList(resolvedPhotos) else ""

        return ListingEntity(
            id = if (isDraft) activeDraftId else 0,
            type = activeType,
            title = _title.value,
            description = _description.value,
            price = priceVal,
            contact = currentUser.phoneNumber,
            society = currentUser.society,
            authorName = currentUser.fullName,
            authorFlat = "${currentUser.blockTower} - ${currentUser.flatNumber}",
            authorPhone = currentUser.phoneNumber,
            authorUid = currentUser.uid,
            timestamp = System.currentTimeMillis(),
            category = if (isProperty) _bhkType.value else _category.value,
            extra1 = photosStr, // Stores Firebase Storage download URLs for published listings
            extra2 = if (isProperty) _bhkType.value else _brand.value, // Keep fallback model variables
            extra3 = if (isProperty) { if (_isAvailable.value) "Available" else "Not Available" } else _condition.value,
            extra4 = if (isProperty) _propertyType.value else _itemAge.value,
            detailsJson = detailsJsonStr,
            isDraft = isDraft,
            isPublic = if (isProperty) false else !_isSocietyOnly.value
        )
    }
}
