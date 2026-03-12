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

import de.gematik.ti.erp.app.utils.Reference

/**
 * **Medication Request Constants**
 * * Constants for FHIR Medication Request profiles, versions, and extensions.
 * * **To add a new version:**
 * Add to MedicationRequestVersion enum and update version-specific logic.
 */
internal object FhirMedicationRequestConstants {

    /**
     * **Medication Request Versions**
     * * Supported KBV prescription profile versions.
     */
    enum class MedicationRequestVersion(val profileUrl: String) {
        V_1_2("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.2"),

        @Reference(
            info = "1.3.2 version",
            url = "https://simplifier.net/packages/kbv.ita.erp/1.3.2/files/2880357"
        )
        V_1_3("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.3"),

        V_1_4("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.4");

        companion object {
            val all: List<String> = entries.map { it.profileUrl }
        }
    }

    const val MULTIPLE_PRESCRIPTION_INFO_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription"

    object MedicationRequestEmergencyFeeUrl {
        const val EMERGENCY_FEE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"
    }

    /**
     * Determines which special regulation flag extension to use based on version.
     */
    fun specialRegulationFlag(versionFlag: String?) = when (versionFlag) {
        MedicationRequestVersion.V_1_2.profileUrl,
        MedicationRequestVersion.V_1_3.profileUrl,
        MedicationRequestVersion.V_1_4.profileUrl -> MedicationRequestSpecialRegulationFlagUrl.IS_SER_EXTENSION_URL

        else -> MedicationRequestSpecialRegulationFlagUrl.IS_BVG_EXTENSION_URL
    }

    private object MedicationRequestSpecialRegulationFlagUrl {
        // Indicates whether the prescription is issued under the Federal War Victims Relief Act (Bundesversorgungsgesetz, abbreviated as BVG).
        const val IS_BVG_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG"

        // Indicates if the prescription is issued under special substitution rules outside of the standard Arzneimittel-Richtlinie (AM-RL), in institutional or specific care scenarios (e.g., nursing homes, prisons).
        const val IS_SER_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_SER"
    }

    /**
     * **Co-Payment Extension URLs**
     * * Version-specific URLs for co-payment status.
     */
    object MedicationRequestCoPaymentUrl {
        const val CO_PAYMENT_EXTENSION_URL_102 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment"
        const val CO_PAYMENT_EXTENSION_URL_110 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"
        const val CO_PAYMENT_EXTENSION_URL_130 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"
    }

    const val PRESCRIBER_ID_EXTENSION_URL_120 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Prescriber_ID"
}
