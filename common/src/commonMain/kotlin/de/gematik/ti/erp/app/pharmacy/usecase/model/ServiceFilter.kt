/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_20285#7",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [ServiceFilter] filter is to filter only by services that are provided."
)
sealed class ServiceFilter(
    open val courier: Boolean = false,
    open val shipment: Boolean = false,
    open val pickup: Boolean = false,
    open val availableServiceCodes: Set<String> = emptySet()
) {

    enum class ServiceType(val code: String, val text: String) {
        PICKUP(code = "10", text = "Handverkauf"),
        COURIER(code = "30", text = "Botendienst"),
        SHIPMENT(code = "40", text = "Versand")
    }

    /**
     * Map a code to a German text keyword used in `nearPharmacy` text-based searches.
     * The API only supports a `text` parameter,
     * so specialty/characteristic filters must be expressed as text keywords.
     */
    interface TextKeyword {
        val code: String
        val text: String
    }

    enum class AvailableServiceTextKeyword(override val code: String, override val text: String) : TextKeyword {
        STERILE_COMPOUNDING(code = "50", text = "Sterilherstellung"),
        BLOOD_PRESSURE(code = "60", text = "Bluthochdruck"),
        INHALATION(code = "70", text = "Inhalationstechnik"),
        POLYMEDICATION(code = "80", text = "Polymedikation"),
        CANCER_THERAPY(code = "90", text = "Krebstherapie"),
        ORGAN_TRANSPLANT(code = "100", text = "Organtransplantation"),
        ALLERGY_TEST(code = "allergietest", text = "Allergietest"),
        VACCINATION(code = "impfung", text = "Impfung"),
        BODY_VALUES(code = "koerperwerte", text = "Körperwerte"),
        TRAVEL_MEDICINE(code = "reisemedizin-beratung", text = "Reisemedizin");

        companion object {
            fun textForCode(code: String): String = entries.textForCode(code)
        }
    }

    enum class OnSiteFeatureTextKeyword(override val code: String, override val text: String) : TextKeyword {
        PICKUP_STATION(code = "abholautomat", text = "Abholautomat"),
        BARRIER_FREE(code = "barrierefrei", text = "Barrierefrei"),
        PUBLIC_TRANSPORT(code = "oepnv", text = "ÖPNV"),
        PARKING(code = "parkmoeglichkeit", text = "Parkmöglichkeit");

        companion object {
            fun textForCode(code: String): String = entries.textForCode(code)
        }
    }

    // Common properties for FHIR VZD mapping
    val fhirVzdCourier: String? get() = if (courier) courierCode else null
    val fhirVzdShipment: String? get() = if (shipment) shipmentCode else null
    val fhirVzdPickup: String? get() = if (pickup) pickupCode else null

    // Abstract properties to be implemented by subclasses
    abstract val courierCode: String
    abstract val shipmentCode: String
    abstract val pickupCode: String

    /**
     * Generate text search string for FHIR VZD nearby search.
     *
     * For nearPharmacy searches, API only supports a `text` parameter.
     * All filters — service types, available service codes, and on-site feature codes are
     * converted to German text keywords and combined into a single search string.
     *
     * @param additionalText optional user-provided search text
     * @param onSiteFeatureCodes on-site feature codes (e.g., "abholautomat", "barrierefrei")
     *        to include as German text keywords in the search
     */
    fun buildTextSearch(
        additionalText: String? = null,
        onSiteFeatureCodes: Set<String> = emptySet()
    ): String? {
        val serviceTexts = buildList {
            if (pickup) add(ServiceType.PICKUP.text)
            if (courier) add(ServiceType.COURIER.text)
            if (shipment) add(ServiceType.SHIPMENT.text)
            addAll(availableServiceCodes.map { AvailableServiceTextKeyword.textForCode(it) })
            addAll(onSiteFeatureCodes.map { OnSiteFeatureTextKeyword.textForCode(it) })
        }

        return when {
            serviceTexts.isEmpty() && additionalText.isNullOrBlank() -> null
            serviceTexts.isEmpty() -> additionalText?.trim()
            additionalText.isNullOrBlank() -> serviceTexts.joinToString(" ")
            else -> "${serviceTexts.joinToString(" ")} ${additionalText.trim()}"
        }
    }

    companion object {
        /**
         * Creates the appropriate ServiceFilter implementation based on search context
         */
        fun create(
            courier: Boolean = false,
            shipment: Boolean = false,
            pickup: Boolean = false,
            availableServiceCodes: Set<String> = emptySet()
        ): ServiceFilter = CodedServiceFilter(
            courier = courier,
            shipment = shipment,
            pickup = pickup,
            availableServiceCodes = availableServiceCodes
        )
    }
}

// Returns German text for a code, if no mapping exists falls back to code.
private fun <T : ServiceFilter.TextKeyword> Collection<T>.textForCode(code: String): String =
    firstOrNull { it.code == code }?.text ?: code

// https://simplifier.net/packages/de.gematik.fhir.directory/0.11.24/files/2723324
data class CodedServiceFilter(
    override val courier: Boolean = false, // (30)
    override val shipment: Boolean = false, // (40)
    override val pickup: Boolean = false, // (10)
    override val availableServiceCodes: Set<String> = emptySet()
) : ServiceFilter(courier, shipment, pickup, availableServiceCodes) {

    override val courierCode: String = ServiceType.COURIER.code
    override val shipmentCode: String = ServiceType.SHIPMENT.code
    override val pickupCode: String = ServiceType.PICKUP.code
}
