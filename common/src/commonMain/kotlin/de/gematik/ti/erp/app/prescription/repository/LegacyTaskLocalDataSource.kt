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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.AccidentTypeV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationProfileV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IdentifierEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IngredientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationCategoryV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.prescription.errors.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.prescription.mapper.ErpTaskMappers.toErpModel
import de.gematik.ti.erp.app.prescription.mapper.TaskDatabaseMappers.toDatabaseModel
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskStatus
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class LegacyTaskLocalDataSource(
    private val realm: Realm
) {
    data class SaveTaskResult(
        val isCompleted: Boolean,
        val lastModified: Instant,
        val lastMedicationDispense: Instant? = null
    )

    private val mutex = Mutex()

    private fun MutableRealm.findProfile(profileId: ProfileIdentifier): ProfileEntityV1 {
        return queryFirst<ProfileEntityV1>("id = $0", profileId)
            ?: throw PrescriptionDataNotFoundException("ProfileEntity with id $profileId not found in database")
    }

    private fun MutableRealm.findTask(taskId: String): SyncedTaskEntityV1 {
        return queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)
            ?: throw PrescriptionDataNotFoundException("SyncedTaskEntity with taskId $taskId not found in database")
    }

    private fun MutableRealm.findOrCreateTask(taskId: String, profileEntity: ProfileEntityV1): SyncedTaskEntityV1 {
        return queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId) ?: copyToRealm(SyncedTaskEntityV1())
            .also { profileEntity.syncedTasks += it }
    }

    private fun MutableRealm.updateTaskEntityWithMetaData(
        taskEntity: SyncedTaskEntityV1,
        profileEntity: ProfileEntityV1,
        model: FhirTaskMetaDataErpModel
    ): Result<Unit> {
        return runCatching {
            with(taskEntity) {
                parent = profileEntity
                taskId = model.taskId
                accessCode = model.accessCode
                lastModified = model.lastModified.value.toRealmInstant()
                status = model.status.toTaskStatusV1()
                expiresOn = model.expiresOn?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                lastMedicationDispense = model.lastMedicationDispense?.toInstant()?.toRealmInstant()
                acceptUntil = model.acceptUntil?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                authoredOn = model.authoredOn.value.toRealmInstant()
                isEuRedeemableByProperties = model.isEuRedeemableByProperties
                isEuRedeemableByPatientAuthorization = model.isEuRedeemableByPatientAuthorization
            }
        }
    }

    private fun TaskStatus.toTaskStatusV1(): TaskStatusV1 {
        return when (this) {
            TaskStatus.Ready -> TaskStatusV1.Ready
            TaskStatus.InProgress -> TaskStatusV1.InProgress
            TaskStatus.Completed -> TaskStatusV1.Completed
            TaskStatus.Draft -> TaskStatusV1.Draft
            TaskStatus.Requested -> TaskStatusV1.Requested
            TaskStatus.Received -> TaskStatusV1.Received
            TaskStatus.Accepted -> TaskStatusV1.Accepted
            TaskStatus.Rejected -> TaskStatusV1.Rejected
            TaskStatus.Canceled -> TaskStatusV1.Canceled
            TaskStatus.OnHold -> TaskStatusV1.OnHold
            TaskStatus.Failed -> TaskStatusV1.Failed
            else -> TaskStatusV1.Other
        }
    }

    fun latestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .max<RealmInstant>("lastModified")
            .asFlow()
            .map {
                it?.toInstant()
            }

    suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        lastModified: FhirTemporal?
    ) {
        realm.tryWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                lastModified?.let {
                    task.lastModified = lastModified.toInstant().toRealmInstant()
                    task.status = status.toTaskStatusV1()
                }
            }
        }
    }

    suspend fun saveTaskEPrescriptionMetaData(profileId: ProfileIdentifier, model: FhirTaskMetaDataErpModel): Result<Unit> =
        mutex.withLock {
            realm.safeWrite {
                val profileEntity = findProfile(profileId)
                val taskEntity = findOrCreateTask(model.taskId, profileEntity)
                updateTaskEntityWithMetaData(taskEntity, profileEntity, model)
            }
        }

    suspend fun markTaskAsIncomplete(
        taskId: String,
        error: Throwable
    ): Result<SyncedTaskEntityV1> =
        runCatching {
            mutex.withLock {
                realm.safeWrite {
                    val taskEntity = findTask(taskId)
                    taskEntity.apply {
                        this.isIncomplete = true
                        this.failureToReport = error.message ?: ""
                    }
                }
            }
        }

    suspend fun saveTaskEPrescriptionMedicalData(
        taskId: String,
        model: FhirTaskDataErpModel
    ): Result<SaveTaskResult> =
        runCatching {
            mutex.withLock {
                realm.safeWrite {
                    val taskEntity = findTask(taskId)

                    with(taskEntity) {
                        pvsIdentifier = model.pvsId ?: ""
                        organization = model.organization?.toDatabaseModel()
                        patient = model.patient?.toDatabaseModel()
                        practitioner = model.practitioner?.toDatabaseModel()
                        insuranceInformation = model.coverage?.toDatabaseModel()
                        deviceRequest = model.deviceRequest?.toDatabaseModel(taskEntity.deviceRequest?.isNew == null)
                        medicationRequest = model.medicationRequest?.toDatabaseModel().apply {
                            model.medication?.toDatabaseModel()?.let {
                                this?.medication = it
                            }
                        }
                    }

                    // after saving the SyncedTaskEntityV1 for the given taskId,
                    // delete the ScannedTaskEntityV1 if present since we have already downloaded the task
                    queryFirst<ScannedTaskEntityV1>("taskId = $0", taskEntity.taskId)?.let { delete(it) }

                    SaveTaskResult(
                        isCompleted = taskEntity.status == TaskStatusV1.Completed,
                        lastModified = taskEntity.lastModified.toInstant()
                    )
                }
            }
        }

    suspend fun saveTaskMedicationDispense(taskId: String, dispenseCollection: FhirMedicationDispenseErpModelCollection) {
        mutex.withLock {
            realm.safeWrite {
                dispenseCollection.dispensedMedications.forEach { dispensedMedication ->
                    if (query<MedicationDispenseEntityV1>("dispenseId = $0", dispensedMedication.dispenseId)
                        .count()
                        .find() == 0L
                    ) {
                        val taskEntity = findTask(taskId)
                        with(taskEntity) {
                            medicationDispenses += dispensedMedication.toDatabaseModel()
                        }
                    }
                }
            }
        }
    }
}

