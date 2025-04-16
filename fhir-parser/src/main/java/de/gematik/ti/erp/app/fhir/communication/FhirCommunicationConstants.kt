/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.communication

object FhirCommunicationConstants {
    // Base URLs
    const val GEMATIK_BASE_URL = "https://gematik.de/fhir"
    const val ERP_BASE_URL = "$GEMATIK_BASE_URL/erp"

    // Naming systems
    const val NAMING_SYSTEM_BASE_URL = "$GEMATIK_BASE_URL/NamingSystem"
    const val ORDER_ID_SYSTEM = "$NAMING_SYSTEM_BASE_URL/OrderID"

    // Structure definitions
    const val STRUCTURE_DEFINITION_BASE_URL = "$ERP_BASE_URL/StructureDefinition"
    const val PRESCRIPTION_TYPE_EXTENSION = "$STRUCTURE_DEFINITION_BASE_URL/GEM_ERP_EX_PrescriptionType"
    const val SUPPLY_OPTIONS_TYPE_EXTENSION = "$STRUCTURE_DEFINITION_BASE_URL/GEM_ERP_EX_SupplyOptionsType"

    // Profile URLs without versions
    const val COMMUNICATION_REPLY_PROFILE_BASE = "$STRUCTURE_DEFINITION_BASE_URL/GEM_ERP_PR_Communication_Reply"
    const val COMMUNICATION_DISPENSE_PROFILE_BASE = "$STRUCTURE_DEFINITION_BASE_URL/GEM_ERP_PR_Communication_DispReq"

    const val EXT_ON_PREMISE = "onPremise"
    const val EXT_SHIPMENT = "shipment"
    const val EXT_DELIVERY = "delivery"

    const val RECIPIENT_IDENTIFIER_SYSTEM = "https://gematik.de/fhir/sid/telematik-id"
    const val FLOW_TYPE_SYSTEM = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType"

    // Flow type constants
    const val FLOW_TYPE_CODE_160 = "160"
    const val FLOW_TYPE_DISPLAY_160 = "Muster 16 (Apothekenpflichtige Arzneimittel)"
    const val FLOW_TYPE_CODE_162 = "162"
    const val FLOW_TYPE_DISPLAY_162 = "Muster 16 (Digitale Gesundheitsanwendungen)"
    const val FLOW_TYPE_CODE_165 = "165"
    const val FLOW_TYPE_DISPLAY_165 = "Muster 16 (Betäubungsmittel)"
    const val FLOW_TYPE_CODE_166 = "166"
    const val FLOW_TYPE_DISPLAY_166 = "Muster 16 (T-Rezepte)"
    const val FLOW_TYPE_CODE_169 = "169"
    const val FLOW_TYPE_DISPLAY_169 = "Muster 16 (Direkte Zuweisung)"
    const val FLOW_TYPE_CODE_200 = "200"
    const val FLOW_TYPE_DISPLAY_200 = "PKV (Apothekenpflichtige Arzneimittel)"
    const val FLOW_TYPE_CODE_205 = "205"
    const val FLOW_TYPE_DISPLAY_205 = "PKV (Betäubungsmittel)"
    const val FLOW_TYPE_CODE_206 = "206"
    const val FLOW_TYPE_DISPLAY_206 = "PKV (T-Rezepte)"
    const val FLOW_TYPE_CODE_209 = "209"
    const val FLOW_TYPE_DISPLAY_209 = "PKV (Direkte Zuweisung)"

    fun determineFlowType(taskId: String): Pair<String, String> {
        return when {
            taskId.startsWith("160") -> FLOW_TYPE_CODE_160 to FLOW_TYPE_DISPLAY_160
            taskId.startsWith("162") -> FLOW_TYPE_CODE_162 to FLOW_TYPE_DISPLAY_162
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
