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

package de.gematik.ti.erp.app.fhir.dispense.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestDosageInstruction
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestText

/**
 * Common interface for all MedicationDispense models (V14, V15, EU V1.0)
 */
internal interface MedicationDispenseCommon {
    val id: String?
    val meta: FhirMeta?
    val identifier: List<FhirIdentifier>
    val status: String?
    val subject: FhirMedicationDispenseIdentifier?
    val performer: List<FhirMedicationDispenseActor>
    val whenHandedOver: String?
    val dosageInstruction: List<FhirMedicationRequestDosageInstruction>
    val whenPrepared: String?
    val medicationReference: FhirMedicationDispenseReference?
    val substitution: FhirMedicationDispenseSubstitution?
    val extension: List<FhirExtension>
    val note: List<FhirMedicationRequestText>

    /**
     * Checks if this dispense is for a DiGA
     */
    fun isDigaType(): Boolean
}
