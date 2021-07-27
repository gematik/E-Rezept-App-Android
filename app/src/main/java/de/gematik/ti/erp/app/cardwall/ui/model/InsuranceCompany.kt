package de.gematik.ti.erp.app.cardwall.ui.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InsuranceList(
    val entries: List<InsuranceCompany>
)

@JsonClass(generateAdapter = true)
data class InsuranceCompany(
    val name: String,
    val email: String
)
