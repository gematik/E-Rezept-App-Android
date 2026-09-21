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

package de.gematik.ti.erp.app.pharmacy.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class PharmacyServiceSerializationType {
    OnlinePharmacyServiceType,
    PickUpPharmacyServiceType,
    DeliveryPharmacyServiceType,
    EmergencyPharmacyServiceType,
    LocalPharmacyServiceType
}

@Serializable(with = PharmacyServiceErpModelSerializer::class)
sealed interface PharmacyServiceErpModel {

    val name: String
    val type: PharmacyServiceSerializationType

    @Serializable
    data class OnlinePharmacyServiceErpModel(
        override val name: String,
        override val type: PharmacyServiceSerializationType = PharmacyServiceSerializationType.OnlinePharmacyServiceType
    ) : PharmacyServiceErpModel

    @Serializable
    data class PickUpPharmacyServiceErpModel(
        override val name: String,
        override val type: PharmacyServiceSerializationType = PharmacyServiceSerializationType.PickUpPharmacyServiceType
    ) : PharmacyServiceErpModel

    @Serializable
    data class DeliveryPharmacyServiceErpModel(
        override val name: String,
        override val openingHours: PharmacyOpeningHoursErpModel,
        override val type: PharmacyServiceSerializationType = PharmacyServiceSerializationType.DeliveryPharmacyServiceType
    ) : TemporalPharmacyServiceErpModel, PharmacyServiceErpModel

    @Serializable
    data class EmergencyPharmacyServiceErpModel(
        override val name: String,
        override val openingHours: PharmacyOpeningHoursErpModel,
        override val type: PharmacyServiceSerializationType = PharmacyServiceSerializationType.EmergencyPharmacyServiceType
    ) : TemporalPharmacyServiceErpModel, PharmacyServiceErpModel

    @Serializable
    data class LocalPharmacyServiceErpModel(
        override val name: String,
        override val openingHours: PharmacyOpeningHoursErpModel,
        override val type: PharmacyServiceSerializationType = PharmacyServiceSerializationType.LocalPharmacyServiceType
    ) : TemporalPharmacyServiceErpModel, PharmacyServiceErpModel
}

object PharmacyServiceErpModelSerializer :
    JsonContentPolymorphicSerializer<PharmacyServiceErpModel>(PharmacyServiceErpModel::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out PharmacyServiceErpModel> {
        val classType = element.jsonObject["type"]?.jsonPrimitive?.content
        if (classType == null || classType.isEmpty()) {
            throw SerializationException(
                "PharmacyServiceErpModelSerializer: key 'type' not found or does not matches any module type"
            )
        }
        return when (PharmacyServiceSerializationType.valueOf(classType)) {
            PharmacyServiceSerializationType.OnlinePharmacyServiceType ->
                PharmacyServiceErpModel.OnlinePharmacyServiceErpModel.serializer()
            PharmacyServiceSerializationType.PickUpPharmacyServiceType ->
                PharmacyServiceErpModel.PickUpPharmacyServiceErpModel.serializer()
            PharmacyServiceSerializationType.DeliveryPharmacyServiceType ->
                PharmacyServiceErpModel.DeliveryPharmacyServiceErpModel.serializer()
            PharmacyServiceSerializationType.EmergencyPharmacyServiceType ->
                PharmacyServiceErpModel.EmergencyPharmacyServiceErpModel.serializer()
            PharmacyServiceSerializationType.LocalPharmacyServiceType ->
                PharmacyServiceErpModel.LocalPharmacyServiceErpModel.serializer()
        }
    }
}

interface TemporalPharmacyServiceErpModel {
    val openingHours: PharmacyOpeningHoursErpModel
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
