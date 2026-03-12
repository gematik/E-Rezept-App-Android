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

import de.gematik.ti.erp.app.fhir.constant.communication.CommunicationDigaConstants.DigaDispenseRequestVersion
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants.COMMUNICATION_DISPENSE_DIGA_WORKFLOW_PROFILE
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants.COMMUNICATION_REPLY_REPLY_WORKFLOW_PROFILE

/**
 * **Communication Version Management**
 * * Single source of truth for communication profile versions.
 * * **To add a new version:**
 * 1. Add to CommunicationVersion enum
 * 2. Regexes and lists will auto-update
 */
object FhirCommunicationVersions {

    /**
     * **Communication Versions Enum**
     * * Single source of truth for all communication versions.
     */
    enum class CommunicationVersion(val version: String) {
        V_1_2("1.2"),
        V_1_3("1.3"),
        V_1_4("1.4"),
        V_1_5("1.5"),
        V_1_6("1.6");

        companion object {
            val all: List<String> = entries.map { it.version }
        }
    }

    val COMMUNICATION_REPLY_PROFILE_REGEX =
        Regex("""$COMMUNICATION_REPLY_REPLY_WORKFLOW_PROFILE\|(.+)""")
    val COMMUNICATION_DISPENSE_PROFILE_REGEX =
        Regex("""$COMMUNICATION_DISPENSE_WORKFLOW_PROFILE\|(.+)""")
    val COMMUNICATION_DIGA_DISPENSE_PROFILE_REGEX =
        Regex("""$COMMUNICATION_DISPENSE_DIGA_WORKFLOW_PROFILE\|(.+)""")

    /**
     * Supported versions for Communication Reply profiles.
     * Derived from CommunicationVersion enum.
     */
    enum class SupportedCommunicationReplyVersions(val version: String) {
        V_1_2(CommunicationVersion.V_1_2.version),
        V_1_3(CommunicationVersion.V_1_3.version),
        V_1_4(CommunicationVersion.V_1_4.version),
        V_1_5(CommunicationVersion.V_1_5.version),
        V_1_6(CommunicationVersion.V_1_6.version)
    }

    /**
     * Supported versions for Communication Dispense profiles.
     * Derived from CommunicationVersion enum.
     */
    enum class SupportedCommunicationDispenseVersions(val version: String) {
        V_1_2(CommunicationVersion.V_1_2.version),
        V_1_3(CommunicationVersion.V_1_3.version),
        V_1_4(CommunicationVersion.V_1_4.version),
        V_1_5(CommunicationVersion.V_1_5.version),
        V_1_6(CommunicationVersion.V_1_6.version)
    }

    /**
     * Supported versions for Communication DiGA Dispense profiles.
     * Derived from [DigaDispenseRequestVersion] enum.
     * Production default is [DigaDispenseRequestVersion.PRODUCTION_DEFAULT] (v1.4).
     */
    enum class SupportedCommunicationDigaDispenseVersions(val profileUrl: String) {
        V_1_4(DigaDispenseRequestVersion.V_1_4.profileUrl),
        V_1_5(DigaDispenseRequestVersion.V_1_5.profileUrl),
        V_1_6(DigaDispenseRequestVersion.V_1_6.profileUrl)
    }
}
