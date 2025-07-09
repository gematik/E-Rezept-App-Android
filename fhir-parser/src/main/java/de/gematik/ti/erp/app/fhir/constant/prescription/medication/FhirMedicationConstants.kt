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

package de.gematik.ti.erp.app.fhir.constant.prescription.medication

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl

@Suppress("ktlint:max-line-length")
object FhirMedicationConstants {
    private const val MEDICATION_CATEGORY = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"
    private const val MEDICATION_CATEGORY_SYSTEM = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category"
    private const val MEDICATION_VACCINE_EXTENSION = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine"
    private const val EPA_MEDICATION_VACCINE_EXTENSION = "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-id-vaccine-extension"
    private const val MEDICATION_NORM_GROSSE = "http://fhir.de/StructureDefinition/normgroesse"
    private const val MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction"
    private const val EPA_MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS = "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-manufacturing-instructions-extension"
    private const val MEDICATION_COMPOUNDING_PACKAGING = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging"
    private const val EPA_MEDICATION_COMPOUNDING_PACKAGING = "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"

    internal fun List<FhirExtension>.findMedicationCategory110() =
        findExtensionByUrl(MEDICATION_CATEGORY)?.valueCoding?.code

    internal fun List<FhirExtension>.findMedicationCategory102(): String? =
        findExtensionByUrl(MEDICATION_CATEGORY)
            ?.takeIf { it.valueCoding?.system == MEDICATION_CATEGORY_SYSTEM }
            ?.valueCoding?.code

    internal fun List<FhirExtension>.getVaccine(): Boolean =
        findExtensionByUrl(MEDICATION_VACCINE_EXTENSION)?.valueBoolean == true

    internal fun List<FhirExtension>.getEpaVaccine(): Boolean =
        findExtensionByUrl(EPA_MEDICATION_VACCINE_EXTENSION)?.valueBoolean == true

    internal fun List<FhirExtension>.getNormSizeCode(): String? = findExtensionByUrl(
        MEDICATION_NORM_GROSSE
    )?.valueCode

    internal fun List<FhirExtension>.getCompoundingInstructions(): String? =
        findExtensionByUrl(MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS)?.valueString

    internal fun List<FhirExtension>.getEpaCompoundingInstructions(): String? =
        findExtensionByUrl(EPA_MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS)?.valueString

    internal fun List<FhirExtension>.getCompoundingPackaging(): String? =
        findExtensionByUrl(MEDICATION_COMPOUNDING_PACKAGING)?.valueString

    internal fun List<FhirExtension>.getEpaCompoundingPackaging(): String? =
        findExtensionByUrl(EPA_MEDICATION_COMPOUNDING_PACKAGING)?.valueString
}
