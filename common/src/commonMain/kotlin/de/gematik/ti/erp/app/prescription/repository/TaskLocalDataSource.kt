/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.AccidentTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationCategoryV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.TaskStatusV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.fhir.model.AccidentType
import de.gematik.ti.erp.app.fhir.model.MedicationCategory
import de.gematik.ti.erp.app.fhir.model.MedicationProfile
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.model.extractKBVBundle
import de.gematik.ti.erp.app.fhir.model.extractMedicationDispense
import de.gematik.ti.erp.app.fhir.model.extractTask
import de.gematik.ti.erp.app.fhir.model.extractTaskAndKBVBundle
import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
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
import kotlinx.serialization.json.JsonElement

class TaskLocalDataSource(
    private val realm: Realm
) {
    data class SaveTaskResult(
        val isCompleted: Boolean,
        val lastModified: Instant
    )

    fun latestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .max<RealmInstant>("lastModified")
            .asFlow()
            .map {
                it?.toInstant()
            }

    private val mutex = Mutex()

    @Suppress("LongMethod", "ComplexMethod")
    suspend fun saveTask(profileId: ProfileIdentifier, bundle: JsonElement): SaveTaskResult? = mutex.withLock {
        return realm.tryWrite {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
                lateinit var taskEntity: SyncedTaskEntityV1

                extractTaskAndKBVBundle(
                    bundle,
                    process = { taskResource, bundleResource ->
                        extractTask(
                            task = taskResource,
                            process = { taskId: String, accessCode: String?, lastModified: FhirTemporal.Instant,
                                expiresOn: FhirTemporal.LocalDate?, acceptUntil: FhirTemporal.LocalDate?,
                                authoredOn: FhirTemporal.Instant, status: TaskStatus ->

                                taskEntity = queryFirst("taskId = $0", taskId) ?: run {
                                    copyToRealm(SyncedTaskEntityV1()).also {
                                        profile.syncedTasks += it
                                    }
                                }

                                taskEntity.apply {
                                    this.parent = profile
                                    this.taskId = taskId
                                    this.accessCode = accessCode
                                    this.lastModified = lastModified.value.toRealmInstant()
                                    this.status = when (status) {
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
                                    this.expiresOn =
                                        expiresOn?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                                    this.acceptUntil =
                                        acceptUntil?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                                    this.authoredOn = authoredOn.value.toRealmInstant()
                                }
                            }
                        )

                        try {
                            extractKBVBundle(
                                bundleResource,
                                processOrganization = { name, address, bsnr, iknr, phone, mail ->
                                    OrganizationEntityV1().apply {
                                        this.name = name
                                        this.address = address
                                        this.uniqueIdentifier = bsnr
                                        this.phone = phone
                                        this.mail = mail
                                    }
                                },
                                processPatient = { name, address, birthDate, insuranceIdentifier ->
                                    PatientEntityV1().apply {
                                        this.name = name
                                        this.address = address
                                        this.dateOfBirth = birthDate
                                        this.insuranceIdentifier = insuranceIdentifier
                                    }
                                },
                                processPractitioner = { name, qualification, practitionerIdentifier ->
                                    PractitionerEntityV1().apply {
                                        this.name = name
                                        this.qualification = qualification
                                        this.practitionerIdentifier = practitionerIdentifier
                                    }
                                },
                                processInsuranceInformation = { name, statusCode ->
                                    InsuranceInformationEntityV1().apply {
                                        this.name = name
                                        this.statusCode = statusCode
                                    }
                                },
                                processAddress = { line, postalCode, city ->
                                    AddressEntityV1().apply {
                                        this.line1 = line?.getOrNull(0) ?: ""
                                        this.line2 = line?.getOrNull(1) ?: ""
                                        this.postalCodeAndCity = listOfNotNull(postalCode, city).joinToString(" ")
                                    }
                                },
                                processQuantity = { value, unit ->
                                    QuantityEntityV1().apply {
                                        this.value = value
                                        this.unit = unit
                                    }
                                },
                                processRatio = { numerator, denominator ->
                                    RatioEntityV1().apply {
                                        this.numerator = numerator
                                        this.denominator = denominator
                                    }
                                },
                                processIngredient = { text, form, number, amount, strength ->
                                    IngredientEntityV1().apply {
                                        this.text = text
                                        this.form = form
                                        this.number = number
                                        this.amount = amount
                                        this.strength = strength
                                    }
                                },
                                processMedication = { text,
                                    medicationProfile,
                                    medicationCategory,
                                    form,
                                    amount,
                                    vaccine,
                                    manufacturingInstructions,
                                    packaging,
                                    normSizeCode,
                                    uniqueIdentifier,
                                    ingredients,
                                    _,
                                    _ ->
                                    MedicationEntityV1().apply {
                                        this.text = text ?: ""
                                        this.medicationProfile = when (medicationProfile) {
                                            MedicationProfile.PZN -> MedicationProfileV1.PZN
                                            MedicationProfile.COMPOUNDING -> MedicationProfileV1.COMPOUNDING
                                            MedicationProfile.INGREDIENT -> MedicationProfileV1.INGREDIENT
                                            MedicationProfile.FREETEXT -> MedicationProfileV1.FREETEXT
                                            else -> MedicationProfileV1.UNKNOWN
                                        }
                                        this.medicationCategory = when (medicationCategory) {
                                            MedicationCategory.ARZNEI_UND_VERBAND_MITTEL ->
                                                MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL

                                            MedicationCategory.BTM -> MedicationCategoryV1.BTM
                                            MedicationCategory.AMVV -> MedicationCategoryV1.AMVV
                                            MedicationCategory.SONSTIGES -> MedicationCategoryV1.SONSTIGES
                                            else -> MedicationCategoryV1.UNKNOWN
                                        }
                                        this.form = form
                                        this.amount = amount
                                        this.vaccine = vaccine
                                        this.manufacturingInstructions = manufacturingInstructions
                                        this.packaging = packaging
                                        this.normSizeCode = normSizeCode
                                        this.uniqueIdentifier = uniqueIdentifier
                                        this.ingredients = ingredients.toRealmList()
                                    }
                                },
                                processMultiplePrescriptionInfo = { indicator, numbering, start ->
                                    MultiplePrescriptionInfoEntityV1().apply {
                                        this.indicator = indicator
                                        this.numbering = numbering
                                        this.start = start?.toInstant(TimeZone.UTC)?.toRealmInstant()
                                    }
                                },
                                processMedicationRequest = {
                                        authoredOn,
                                        dateOfAccident,
                                        location,
                                        accidentType,
                                        emergencyFee,
                                        substitutionAllowed,
                                        dosageInstruction,
                                        quantity,
                                        multiplePrescriptionInfo,
                                        note,
                                        bvg,
                                        additionalFee
                                    ->
                                    MedicationRequestEntityV1().apply {
                                        this.authoredOn = authoredOn
                                        this.dateOfAccident =
                                            dateOfAccident?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                                        this.location = location
                                        this.accidentType = when (accidentType) {
                                            AccidentType.Unfall -> AccidentTypeV1.Unfall
                                            AccidentType.Arbeitsunfall -> AccidentTypeV1.Arbeitsunfall
                                            AccidentType.Berufskrankheit -> AccidentTypeV1.Berufskrankheit
                                            AccidentType.None -> AccidentTypeV1.None
                                        }
                                        this.emergencyFee = emergencyFee
                                        this.substitutionAllowed = substitutionAllowed
                                        this.dosageInstruction = dosageInstruction
                                        this.quantity = quantity
                                        this.multiplePrescriptionInfo = multiplePrescriptionInfo
                                        this.note = note
                                        this.bvg = bvg
                                        this.additionalFee = additionalFee
                                    }
                                },
                                savePVSIdentifier = { pvsId: String? ->
                                    taskEntity.apply {
                                        this.pvsIdentifier = pvsId ?: ""
                                    }
                                },
                                save = { organization,
                                    patient,
                                    practitioner,
                                    insuranceInformation,
                                    medication,
                                    medicationRequest ->
                                    taskEntity.apply {
                                        this.organization = organization
                                        this.patient = patient
                                        this.practitioner = practitioner
                                        this.insuranceInformation = insuranceInformation
                                        this.medicationRequest = medicationRequest.apply {
                                            this.medication = medication
                                        }
                                    }
                                }
                            )
                        } catch (expected: Exception) {
                            taskEntity.apply {
                                this.isIncomplete = true
                                this.failureToReport = expected.message ?: ""
                            }
                        }
                    }
                )

                // delete scanned task
                queryFirst<ScannedTaskEntityV1>("taskId = $0", taskEntity.taskId)?.let { delete(it) }

                SaveTaskResult(
                    isCompleted = taskEntity.status == TaskStatusV1.Completed,
                    lastModified = taskEntity.lastModified.toInstant()
                )
            }
        }
    }

    suspend fun saveMedicationDispense(taskId: String, bundle: JsonElement) {
        realm.tryWrite<Unit> {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { syncedTask ->

                extractMedicationDispense(
                    bundle,
                    quantityFn = { value, unit ->
                        QuantityEntityV1().apply {
                            this.value = value
                            this.unit = unit
                        }
                    },
                    ratioFn = { numerator, denominator ->
                        RatioEntityV1().apply {
                            this.numerator = numerator
                            this.denominator = denominator
                        }
                    },
                    ingredientFn = { text, form, number, amount, strength ->
                        IngredientEntityV1().apply {
                            this.text = text
                            this.form = form
                            this.number = number
                            this.amount = amount
                            this.strength = strength
                        }
                    },
                    processMedication = { text,
                        medicationProfile,
                        medicationCategory,
                        form,
                        amount,
                        vaccine,
                        manufacturingInstructions,
                        packaging,
                        normSizeCode,
                        uniqueIdentifier,
                        ingredients,
                        lotNumber,
                        expirationDate ->
                        MedicationEntityV1().apply {
                            this.text = text ?: ""
                            this.medicationProfile = when (medicationProfile) {
                                MedicationProfile.PZN -> MedicationProfileV1.PZN
                                MedicationProfile.COMPOUNDING -> MedicationProfileV1.COMPOUNDING
                                MedicationProfile.INGREDIENT -> MedicationProfileV1.INGREDIENT
                                MedicationProfile.FREETEXT -> MedicationProfileV1.FREETEXT
                                else -> MedicationProfileV1.UNKNOWN
                            }
                            this.medicationCategory = when (medicationCategory) {
                                MedicationCategory.ARZNEI_UND_VERBAND_MITTEL ->
                                    MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL

                                MedicationCategory.BTM -> MedicationCategoryV1.BTM
                                MedicationCategory.AMVV -> MedicationCategoryV1.AMVV
                                else -> MedicationCategoryV1.UNKNOWN
                            }
                            this.form = form
                            this.amount = amount
                            this.vaccine = vaccine
                            this.manufacturingInstructions = manufacturingInstructions
                            this.packaging = packaging
                            this.normSizeCode = normSizeCode
                            this.uniqueIdentifier = uniqueIdentifier
                            this.ingredients = ingredients.toRealmList()
                            this.lotNumber = lotNumber
                            this.expirationDate = expirationDate
                        }
                    },
                    processMedicationDispense = { dispenseId, patientIdentifier, medication,
                        wasSubstituted, dosageInstruction, performer, whenHandedOver ->

                        if (query<MedicationDispenseEntityV1>("dispenseId = $0", dispenseId)
                            .count()
                            .find() == 0L
                        ) {
                            syncedTask.medicationDispenses += MedicationDispenseEntityV1().apply {
                                this.dispenseId = dispenseId
                                this.patientIdentifier = patientIdentifier
                                this.medication = medication
                                this.wasSubstituted = wasSubstituted
                                this.dosageInstruction = dosageInstruction
                                this.performer = performer
                                this.handedOverOn = whenHandedOver
                            }
                        }
                    }
                )
            }
        }
    }
}

