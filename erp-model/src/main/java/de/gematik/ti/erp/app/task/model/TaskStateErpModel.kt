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

package de.gematik.ti.erp.app.task.model

import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.daysUntil
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.days

/**
 * copied [SyncedTaskData.TaskState]
 */
@Serializable(with = TaskStateErpModel.TaskStateErpModelSyncedTaskDataSerializer::class)
interface TaskStateErpModel {
    val type: TaskStateErpModelSerializationType

    @Serializable
    @SerialName("Ready")
    data class Ready(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Ready,
        val expiresOn: Instant,
        val acceptUntil: Instant
    ) : TaskStateErpModel {
        // -1 because on the day of acceptUntil, the prescription is not paid by the health insurance
        fun acceptDaysLeft(now: Instant): Int =
            now.toLocalDate().daysUntil(acceptUntil.minus(1.days).toLocalDate())

        // -1 because on the day of expiresOn, the prescription is not redeemable
        fun expiryDaysLeft(now: Instant): Int =
            now.toLocalDate().daysUntil(expiresOn.minus(1.days).toLocalDate())
    }

    @Serializable
    @SerialName("Deleted")
    data class Deleted(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Deleted,
        val lastModified: Instant
    ) : TaskStateErpModel

    @Serializable
    @SerialName("LaterRedeemable")
    data class LaterRedeemable(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.LaterRedeemable,
        val redeemableOn: Instant
    ) : TaskStateErpModel

    @Serializable
    @SerialName("Pending")
    data class Pending(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Pending,
        val sentOn: Instant,
        val toTelematikId: String
    ) : TaskStateErpModel

    @Serializable
    @SerialName("InProgress")
    data class InProgress(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.InProgress,
        val lastModified: Instant
    ) : TaskStateErpModel

    @Serializable
    @SerialName("Expired")
    data class Expired(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Expired,
        val expiredOn: Instant
    ) : TaskStateErpModel

    @Serializable
    @SerialName("InProgress")
    data class Provided(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Provided,
        val lastMedicationDispense: Instant
    ) : TaskStateErpModel

    @Serializable
    @SerialName("Other")
    data class Other(
        override val type: TaskStateErpModelSerializationType = TaskStateErpModelSerializationType.Other,
        val state: FhirTaskStatusErpModel,
        val lastModified: Instant
    ) : TaskStateErpModel

    object TaskStateErpModelSyncedTaskDataSerializer : JsonContentPolymorphicSerializer<TaskStateErpModel>(
        TaskStateErpModel::class
    ) {
        override fun selectDeserializer(element: JsonElement): KSerializer<out TaskStateErpModel> {
            val classType = element.jsonObject["type"]?.jsonPrimitive?.content
                ?: throw SerializationException(
                    "TaskStateErpModelSyncedTaskDataSerializer: key 'type' not found or does not match any task state type"
                )
            return when (TaskStateErpModelSerializationType.valueOf(classType)) {
                TaskStateErpModelSerializationType.Ready -> Ready.serializer()
                TaskStateErpModelSerializationType.Deleted -> Deleted.serializer()
                TaskStateErpModelSerializationType.LaterRedeemable -> LaterRedeemable.serializer()
                TaskStateErpModelSerializationType.Pending -> Pending.serializer()
                TaskStateErpModelSerializationType.InProgress -> InProgress.serializer()
                TaskStateErpModelSerializationType.Expired -> Expired.serializer()
                TaskStateErpModelSerializationType.Provided -> Provided.serializer()
                TaskStateErpModelSerializationType.Other -> Other.serializer()
            }
        }
    }
}

/**
 * copied [SyncedTaskData.TaskStatus] // maybe we can use FhirTaskStatusErpModel
 */
enum class TaskStatusEnum {
    Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed
}

enum class TaskStateErpModelSerializationType {
    Ready,
    Deleted,
    LaterRedeemable,
    Pending,
    InProgress,
    Expired,
    Provided,
    Other
}
