package com.example.ui

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

    fun isListingValid(title: String, description: String, category: String): Boolean {
        return title.isNotBlank() && description.isNotBlank() && category.isNotBlank()
    }
}
