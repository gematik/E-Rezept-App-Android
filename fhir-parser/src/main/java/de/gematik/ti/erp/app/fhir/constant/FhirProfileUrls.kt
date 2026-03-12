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

package de.gematik.ti.erp.app.fhir.constant

import de.gematik.ti.erp.app.fhir.constant.communication.CommunicationDigaConstants
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions
import de.gematik.ti.erp.app.fhir.constant.consent.ConsentConstants
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants
import de.gematik.ti.erp.app.fhir.constant.prescription.medicationrequest.FhirMedicationRequestConstants

/**
 * **FHIR Profile URLs**
 *
 * Centralized lists of profile URLs used across the application.
 */
object FhirProfileUrls {

    val TASK_PROFILE_URLS: List<String> = FhirVersions.TaskVersion.allProfileUrls

    val KBV_BUNDLE_PROFILE_URLS: List<String> = FhirVersions.KbvBundleVersion.supportedProfileUrls

    val KBV_DEVICE_REQUEST_PROFILE_URLS: List<String> = FhirVersions.KbvDeviceRequestVersion.allProfileUrls

    val MEDICATION_DISPENSE_PROFILE_URLS: List<String> =
        FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.entries
            .filter { it.profileUrl.isNotEmpty() }
            .map { it.profileUrl }

    val CONSENT_PROFILE_URLS: List<String> = listOf(
        ConsentConstants.ErpCharge.V1_0.profileUrl,
        ConsentConstants.ErpCharge.V1_1.profileUrl,
        ConsentConstants.ErpEu().profileUrl
    )

    val COMMUNICATION_PROFILE_URLS: List<String> =
        FhirCommunicationVersions.CommunicationVersion.entries.map {
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication|${it.version}"
        }

    val COMMUNICATION_DIGA_PROFILE_URLS: List<String> =
        CommunicationDigaConstants.DigaDispenseRequestVersion.all

    val EUREDEEM_PROFILE_URLS: List<String> =
        FhirEuRedeemAccessCodeRequestConstants.FhirEuRedeemAccessCodeRequestMeta.entries.map { it.identifier } +
            FhirEuRedeemAccessCodeResponseConstants.FhirEuRedeemAccessCodeResponseMeta.entries.map { it.identifier }

    val MEDICATION_REQUEST_PROFILE_URLS: List<String> =
        FhirMedicationRequestConstants.MedicationRequestVersion.all
}
