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

package de.gematik.ti.erp.app.fhir.constant.prescription.medicationrequest

import de.gematik.ti.erp.app.fhir.constant.prescription.medicationrequest.FhirMedicationRequestConstants.MedicationRequestSpecialRegulationFlagUrl.IS_BVG_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.constant.prescription.medicationrequest.FhirMedicationRequestConstants.MedicationRequestSpecialRegulationFlagUrl.IS_SER_EXTENSION_URL

internal object FhirMedicationRequestConstants {
    const val MEDICATION_REQUEST_1_2_VERSION = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.2"
    const val MEDICATION_REQUEST_1_3_VERSION = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.3"
    const val MULTIPLE_PRESCRIPTION_INFO_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription"

    object MedicationRequestEmergencyFeeUrl {
        const val EMERGENCY_FEE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"
    }

    fun specialRegulationFlag(
        versionFlag: String?
    ) = when (versionFlag) {
        MEDICATION_REQUEST_1_2_VERSION, MEDICATION_REQUEST_1_3_VERSION -> IS_SER_EXTENSION_URL
        else -> IS_BVG_EXTENSION_URL
    }

    private object MedicationRequestSpecialRegulationFlagUrl {
        // Indicates whether the prescription is issued under the Federal War Victims Relief Act (Bundesversorgungsgesetz, abbreviated as BVG).
        const val IS_BVG_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG"

        // Indicates if the prescription is issued under special substitution rules outside of the standard Arzneimittel-Richtlinie (AM-RL), in institutional or specific care scenarios (e.g., nursing homes, prisons).
        const val IS_SER_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_SER"
    }

    object MedicationRequestCoPaymentUrl {
        const val CO_PAYMENT_EXTENSION_URL_102 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment"
        const val CO_PAYMENT_EXTENSION_URL_110 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"
        const val CO_PAYMENT_EXTENSION_URL_130 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"
    }

    const val PRESCRIBER_ID_EXTENSION_URL_120 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Prescriber_ID"
}
