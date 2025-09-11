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

package de.gematik.ti.erp.app.fhir.constant.prescription.organization

object FhirOrganizationConstants {
    /**
     * Identifier for organizations using Betriebsstättennummer (BSNR).
     */
    const val ORGANIZATION_IDENTIFIER_BSNR_NAME = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"

    /**
     * Identifier for organizations using Institutionskennzeichen (IKNR).
     */
    const val ORGANIZATION_IDENTIFIER_IKNR_NAME = "http://fhir.de/sid/arge-ik/iknr"

    const val ORGANIZATION_IDENTIFIER_TELEMATIK_ID = "https://gematik.de/fhir/sid/telematik-id"

    const val TELECOM_PHONE = "phone"
    const val TELECOM_EMAIL = "email"
    const val TELECOM_FAX = "fax"
}
