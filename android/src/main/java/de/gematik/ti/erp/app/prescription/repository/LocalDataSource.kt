/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.deleteAll
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationCategoryV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
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
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.BooleanType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.DomainResource
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.MedicationDispense
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.PrimitiveType
import org.hl7.fhir.r4.model.StringType
import java.time.Instant
import java.util.Date

class LocalDataSource(
    private val realm: Realm
) {
    suspend fun saveScannedTasks(profileId: ProfileIdentifier, tasks: List<ScannedTaskData.ScannedTask>) {
        realm.tryWrite<Unit> {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
                tasks.forEach { task ->
                    if (query<ProfileEntityV1>(
                            "syncedTasks.taskId = $0 OR scannedTasks.taskId = $0",
                            task.taskId
                        ).count().find() == 0L
                    ) {
                        profile.scannedTasks += copyToRealm(
                            ScannedTaskEntityV1().apply {
                                this.parent = profile
                                this.taskId = task.taskId
                                this.accessCode = task.accessCode
                                this.scannedOn = task.scannedOn.toRealmInstant()
                                this.redeemedOn = task.redeemedOn?.toRealmInstant()
                            }
                        )
                    }
                }
            }
        }
    }

    data class SaveTaskResult(
        val isCompleted: Boolean,
        val lastModified: Instant
    )

    val mutex = Mutex()

    suspend fun saveTask(profileId: ProfileIdentifier, bundle: Bundle): SaveTaskResult? = mutex.withLock {
        val task = bundle.extractResources<FhirTask>().first()

        return realm.tryWrite {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
                val taskId = task.idElement.idPart

                val taskEntity = queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)
                    ?.apply { applyBasicTaskData(task) }
                    ?: run {
                        val kbvBundleReference = requireNotNull(task.extractKBVBundleReference())
                        val kbvBundle = requireNotNull(bundle.extractKBVBundle(kbvBundleReference))

                        var _fhirMedication: FhirMedication? = null
                        var _fhirMedicationRequest: FhirMedicationRequest? = null
                        var _fhirOrganization: FhirOrganization? = null
                        var _fhirPractitioner: FhirPractitioner? = null
                        var _fhirPatient: FhirPatient? = null
                        var _fhirInsuranceInformation: FhirCoverage? = null

                        kbvBundle.entries().map {
                            when (val resource = it.resource) {
                                is FhirMedication -> _fhirMedication = resource
                                is FhirMedicationRequest -> _fhirMedicationRequest = resource
                                is FhirOrganization -> _fhirOrganization = resource
                                is FhirPractitioner -> _fhirPractitioner = resource
                                is FhirPatient -> _fhirPatient = resource
                                is FhirCoverage -> _fhirInsuranceInformation = resource
                            }
                        }

                        val medication = requireNotNull(_fhirMedication)
                        val medicationRequest = requireNotNull(_fhirMedicationRequest)
                        val organization = requireNotNull(_fhirOrganization)
                        val practitioner = requireNotNull(_fhirPractitioner)
                        val patient = requireNotNull(_fhirPatient)
                        val insuranceInformation = requireNotNull(_fhirInsuranceInformation)

                        copyToRealm(
                            SyncedTaskEntityV1().apply {
                                this.applyBasicTaskData(task)
                                this.medicationRequest =
                                    medicationRequest.toMedicationRequestEntityV1(medication.toMedicationEntityV1())
                                this.organization = organization.toOrganizationEntityV1()
                                this.practitioner = practitioner.toPractitionerEntityV1()
                                this.patient = patient.toPatientEntityV1()
                                this.insuranceInformation = insuranceInformation.toInsuranceInformationEntityV1()
                                this.parent = profile
                            }
                        ).also {
                            profile.syncedTasks += it
                        }
                    }

                // delete scanned task
                queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { delete(it) }

                SaveTaskResult(
                    isCompleted = taskEntity.status == TaskStatusV1.Completed,
                    lastModified = taskEntity.lastModified.toInstant()
                )
            }
        }
    }

    fun loadSyncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        realm.query<ProfileEntityV1>("id = $0", profileId)
            .first()
            .asFlow()
            .map { profile ->
                profile.obj?.syncedTasks?.map { syncedTask ->
                    syncedTask.toSyncedTask()
                } ?: emptyList()
            }

    fun loadSyncedTaskByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        realm.query<SyncedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { syncedTask ->
                syncedTask.obj?.toSyncedTask()
            }

    fun loadScannedTaskByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        realm.query<ScannedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { scannedTask ->
                scannedTask.obj?.toScannedTask()
            }

    suspend fun saveCommunications(communications: List<FhirCommunication>) {
        realm.tryWrite<Unit> {
            communications.forEach { communication ->
                val taskId = communication.basedOn[0].reference.split("/")[1]
                val communicationAlreadyExists =
                    query<CommunicationEntityV1>("communicationId = $0", communication.idElement.idPart).count()
                        .find() > 0
                if (!communicationAlreadyExists) {
                    queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { syncedTask ->
                        syncedTask.communications +=
                            copyToRealm(communication.toCommunicationEntityV1(syncedTask))
                    }
                }
            }
        }
    }

    suspend fun saveMedicationDispense(taskId: String, bundle: MedicationDispense) {
        realm.tryWrite<Unit> {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { syncedTask ->
                if (query<MedicationDispenseEntityV1>("dispenseId = $0", bundle.idElement.idPart)
                    .count()
                    .find() == 0L
                ) {
                    syncedTask.medicationDispenses += copyToRealm(bundle.toMedicationDispenseEntityV1())
                }
            }
        }
    }

    fun loadScannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>> =
        realm.query<ProfileEntityV1>("id = $0", profileId)
            .first()
            .asFlow()
            .map { profile ->
                profile.obj?.let {
                    it.scannedTasks.map { task ->
                        task.toScannedTask()
                    }
                } ?: emptyList()
            }

    suspend fun deleteTask(taskId: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { delete(it) }
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let {
                deleteAll(it)
            }
        }
    }

    suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = timestamp?.toRealmInstant()
            }
        }
    }

    fun loadTaskIds(): Flow<List<String>> =
        combine(
            realm.query<SyncedTaskEntityV1>().asFlow(),
            realm.query<ScannedTaskEntityV1>().asFlow()
        ) { syncedTasks, scannedTasks ->
            syncedTasks.list.map { it.taskId } + scannedTasks.list.map { it.taskId }
        }

    suspend fun updateTaskSyncedUpTo(profileId: ProfileIdentifier, timestamp: Instant) {
        realm.tryWrite<Unit> {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.lastTaskSynced = timestamp.toRealmInstant()
            }
        }
    }

    fun taskSyncedUpTo(profileId: ProfileIdentifier): Flow<Instant?> =
        realm.query<ProfileEntityV1>("id = $0", profileId)
            .first()
            .asFlow()
            .map { profile ->
                profile.obj?.lastTaskSynced?.toInstant()
            }
}

