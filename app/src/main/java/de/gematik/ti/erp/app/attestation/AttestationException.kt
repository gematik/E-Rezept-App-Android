package de.gematik.ti.erp.app.attestation

class AttestationException(
    attestationExceptionType: AttestationExceptionType,
    message: String? = null,
    cause: Throwable? = null
) : Exception("$attestationExceptionType: $message", cause) {

    enum class AttestationExceptionType {
        ATTESTATION_FAILED,
        NONCE_MISMATCH,
        HOSTNAME_NOT_VERIFIED,
        BASIC_INTEGRITY_REQUIRED,
        CTS_PROFILE_MATCH_NEEDED,
        PLAY_SERVICES_VERSION_MISMATCH,
    }
}
