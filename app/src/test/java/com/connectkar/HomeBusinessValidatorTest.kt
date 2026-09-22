package com.connectkar

import com.connectkar.data.local.HomeBusinessDetailsJson
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.MoshiHelper
import com.connectkar.data.local.homeBusinessDetails
import com.connectkar.ui.FormValidators
import org.junit.Assert.*
import org.junit.Test

class HomeBusinessValidatorTest {

    // ==========================================
    // 1. Validation Rules (Section B) Unit Tests
    // ==========================================

    @Test
    fun testValidation_validInputs_returnsValid() {
        val result = FormValidators.validateHomeBusiness(
            title = "Delicious Homemade Cakes",
            category = "Baking & Food",
            description = "Freshly baked artisan sourdough bread and customized birthday cakes.",
            whatsappNumber = "9876543210",
            isSameAsContact = true,
            instagramHandle = "sweet_delights.in"
        )
        assertTrue("Expected validation to pass", result.isValid)
        assertNull(result.titleError)
        assertNull(result.categoryError)
        assertNull(result.descriptionError)
        assertNull(result.whatsappError)
        assertNull(result.instagramError)
    }

    @Test
    fun testValidation_blankTitle_returnsTitleError() {
        val result = FormValidators.validateHomeBusiness(
            title = "   ",
            category = "Baking & Food",
            description = "Freshly baked goods delivered daily to your doorstep."
        )
        assertFalse(result.isValid)
        assertEquals("Please enter a business title.", result.titleError)
    }

    @Test
    fun testValidation_titleExceeds80Chars_returnsTitleError() {
        val longTitle = "A".repeat(81)
        val result = FormValidators.validateHomeBusiness(
            title = longTitle,
            category = "Baking & Food",
            description = "Freshly baked goods delivered daily to your doorstep."
        )
        assertFalse(result.isValid)
        assertEquals("Business title must be 80 characters or fewer.", result.titleError)
    }

    @Test
    fun testValidation_titleBoundary80Chars_returnsValid() {
        val title80 = "A".repeat(80)
        val result = FormValidators.validateHomeBusiness(
            title = title80,
            category = "Baking & Food",
            description = "Freshly baked goods delivered daily to your doorstep."
        )
        assertNull(result.titleError)
    }

    @Test
    fun testValidation_blankCategory_returnsCategoryError() {
        val result = FormValidators.validateHomeBusiness(
            title = "Organic Terrace Greens",
            category = "   ",
            description = "Locally grown hydroponic and organic vegetables delivered."
        )
        assertFalse(result.isValid)
        assertEquals("Please select a category.", result.categoryError)
    }

    @Test
    fun testValidation_blankDescription_returnsDescriptionError() {
        val result = FormValidators.validateHomeBusiness(
            title = "Math & Science Tutoring",
            category = "Tutoring & Classes",
            description = "   "
        )
        assertFalse(result.isValid)
        assertEquals("Please enter a description.", result.descriptionError)
    }

    @Test
    fun testValidation_descriptionExceeds500Chars_returnsDescriptionError() {
        val longDesc = "D".repeat(501)
        val result = FormValidators.validateHomeBusiness(
            title = "Custom Embroidery",
            category = "Handicrafts & Art",
            description = longDesc
        )
        assertFalse(result.isValid)
        assertEquals("Description must be 500 characters or fewer.", result.descriptionError)
    }

    @Test
    fun testValidation_descriptionBoundary500Chars_returnsValid() {
        val desc500 = "D".repeat(500)
        val result = FormValidators.validateHomeBusiness(
            title = "Custom Embroidery",
            category = "Handicrafts & Art",
            description = desc500
        )
        assertNull(result.descriptionError)
    }

    @Test
    fun testValidation_customWhatsAppValid_returnsNoError() {
        val result = FormValidators.validateHomeBusiness(
            title = "Handmade Clay Pottery",
            category = "Handicrafts & Art",
            description = "Terracotta pots and handcrafted cups.",
            whatsappNumber = "+91 98765-43210",
            isSameAsContact = false
        )
        assertNull(result.whatsappError)
    }