fun FhirTaskStatus.toTaskStatusV1() =
    when (this) {
        FhirTaskStatus.READY -> TaskStatusV1.Ready
        FhirTaskStatus.INPROGRESS -> TaskStatusV1.InProgress
        FhirTaskStatus.COMPLETED -> TaskStatusV1.Completed
        FhirTaskStatus.DRAFT -> TaskStatusV1.Draft
        FhirTaskStatus.REQUESTED -> TaskStatusV1.Requested
        FhirTaskStatus.RECEIVED -> TaskStatusV1.Received
        FhirTaskStatus.ACCEPTED -> TaskStatusV1.Accepted
        FhirTaskStatus.REJECTED -> TaskStatusV1.Rejected
        FhirTaskStatus.CANCELLED -> TaskStatusV1.Canceled
        FhirTaskStatus.ONHOLD -> TaskStatusV1.OnHold
        FhirTaskStatus.FAILED -> TaskStatusV1.Failed
        else -> TaskStatusV1.Other
    }

fun FhirPatient.toPatientEntityV1() =
    PatientEntityV1().apply {
        this.name = this@toPatientEntityV1.name.find { it.use == HumanName.NameUse.OFFICIAL }?.nameAsSingleString
        this.address = this@toPatientEntityV1.address.find { it.type == Address.AddressType.BOTH }?.toAddressEntityV1()
        this.birthdate = this@toPatientEntityV1.birthDate?.toInstant()?.toRealmInstant()
        this.insuranceIdentifier = this@toPatientEntityV1.identifier.firstOrNull()?.value
    }

