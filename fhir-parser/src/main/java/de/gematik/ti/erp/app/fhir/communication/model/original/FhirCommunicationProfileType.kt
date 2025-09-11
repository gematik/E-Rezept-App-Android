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

package de.gematik.ti.erp.app.fhir.communication.model.original

import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions.COMMUNICATION_DIGA_DISPENSE_PROFILE_REGEX
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions.COMMUNICATION_DISPENSE_PROFILE_REGEX
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions.COMMUNICATION_REPLY_PROFILE_REGEX

enum class CommunicationProfileType(val identifier: String) {
    REPLY(FhirCommunicationConstants.COMMUNICATION_REPLY_REPLY_WORKFLOW_PROFILE),
    DISPENSE(FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE),

    // https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DiGA
    DIGA_DISPENSE(FhirCommunicationConstants.COMMUNICATION_DISPENSE_DIGA_WORKFLOW_PROFILE),
    UNKNOWN("");

    fun getVersionFromProfile(profile: String): String {
        val regex = when (this) {
            REPLY -> COMMUNICATION_REPLY_PROFILE_REGEX
            DISPENSE -> COMMUNICATION_DISPENSE_PROFILE_REGEX
            DIGA_DISPENSE -> COMMUNICATION_DIGA_DISPENSE_PROFILE_REGEX
            UNKNOWN -> return ""
        }

        val match = regex.find(profile)
        return match?.groupValues?.getOrNull(1) ?: ""
    }

    companion object {
        fun fromProfiles(profiles: List<String>?): CommunicationProfileType {
            return profiles?.firstNotNullOfOrNull { profile ->
                CommunicationProfileType.entries.firstOrNull { it != UNKNOWN && profile.contains(it.identifier) }
            } ?: UNKNOWN
        }
    }
}
