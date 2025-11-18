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

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirPeriod
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTypeCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModelPeriod
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdAvailableTime.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdSpecialty.Companion.getSpecialtyTypes
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.SpecialOpeningTimesWrapper.Companion.toSpecialOpeningTimeModel
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import de.gematik.ti.erp.app.utils.Reference
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Reference(
    url = "https://simplifier.net/vzd-fhir-directory/healthcareservicedirectory"
)
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

internal fun FhirVZDHealthcareService.parseSpecialOpeningTimes(): List<SpecialOpeningTimeErpModel> {
    return availableTime.mapNotNull { availableTimeElement ->
        try {
            availableTimeElement.specialOpeningTimesExtension.toSpecialOpeningTimeModel()
        } catch (e: Exception) {
            Napier.e(tag = "FHIRVZD", throwable = e) {
                "Error parsing special opening times for $id"
            }
            null
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
                url = getUrl()
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
internal data class FhirVzdNotAvailable(
    @SerialName("description") val description: String?,
    @SerialName("during") val during: FhirPeriod?
)

@Serializable
internal data class FhirVzdAvailableTime(
    @SerialName("daysOfWeek") val daysOfWeek: List<String> = emptyList(),
    @SerialName("availableStartTime") val availableStartTime: String? = null,
    @SerialName("availableEndTime") val availableEndTime: String? = null,
    @SerialName("extension") val specialOpeningTimesExtension: List<SpecialOpeningTimesWrapper>? = null
) {
    companion object {
        fun List<FhirVzdAvailableTime>.toErpModel(): Map<DayOfWeek, List<OpeningTimeErpModel>> {
            val openingTimeMap = mutableMapOf<DayOfWeek, MutableList<OpeningTimeErpModel>>()

            forEach { element ->
                val dayOfWeek = element.daysOfWeek.firstOrNull()?.toDayOfWeek()
                val startTime = element.availableStartTime?.let { LocalTime.parse(it) }
                val endTime = element.availableEndTime?.let { LocalTime.parse(it) }

                letNotNull(dayOfWeek, startTime, endTime) { day, start, end ->
                    openingTimeMap
                        .getOrPut(day) { mutableListOf() }
                        .add(OpeningTimeErpModel(start, end))
                }
            }

            return openingTimeMap.mapValues { it.value.toList() }
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

@Serializable
internal data class SpecialOpeningTimesWrapper(
    @SerialName("url") val url: String?,
    @SerialName("extension") val extension: List<SpecialOpeningTimesField>? = null
) {
    companion object {
        private const val SPECIAL_OPENING_TIMES_URL =
            "https://gematik.de/fhir/directory/StructureDefinition/SpecialOpeningTimesEX"
        private const val PERIOD_URL = "period"
        private const val QUALIFIER_URL = "qualifier"

        fun List<SpecialOpeningTimesWrapper>?.toSpecialOpeningTimeModel(): SpecialOpeningTimeErpModel? {
            val specialOpeningTimesExtension = this?.find { it.url == SPECIAL_OPENING_TIMES_URL }
            val extensionFields = specialOpeningTimesExtension?.extension ?: return null

            val periodValue = extensionFields.find { it.url == PERIOD_URL }?.valuePeriod
            val period = periodValue?.let {
                FhirPharmacyErpModelPeriod(
                    start = it.start?.asFhirTemporal(),
                    end = it.end?.asFhirTemporal()
                )
            }

            val qualifierValue = extensionFields.find { it.url == QUALIFIER_URL }?.valueCoding
            val description = qualifierValue?.display

            return period?.let {
                SpecialOpeningTimeErpModel(
                    description = description,
                    period = it
                )
            }
        }
    }
}

@Serializable
internal data class SpecialOpeningTimesField(
    @SerialName("url") val url: String?,
    @SerialName("valuePeriod") val valuePeriod: FhirPeriod? = null,
    @SerialName("valueCoding") val valueCoding: FhirTypeCoding? = null
)

@Serializable
internal data class FhirVzdSpecialty(
    @SerialName("coding") val codings: List<FhirTypeCoding> = emptyList(),
    @SerialName("text") val text: String?
) {
    companion object {

        fun List<FhirVzdSpecialty>.getSpecialtyTypes(): List<FhirVzdSpecialtyType> =
            flatMap { it.codings }
                .map { FhirVzdSpecialtyType.fromCode(it.code) }
                .distinct()
    }
}