// todo: replace with erp-model
@Suppress("CyclomaticComplexMethod")
fun SyncedTaskEntityV1.toSyncedTask(): SyncedTaskData.SyncedTask =
    SyncedTaskData.SyncedTask(
        profileId = this.parent!!.id,
        isIncomplete = this.isIncomplete,
        pvsIdentifier = this.pvsIdentifier,
        failureToReport = this.failureToReport,
        taskId = this.taskId,
        accessCode = this.accessCode,
        lastModified = this.lastModified.toInstant(),
        isEuRedeemable = this.isEuRedeemableByProperties,
        organization = SyncedTaskData.Organization(
            name = this.organization?.name,
            address = this.organization?.address?.let {
                SyncedTaskData.Address(
                    line1 = it.line1,
                    line2 = it.line2,
                    postalCode = it.postalCode,
                    city = it.city
                )
            },
            uniqueIdentifier = this.organization?.uniqueIdentifier,
            phone = this.organization?.phone,
            mail = this.organization?.mail
        ),
        practitioner = SyncedTaskData.Practitioner(
            name = this.practitioner?.name,
            qualification = this.practitioner?.qualification,
            practitionerIdentifier = this.practitioner?.practitionerIdentifier
        ),
        patient = SyncedTaskData.Patient(
            name = this.patient?.name,
            address = this.patient?.address?.let {
                SyncedTaskData.Address(
                    line1 = it.line1,
                    line2 = it.line2,
                    postalCode = it.postalCode,
                    city = it.city
                )
            },
            birthdate = this.patient?.dateOfBirth,
            insuranceIdentifier = this.patient?.insuranceIdentifier
        ),
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = this.insuranceInformation?.name,
            status = this.insuranceInformation?.statusCode,
            identifierNumber = this.insuranceInformation?.identifierNumber,
            coverageType = SyncedTaskData.CoverageType.mapTo(this.insuranceInformation?.coverageType?.name)
        ),
        expiresOn = this.expiresOn?.toInstant(),
        acceptUntil = this.acceptUntil?.toInstant(),
        authoredOn = this.authoredOn.toInstant(),
        status = when (this.status) {
            TaskStatusV1.Ready -> SyncedTaskData.TaskStatus.Ready
            TaskStatusV1.InProgress -> SyncedTaskData.TaskStatus.InProgress
            TaskStatusV1.Completed -> SyncedTaskData.TaskStatus.Completed
            TaskStatusV1.Other -> SyncedTaskData.TaskStatus.Other
            TaskStatusV1.Draft -> SyncedTaskData.TaskStatus.Draft
            TaskStatusV1.Requested -> SyncedTaskData.TaskStatus.Requested
            TaskStatusV1.Received -> SyncedTaskData.TaskStatus.Received
            TaskStatusV1.Accepted -> SyncedTaskData.TaskStatus.Accepted
            TaskStatusV1.Rejected -> SyncedTaskData.TaskStatus.Rejected
            TaskStatusV1.Canceled -> SyncedTaskData.TaskStatus.Canceled
            TaskStatusV1.OnHold -> SyncedTaskData.TaskStatus.OnHold
            TaskStatusV1.Failed -> SyncedTaskData.TaskStatus.Failed
        },
        medicationRequest = SyncedTaskData.MedicationRequest(
            medication = this.medicationRequest?.medication?.toMedication(),
            dateOfAccident = this.medicationRequest?.dateOfAccident?.toInstant(),
            location = this.medicationRequest?.location,
            accidentType = when (this.medicationRequest?.accidentType) {
                AccidentTypeV1.Unfall -> SyncedTaskData.AccidentType.Unfall
                AccidentTypeV1.Arbeitsunfall -> SyncedTaskData.AccidentType.Arbeitsunfall
                AccidentTypeV1.Berufskrankheit -> SyncedTaskData.AccidentType.Berufskrankheit
                else -> SyncedTaskData.AccidentType.None
            },
            emergencyFee = this.medicationRequest?.emergencyFee,
            substitutionAllowed = this.medicationRequest?.substitutionAllowed ?: false,
            dosageInstruction = this.medicationRequest?.dosageInstruction,
            multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(
                indicator = this.medicationRequest?.multiplePrescriptionInfo?.indicator ?: false,
                numbering = Ratio(
                    numerator = Quantity(
                        value = this.medicationRequest?.multiplePrescriptionInfo?.numbering?.numerator?.value ?: "",
                        unit = ""
                    ),
                    denominator = Quantity(
                        value = this.medicationRequest?.multiplePrescriptionInfo?.numbering?.denominator?.value ?: "",
                        unit = ""
                    )
                ),
                start = this.medicationRequest?.multiplePrescriptionInfo?.start?.toInstant()
            ),
            additionalFee = when (this.medicationRequest?.additionalFee) {
                "0" -> SyncedTaskData.AdditionalFee.NotExempt
                "1" -> SyncedTaskData.AdditionalFee.Exempt
                "2" -> SyncedTaskData.AdditionalFee.ArtificialFertilization
                else -> SyncedTaskData.AdditionalFee.None
            },
            quantity = this.medicationRequest?.quantity ?: 0,
            note = this.medicationRequest?.note,
            bvg = this.medicationRequest?.bvg
        ),
        lastMedicationDispense = this.lastMedicationDispense?.toInstant(),
        medicationDispenses = this.medicationDispenses.map { medicationDispense ->
            SyncedTaskData.MedicationDispense(
                dispenseId = medicationDispense.dispenseId,
                patientIdentifier = medicationDispense.patientIdentifier,
                medication = medicationDispense.medication.toMedication(),
                deviceRequest = medicationDispense.deviceRequest?.toErpModel(),
                wasSubstituted = medicationDispense.wasSubstituted,
                dosageInstruction = medicationDispense.dosageInstruction,
                performer = medicationDispense.performer,
                whenHandedOver = medicationDispense.handedOverOn
            )
        },
        deviceRequest = this.deviceRequest?.toErpModel(),
        communications = this.communications.mapNotNull { communication ->
            communication.toCommunication()
        }
    )

