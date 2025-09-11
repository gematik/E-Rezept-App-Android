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

package de.gematik.ti.erp.app.fhir.pkv.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.FhirChargeItemEntryParserResultErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleLinks
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.firstPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.nextPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.previousPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.selfPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findAccessCode
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findIdentifierFromSystemUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPractitionerTelematikId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPrescriptionId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta.Companion.byProfile
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvEntryConstants
import de.gematik.ti.erp.app.fhir.pkv.model.FhirChargeItemEntryDataErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirChargeItemBundleEntryModel.Companion.chargeItemResource
import kotlinx.serialization.json.JsonElement

class ChargeItemBundleEntryParser : BundleParser {
    override fun extract(bundle: JsonElement): FhirChargeItemEntryParserResultErpModel? {
        val resources = bundle.getBundleEntries()
        val pagingLinks = bundle.getBundleLinks()
        val bundleTotal = resources.size

        val chargeItemEntries = resources.mapNotNull { entry ->
            entry.resource.chargeItemResource().takeIf {
                it?.meta.byProfile(FhirPkvEntryConstants.FHIR_PKV_PROFILE_TAG)
            }?.let { bundleEntry ->
                bundleEntry.identifier.findPrescriptionId()?.let { taskId ->
                    FhirChargeItemEntryDataErpModel(
                        taskId = taskId,
                        accessCode = bundleEntry.identifier.findAccessCode(),
                        telematikId = bundleEntry.enterer.findPractitionerTelematikId(),
                        kvId = bundleEntry.enterer.findIdentifierFromSystemUrl(FhirPkvEntryConstants.FHIR_PKV_KVID)
                    )
                }
            }
        }

        return FhirChargeItemEntryParserResultErpModel(
            bundleTotal = bundleTotal,
            chargeItemEntries = chargeItemEntries,
            firstPageUrl = pagingLinks.firstPage(),
            previousPageUrl = pagingLinks.previousPage(),
            nextPageUrl = pagingLinks.nextPage(),
            selfPageUrl = pagingLinks.selfPage()
        )
    }
}
