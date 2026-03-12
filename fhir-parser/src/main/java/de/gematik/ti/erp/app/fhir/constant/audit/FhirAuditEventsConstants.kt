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

package de.gematik.ti.erp.app.fhir.constant.audit

import de.gematik.ti.erp.app.fhir.constant.FhirIdentifierSystems

/**
 * **Audit Event Constants**
 * * Version-specific identifiers for FHIR Audit Events.
 */
object FhirAuditEventsConstants {

    /**
     * Version-specific identifier mappings for audit events.
     * Version 1.1 uses legacy naming, 1.2+ uses current standards.
     */
    object PrescriptionIdentifiers {
        /** Legacy (v1.1) prescription identifier system */
        const val VERSION_1_1 = "https://gematik.de/fhir/NamingSystem/PrescriptionID"

        /** Current (v1.2+) prescription identifier system */
        const val VERSION_1_2 = FhirIdentifierSystems.Prescription.PRESCRIPTION_ID
    }

    object PatientIdentifiers {
        /** Legacy (v1.1) KVNR naming system */
        const val VERSION_1_1 = "http://fhir.de/NamingSystem/gkv/kvid-10"

        /** Current (v1.2+) KVNR identifier */
        const val VERSION_1_2 = FhirIdentifierSystems.Patient.KVNR_GKV
    }

    /** Telematik ID (consistent across versions) */
    const val TELEMATIK_ID = FhirIdentifierSystems.Healthcare.TELEMATIK_ID

    internal fun String.sanitizeAsAuditEventDescription(): String {
        return removeSurrounding(
            "<div xmlns=\"http://www.w3.org/1999/xhtml\">",
            "</div>"
        )
    }
}
