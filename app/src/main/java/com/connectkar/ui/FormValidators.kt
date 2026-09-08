package com.connectkar.ui

object FormValidators {
    fun isStep1Valid(fullName: String, phoneNumber: String): Boolean {
        return fullName.trim().isNotBlank() && phoneNumber.trim().length >= 10
    }

    fun isStep2Valid(
        blockTower: String,
        flatNumber: String,
        floor: String,
        moveInDate: String,
        proofDocumentUri: String
    ): Boolean {
        return blockTower.trim().isNotBlank() &&
                flatNumber.trim().isNotBlank() &&
                floor.trim().isNotBlank() &&
                moveInDate.trim().isNotBlank() &&
                proofDocumentUri.trim().isNotBlank()
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
        if (type.equals("PROPERTY", ignoreCase = true)) {
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
}
