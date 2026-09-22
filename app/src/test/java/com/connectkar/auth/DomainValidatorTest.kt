package com.connectkar.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainValidatorTest {

    @Test
    fun authorizedDomains_areAccepted() {
        val validEmails = listOf(
            "john.doe@resident.community",
            "alice@society.org",
            "manager@connectkar.com",
            "security@resident.connectkar.internal",
            "tower.a@resident.community"
        )

        for (email in validEmails) {
            assertTrue("Expected $email to be authorized", DomainValidator.isAuthorized(email))
            val result = DomainValidator.validateResidentEmail(email)
            assertTrue("Expected Success for $email", result is DomainValidationResult.Success)
        }
    }

    @Test
    fun unauthorizedDomains_areRejected() {
        val unauthorizedEmails = listOf(
            "user@gmail.com",
            "intruder@yahoo.com",
            "test@outlook.com",
            "resident@connectkar.fake",
            "hacker@suspicious-domain.org"
        )

        for (email in unauthorizedEmails) {
            assertFalse("Expected $email to be rejected", DomainValidator.isAuthorized(email))
            val result = DomainValidator.validateResidentEmail(email)
            assertTrue("Expected UnauthorizedDomain for $email", result is DomainValidationResult.UnauthorizedDomain)
        }
    }

    @Test
    fun emptyOrInvalidEmail_isRejected() {
        assertEquals(
            DomainValidationResult.EmptyEmail,
            DomainValidator.validateResidentEmail("")
        )
        assertEquals(
            DomainValidationResult.EmptyEmail,
            DomainValidator.validateResidentEmail("   ")
        )
        assertEquals(
            DomainValidationResult.InvalidFormat,
            DomainValidator.validateResidentEmail("not-an-email")
        )
        assertEquals(
            DomainValidationResult.InvalidFormat,
            DomainValidator.validateResidentEmail("@missingusername.com")
        )
    }
}
