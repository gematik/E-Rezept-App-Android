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

package de.gematik.ti.erp.app.fhir.constant.communication

import de.gematik.ti.erp.app.fhir.constant.communication.CommunicationDigaConstants.DigaDispenseRequestVersion.Companion.all

/**
 * **DiGA (Digital Health Applications) Communication Constants**
 * * Specific constants for DiGA prescriptions (flow type 162).
 */
object CommunicationDigaConstants {

    /**
     * **DiGA Dispense Request Versions**
     * * Single source of truth for all DiGA dispatch request profile versions.
     * * These are profile versions for [FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE]
     *   used when building a DiGA (flow type 162) communication resource.
     * * **To add a new version:** Add an entry here; [all] and [FhirCommunicationVersions] auto-update.
     * * Production default is [V_1_4].
     */
    enum class DigaDispenseRequestVersion(val profileUrl: String) {
        V_1_4("${FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE}|1.4"),
        V_1_5("${FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE}|1.5"),
        V_1_6("${FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE}|1.6");

        companion object {
            /** Production default – only this version is used in non-debug builds. */
            val PRODUCTION_DEFAULT: DigaDispenseRequestVersion = V_1_4

            val all: List<String> = entries.map { it.profileUrl }
        }
    }

    /**
     * The URL identifying the FHIR extension for prescription type (e.g., DiGA).
     */
    internal const val PRESCRIPTION_TYPE_VALUE_CODING =
        "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType"

    /**
     * The code representing prescription type 162, used for DiGA (Digitale Gesundheitsanwendungen).
     * References the flow type defined in FhirCommunicationConstants.FlowTypes.
     */
    internal const val VALUE_CODING_TYPE_162 = FhirCommunicationConstants.FlowTypes.CODE_162

    /**
     * Display text for DiGA flow type.
     * References the display text defined in FhirCommunicationConstants.FlowTypes.
     */
    internal const val FLOW_TYPE_DISPLAY_162 = FhirCommunicationConstants.FlowTypes.DISPLAY_162
}
