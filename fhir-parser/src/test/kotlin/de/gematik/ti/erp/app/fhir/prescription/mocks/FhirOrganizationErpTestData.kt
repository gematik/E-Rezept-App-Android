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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel

object FhirOrganizationErpTestData {
    val erpOrganization1_v103 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = FhirTaskKbvAddressErpModel(
            streetName = "Herbert-Lewin-Platz",
            houseNumber = "2",
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "721111100",
        iknr = null,
        phone = "0301234567",
        mail = "mvz@e-mail.de"
    )

    val erpOrganization2_v103 = FhirTaskOrganizationErpModel(
        name = "Hausarztpraxis Dr. Topp-Glücklich",
        address = FhirTaskKbvAddressErpModel(
            streetName = "Musterstr.",
            houseNumber = "2",
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "031234567",
        iknr = null,
        phone = "0301234567",
        mail = null
    )

    val erpOrganization1AllPresent_v110 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = FhirTaskKbvAddressErpModel(
            streetName = "Herbert-Lewin-Platz",
            houseNumber = "2",
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "721111100",
        iknr = null,
        phone = "0301234567",
        mail = "mvz@e-mail.de"
    )

    val erpOrganization1NoAddress_v110 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = null,
        bsnr = "721111100",
        iknr = null,
        phone = "0301234567",
        mail = "mvz@e-mail.de"
    )

    val erpOrganization1NoEmail_v110 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = FhirTaskKbvAddressErpModel(
            streetName = null,
            houseNumber = null,
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "721111100",
        iknr = null,
        phone = "0301234567",
        mail = null
    )

    val erpOrganization1NoFax_v110 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = FhirTaskKbvAddressErpModel(
            streetName = null,
            houseNumber = null,
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "721111100",
        iknr = null,
        phone = "0301234567",
        mail = "mvz@e-mail.de"
    )

    val erpOrganization1NoContact_v110 = FhirTaskOrganizationErpModel(
        name = "MVZ",
        address = FhirTaskKbvAddressErpModel(
            streetName = null,
            houseNumber = null,
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "721111100",
        iknr = null,
        phone = null,
        mail = null
    )

    val erpOrganization2_v110 = FhirTaskOrganizationErpModel(
        name = "Hausarztpraxis Dr. Topp-Glücklich",
        address = FhirTaskKbvAddressErpModel(
            streetName = "Musterstr.",
            houseNumber = "2",
            postalCode = "10623",
            city = "Berlin"
        ),
        bsnr = "031234567",
        iknr = null,
        phone = "0301234567",
        mail = null
    )
}