fun SyncedTaskEntityV1.toSyncedTask(): SyncedTaskData.SyncedTask =
    SyncedTaskData.SyncedTask(
        profileId = this.parent!!.id,
        isIncomplete = this.isIncomplete,
        pvsIdentifier = this.pvsIdentifier,
        failureToReport = this.failureToReport,
        taskId = this.taskId,
        accessCode = this.accessCode,
        lastModified = this.lastModified.toInstant(),
        organization = SyncedTaskData.Organization(
            name = this.organization?.name,
            address = this.organization?.address?.let {
                SyncedTaskData.Address(
                    line1 = it.line1,
                    line2 = it.line2,
                    postalCodeAndCity = it.postalCodeAndCity
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
                    postalCodeAndCity = it.postalCodeAndCity
                )
            },
            birthdate = this.patient?.dateOfBirth,
            insuranceIdentifier = this.patient?.insuranceIdentifier
        ),
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = this.insuranceInformation?.name,
            status = this.insuranceInformation?.statusCode
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
                numbering = SyncedTaskData.Ratio(
                    numerator = SyncedTaskData.Quantity(
                        value = this.medicationRequest?.multiplePrescriptionInfo?.numbering?.numerator?.value ?: "",
                        unit = ""
                    ),
                    denominator = SyncedTaskData.Quantity(
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
        medicationDispenses = this.medicationDispenses.map { medicationDispense ->
            SyncedTaskData.MedicationDispense(
                dispenseId = medicationDispense.dispenseId,
                patientIdentifier = medicationDispense.patientIdentifier,
                medication = medicationDispense.medication.toMedication(),
                wasSubstituted = medicationDispense.wasSubstituted,
                dosageInstruction = medicationDispense.dosageInstruction,
                performer = medicationDispense.performer,
                whenHandedOver = medicationDispense.handedOverOn
            )
        },
        communications = this.communications.mapNotNull { communication ->
            communication.toCommunication()
        }
    )
fun MedicationEntityV1?.toMedication(): SyncedTaskData.Medication? =
    when (this?.medicationProfile) {
        MedicationProfileV1.PZN -> SyncedTaskData.MedicationPZN(
            uniqueIdentifier = this.uniqueIdentifier ?: "",
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate,
            normSizeCode = this.normSizeCode,
            amount = this.amount.toRatio()
        )

        MedicationProfileV1.COMPOUNDING -> SyncedTaskData.MedicationCompounding(
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate,
            manufacturingInstructions = this.manufacturingInstructions,
            packaging = this.packaging,
            amount = this.amount.toRatio(),
            ingredients = this.ingredients.toIngredients()
        )

        MedicationProfileV1.INGREDIENT -> SyncedTaskData.MedicationIngredient(
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate,
            normSizeCode = this.normSizeCode,
            amount = this.amount.toRatio(),
            ingredients = this.ingredients.toIngredients()
        )

        MedicationProfileV1.FREETEXT -> SyncedTaskData.MedicationFreeText(
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate
        )

        else -> null
    }

private fun RatioEntityV1?.toRatio(): SyncedTaskData.Ratio? = this?.let {
    SyncedTaskData.Ratio(
        numerator = it.numerator?.let { quantity ->
            SyncedTaskData.Quantity(
                value = quantity.value,
                unit = quantity.unit
            )
        },
        denominator = it.denominator?.let { quantity ->
            SyncedTaskData.Quantity(
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
        SyncedTaskData.Communication(
            taskId = this.taskId,
            communicationId = this.communicationId,
            orderId = this.orderId,
            profile = when (this.profile) {
                CommunicationProfileV1.ErxCommunicationDispReq ->
                    SyncedTaskData.CommunicationProfile.ErxCommunicationDispReq

                CommunicationProfileV1.ErxCommunicationReply ->
                    SyncedTaskData.CommunicationProfile.ErxCommunicationReply

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
        accessCode = this.accessCode,
        scannedOn = this.scannedOn.toInstant(),
        redeemedOn = this.redeemedOn?.toInstant()
    )
