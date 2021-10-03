package de.gematik.ti.erp.app.attestation

data class SafetynetResult(
    val jws: String
) : Attestation.Result