fun FhirAddress.toAddressEntityV1() =
    AddressEntityV1().apply {
        this.line1 = this@toAddressEntityV1.line.getOrNull(0)?.value ?: ""
        this.line2 = this@toAddressEntityV1.line.getOrNull(1)?.value ?: ""
        this.postalCodeAndCity = this@toAddressEntityV1.postalCode + " " + this@toAddressEntityV1.city
    }

fun FhirCoverage.toInsuranceInformationEntityV1() =
    InsuranceInformationEntityV1().apply {
        this.name = this@toInsuranceInformationEntityV1.payorFirstRep?.display
        this.statusCode =
            this@toInsuranceInformationEntityV1.getCodeValueExtensionByUrl(
                "http://fhir.de/StructureDefinition/gkv/versichertenart"
            )
    }

fun FhirOrganization.toOrganizationEntityV1() =
    OrganizationEntityV1().apply {
        this.name = this@toOrganizationEntityV1.name
        this.address =
            this@toOrganizationEntityV1.address.find { it.type == Address.AddressType.BOTH }?.toAddressEntityV1()
        this.uniqueIdentifier =
            this@toOrganizationEntityV1.identifier?.find {
                it.system == "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"
            }?.value
        this.phone =
            this@toOrganizationEntityV1.telecom?.find { it.system == ContactPoint.ContactPointSystem.PHONE }?.value
        this.mail =
            this@toOrganizationEntityV1.telecom?.find { it.system == ContactPoint.ContactPointSystem.EMAIL }?.value
    }

fun FhirPractitioner.toPractitionerEntityV1() =
    PractitionerEntityV1().apply {
        this.name = this@toPractitionerEntityV1.name.find { it.use == HumanName.NameUse.OFFICIAL }?.nameAsSingleString
        this.qualification = this@toPractitionerEntityV1.qualification.find { it.code?.hasText() == true }?.code?.text
        this.practitionerIdentifier = this@toPractitionerEntityV1.identifier.firstOrNull()?.value
    }

fun FhirRatio.toRatioEntityV1() =
    RatioEntityV1().apply {
        this.numerator = this@toRatioEntityV1.numerator?.toQuantityEntityV1()
        this.denominator = this@toRatioEntityV1.denominator?.toQuantityEntityV1()
    }

fun FhirQuantity.toQuantityEntityV1() =
    QuantityEntityV1().apply {
        this.value = this@toQuantityEntityV1.value?.toString() ?: ""
        this.unit = this@toQuantityEntityV1.unit ?: ""
    }

fun FhirMedication.toMedicationEntityV1() =
    MedicationEntityV1().apply {
        this.text = this@toMedicationEntityV1.code.text ?: ""
        this.medicationProfile = when (this@toMedicationEntityV1.meta.profile[0].value.split("|").first()) {
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN" -> MedicationProfileV1.PZN
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding" ->
                MedicationProfileV1.COMPOUNDING
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient" -> MedicationProfileV1.INGREDIENT
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText" -> MedicationProfileV1.FREETEXT
            else -> error("empty medication profile")
        }

        this.medicationCategory =
            when (
                this@toMedicationEntityV1.getCodeValueExtensionByUrl(
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"
                )
            ) {
                "00" -> MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL
                "01" -> MedicationCategoryV1.BTM
                "02" -> MedicationCategoryV1.AMVV
                else -> error("unknown medication category")
            }
        this.amount = this@toMedicationEntityV1.amount.toRatioEntityV1()
        this.form =
            (
                this@toMedicationEntityV1.form?.coding?.find {
                    it.system == "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"
                }?.code
                ) ?: this@toMedicationEntityV1.form.text
        this.vaccine =
            this@toMedicationEntityV1.getValueExtensionByUrl<BooleanType, Boolean>(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine"
            )
                ?: false
        this.manufacturingInstructions =
            this@toMedicationEntityV1.getValueExtensionByUrl<StringType, String>(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction"
            )
        this.packaging =
            this@toMedicationEntityV1.getValueExtensionByUrl<StringType, String>(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging"
            )
        this.normSizeCode =
            this@toMedicationEntityV1.getValueExtensionByUrl<CodeType, String>(
                "http://fhir.de/StructureDefinition/normgroesse"
            )
        this.uniqueIdentifier =
            this@toMedicationEntityV1.code?.coding?.find { it.system == "http://fhir.de/CodeSystem/ifa/pzn" }?.code
        this.lotNumber = this@toMedicationEntityV1.batch.lotNumber
        this.expirationDate =
            this@toMedicationEntityV1.batch.expirationDate?.let { it.toInstant().toRealmInstant() }
        this.ingredients = this@toMedicationEntityV1.ingredient?.map {
            IngredientEntityV1().apply {
                this.text = it.itemCodeableConcept.text
                this.form =
                    it.getValueExtensionByUrl<StringType, String>(
                        "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form"
                    )
                this.amount =
                    it.strength.getValueExtensionByUrl<StringType, String>(
                        "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount"
                    )
                this.strength = it.strength.toRatioEntityV1()
            }
        }?.toRealmList() ?: realmListOf()
    }