fun MedicationEntityV1?.toMedication(): SyncedTaskData.Medication? =
    this?.let { medication ->
        SyncedTaskData.Medication(
            identifier = medication.identifier?.toIdentifier() ?: SyncedTaskData.Identifier(),
            category = medication.medicationCategory.toMedicationCategory(),
            medicationProfile = FhirTaskKbvMedicationProfileErpModel.restoreTaskKbvMedicationProfileErpModel(
                typeString = medication.medicationProfileType,
                versionString = medication.medicationProfileVersion
            ),
            vaccine = medication.vaccine,
            text = if (medication.text.isNotNullOrEmpty()) medication.text else medication.identifier?.pzn ?: "",
            form = medication.form,
            normSizeCode = medication.normSizeCode,
            amount = medication.amount.toRatio(),
            manufacturingInstructions = medication.manufacturingInstructions,
            packaging = medication.packaging,
            ingredients = medication.ingredients.toIngredients(),
            lotNumber = medication.lotNumber,
            ingredientMedications = medication.ingredientMedications.map {
                it.toMedication()
            },
            expirationDate = medication.expirationDate
        )
    }

fun IdentifierEntityV1.toIdentifier(): SyncedTaskData.Identifier = SyncedTaskData.Identifier(
    pzn = this.pzn,
    atc = this.atc,
    ask = this.ask,
    snomed = this.snomed
)

