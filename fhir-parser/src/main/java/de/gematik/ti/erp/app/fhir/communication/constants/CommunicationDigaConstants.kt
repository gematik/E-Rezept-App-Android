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

package de.gematik.ti.erp.app.fhir.communication.constants

object CommunicationDigaConstants {

    internal const val COMMUNICATION_DIGA_DISP_REQUEST_VERSION = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq|1.4"

    /**
     * The URL identifying the FHIR extension for prescription type (e.g., DiGA).
     */
    internal const val PRESCRIPTION_TYPE_VALUE_CODING =
        "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType"

    /**
     * The code representing prescription type 162, used for DiGA (Digitale Gesundheitsanwendungen).
     */
    internal const val VALUE_CODING_TYPE_162 = "162"

    internal const val FLOW_TYPE_DISPLAY_162 = "Muster 16 (Digitale Gesundheitsanwendungen)"
}