fun FhirMedicationRequest.toMedicationRequestEntityV1(medication: MedicationEntityV1) =
    MedicationRequestEntityV1().apply {
        this.medication = medication
        this.dateOfAccident =
            this@toMedicationRequestEntityV1.getExtensionByUrl(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"
            )
                ?.getValueExtensionByUrl<DateType, Date>("unfalltag")
                ?.toInstant()
                ?.toRealmInstant()
        this.location =
            this@toMedicationRequestEntityV1.getExtensionByUrl(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"
            )
                ?.getValueExtensionByUrl<StringType, String>("unfallbetrieb")
        this.emergencyFee =
            this@toMedicationRequestEntityV1.getValueExtensionByUrl<BooleanType, Boolean>(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"
            )
        this.substitutionAllowed =
            this@toMedicationRequestEntityV1.substitution.allowedBooleanType.booleanValue()
        this.dosageInstruction = this@toMedicationRequestEntityV1.extractDosageInstructions()
    }

fun FhirCommunication.toCommunicationEntityV1(parent: SyncedTaskEntityV1) =
    CommunicationEntityV1().apply {
        this.profile = when {
            this@toCommunicationEntityV1.isProfile(
                "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"
            ) ->
                CommunicationProfileV1.ErxCommunicationDispReq
            this@toCommunicationEntityV1.isProfile(
                "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"
            ) ->
                CommunicationProfileV1.ErxCommunicationReply
            else -> CommunicationProfileV1.Unknown // we can't handle other profiles currently
        }
        this.taskId = this@toCommunicationEntityV1.basedOn[0].reference.split("/")[1]
        this.communicationId = this@toCommunicationEntityV1.idElement.idPart.toString()
        this.orderId = if (this.profile == CommunicationProfileV1.ErxCommunicationDispReq) {
            this@toCommunicationEntityV1.identifier
                .find { it.system == "https://gematik.de/fhir/NamingSystem/OrderID" }?.value ?: ""
        } else {
            ""
        }
        this.sentOn = this@toCommunicationEntityV1.sent.toInstant().toRealmInstant()
        this.sender = this@toCommunicationEntityV1.sender.identifier.value
        this.recipient = this@toCommunicationEntityV1.recipient[0].identifier.value
        this.payload = this@toCommunicationEntityV1.payload[0].content.toString()
        this.consumed = false
        this.parent = parent
    }

fun FhirResource.isProfile(name: String) =
    this.meta.profile[0].value.split("|").first() == name

fun MedicationRequest.extractDosageInstructions(): String? {
    if (this.hasDosageInstruction() && (
        this.dosageInstruction?.first()
            ?.getExtensionByUrl(
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"
                )?.value as? BooleanType?
        )?.value == true
    ) {
        return this.dosageInstruction?.first()?.text ?: ""
    }
    return null
}

inline fun <reified T : PrimitiveType<R>, R> FhirElement.getValueExtensionByUrl(url: String): R? =
    (getExtensionByUrl(url)?.value as? T?)?.value

inline fun <reified T : PrimitiveType<R>, R> DomainResource.getValueExtensionByUrl(url: String): R? =
    (getExtensionByUrl(url)?.value as? T?)?.value

fun DomainResource.getCodeValueExtensionByUrl(url: String): String? =
    (getExtensionByUrl(url)?.value as? Coding?)?.code

