/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTypeCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirVzdSpecialtyType
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdAvailableTime.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdSpecialty.Companion.getSpecialtyTypes
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirVZDHealthcareService(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String?,
    @SerialName("type") val type: List<FhirVzdType>? = emptyList(),
    @SerialName("specialty") val specialty: List<FhirVzdSpecialty> = emptyList(),
    @SerialName("telecom") val telecom: List<FhirVzdTelecom> = emptyList(),
    @SerialName("identifier") val identifiers: List<FhirVzdIdentifier> = emptyList(),
    @SerialName("availableTime") val availableTime: List<FhirVzdAvailableTime> = emptyList(),
    @SerialName("notAvailable") val notAvailable: List<FhirVzdNotAvailable> = emptyList(),
    @SerialName("availabilityExceptions") val availabilityExceptions: String? = null
) {
    companion object {
        fun JsonElement.getHealthcareService(): FhirVZDHealthcareService {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }

        fun FhirVZDHealthcareService.getOpeningHours(): OpeningHoursErpModel {
            return OpeningHoursErpModel(availableTime.toErpModel())
        }

        fun FhirVZDHealthcareService.getSpecialities(): List<FhirVzdSpecialtyType> {
            return specialty.getSpecialtyTypes()
        }
    }
}

@Serializable
internal data class FhirVzdTelecom(
    @SerialName("system") val system: String?,
    @SerialName("value") val value: String?
) {
    companion object {

        private const val PHONE = "phone"
        private const val EMAIL = "email"
        private const val URL = "url"

        fun List<FhirVzdTelecom>.toErpModel(): FhirContactInformationErpModel {
            // required only when we activate zuweisung-ohne-telematik-id
            // val deliveryEndPoint = endpoints?.find { it.connectionType == ConnectionType.Delivery }
            // val onlineEndPoint = endpoints?.find { it.connectionType == ConnectionType.Shipment }
            // val pickupEndPoint = endpoints?.find { it.connectionType == ConnectionType.OnPremise }

            return FhirContactInformationErpModel(
                phone = getPhone(),
                mail = getEmail(),
                url = getUrl(),
                // required only when we activate zuweisung-ohne-telematik-id
                pickUpUrl = null,
                deliveryUrl = null,
                onlineServiceUrl = null
            )
        }

        private fun List<FhirVzdTelecom>.getPhone(): String {
            return this.find { it.system == PHONE }?.value.orEmpty()
        }

        private fun List<FhirVzdTelecom>.getEmail(): String {
            return this.find { it.system == EMAIL }?.value.orEmpty()
        }

        private fun List<FhirVzdTelecom>.getUrl(): String {
            return this.find { it.system == URL }?.value.orEmpty()
        }
    }
}

@Serializable
internal data class FhirPeriod(
    @SerialName("start") val start: String?,
    @SerialName("end") val end: String?
)

@Serializable
internal data class FhirVzdNotAvailable(
    @SerialName("description") val description: String?,
    @SerialName("during") val during: FhirPeriod?
)

@Serializable
internal data class FhirVzdAvailableTime(
    @SerialName("daysOfWeek") val daysOfWeek: List<String> = emptyList(),
    @SerialName("availableStartTime") val availableStartTime: String? = null,
    @SerialName("availableEndTime") val availableEndTime: String? = null,
    @SerialName("specialOpeningTimes") val specialOpeningTimesExtension: SpecialOpeningTimesExtensions? = null
) {
    companion object {
        fun List<FhirVzdAvailableTime>.toErpModel(): Map<DayOfWeek, List<OpeningTimeErpModel>> {
            val openingTimeMap = mutableMapOf<DayOfWeek, List<OpeningTimeErpModel>>()

            forEach { element ->
                val dayOfWeek = element.daysOfWeek.firstOrNull()?.toDayOfWeek()
                val startTime = element.availableStartTime?.let { LocalTime.parse(it) }
                val endTime = element.availableEndTime?.let { LocalTime.parse(it) }

                letNotNull(dayOfWeek, startTime, endTime) { day, start, end ->
                    // list is overkill
                    openingTimeMap[day] = listOf(OpeningTimeErpModel(start, end))
                }
            }
            return openingTimeMap.toMap()
        }

        private fun String.toDayOfWeek(): DayOfWeek? =
            mapOf(
                "mon" to DayOfWeek.MONDAY,
                "tue" to DayOfWeek.TUESDAY,
                "wed" to DayOfWeek.WEDNESDAY,
                "thu" to DayOfWeek.THURSDAY,
                "fri" to DayOfWeek.FRIDAY,
                "sat" to DayOfWeek.SATURDAY,
                "sun" to DayOfWeek.SUNDAY
            )[this.lowercase()]
    }
}

// https://gematik.de/fhir/directory/StructureDefinition/SpecialOpeningTimesEX
@Serializable
internal data class SpecialOpeningTimesExtensions(
    @SerialName("extension") val extension: List<SpecialOpeningTimes> = emptyList()
)

@Serializable
internal data class SpecialOpeningTimes(
    @SerialName("period") val period: FhirExtension?, // valuePeriod
    @SerialName("qualifier") val qualifier: String? // valueCoding
)

@Serializable
internal data class FhirVzdSpecialty(
    @SerialName("coding") val codings: List<FhirTypeCoding> = emptyList(),
    @SerialName("text") val text: String?
) {
    companion object {
        // using the app-vzd since only that matches with the data from APO-VZD backend
        private const val SERVICE_PROVIDER_IDENTIFIER = "apo-vzd"

        private fun List<FhirVzdSpecialty>.getServiceProvider(): FhirVzdSpecialty? =
            find { it.text == SERVICE_PROVIDER_IDENTIFIER }

        private fun FhirVzdSpecialty.mapToSpecialtyTypes(): List<FhirVzdSpecialtyType> =
            codings.map { FhirVzdSpecialtyType.fromCode(it.code) }

        fun List<FhirVzdSpecialty>.getSpecialtyTypes(): List<FhirVzdSpecialtyType> =
            getServiceProvider()?.mapToSpecialtyTypes().orEmpty()
    }
}
