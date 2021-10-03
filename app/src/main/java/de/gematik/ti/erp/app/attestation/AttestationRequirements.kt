package de.gematik.ti.erp.app.attestation

interface AttestationRequirements {
    val requireBasicIntegrity: Boolean
    val requireCTSProfileMatch: Boolean
    val requireEvaluationTypeBasic: Boolean
    val requireEvaluationTypeHardwareBacked: Boolean
}

data class SafetynetAttestationRequirements(
    override val requireBasicIntegrity: Boolean = true,
    override val requireCTSProfileMatch: Boolean = true,
    override val requireEvaluationTypeBasic: Boolean = true,
    override val requireEvaluationTypeHardwareBacked: Boolean = true
) : AttestationRequirements