fun RatioEntityV1?.toRatio(): Ratio? = this?.let {
    Ratio(
        numerator = it.numerator?.let { quantity ->
            Quantity(
                value = quantity.value,
                unit = quantity.unit
            )
        },
        denominator = it.denominator?.let { quantity ->
            Quantity(
                value = quantity.value,
                unit = quantity.unit
            )
        }
    )
}

private fun RealmList<IngredientEntityV1>.toIngredients(): List<SyncedTaskData.Ingredient> =
    this.map {
        SyncedTaskData.Ingredient(
            text = it.text,
            form = it.form,
            number = it.number,
            amount = it.amount,
            strength = it.strength.toRatio()
        )
    }

private fun MedicationCategoryV1?.toMedicationCategory(): SyncedTaskData.MedicationCategory =
    when (this) {
        MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL -> SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        MedicationCategoryV1.BTM -> SyncedTaskData.MedicationCategory.BTM
        MedicationCategoryV1.AMVV -> SyncedTaskData.MedicationCategory.AMVV
        MedicationCategoryV1.SONSTIGES -> SyncedTaskData.MedicationCategory.SONSTIGES
        else -> SyncedTaskData.MedicationCategory.UNKNOWN
    }

fun CommunicationEntityV1.toCommunication() =
    if (this.profile == CommunicationProfileV1.Unknown) {
        null
    } else {
        Communication(
            taskId = this.taskId,
            communicationId = this.communicationId,
            orderId = this.orderId,
            profile = when (this.profile) {
                CommunicationProfileV1.ErxCommunicationDispReq ->
                    CommunicationProfile.ErxCommunicationDispReq

                CommunicationProfileV1.ErxCommunicationReply ->
                    CommunicationProfile.ErxCommunicationReply

                else -> error("should not happen")
            },
            sentOn = this.sentOn.toInstant(),
            sender = this.sender,
            recipient = this.recipient,
            payload = this.payload,
            consumed = this.consumed
        )
    }

fun ScannedTaskEntityV1.toScannedTask() =
    ScannedTaskData.ScannedTask(
        profileId = this.parent!!.id,
        taskId = this.taskId,
        name = this.name ?: "",
        index = this.index,
        accessCode = this.accessCode,
        scannedOn = this.scannedOn.toInstant(),
        redeemedOn = this.redeemedOn?.toInstant(),
        communications = this.communications.mapNotNull { it.toCommunication() }
    )
