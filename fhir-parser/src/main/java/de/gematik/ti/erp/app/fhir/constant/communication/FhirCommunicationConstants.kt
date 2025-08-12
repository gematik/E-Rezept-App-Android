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

object FhirCommunicationConstants {
    // Base URLs
    private const val GEMATIK_FHIR_URL = "https://gematik.de/fhir"
    private const val ERP_FHIR_URL = "$GEMATIK_FHIR_URL/erp"

    // Naming systems
    private const val NAMING_SYSTEM_BASE_URL = "$GEMATIK_FHIR_URL/NamingSystem"
    internal const val ORDER_ID_SYSTEM = "$NAMING_SYSTEM_BASE_URL/OrderID"

    // Structure definitions
    private const val STRUCTURE_DEFINITION_ERP_FHIR_URL = "$ERP_FHIR_URL/StructureDefinition"
    internal const val PRESCRIPTION_TYPE_EXTENSION = "$STRUCTURE_DEFINITION_ERP_FHIR_URL/GEM_ERP_EX_PrescriptionType"
    internal const val SUPPLY_OPTIONS_TYPE_EXTENSION = "$STRUCTURE_DEFINITION_ERP_FHIR_URL/GEM_ERP_EX_SupplyOptionsType"

    // Profile URLs without versions
    internal const val COMMUNICATION_REPLY_PROFILE_BASE = "$STRUCTURE_DEFINITION_ERP_FHIR_URL/GEM_ERP_PR_Communication_Reply"
    internal const val COMMUNICATION_DISPENSE_PROFILE_BASE = "$STRUCTURE_DEFINITION_ERP_FHIR_URL/GEM_ERP_PR_Communication_DispReq"

    internal const val EXT_ON_PREMISE = "onPremise"
    internal const val EXT_SHIPMENT = "shipment"
    internal const val EXT_DELIVERY = "delivery"

    const val FLOW_TYPE_SYSTEM = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType"

    // Flow type constants
    private const val FLOW_TYPE_CODE_160 = "160"
    private const val FLOW_TYPE_DISPLAY_160 = "Muster 16 (Apothekenpflichtige Arzneimittel)"

    private const val FLOW_TYPE_CODE_165 = "165"
    private const val FLOW_TYPE_DISPLAY_165 = "Muster 16 (Betäubungsmittel)"

    private const val FLOW_TYPE_CODE_166 = "166"
    private const val FLOW_TYPE_DISPLAY_166 = "Muster 16 (T-Rezepte)"

    private const val FLOW_TYPE_CODE_169 = "169"
    private const val FLOW_TYPE_DISPLAY_169 = "Muster 16 (Direkte Zuweisung)"

    private const val FLOW_TYPE_CODE_200 = "200"
    private const val FLOW_TYPE_DISPLAY_200 = "PKV (Apothekenpflichtige Arzneimittel)"

    private const val FLOW_TYPE_CODE_205 = "205"
    private const val FLOW_TYPE_DISPLAY_205 = "PKV (Betäubungsmittel)"

    private const val FLOW_TYPE_CODE_206 = "206"
    private const val FLOW_TYPE_DISPLAY_206 = "PKV (T-Rezepte)"

    private const val FLOW_TYPE_CODE_209 = "209"
    private const val FLOW_TYPE_DISPLAY_209 = "PKV (Direkte Zuweisung)"

    fun determineFlowType(taskId: String): Pair<String, String> {
        return when {
            taskId.startsWith("160") -> FLOW_TYPE_CODE_160 to FLOW_TYPE_DISPLAY_160
            taskId.startsWith("162") -> CommunicationDigaConstants.VALUE_CODING_TYPE_162 to CommunicationDigaConstants.FLOW_TYPE_DISPLAY_162
            taskId.startsWith("165") -> FLOW_TYPE_CODE_165 to FLOW_TYPE_DISPLAY_165
            taskId.startsWith("166") -> FLOW_TYPE_CODE_166 to FLOW_TYPE_DISPLAY_166
            taskId.startsWith("169") -> FLOW_TYPE_CODE_169 to FLOW_TYPE_DISPLAY_169
            taskId.startsWith("200") -> FLOW_TYPE_CODE_200 to FLOW_TYPE_DISPLAY_200
            taskId.startsWith("205") -> FLOW_TYPE_CODE_205 to FLOW_TYPE_DISPLAY_205
            taskId.startsWith("206") -> FLOW_TYPE_CODE_206 to FLOW_TYPE_DISPLAY_206
            taskId.startsWith("209") -> FLOW_TYPE_CODE_209 to FLOW_TYPE_DISPLAY_209
            else -> {
                FLOW_TYPE_CODE_160 to FLOW_TYPE_DISPLAY_160
            }
        }
    }
}
