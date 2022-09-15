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

import androidx.annotation.StringRes
import ca.uhn.fhir.parser.IParser
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.COMMUNICATION_TYPE_DISP_REQ
import de.gematik.ti.erp.app.db.entities.COMMUNICATION_TYPE_REPLY
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.utils.convertFhirDateToLocalDate
import de.gematik.ti.erp.app.utils.convertFhirDateToOffsetDateTime
import java.time.LocalDate
import javax.inject.Inject
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.AuditEvent
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
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Task

typealias EntityTask = de.gematik.ti.erp.app.db.entities.Task
typealias FhirPractitioner = org.hl7.fhir.r4.model.Practitioner
typealias FhirMedication = org.hl7.fhir.r4.model.Medication
typealias FhirMedicationRequest = MedicationRequest
typealias FhirPatient = org.hl7.fhir.r4.model.Patient
typealias FhirOrganization = org.hl7.fhir.r4.model.Organization
typealias FhirCoverage = org.hl7.fhir.r4.model.Coverage

inline fun <reified T : DomainResource> Bundle.extractResources(): List<T>? {
    return entry?.let { it ->
        it.filter { it.resource is T }
            .map { it.resource as T }
    }
}

inline fun <reified T : DomainResource> Bundle.extractSingleResource(): T? {
    return entry?.firstOrNull()?.let { it as T }
}

inline fun <reified T : DomainResource> Bundle.BundleEntryComponent.extractResource(): T? {
    return entries().let { it ->
        it.filter { it.resource is T }
            .map { it.resource as T }
            .firstOrNull()
    }
}

inline fun <reified T : DomainResource> Bundle.BundleEntryComponent.extractResourceForReference(
    reference: String
): T? {
    return entries().let { it ->
        it.filter {
            it.resource is T && it.resource.id == reference
        }.map {
            it.resource as T
        }
            .firstOrNull()
    }
}

@Suppress("UNCHECKED_CAST")
fun Bundle.BundleEntryComponent.entries(): List<Bundle.BundleEntryComponent> {
    return resource.getChildByName("entry").values as List<Bundle.BundleEntryComponent>
}

fun Task.extractKBVBundleReference(): String? {
    return (
        input.find {
            val code = (it as Task.ParameterComponent).type.coding[0].code
            val system = it.type.coding[0].system

            code == "2" && system == "https://gematik.de/fhir/CodeSystem/Documenttype"
        }?.value as Reference
        ).reference
}

fun MedicationRequest.findReferences() = mapOf(
    "medication" to medicationReference.reference,
    "patient" to subject.reference,
    "practitioner" to requester.reference,
    "insuranceReference" to insurance[0].reference
)

// extracts the very first dosage instruction
fun MedicationRequest.extractDosageInstructions(): String? {
    if (this.hasDosageInstruction() && (
        this.dosageInstruction?.first()
            ?.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag")?.value as? BooleanType?
        )?.value == true
    ) {
        return this.dosageInstruction?.first()?.text ?: ""
    }
    return null
}

fun Bundle.extractKBVBundle(reference: String): Bundle.BundleEntryComponent? {
    val cleanRefId =
        if (reference.first() == '#') {
            reference.subSequence(1, reference.length)
        } else {
            reference
        }

    // BUG: Workaround for https://github.com/hapifhir/org.hl7.fhir.core/pull/12
    return entry.find { it.resource.id.removePrefix("urn:uuid:") == cleanRefId }
}

fun Task.accessCode(): String? {
    identifier.forEach {
        if (it.hasSystem()) {
            if (it.system == "https://gematik.de/fhir/NamingSystem/AccessCode") {
                return it.value
            }
        }
    }
    return null
}

fun Task.prescriptionId(): String? {
    identifier.forEach {
        if (it.hasSystem()) {
            if (it.system == "https://gematik.de/fhir/NamingSystem/PrescriptionID") {
                return it.value
            }
        }
    }
    return null
}
//
// private fun Task.prescriptionFlowTypeCode(): Int {
//    return (extension[0].value as Coding).code.toInt()
// }