    @Test
    fun testValidation_customWhatsAppTooShort_returnsWhatsAppError() {
        val result = FormValidators.validateHomeBusiness(
            title = "Handmade Clay Pottery",
            category = "Handicrafts & Art",
            description = "Terracotta pots and handcrafted cups.",
            whatsappNumber = "12345",
            isSameAsContact = false
        )
        assertFalse(result.isValid)
        assertEquals("Please enter a valid WhatsApp phone number (at least 10 digits).", result.whatsappError)
    }

    @Test
    fun testValidation_sameAsContactSkipsWhatsAppValidation() {
        val result = FormValidators.validateHomeBusiness(
            title = "Handmade Clay Pottery",
            category = "Handicrafts & Art",
            description = "Terracotta pots and handcrafted cups.",
            whatsappNumber = "", // Empty or invalid, but isSameAsContact is true
            isSameAsContact = true
        )
        assertNull(result.whatsappError)
    }

    @Test
    fun testValidation_validInstagramHandles_returnNoError() {
        val validHandles = listOf(
            "connectkar_biz",
            "@connectkar.official",
            "bakery_123",
            "A.B_C",
            "user123"
        )
        for (handle in validHandles) {
            val result = FormValidators.validateHomeBusiness(
                title = "Test Business",
                category = "Services",
                description = "Providing high quality home services.",
                instagramHandle = handle
            )
            assertNull("Expected handle '$handle' to be valid", result.instagramError)
        }
    }

    @Test
    fun testValidation_blankInstagramHandle_isOptional_returnsNoError() {
        val result = FormValidators.validateHomeBusiness(
            title = "Test Business",
            category = "Services",
            description = "Providing high quality home services.",
            instagramHandle = ""
        )
        assertNull(result.instagramError)
    }

    @Test
    fun testValidation_invalidInstagramHandle_returnsInstagramError() {
        val invalidHandles = listOf(
            "invalid handle with spaces",
            "invalid!symbols#$",
            "@this_instagram_handle_is_way_too_long_exceeding_thirty_characters",
            "bad@handle"
        )
        for (handle in invalidHandles) {
            val result = FormValidators.validateHomeBusiness(
                title = "Test Business",
                category = "Services",
                description = "Providing high quality home services.",
                instagramHandle = handle
            )
            assertFalse("Expected handle '$handle' to fail validation", result.isValid)
            assertEquals(
                "Instagram handle must be 1-30 characters (letters, numbers, periods, underscores).",
                result.instagramError
            )
        }
    }

    @Test
    fun testGetPublishValidationError_forHomeBusiness() {
        val errorBlank = FormValidators.getPublishValidationError(
            type = "HOME_BUSINESS",
            title = "",
            category = "Baking & Food",
            price = "", // Price should not be required for home business
            description = "Some description here"
        )
        assertEquals("Please enter a business title.", errorBlank)

        val errorValid = FormValidators.getPublishValidationError(
            type = "HOME_BUSINESS",
            title = "Valid Business Title",
            category = "Baking & Food",
            price = "", // Price is not validated as numeric > 0 for home business
            description = "Valid long enough description"
        )
        assertNull("Expected no publish validation error for valid home business", errorValid)
    }

    // =========================================================================
    // 2. Backward Compatibility & Null / Empty Serialization Tests
    // =========================================================================

    @Test
    fun testHomeBusinessDetailsJson_defaultValues() {
        val defaultObj = HomeBusinessDetailsJson()
        assertEquals("", defaultObj.category)
        assertEquals("", defaultObj.operatesFromFlat)
        assertEquals("", defaultObj.businessHours)
        assertEquals("", defaultObj.priceRange)
        assertNull(defaultObj.whatsappNumber)
        assertNull(defaultObj.instagramHandle)
        assertTrue(defaultObj.isRecurring)
    }

    @Test
    fun testHomeBusinessDetailsJson_parseEmptyJson_populatesDefaults() {
        val json = "{}"
        val parsed = MoshiHelper.fromJson<HomeBusinessDetailsJson>(json)
        assertNotNull(parsed)
        parsed?.let {
            assertEquals("", it.category)
            assertEquals("", it.operatesFromFlat)
            assertEquals("", it.businessHours)
            assertEquals("", it.priceRange)
            assertNull(it.whatsappNumber)
            assertNull(it.instagramHandle)
            assertTrue(it.isRecurring)
        }
    }

