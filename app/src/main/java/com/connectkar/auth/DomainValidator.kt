package com.connectkar.auth

import java.util.regex.Pattern

sealed class DomainValidationResult {
    data class Success(val domain: String) : DomainValidationResult()
    object EmptyEmail : DomainValidationResult()
    object InvalidFormat : DomainValidationResult()
    data class UnauthorizedDomain(
        val attemptedDomain: String,
        val allowedDomains: List<String>
    ) : DomainValidationResult()
}

object DomainValidator {

    val AUTHORIZED_RESIDENT_DOMAINS: List<String> = listOf(
        "resident.community",
        "society.org",
        "connectkar.com",
        "resident.connectkar.internal"
    )

    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    )

    fun validateResidentEmail(email: String): DomainValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return DomainValidationResult.EmptyEmail
        }

        val matcher = EMAIL_PATTERN.matcher(trimmed)
        if (!matcher.matches()) {
            return DomainValidationResult.InvalidFormat
        }

        val domain = matcher.group(1)?.lowercase() ?: return DomainValidationResult.InvalidFormat
        val isAuthorized = AUTHORIZED_RESIDENT_DOMAINS.any { allowed ->
            domain == allowed || domain.endsWith(".$allowed")
        }

        return if (isAuthorized) {
            DomainValidationResult.Success(domain)
        } else {
            DomainValidationResult.UnauthorizedDomain(
                attemptedDomain = domain,
                allowedDomains = AUTHORIZED_RESIDENT_DOMAINS
            )
        }
    }

    fun isAuthorized(email: String): Boolean {
        return validateResidentEmail(email) is DomainValidationResult.Success
    }
}