class Mapper @Inject constructor(
    private val fhirParser: IParser
) {

    private fun extractDateExtension(task: FhirTask, extensionUrl: String): LocalDate? {

        val fhirDate =
            task.getExtensionByUrl(extensionUrl)
                ?.value as DateType?

        return LocalDate.parse(fhirDate?.valueAsString)
    }

    fun parseTaskIds(bundle: Bundle): List<String> {
        return bundle.extractResources<FhirTask>()?.map {
            it.idElement.idPart
        } ?: listOf()
    }

    /**
     * Maps a list of Fhir Communications to Communication entities.
     */
    fun mapFhirBundleToCommunications(bundle: Bundle, profileName: String): List<Communication> {
        return bundle.extractResources<FhirCommunication>()?.mapNotNull { fhirCommunication ->
            when (fhirCommunication.meta.profile[0].value.split("|").first()) {
                COMMUNICATION_TYPE_DISP_REQ -> mapToDispReqCommunication(
                    fhirCommunication, profileName
                )
                COMMUNICATION_TYPE_REPLY -> mapToCommunicationReply(
                    fhirCommunication, profileName
                )
                else -> null // we can't handle other profiles currently
            }
        } ?: error("No communication found!")
    }

    private fun extractTaskIdFromReference(reference: String): String {
        return reference.split("/")[1]
    }

    private fun mapToDispReqCommunication(
        fhirCommunication: FhirCommunication,
        profileName: String
    ) = Communication(
        communicationId = fhirCommunication.idElement.idPart,
        profile = CommunicationProfile.ErxCommunicationDispReq,
        taskId = extractTaskIdFromReference(fhirCommunication.basedOn[0].reference),
        time = fhirCommunication.sent.toString(),
        telematicsId = fhirCommunication.recipient[0].identifier.value,
        kbvUserId = fhirCommunication.sender.identifier.value,
        payload = fhirCommunication.payload[0].content.toString(),
        profileName = profileName
    )

    private fun mapToCommunicationReply(
        fhirCommunication: FhirCommunication,
        profileName: String
    ) = Communication(
        communicationId = fhirCommunication.idElement.idPart,
        profile = CommunicationProfile.ErxCommunicationReply,
        taskId = extractTaskIdFromReference(fhirCommunication.basedOn[0].reference),
        time = fhirCommunication.sent.toString(),
        kbvUserId = fhirCommunication.recipient[0].identifier.value,
        telematicsId = fhirCommunication.sender.identifier.value,
        payload = fhirCommunication.payload[0].content.toString(),
        profileName = profileName
    )

    /**
     * Maps Task and KBV Bundle together to a complete Task Entity
     *
     * @param bundle the bundle consists of a Task which is referencing an included KBV Bundle.
     */
    fun mapFhirBundleToTaskWithKBVBundle(bundle: Bundle, profileName: String): EntityTask =
        bundle.extractResources<FhirTask>()?.firstOrNull()?.let { fhirTask ->
            fhirTask.extractKBVBundleReference()?.let { kbvBundleReference ->
                val kbvBundle = requireNotNull(bundle.extractKBVBundle(kbvBundleReference))

                var _fhirMedication: FhirMedication? = null
                var _fhirMedicationRequest: FhirMedicationRequest? = null
                var _fhirOrganization: FhirOrganization? = null
                var _fhirPractitioner: FhirPractitioner? = null

                kbvBundle.entries().map {
                    when (val resource = it.resource) {
                        is FhirMedication -> _fhirMedication = resource
                        is FhirMedicationRequest -> _fhirMedicationRequest = resource
                        is FhirOrganization -> _fhirOrganization = resource
                        is FhirPractitioner -> _fhirPractitioner = resource
                    }
                }

                val fhirMedication = requireNotNull(_fhirMedication)
                val fhirMedicationRequest = requireNotNull(_fhirMedicationRequest)
                val fhirOrganization = requireNotNull(_fhirOrganization)
                val fhirPractitioner = requireNotNull(_fhirPractitioner)

                val kbvBundleRawString =
                    fhirParser.setPrettyPrint(false).encodeResourceToString(kbvBundle.resource)

                EntityTask(
                    taskId = fhirTask.idElement.idPart,
                    profileName = profileName,
                    accessCode = fhirTask.accessCode(),
                    lastModified = fhirTask.lastModified.convertFhirDateToOffsetDateTime(),
                    organization = fhirOrganization.name
                        ?: fhirPractitioner.nameFirstRep.nameAsSingleString,
                    medicationText = fhirMedication.code.text,
                    expiresOn = extractDateExtension(
                        fhirTask,
                        "https://gematik.de/fhir/StructureDefinition/ExpiryDate"
                    ),
                    acceptUntil = extractDateExtension(
                        fhirTask,
                        "https://gematik.de/fhir/StructureDefinition/AcceptDate"
                    ),
                    authoredOn = fhirMedicationRequest.authoredOn?.convertFhirDateToOffsetDateTime(),
                    status = TaskStatus.fromFhirTask(fhirTask.status),
                    scannedOn = null,
                    scanSessionEnd = null,
                    nrInScanSession = null,
                    rawKBVBundle = kbvBundleRawString.toByteArray()
                )
            } ?: error("KBV Bundle not found!")
        } ?: error("No task found!")

    /**
     * Throws an exception if the bundle couldn't be parsed.
     */
    fun parseKBVBundle(rawKBVBundle: ByteArray): Bundle {
        return fhirParser.parseResource(rawKBVBundle.decodeToString()) as Bundle
    }

    fun mapFhirBundleToAuditEvents(profileName: String, fhirBundle: Bundle): List<AuditEventSimple> {
        return fhirBundle.extractResources<AuditEvent>()?.map {
            AuditEventSimple(
                id = it.idElement.idPart,
                locale = if (it.language.isNullOrEmpty()) "de" else it.language,
                text = it.text.div.allText(),
                timestamp = it.recorded.convertFhirDateToOffsetDateTime(),
                taskId = it.entity[0].what.referenceElement.idPart,
                profileName = profileName
            )
        } ?: error("No AuditEvents found in given Bundle $fhirBundle")
    }

    fun mapMedicationDispenseToMedicationDispenseSimple(medicationDispense: MedicationDispense): MedicationDispenseSimple {
        val medication = medicationDispense.contained[0] as FhirMedication

        return MedicationDispenseSimple(
            taskId = medicationDispense.identifier[0].value,
            patientIdentifier = medicationDispense.subject.identifier.value,
            // PZN could be optional in future
            uniqueIdentifier = medication.code?.coding?.find { it.system == "http://fhir.de/CodeSystem/ifa/pzn" }?.code
                ?: "",
            wasSubstituted = medicationDispense.substitution.wasSubstituted,
            text = medication.code?.text,
            type = medication.form?.coding?.find { it.system == "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM" }?.code,
            dosageInstruction = medicationDispense.dosageInstruction[0].text,
            performer = (medicationDispense.performer[0] as MedicationDispense.MedicationDispensePerformerComponent).actor.identifier.value,
            whenHandedOver = medicationDispense.whenHandedOver.convertFhirDateToOffsetDateTime()
        )
    }
}