fun FhirMedicationDispense.toMedicationDispenseEntityV1() =
    MedicationDispenseEntityV1().apply {
        this.dispenseId = this@toMedicationDispenseEntityV1.idElement.idPart
        this.patientIdentifier = this@toMedicationDispenseEntityV1.subject.identifier.value
        this.medication =
            (this@toMedicationDispenseEntityV1.contained[0] as FhirMedication).toMedicationEntityV1()
        this.wasSubstituted = this@toMedicationDispenseEntityV1.substitution.wasSubstituted
        this.dosageInstruction = this@toMedicationDispenseEntityV1.dosageInstruction.firstOrNull()?.text
        this.performer = (
            this@toMedicationDispenseEntityV1.performer[0] as
                MedicationDispense.MedicationDispensePerformerComponent
            )
            .actor.identifier.value
        this.whenHandedOver = this@toMedicationDispenseEntityV1.whenHandedOver.toInstant().toRealmInstant()
    }

fun SyncedTaskEntityV1.toSyncedTask(): SyncedTaskData.SyncedTask =
    SyncedTaskData.SyncedTask(
        profileId = this.parent!!.id,
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
            birthdate = this.patient?.birthdate?.toInstant(),
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
            emergencyFee = this.medicationRequest?.emergencyFee,
            substitutionAllowed = this.medicationRequest?.substitutionAllowed ?: false,
            dosageInstruction = this.medicationRequest?.dosageInstruction
        ),
        medicationDispenses = this.medicationDispenses.map { medicationDispense ->
            SyncedTaskData.MedicationDispense(
                dispenseId = medicationDispense.dispenseId,
                patientIdentifier = medicationDispense.patientIdentifier,
                medication = medicationDispense.medication.toMedication(),
                wasSubstituted = medicationDispense.wasSubstituted,
                dosageInstruction = medicationDispense.dosageInstruction,
                performer = medicationDispense.performer,
                whenHandedOver = medicationDispense.whenHandedOver.toInstant()
            )
        },
        communications = this.communications.mapNotNull { communication ->
            communication.toCommunication()
        }
    )

private fun MedicationEntityV1?.toMedication(): SyncedTaskData.Medication? =
    when (this?.medicationProfile) {
        MedicationProfileV1.PZN -> SyncedTaskData.MedicationPZN(
            uniqueIdentifier = this.uniqueIdentifier ?: "",
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate?.toInstant(),
            normSizeCode = this.normSizeCode,
            amount = this.amount.toRatio()
        )

        MedicationProfileV1.COMPOUNDING -> SyncedTaskData.MedicationCompounding(
            category = this.medicationCategory.toMedicationCategory(),
            vaccine = this.vaccine,
            text = this.text,
            form = this.form,
            lotNumber = this.lotNumber,
            expirationDate = this.expirationDate?.toInstant(),
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
            expirationDate = this.expirationDate?.toInstant(),
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
            expirationDate = this.expirationDate?.toInstant()
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
        }
    )
}

private fun RealmList<IngredientEntityV1>.toIngredients(): List<SyncedTaskData.Ingredient> =
    this.map {
        SyncedTaskData.Ingredient(
            text = it.text,
            form = it.form,
            amount = it.amount,
            strength = it.strength.toRatio()
        )
    }

private fun MedicationCategoryV1?.toMedicationCategory(): SyncedTaskData.MedicationCategory =
    when (this) {
        MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL -> SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        MedicationCategoryV1.BTM -> SyncedTaskData.MedicationCategory.BTM
        MedicationCategoryV1.AMVV -> SyncedTaskData.MedicationCategory.AMVV
        else -> error("unknown medication category")
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

fun SyncedTaskEntityV1.applyBasicTaskData(task: FhirTask) {
    this.taskId = task.idElement.idPart
    this.accessCode = task.accessCode()
    this.lastModified = task.lastModified.toInstant().toRealmInstant()
    this.status = task.status.toTaskStatusV1()
    this.expiresOn =
        task.extractDateExtension("https://gematik.de/fhir/StructureDefinition/ExpiryDate")
            ?.toRealmInstant()
    this.acceptUntil =
        task.extractDateExtension("https://gematik.de/fhir/StructureDefinition/AcceptDate")
            ?.toRealmInstant()
    this.authoredOn = task.authoredOn.toInstant().toRealmInstant()
}
