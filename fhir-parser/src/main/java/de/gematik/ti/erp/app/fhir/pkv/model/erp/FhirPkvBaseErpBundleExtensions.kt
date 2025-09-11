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

package de.gematik.ti.erp.app.fhir.pkv.model.erp

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findIdentifierFromSystemUrl
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvChargeItemConstants.ACCESS_CODE_SYSTEM_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvChargeItemConstants.KVNR_SYSTEM_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvChargeItemConstants.TASKID_SYSTEM_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvChargeItemConstants.TELEMATIK_ID_SYSTEM_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.GEM_ERP_INVOICE_BINARY_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.GEM_ERP_KBV_PR_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBundleReference.Companion.getReferenceByUrl
import de.gematik.ti.erp.app.utils.sanitize

internal fun FhirPkvBaseBundle.toFhirPkvChargeItemErpModel() = FhirPkvChargeItemErpModel(
    id = id,
    taskId = identifier.findIdentifierFromSystemUrl(TASKID_SYSTEM_URL),
    accessCode = identifier.findIdentifierFromSystemUrl(ACCESS_CODE_SYSTEM_URL),
    kvnr = subject.findIdentifierFromSystemUrl(KVNR_SYSTEM_URL),
    telematikId = enterer.findIdentifierFromSystemUrl(TELEMATIK_ID_SYSTEM_URL),
    version = meta?.profiles?.firstOrNull()?.trim()?.split("|")?.getOrNull(1),
    kbvReference = supportingInformation.getReferenceByUrl(GEM_ERP_KBV_PR_BUNDLE_URL)?.sanitize(),
    invoiceReference = supportingInformation.getReferenceByUrl(DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL)
        ?.sanitize(),
    invoiceBinaryReference = supportingInformation.getReferenceByUrl(
        GEM_ERP_INVOICE_BINARY_BUNDLE_URL
    )?.sanitize()
)
