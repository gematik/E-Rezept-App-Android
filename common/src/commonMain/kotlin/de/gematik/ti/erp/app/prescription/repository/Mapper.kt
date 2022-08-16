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

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.DomainResource
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Task
import java.time.Instant

typealias FhirTask = org.hl7.fhir.r4.model.Task
typealias FhirCommunication = org.hl7.fhir.r4.model.Communication
typealias FhirPractitioner = org.hl7.fhir.r4.model.Practitioner
typealias FhirMedication = org.hl7.fhir.r4.model.Medication
typealias FhirRatio = org.hl7.fhir.r4.model.Ratio
typealias FhirQuantity = org.hl7.fhir.r4.model.Quantity
typealias FhirMedicationRequest = MedicationRequest
typealias FhirMedicationDispense = org.hl7.fhir.r4.model.MedicationDispense
typealias FhirPatient = org.hl7.fhir.r4.model.Patient
typealias FhirAddress = org.hl7.fhir.r4.model.Address
typealias FhirElement = org.hl7.fhir.r4.model.Element
typealias FhirTaskStatus = org.hl7.fhir.r4.model.Task.TaskStatus
typealias FhirResource = org.hl7.fhir.r4.model.Resource
typealias FhirOrganization = org.hl7.fhir.r4.model.Organization
typealias FhirCoverage = org.hl7.fhir.r4.model.Coverage

inline fun <reified T : DomainResource> Bundle.extractResources(): List<T> =
    entry.map { it.resource }.filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
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

// extracts the very first dosage instruction

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

fun FhirTask.accessCode(): String? {
    identifier.forEach {
        if (it.hasSystem()) {
            if (it.system == "https://gematik.de/fhir/NamingSystem/AccessCode") {
                return it.value
            }
        }
    }
    return null
}

fun FhirTask.extractDateExtension(extensionUrl: String): Instant? {
    val fhirDate = this.getExtensionByUrl(extensionUrl)?.value as DateType?

    return fhirDate?.value?.toInstant()
}
