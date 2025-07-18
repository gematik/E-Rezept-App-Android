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

package de.gematik.ti.erp.app.fhir.audit.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.audit.model.erp.FhirAuditEventErpModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditBundleMetaModel.Companion.getMeta
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditEventModel.Companion.toAuditEvent
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirAuditEventsErpModelCollection
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceBundle.Companion.parseResourceBundle
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

class AuditEventsParser : BundleParser {

    override fun extract(bundle: JsonElement): FhirAuditEventsErpModelCollection? {
        val auditBundleMeta = bundle.getMeta()
        val entries = bundle.parseResourceBundle()

        try {
            return FhirAuditEventsErpModelCollection(
                bundleId = auditBundleMeta.bundleId ?: "",
                count = auditBundleMeta.total ?: 0,
                auditEvents = entries.map { entry ->
                    entry.resource.toAuditEvent().toErpModel()
                }
            )
        } catch (e: Exception) {
            Napier.e { "Error parsing audit events ${e.message}" }
            return null
        }
    }
}
