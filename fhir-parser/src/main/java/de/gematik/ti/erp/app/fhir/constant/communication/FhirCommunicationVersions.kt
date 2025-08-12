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

import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants.COMMUNICATION_DISPENSE_PROFILE_BASE
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants.COMMUNICATION_REPLY_PROFILE_BASE

object FhirCommunicationVersions {
    const val COMMUNICATION_VERSION_1_2 = "1.2"
    const val COMMUNICATION_VERSION_1_3 = "1.3"
    const val COMMUNICATION_VERSION_1_4 = "1.4"

    val COMMUNICATION_REPLY_PROFILE_REGEX =
        Regex("""$COMMUNICATION_REPLY_PROFILE_BASE\|(.+)""")
    val COMMUNICATION_DISPENSE_PROFILE_REGEX =
        Regex("""$COMMUNICATION_DISPENSE_PROFILE_BASE\|(.+)""")

    enum class SupportedCommunicationReplyVersions(val version: String) {
        V_1_2(COMMUNICATION_VERSION_1_2),
        V_1_3(COMMUNICATION_VERSION_1_3),
        V_1_4(COMMUNICATION_VERSION_1_4);
    }

    enum class SupportedCommunicationDispenseVersions(val version: String) {
        V_1_2(COMMUNICATION_VERSION_1_2),
        V_1_3(COMMUNICATION_VERSION_1_3),
        V_1_4(COMMUNICATION_VERSION_1_4)
    }
}