data class PatientDetail(
    val name: String? = null,
    val address: String? = null,
    val birthdate: LocalDate? = null,
    val insuranceIdentifier: String? = null // code == GKV
)

data class PractitionerDetail(
    val name: String? = null,
    val qualification: String? = null,
    val practitionerIdentifier: String? = null // code == LANR (long term practitioner id)
)

data class NormSize(val code: String, @StringRes val text: Int?)

data class MedicationDetail(
    val text: String? = null,
    @StringRes val type: Int? = null,
    val normSize: NormSize? = null,
    val uniqueIdentifier: String? = null, // PZN
)

data class InsuranceCompanyDetail(
    val name: String? = null,
    @StringRes val status: Int? = null
)

data class OrganizationDetail(
    val name: String? = null,
    val address: String? = null,
    val uniqueIdentifier: String? = null, // BSNR
    val phone: String? = null,
    val mail: String? = null
)

data class MedicationRequestDetail(
    val dateOfAccident: LocalDate? = null, // unfalltag
    val location: String? = null, // unfallbetrieb
    val emergencyFee: Boolean? = null, // emergency service fee = notfallgebuehr
    val substitutionAllowed: Boolean = false,
    val dosageInstruction: String? = null
)

fun FhirPatient.mapToUi(): PatientDetail = PatientDetail(
    name = this.name.find { it.use == HumanName.NameUse.OFFICIAL }?.nameAsSingleString,
    address = this.address.find { it.type == Address.AddressType.BOTH }?.let {
        val lines = it.line?.map { l -> l?.value } ?: emptyList()
        val address = lines + it.postalCode + it.city

        address.filterNot { a -> a.isNullOrBlank() }.takeIf { a -> a.isNotEmpty() }
            ?.joinToString(", ")
    },
    birthdate = this.birthDate?.let { LocalDate.from(it.convertFhirDateToLocalDate()) },
    insuranceIdentifier = this.identifier.firstOrNull()?.value
)

