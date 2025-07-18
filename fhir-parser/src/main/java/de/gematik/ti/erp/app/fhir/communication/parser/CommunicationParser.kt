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

package de.gematik.ti.erp.app.fhir.communication.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationVersions
import de.gematik.ti.erp.app.fhir.communication.model.original.CommunicationProfileType
import de.gematik.ti.erp.app.fhir.communication.model.original.FhirCommunicationResourceType.FhirCommunication
import de.gematik.ti.erp.app.fhir.communication.model.original.FhirCommunicationResourceType.FhirCommunication.Companion.getCommunication
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

@Requirement(
    "O.Source_2#12",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser handles structured FHIR `Communication` resources securely by:
            • Extracting communication entries from the bundle and validating the presence of supported `Communication` profile types [`REPLY`, `DISPENSE`].
            • Applying strict version validation via dedicated profile-version matchers for each type.
            • Mapping only version-conformant entries to internal ERP models via safe mapping functions.
            • Logging and skipping any unknown, malformed, or unsupported profile types or versions without failing the entire parsing process.
        This ensures compliance which mandates secure and profile-conformant parsing of structured `Communication` input.
    """
)
class CommunicationParser : BundleParser {

    override fun extract(bundle: JsonElement): FhirCommunicationBundleErpModel? {
        return runCatching {
            val entries = try {
                bundle.getBundleEntries()
            } catch (e: Exception) {
                Napier.e("Failed to parse input as a bundle", e)
                return@runCatching null
            }

            if (entries.isEmpty()) {
                Napier.w("Communication bundle contains no entries")
                return@runCatching FhirCommunicationBundleErpModel(total = 0, messages = emptyList())
            }

            val messages = entries.mapNotNull { it.resource.getCommunication() }
                .mapNotNull { mapCommunicationToErpModel(it) }

            FhirCommunicationBundleErpModel(total = messages.size, messages = messages)
        }.getOrNull()
    }

    private fun mapCommunicationToErpModel(communication: FhirCommunication): FhirCommunicationEntryErpModel? {
        val profileType = communication.getProfileType()
        val profileVersion = communication.getProfileVersion()

        return when (profileType) {
            CommunicationProfileType.REPLY -> {
                if (isSupportedReplyVersion(profileVersion)) {
                    communication.toReplyErpModel()
                } else {
                    Napier.w("Unsupported Reply Communication version: $profileVersion")
                    null
                }
            }

            CommunicationProfileType.DISPENSE -> {
                if (isSupportedDispenseVersion(profileVersion)) {
                    communication.toDispenseErpModel()
                } else {
                    Napier.w("Unsupported Dispense Communication version: $profileVersion")
                    null
                }
            }

            else -> {
                Napier.w("Unknown communication profile type: ${communication.meta?.profiles}")
                null
            }
        }
    }

    private fun isSupportedReplyVersion(version: String): Boolean {
        return FhirCommunicationVersions.SupportedCommunicationReplyVersions.entries.any { it.version == version }
    }

    private fun isSupportedDispenseVersion(version: String): Boolean {
        return FhirCommunicationVersions.SupportedCommunicationDispenseVersions.entries.any { it.version == version }
    }
}
