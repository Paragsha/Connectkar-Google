package com.connectkar

import com.connectkar.ui.FormValidators
import org.junit.Assert.*
import org.junit.Test

class FormValidatorsTest {

    @Test
    fun testIsPriceValid() {
        assertFalse(FormValidators.isPriceValid(""))
        assertFalse(FormValidators.isPriceValid("abc"))
        assertFalse(FormValidators.isPriceValid("0"))
        assertFalse(FormValidators.isPriceValid("-100"))
        assertFalse(FormValidators.isPriceValid("   "))

        assertTrue(FormValidators.isPriceValid("1"))
        assertTrue(FormValidators.isPriceValid("500.50"))
        assertTrue(FormValidators.isPriceValid("  25000  "))
    }

    @Test
    fun testIsDescriptionValid() {
        assertFalse(FormValidators.isDescriptionValid("", minLength = 10))
        assertFalse(FormValidators.isDescriptionValid("Short", minLength = 10))
        assertTrue(FormValidators.isDescriptionValid("1234567890", minLength = 10))
        assertTrue(FormValidators.isDescriptionValid("This is a clean and authentic wooden table.", minLength = 10))
    }

    @Test
    fun testIsListingStep1Valid() {
        // Invalid title
        assertFalse(
            FormValidators.isListingStep1Valid(
                title = "   ",
                categoryOrBhk = "Electronics",
                price = "1500",
                description = "Like new condition, used for 2 months"
            )
        )

        // Invalid category
        assertFalse(
            FormValidators.isListingStep1Valid(
                title = "Sony Headphones",
                categoryOrBhk = "",
                price = "1500",
                description = "Like new condition, used for 2 months"
            )
        )

        // Invalid price
        assertFalse(
            FormValidators.isListingStep1Valid(
                title = "Sony Headphones",
                categoryOrBhk = "Electronics",
                price = "0",
                description = "Like new condition, used for 2 months"
            )
        )

        // Short description
        assertFalse(
            FormValidators.isListingStep1Valid(
                title = "Sony Headphones",
                categoryOrBhk = "Electronics",
                price = "1500",
                description = "Short"
            )
        )

        // Valid
        assertTrue(
            FormValidators.isListingStep1Valid(
                title = "Sony Headphones",
                categoryOrBhk = "Electronics",
                price = "1500",
                description = "Like new condition, used for 2 months"
            )
        )
    }

    @Test
    fun testIsStep2ValidForType() {
        // Property requires wingFlatNumber, propertyType, bhkType
        assertFalse(
            FormValidators.isStep2ValidForType(
                type = "PROPERTY",
                wingFlatNumber = "",
                propertyType = "Apartment",
                bhkType = "2 BHK"
            )
        )
        assertFalse(
            FormValidators.isStep2ValidForType(
                type = "PROPERTY",
                wingFlatNumber = "Wing A 101",
                propertyType = "",
                bhkType = "2 BHK"
            )
        )
        assertFalse(
            FormValidators.isStep2ValidForType(
                type = "PROPERTY",
                wingFlatNumber = "Wing A 101",
                propertyType = "Apartment",
                bhkType = ""
            )
        )
        assertTrue(
            FormValidators.isStep2ValidForType(
                type = "PROPERTY",
                wingFlatNumber = "Wing A 101",
                propertyType = "Apartment",
                bhkType = "2 BHK"
            )
        )

        // Generic types default to true
        assertTrue(
            FormValidators.isStep2ValidForType(
                type = "MARKETPLACE"
            )
        )
    }

    @Test
    fun testValidationErrors() {
        assertEquals(
            "Please enter a listing title.",
            FormValidators.getStep1ValidationError(
                type = "MARKETPLACE",
                title = "",
                category = "Furniture",
                price = "2000",
                description = "Solid teakwood dining table"
            )
        )

        assertEquals(
            "Please select a category.",
            FormValidators.getStep1ValidationError(
                type = "MARKETPLACE",
                title = "Dining Table",
                category = "",
                price = "2000",
                description = "Solid teakwood dining table"
            )
        )

        assertEquals(
            "Please enter a valid price greater than 0.",
            FormValidators.getStep1ValidationError(
                type = "MARKETPLACE",
                title = "Dining Table",
                category = "Furniture",
                price = "invalid_price",
                description = "Solid teakwood dining table"
            )
        )

        assertEquals(
            "Description must be at least 10 characters.",
            FormValidators.getStep1ValidationError(
                type = "MARKETPLACE",
                title = "Dining Table",
                category = "Furniture",
                price = "2000",
                description = "Small"
            )
        )

        assertNull(
            FormValidators.getStep1ValidationError(
                type = "MARKETPLACE",
                title = "Dining Table",
                category = "Furniture",
                price = "2000",
                description = "Solid teakwood dining table in great condition"
            )
        )
    }

    @Test
    fun testIsStep2ValidProofDocumentUri() {
        // Valid remote HTTPS doc
        assertTrue(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "https://firebasestorage.googleapis.com/v0/b/app/o/proof.pdf?alt=media",
                isDebugBuild = false
            )
        )

        // Local content:// URI must be rejected on release and debug
        assertFalse(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "content://media/external/images/media/12345",
                isDebugBuild = false
            )
        )
        assertFalse(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "content://media/external/images/media/12345",
                isDebugBuild = true
            )
        )

        // Empty / blank URI must be rejected
        assertFalse(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "",
                isDebugBuild = false
            )
        )

        // Debug placeholder allowed only when isDebugBuild = true
        assertTrue(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "simulated_proof_of_residence.pdf",
                isDebugBuild = true
            )
        )
        assertTrue(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "proof_of_residence.pdf",
                isDebugBuild = true
            )
        )
        assertFalse(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "simulated_proof_of_residence.pdf",
                isDebugBuild = false
            )
        )
        assertFalse(
            FormValidators.isStep2Valid(
                blockTower = "Block A",
                flatNumber = "101",
                floor = "1",
                moveInDate = "2026-09-17",
                proofDocumentUri = "proof_of_residence.pdf",
                isDebugBuild = false
            )
        )
    }
}