    @Test
    fun testHomeBusinessDetailsJson_parseLegacyJson_withMissingOptionalFields() {
        // Legacy data created before new fields were added or with nulls
        val legacyJson = """
            {
                "category": "Baking & Food",
                "operatesFromFlat": "Tower B-402",
                "businessHours": "10 AM - 6 PM"
            }
        """.trimIndent()

        val parsed = MoshiHelper.fromJson<HomeBusinessDetailsJson>(legacyJson)
        assertNotNull(parsed)
        parsed?.let {
            assertEquals("Baking & Food", it.category)
            assertEquals("Tower B-402", it.operatesFromFlat)
            assertEquals("10 AM - 6 PM", it.businessHours)
            assertEquals("", it.priceRange)
            assertNull(it.whatsappNumber)
            assertNull(it.instagramHandle)
            assertTrue(it.isRecurring)
        }
    }

    @Test
    fun testHomeBusinessDetailsJson_parseExplicitNullFields() {
        val jsonWithNulls = """
            {
                "category": "Handicrafts & Art",
                "operatesFromFlat": "A-101",
                "businessHours": "Weekends only",
                "priceRange": "₹200 - ₹1000",
                "whatsappNumber": null,
                "instagramHandle": null,
                "isRecurring": false
            }
        """.trimIndent()

        val parsed = MoshiHelper.fromJson<HomeBusinessDetailsJson>(jsonWithNulls)
        assertNotNull(parsed)
        parsed?.let {
            assertEquals("Handicrafts & Art", it.category)
            assertEquals("A-101", it.operatesFromFlat)
            assertEquals("Weekends only", it.businessHours)
            assertEquals("₹200 - ₹1000", it.priceRange)
            assertNull(it.whatsappNumber)
            assertNull(it.instagramHandle)
            assertFalse(it.isRecurring)
        }
    }

    @Test
    fun testHomeBusinessDetailsJson_fullSerializationAndDeserialization() {
        val original = HomeBusinessDetailsJson(
            category = "Fitness & Yoga",
            operatesFromFlat = "Clubhouse / Flat 204",
            businessHours = "6:00 AM - 9:00 AM",
            priceRange = "₹1500 / month",
            whatsappNumber = "9876543210",
            instagramHandle = "yoga_with_anita",
            isRecurring = true
        )

        val json = MoshiHelper.toJson(original)
        assertTrue(json.contains("\"category\":\"Fitness & Yoga\""))
        assertTrue(json.contains("\"whatsappNumber\":\"9876543210\""))
        assertTrue(json.contains("\"instagramHandle\":\"yoga_with_anita\""))

        val restored = MoshiHelper.fromJson<HomeBusinessDetailsJson>(json)
        assertEquals(original, restored)
    }

    @Test
    fun testListingEntity_homeBusinessDetailsExtension_fallbackOnBlankOrInvalidJson() {
        // 1. When detailsJson is empty string
        val entityBlankJson = ListingEntity(
            id = 10,
            type = "HOME_BUSINESS",
            title = "Handmade Chocolates",
            description = "Rich dark and milk chocolate truffles.",
            detailsJson = ""
        )
        val detailsFromBlank = entityBlankJson.homeBusinessDetails()
        assertNotNull(detailsFromBlank)
        assertEquals("", detailsFromBlank.category)
        assertNull(detailsFromBlank.whatsappNumber)

        // 2. When detailsJson is invalid/corrupt JSON
        val entityCorruptJson = ListingEntity(
            id = 11,
            type = "HOME_BUSINESS",
            title = "Handmade Chocolates",
            description = "Rich dark and milk chocolate truffles.",
            detailsJson = "invalid_json_data_123"
        )
        val detailsFromCorrupt = entityCorruptJson.homeBusinessDetails()
        assertNotNull(detailsFromCorrupt)
        assertEquals("", detailsFromCorrupt.category)

        // 3. When detailsJson is valid
        val validJson = MoshiHelper.toJson(
            HomeBusinessDetailsJson(
                category = "Food",
                priceRange = "₹100 - ₹500"
            )
        )
        val entityValid = ListingEntity(
            id = 12,
            type = "HOME_BUSINESS",
            title = "Handmade Chocolates",
            description = "Rich dark and milk chocolate truffles.",
            detailsJson = validJson
        )
        val detailsValid = entityValid.homeBusinessDetails()
        assertEquals("Food", detailsValid.category)
        assertEquals("₹100 - ₹500", detailsValid.priceRange)
    }
}
