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

package de.gematik.ti.erp.app.fhir.constant.prescription.euredeem

import de.gematik.ti.erp.app.utils.Reference

@Reference(
    info = "Link to GEM ERPEU PR PAR Access Authorization Request version 1.0.0",
    url = "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_Access_Authorization_Request"
)
object FhirEuRedeemAccessCodeRequestConstants {
    const val RESOURCE_TYPE = "Parameters"
    const val ID = "erp-eprescription-03-POST-AccessCode-EU-Request"
    const val PROFILE_URL = "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_Access_Authorization_Request|1.0"

    object CountryCodeParameter {
        const val NAME = "countryCode"
        const val SYSTEM = "urn:iso:std:iso:3166"
    }

    object AccessCodeParameter {
        const val NAME = "accessCode"
        const val SYSTEM = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_EU_AccessCode"
    }
}
