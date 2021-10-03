package de.gematik.ti.erp.app.attestation

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.jose4j.jwt.consumer.JwtContext

class SafetynetReport(jwtContext: JwtContext, private val nonce: ByteArray) : Attestation.Report {

    val timestampMS = jwtContext.jwtClaims.getClaimValue("timestampMs") as Long

    private val nonceFromClaim =
        jwtContext.jwtClaims.getClaimValue("nonce").toString().decodeBase64()
    private val apkPackageName = jwtContext.jwtClaims.getClaimValue("apkPackageName")?.toString()
    private val basicIntegrity = jwtContext.jwtClaims.getClaimValue("basicIntegrity") == true
    private val ctsProfileMatch = jwtContext.jwtClaims.getClaimValue("ctsProfileMatch") == true

    private val error = jwtContext.jwtClaims.getClaimValue("error")?.toString()
    private val advice = jwtContext.jwtClaims.getClaimValue("advice")?.toString()

    fun attestationCheckOK(attestationRequirements: AttestationRequirements) {
        if (attestationRequirements.requireBasicIntegrity != basicIntegrity) {
            throw AttestationException(
                AttestationException.AttestationExceptionType.BASIC_INTEGRITY_REQUIRED,
                "basicIntegrity violated ($advice)."
            )
        }

        if (attestationRequirements.requireCTSProfileMatch != ctsProfileMatch) {
            throw AttestationException(
                AttestationException.AttestationExceptionType.CTS_PROFILE_MATCH_NEEDED,
                "ctsProfile not matched ($advice)."
            )
        }

        if (nonceFromClaim != nonce.toByteString()) {
            throw AttestationException(
                AttestationException.AttestationExceptionType.NONCE_MISMATCH,
                "nonce mismatch."
            )
        }
    }
}