fun FhirPractitioner.mapToUi(): PractitionerDetail = PractitionerDetail(
    name = this.name.find { it.use == HumanName.NameUse.OFFICIAL }?.nameAsSingleString,
    qualification = this.qualification.find { it.code?.hasText() == true }?.code?.text,
    practitionerIdentifier = this.identifier.firstOrNull()?.value
)

fun FhirMedication.mapToUi(): MedicationDetail = MedicationDetail(
    text = this.code?.text,
    type = this.form?.coding?.find { it.system == "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM" }?.code?.let {
        codeToDosageFormMapping[it]
    },
    normSize = (this.getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse")?.value as? CodeType?)?.value?.let {
        NormSize(it, normSizeMapping[it])
    },
    uniqueIdentifier = this.code?.coding?.find { it.system == "http://fhir.de/CodeSystem/ifa/pzn" }?.code,
)

fun FhirCoverage.mapToUi() = InsuranceCompanyDetail(
    name = this.payorFirstRep?.display,
    status = (this.getExtensionByUrl("http://fhir.de/StructureDefinition/gkv/versichertenart")?.value as? Coding?)?.code?.let {
        statusMapping[it]
    },
)

fun FhirOrganization.mapToUi() = OrganizationDetail(
    name = this.name,
    address = this.address.find { it.type == Address.AddressType.BOTH }?.line?.firstOrNull()?.value,
    uniqueIdentifier = this.identifier?.find { it.system == "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR" }?.value,
    phone = this.telecom?.find { it.system == ContactPoint.ContactPointSystem.PHONE }?.value,
    mail = this.telecom?.find { it.system == ContactPoint.ContactPointSystem.EMAIL }?.value
)

fun FhirMedicationRequest.mapToUi() = MedicationRequestDetail(
    dateOfAccident = (
        this.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident")
            ?.getExtensionByUrl("unfalltag")?.value as? DateType?
        )
        ?.value
        ?.let { LocalDate.from(it.convertFhirDateToLocalDate()) },
    location = (
        this.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident")
            ?.getExtensionByUrl("unfallbetrieb")?.value as? StringType?
        )?.value,
    emergencyFee = (
        this.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee")
            ?.value as? BooleanType?
        )?.value,
    substitutionAllowed = this.substitution.allowedBooleanType.booleanValue(),
    dosageInstruction = this.extractDosageInstructions()
)

fun Bundle.extractPatient() = this.extractResources<FhirPatient>()?.firstOrNull()?.mapToUi()

fun Bundle.extractMedication() =
    this.extractResources<FhirMedication>()?.firstOrNull()?.mapToUi()

fun Bundle.extractMedicationRequest() =
    this.extractResources<FhirMedicationRequest>()?.firstOrNull()?.mapToUi()

fun Bundle.extractPractitioner() =
    this.extractResources<FhirPractitioner>()?.firstOrNull()?.mapToUi()

fun Bundle.extractInsurance() = this.extractResources<FhirCoverage>()?.firstOrNull()?.mapToUi()

fun Bundle.extractOrganization() =
    this.extractResources<FhirOrganization>()?.firstOrNull()?.mapToUi()
