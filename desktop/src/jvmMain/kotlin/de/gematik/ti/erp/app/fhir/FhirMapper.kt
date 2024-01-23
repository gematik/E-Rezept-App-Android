/*
 * Copyright (c) 2024 gematik GmbH
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

@file:Suppress("ktlint:max-line-length", "ktlint:argument-list-wrapping")

package de.gematik.ti.erp.app.fhir

import ca.uhn.fhir.parser.IParser
import de.gematik.ti.erp.app.prescription.repository.CommunicationPayloadInbox
import de.gematik.ti.erp.app.prescription.repository.CommunicationProfile
import de.gematik.ti.erp.app.prescription.repository.SimpleCommunication
import de.gematik.ti.erp.app.prescription.repository.SimpleCommunicationWithPharmacy
import de.gematik.ti.erp.app.prescription.repository.model.SimpleAuditEvent
import de.gematik.ti.erp.app.prescription.repository.model.SimpleMedicationDispense
import de.gematik.ti.erp.app.prescription.repository.model.SimpleTask
import de.gematik.ti.erp.app.utils.convertFhirDateToLocalDate
import de.gematik.ti.erp.app.utils.convertFhirDateToLocalDateTime
import java.time.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
import org.hl7.fhir.r4.model.MedicationDispense.MedicationDispensePerformerComponent
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Task

typealias FhirPractitioner = org.hl7.fhir.r4.model.Practitioner
typealias FhirMedication = org.hl7.fhir.r4.model.Medication
typealias FhirMedicationRequest = MedicationRequest
typealias FhirPatient = org.hl7.fhir.r4.model.Patient
typealias FhirOrganization = org.hl7.fhir.r4.model.Organization
typealias FhirCoverage = org.hl7.fhir.r4.model.Coverage
typealias FhirCommunication = org.hl7.fhir.r4.model.Communication
typealias FhirTask = org.hl7.fhir.r4.model.Task

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
    return entries()?.let { it ->
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

fun Bundle.BundleEntryComponent.entries(): List<Bundle.BundleEntryComponent> {
    return resource.getChildByName("entry").values as List<Bundle.BundleEntryComponent>
}

fun FhirTask.extractKBVBundleReference(): String? {
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
    val dosageInstruction = dosageInstruction.firstOrNull()

    val dosageFlag =
        (dosageInstruction?.getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag")?.value as? BooleanType?)?.value
    return dosageInstruction?.let {
        if (dosageFlag != null) {
            // dosage flag is set
            if (dosageFlag) {
                dosageInstruction.text ?: ""
            } else {
                null
            }
        } else {
            dosageInstruction.text
        }
    }
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

fun FhirTask.prescriptionId(): String? {
    identifier.forEach {
        if (it.hasSystem()) {
            if (it.system == "https://gematik.de/fhir/NamingSystem/PrescriptionID") {
                return it.value
            }
        }
    }
    return null
}

class FhirMapper(
    private val fhirParser: IParser,
    private val json: Json
) {
    fun parseBundle(bundle: String): Bundle = fhirParser.parseResource(bundle) as Bundle

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
     * Maps Task and KBV Bundle together to a complete Task Entity
     *
     * @param bundle the bundle consists of a Task which is referencing an included KBV Bundle.
     */
    fun mapFhirBundleToTaskWithKBVBundle(bundle: Bundle): SimpleTask =
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

                SimpleTask(
                    taskId = fhirTask.idElement.idPart,
                    lastModified = fhirTask.lastModified.convertFhirDateToLocalDateTime(),
                    organization = fhirOrganization.name
                        ?: fhirPractitioner.nameFirstRep.nameAsSingleString,
                    medicationText = fhirMedication.code.text,
                    expiresOn = requireNotNull(
                        extractDateExtension(
                            fhirTask,
                            "https://gematik.de/fhir/StructureDefinition/ExpiryDate"
                        )
                    ),
                    acceptUntil = requireNotNull(
                        extractDateExtension(
                            fhirTask,
                            "https://gematik.de/fhir/StructureDefinition/AcceptDate"
                        )
                    ),
                    authoredOn = fhirMedicationRequest.authoredOn.convertFhirDateToLocalDateTime(),
                    status = fhirTask.status.definition,
                    rawKBVBundle = kbvBundle.resource as Bundle
                )
            } ?: error("KBV Bundle not found!")
        } ?: error("No task found!")

    /**
     * Throws an exception if the bundle couldn't be parsed.
     */
    fun parseKBVBundle(rawKBVBundle: ByteArray): Bundle {
        return fhirParser.parseResource(rawKBVBundle.decodeToString()) as Bundle
    }

    fun mapFhirBundleToAuditEvents(fhirBundle: Bundle): List<SimpleAuditEvent> {
        return fhirBundle.extractResources<AuditEvent>()?.map {
            SimpleAuditEvent(
                id = it.idElement.idPart,
                locale = if (it.language.isNullOrEmpty()) "de" else it.language,
                text = it.text.div.allText(),
                timestamp = it.recorded.convertFhirDateToLocalDateTime(),
                taskId = it.entity[0].what.referenceElement.idPart
            )
        } ?: error("No AuditEvent found in given Bundle $fhirBundle")
    }

    fun mapFhirMedicationDispenseToSimpleMedicationDispense(fhirBundle: Bundle): List<SimpleMedicationDispense> {
        return fhirBundle.extractResources<MedicationDispense>()?.map {
            SimpleMedicationDispense(
                id = it.idElement.idPart,
                taskId = it.identifier.first().value,
                patientIdentifier = it.subject.identifier.value,
                wasSubstituted = it.substitution.wasSubstituted,
                medicationDetail = (it.contained.first() as FhirMedication).mapToUi(),
                dosageInstruction = it.dosageInstruction.firstOrNull()?.text,
                performer = (it.performer.first() as MedicationDispensePerformerComponent).actor.identifier.value,
                whenHandedOver = it.whenHandedOver.convertFhirDateToLocalDateTime()
            )
        } ?: error("No MedicationDispense found in given Bundle $fhirBundle")
    }

    private val COMMUNICATION_TYPE_DISP_REQ = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"
    private val COMMUNICATION_TYPE_REPLY = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"

    fun mapFhirBundleToSimpleCommunications(bundle: Bundle): List<SimpleCommunication> {
        return bundle.extractResources<FhirCommunication>()?.mapNotNull { fhirCommunication ->
            when (fhirCommunication.meta.profile.first().value.split("|").first()) {
                COMMUNICATION_TYPE_DISP_REQ -> mapToSimpleCommunicationWithPharmacyAsDispReq(fhirCommunication)
                COMMUNICATION_TYPE_REPLY -> mapToSimpleCommunicationWithPharmacyAsReply(fhirCommunication)
                else -> null // ignore other communications
            }
        } ?: error("No communication found!")
    }

    private fun extractTaskIdFromReference(reference: String): String {
        return reference.split("/")[1]
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun mapToSimpleCommunicationWithPharmacyAsDispReq(
        fhirCommunication: FhirCommunication
    ) = SimpleCommunicationWithPharmacy(
        id = fhirCommunication.idElement.idPart,
        profile = CommunicationProfile.DispenseRequest,
        basedOnTaskWithId = extractTaskIdFromReference(fhirCommunication.basedOn.first().reference),
        sent = fhirCommunication.sent?.convertFhirDateToLocalDateTime(),
        telematicsId = fhirCommunication.recipient.first().identifier.value,
        userId = fhirCommunication.sender.identifier.value,
        payload = runCatching {
            json.decodeFromString<CommunicationPayloadInbox>(fhirCommunication.payload.first().content.toString())
        }.getOrNull()
    )

    @OptIn(ExperimentalSerializationApi::class)
    private fun mapToSimpleCommunicationWithPharmacyAsReply(
        fhirCommunication: FhirCommunication
    ) = SimpleCommunicationWithPharmacy(
        id = fhirCommunication.idElement.idPart,
        profile = CommunicationProfile.Reply,
        basedOnTaskWithId = extractTaskIdFromReference(fhirCommunication.basedOn.first().reference),
        sent = fhirCommunication.sent?.convertFhirDateToLocalDateTime(),
        userId = fhirCommunication.recipient.first().identifier.value,
        telematicsId = fhirCommunication.sender.identifier.value,
        payload = json.decodeFromString(fhirCommunication.payload.first().content.toString())
    )
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

data class MedicationDetail(
    val text: String? = null,
    val dosageCode: String? = null,
    val normSizeCode: String? = null,
    val uniqueIdentifier: String? = null // PZN
)

data class InsuranceCompanyDetail(
    val name: String? = null,
    val statusCode: String? = null
)

data class OrganizationDetail(
    val name: String? = null,
    val address: String? = null,
    val uniqueIdentifier: String? = null, // BSNR
    val phone: String? = null,
    val mail: String? = null
)

// reference: https://simplifier.net/erezept/kbvprerpprescription
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
    dosageCode = this.form?.coding?.find { it.system == "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM" }?.code,
    normSizeCode = (this.getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse")?.value as? CodeType?)?.value,
    uniqueIdentifier = this.code?.coding?.find { it.system == "http://fhir.de/CodeSystem/ifa/pzn" }?.code
)

fun FhirCoverage.mapToUi() = InsuranceCompanyDetail(
    name = this.payorFirstRep?.display,
    statusCode = (this.getExtensionByUrl("http://fhir.de/StructureDefinition/gkv/versichertenart")?.value as? Coding?)?.code
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
