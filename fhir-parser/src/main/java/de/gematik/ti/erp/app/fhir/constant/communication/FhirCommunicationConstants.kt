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

import de.gematik.ti.erp.app.fhir.constant.FhirIdentifierSystems

/**
 * **FHIR Communication Constants**
 * * Contains URLs, extensions, and flow type definitions for FHIR Communication resources.
 */
object FhirCommunicationConstants {

    // ========== Naming Systems ==========

    /** Order ID system for communication resources */
    internal const val ORDER_ID_SYSTEM = FhirIdentifierSystems.Communication.ORDER_ID

    // ========== Extension URLs ==========

    /** Extension for prescription type (e.g., standard, DiGA) */
    internal const val PRESCRIPTION_TYPE_EXTENSION = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType"

    /** Extension for supply options (delivery, on-premise, shipment) */
    internal const val SUPPLY_OPTIONS_TYPE_EXTENSION = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_SupplyOptionsType"

    // ========== Communication Profile URLs ==========

    /** Profile URL for reply communications */
    internal const val COMMUNICATION_REPLY_REPLY_WORKFLOW_PROFILE = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply"

    /** Profile URL for dispense request communications */
    internal const val COMMUNICATION_DISPENSE_WORKFLOW_PROFILE = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq"

    /** Profile URL for DiGA dispense request communications */
    internal const val COMMUNICATION_DISPENSE_DIGA_WORKFLOW_PROFILE = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DiGA"

    // ========== Supply Option Values ==========

    /** Supply option: Pick up on premises */
    internal const val EXT_ON_PREMISE = "onPremise"

    /** Supply option: Shipment to address */
    internal const val EXT_SHIPMENT = "shipment"

    /** Supply option: Delivery service */
    internal const val EXT_DELIVERY = "delivery"

    // ========== Flow Type System ==========

    /** Code system URL for prescription flow types */
    const val FLOW_TYPE_SYSTEM = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType"

    /**
     * **Prescription Flow Types**
     * * Defines all prescription flow types with their codes and display names.
     * Flow types are identified by task ID prefixes (e.g., task "160.xxx.xxx" is MUSTER_16_STANDARD).
     * * Reference: https://simplifier.net/erezept-workflow/gem-erp-cs-flowtype
     * * **GKV (Statutory Health Insurance) - Muster 16:**
     * - 160: Standard prescription (Apothekenpflichtige Arzneimittel)
     * - 162: DiGA prescription (Digitale Gesundheitsanwendungen)
     * - 165: Controlled substances (Betäubungsmittel)
     * - 166: T-prescription (T-Rezepte)
     * - 169: Direct assignment (Direkte Zuweisung)
     * * **PKV (Private Health Insurance):**
     * - 200: Standard prescription
     * - 205: Controlled substances
     * - 206: T-prescription
     * - 209: Direct assignment
     */
    object FlowTypes {
        // GKV (Muster 16) Flow Types
        const val CODE_160 = "160"
        const val DISPLAY_160 = "Muster 16 (Apothekenpflichtige Arzneimittel)"

        const val CODE_162 = "162"
        const val DISPLAY_162 = "Muster 16 (Digitale Gesundheitsanwendungen)"

        const val CODE_165 = "165"
        const val DISPLAY_165 = "Muster 16 (Betäubungsmittel)"

        const val CODE_166 = "166"
        const val DISPLAY_166 = "Muster 16 (T-Rezepte)"

        const val CODE_169 = "169"
        const val DISPLAY_169 = "Muster 16 (Direkte Zuweisung)"

        // PKV Flow Types
        const val CODE_200 = "200"
        const val DISPLAY_200 = "PKV (Apothekenpflichtige Arzneimittel)"

        const val CODE_205 = "205"
        const val DISPLAY_205 = "PKV (Betäubungsmittel)"

        const val CODE_206 = "206"
        const val DISPLAY_206 = "PKV (T-Rezepte)"

        const val CODE_209 = "209"
        const val DISPLAY_209 = "PKV (Direkte Zuweisung)"
    }

    /**
     * Determines the flow type (code and display) based on the task ID.
     * * The task ID starts with the flow type code (e.g., "160.123.456.789").
     * If no matching flow type is found, defaults to flow type 160.
     * * @param taskId The prescription task ID
     * @return Pair of (flowTypeCode, flowTypeDisplay)
     * * **Example:**
     * ```
     * determineFlowType("162.123.456.789") *   returns ("162", "Muster 16 (Digitale Gesundheitsanwendungen)")
     * ```
     * * **To add a new flow type:**
     * 1. Add constants to FlowTypes object (CODE_XXX and DISPLAY_XXX)
     * 2. Add a new case in the when statement below
     */
    fun determineFlowType(taskId: String): Pair<String, String> = when {
        taskId.startsWith(FlowTypes.CODE_160) -> FlowTypes.CODE_160 to FlowTypes.DISPLAY_160
        taskId.startsWith(FlowTypes.CODE_162) -> FlowTypes.CODE_162 to FlowTypes.DISPLAY_162
        taskId.startsWith(FlowTypes.CODE_165) -> FlowTypes.CODE_165 to FlowTypes.DISPLAY_165
        taskId.startsWith(FlowTypes.CODE_166) -> FlowTypes.CODE_166 to FlowTypes.DISPLAY_166
        taskId.startsWith(FlowTypes.CODE_169) -> FlowTypes.CODE_169 to FlowTypes.DISPLAY_169
        taskId.startsWith(FlowTypes.CODE_200) -> FlowTypes.CODE_200 to FlowTypes.DISPLAY_200
        taskId.startsWith(FlowTypes.CODE_205) -> FlowTypes.CODE_205 to FlowTypes.DISPLAY_205
        taskId.startsWith(FlowTypes.CODE_206) -> FlowTypes.CODE_206 to FlowTypes.DISPLAY_206
        taskId.startsWith(FlowTypes.CODE_209) -> FlowTypes.CODE_209 to FlowTypes.DISPLAY_209
        else -> FlowTypes.CODE_160 to FlowTypes.DISPLAY_160 // Default to standard prescription
    }
}
