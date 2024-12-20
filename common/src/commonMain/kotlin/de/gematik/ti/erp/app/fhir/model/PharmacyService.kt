/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.model.PharmacyServiceSerializationType.DeliveryPharmacyServiceType
import de.gematik.ti.erp.app.fhir.model.PharmacyServiceSerializationType.EmergencyPharmacyServiceType
import de.gematik.ti.erp.app.fhir.model.PharmacyServiceSerializationType.LocalPharmacyServiceType
import de.gematik.ti.erp.app.fhir.model.PharmacyServiceSerializationType.OnlinePharmacyServiceType
import de.gematik.ti.erp.app.fhir.model.PharmacyServiceSerializationType.PickUpPharmacyServiceType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.DayOfWeek

/*
  Since kotlinx.serialization does not support PolymorphicSerializer of sealed interfaces
  out of the box we need to add a type to let the serializer know the difference if it is
  a sealed class or sealed interface.
*/
enum class PharmacyServiceSerializationType {
    OnlinePharmacyServiceType,
    PickUpPharmacyServiceType,
    DeliveryPharmacyServiceType,
    EmergencyPharmacyServiceType,
    LocalPharmacyServiceType
}

@Serializable(with = PharmacyServiceSerializer::class)
sealed interface PharmacyService {

    val name: String
    val type: PharmacyServiceSerializationType

    @Serializable
    data class OnlinePharmacyService(
        override val name: String,
        override val type: PharmacyServiceSerializationType = OnlinePharmacyServiceType
    ) : PharmacyService

    @Serializable
    data class PickUpPharmacyService(
        override val name: String,
        override val type: PharmacyServiceSerializationType = PickUpPharmacyServiceType
    ) : PharmacyService

    @Serializable
    data class DeliveryPharmacyService(
        override val name: String,
        override val openingHours: OpeningHours,
        override val type: PharmacyServiceSerializationType = DeliveryPharmacyServiceType
    ) : TemporalPharmacyService, PharmacyService

    @Serializable
    data class EmergencyPharmacyService(
        override val name: String,
        override val openingHours: OpeningHours,
        override val type: PharmacyServiceSerializationType = EmergencyPharmacyServiceType
    ) : TemporalPharmacyService, PharmacyService

    @Serializable
    data class LocalPharmacyService(
        override val name: String,
        override val openingHours: OpeningHours,
        override val type: PharmacyServiceSerializationType = LocalPharmacyServiceType
    ) : TemporalPharmacyService, PharmacyService
}

object PharmacyServiceSerializer : JsonContentPolymorphicSerializer<PharmacyService>(PharmacyService::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out PharmacyService> {
        element.jsonObject["type"]?.jsonPrimitive?.content?.let { classType ->
            return when (PharmacyServiceSerializationType.valueOf(classType)) {
                OnlinePharmacyServiceType -> PharmacyService.OnlinePharmacyService.serializer()
                PickUpPharmacyServiceType -> PharmacyService.PickUpPharmacyService.serializer()
                DeliveryPharmacyServiceType -> PharmacyService.DeliveryPharmacyService.serializer()
                EmergencyPharmacyServiceType -> PharmacyService.EmergencyPharmacyService.serializer()
                LocalPharmacyServiceType -> PharmacyService.LocalPharmacyService.serializer()
            }
        }
            ?: throw SerializationException(
                "PharmacyServiceSerializer: key 'type' not found or does not matches any module type"
            )
    }
}

interface TemporalPharmacyService /*: PharmacyService*/ {
    val openingHours: OpeningHours
    fun isOpenAt(tm: LocalDateTime) = openingHours.isOpenAt(tm)
    fun isAllDayOpen(day: DayOfWeek) = openingHours[day]?.any { it.isAllDayOpen() } ?: false
    fun openUntil(localDateTime: LocalDateTime): LocalTime? {
        val localTime = localDateTime.time
        return openingHours[localDateTime.dayOfWeek]?.find {
            it.isOpenAt(localTime)
        }?.closingTime
    }

    fun opensAt(localDateTime: LocalDateTime): LocalTime? {
        val localTime = localDateTime.time
        return openingHours[localDateTime.dayOfWeek]?.find {
            if (it.openingTime == null) {
                true
            } else {
                it.openingTime >= localTime
            }
        }?.openingTime
    }
}
