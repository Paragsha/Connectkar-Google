package com.connectkar.ui

object FormValidators {
    fun isStep1Valid(fullName: String, phoneNumber: String, isAdult: Boolean = false): Boolean {
        return fullName.trim().isNotBlank() && phoneNumber.trim().length >= 10 && isAdult
    }

    fun isStep2Valid(
        blockTower: String,
        flatNumber: String,
        floor: String,
        moveInDate: String,
        proofDocumentUri: String,
        isDebugBuild: Boolean = false
    ): Boolean {
        val trimmedUri = proofDocumentUri.trim()
        val isProofValid = trimmedUri.startsWith("https://", ignoreCase = true) ||
                (isDebugBuild && (trimmedUri == "simulated_proof_of_residence.pdf" || trimmedUri == "proof_of_residence.pdf"))

        return blockTower.trim().isNotBlank() &&
                flatNumber.trim().isNotBlank() &&
                floor.trim().isNotBlank() &&
                moveInDate.trim().isNotBlank() &&
                isProofValid
    }

    fun isPriceValid(price: String): Boolean {
        val parsed = price.trim().toDoubleOrNull() ?: return false
        return parsed > 0.0
    }

    fun isDescriptionValid(description: String, minLength: Int = 10): Boolean {
        return description.trim().length >= minLength
    }

    fun isListingStep1Valid(
        title: String,
        categoryOrBhk: String,
        price: String,
        description: String,
        minDescriptionLength: Int = 10
    ): Boolean {
        return title.trim().isNotBlank() &&
                categoryOrBhk.trim().isNotBlank() &&
                isPriceValid(price) &&
                isDescriptionValid(description, minDescriptionLength)
    }

    fun isStep2ValidForType(
        type: String,
        wingFlatNumber: String = "",
        propertyType: String = "",
        bhkType: String = ""
    ): Boolean {
        return if (type.equals("PROPERTY", ignoreCase = true)) {
            wingFlatNumber.trim().isNotBlank() &&
                    propertyType.trim().isNotBlank() &&
                    bhkType.trim().isNotBlank()
        } else {
            true
        }
    }

    fun getStep1ValidationError(
        type: String,
        title: String,
        category: String,
        price: String,
        description: String,
        wingFlatNumber: String = "",
        bhkType: String = "",
        propertyType: String = "",
        minDescriptionLength: Int = 10
    ): String? {
        if (type.equals("PROPERTY", ignoreCase = true)) {
            if (wingFlatNumber.trim().isBlank()) return "Please enter wing / flat number."
            if (bhkType.trim().isBlank()) return "Please select a BHK configuration."
            if (propertyType.trim().isBlank()) return "Please select a property type."
            return null
        } else {
            if (title.trim().isBlank()) return "Please enter a listing title."
            if (category.trim().isBlank()) return "Please select a category."
            if (!isPriceValid(price)) return "Please enter a valid price greater than 0."
            if (!isDescriptionValid(description, minDescriptionLength)) {
                return "Description must be at least $minDescriptionLength characters."
            }
            return null
        }
    }

    fun getStep2ValidationError(
        type: String,
        price: String = "",
        wingFlatNumber: String = "",
        propertyType: String = "",
        bhkType: String = ""
    ): String? {
        if (type.equals("PROPERTY", ignoreCase = true)) {
            if (!isPriceValid(price)) return "Please enter a valid monthly rent greater than 0."
            return null
        } else {
            return null
        }
    }

    fun getPublishValidationError(
        type: String,
        title: String,
        category: String,
        price: String,
        description: String,
        wingFlatNumber: String = "",
        bhkType: String = "",
        propertyType: String = "",
        minDescriptionLength: Int = 10
    ): String? {
        if (type.equals("HOME_BUSINESS", ignoreCase = true)) {
            val hbResult = validateHomeBusiness(
                title = title,
                category = category,
                description = description
            )
            return hbResult.titleError ?: hbResult.categoryError ?: hbResult.descriptionError
        } else if (type.equals("PROPERTY", ignoreCase = true)) {
            if (wingFlatNumber.trim().isBlank()) return "Please enter wing / flat number."
            if (bhkType.trim().isBlank()) return "Please select a BHK configuration."
            if (propertyType.trim().isBlank()) return "Please select a property type."
            if (!isPriceValid(price)) return "Please enter a valid monthly rent greater than 0."
            return null
        } else {
            if (title.trim().isBlank()) return "Please enter a listing title."
            if (category.trim().isBlank()) return "Please select a category."
            if (!isPriceValid(price)) return "Please enter a valid price greater than 0."
            if (!isDescriptionValid(description, minDescriptionLength)) {
                return "Description must be at least $minDescriptionLength characters."
            }
            return null
        }
    }

    fun isListingValid(title: String, description: String, category: String): Boolean {
        return title.isNotBlank() && description.isNotBlank() && category.isNotBlank()
    }

    // HOME_BUSINESS Validation
    data class HomeBusinessValidationResult(
        val isValid: Boolean,
        val titleError: String? = null,
        val categoryError: String? = null,
        val descriptionError: String? = null,
        val whatsappError: String? = null,
        val instagramError: String? = null
    )

    private val instagramRegex = Regex("^[A-Za-z0-9._]{1,30}$")

    fun validateHomeBusiness(
        title: String,
        category: String,
        description: String,
        whatsappNumber: String = "",
        isSameAsContact: Boolean = true,
        instagramHandle: String = ""
    ): HomeBusinessValidationResult {
        val trimmedTitle = title.trim()
        val titleError = when {
            trimmedTitle.isEmpty() -> "Please enter a business title."
            trimmedTitle.length > 80 -> "Business title must be 80 characters or fewer."
            else -> null
        }

        val categoryError = if (category.trim().isEmpty()) "Please select a category." else null

        val trimmedDesc = description.trim()
        val descriptionError = when {
            trimmedDesc.isEmpty() -> "Please enter a description."
            trimmedDesc.length > 500 -> "Description must be 500 characters or fewer."
            else -> null
        }

        val whatsappError = if (!isSameAsContact) {
            val cleanedPhone = whatsappNumber.trim().filter { it.isDigit() }
            if (cleanedPhone.length < 10) "Please enter a valid WhatsApp phone number (at least 10 digits)." else null
        } else null

        val trimmedInsta = instagramHandle.trim().removePrefix("@")
        val instagramError = if (trimmedInsta.isNotEmpty() && !instagramRegex.matches(trimmedInsta)) {
            "Instagram handle must be 1-30 characters (letters, numbers, periods, underscores)."
        } else null

        val isValid = titleError == null &&
                categoryError == null &&
                descriptionError == null &&
                whatsappError == null &&
                instagramError == null

        return HomeBusinessValidationResult(
            isValid = isValid,
            titleError = titleError,
            categoryError = categoryError,
            descriptionError = descriptionError,
            whatsappError = whatsappError,
            instagramError = instagramError
        )
    }
}
