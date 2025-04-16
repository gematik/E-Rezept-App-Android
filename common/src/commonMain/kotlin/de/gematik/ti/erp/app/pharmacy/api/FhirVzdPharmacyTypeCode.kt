/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.pharmacy.api

// https://simplifier.net/packages/de.gematik.fhir.directory/0.12.0/files/2744864
internal object FhirVzdPharmacyTypeCode {
    const val publicPharmacy = "1.2.276.0.76.4.54"
    const val hospitalPharmacy = "1.2.276.0.76.4.55"
    const val federalPharmacy = "1.2.276.0.76.4.56"
    const val digaProvider = "1.2.276.0.76.4.282"
    const val hospital = "1.2.276.0.76.4.53"
    const val dentalPractice = "1.2.276.0.76.4.51"
    const val doctorOffice = "1.2.276.0.76.4.50"
}
