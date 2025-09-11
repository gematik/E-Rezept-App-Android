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

package de.gematik.ti.erp.app.fhir.constant.prescription.practitioner

object FhirPractitionerConstants {

    /**
     * Naming system for identifying practitioners using the Arztnummer (ANR).
     */
    const val PRACTITIONER_IDENTIFIER_LANR = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR"

    /**
     * Naming system for identifying practitioners using the ZahnArztnummer (ZANR).
     */
    const val PRACTITIONER_IDENTIFIER_ZANR = "http://fhir.de/sid/kzbv/zahnarztnummer"

    /**
     * Naming system for identifying practitioner's office.
     */
    const val PRACTITIONER_TELEMATIK_ID = "https://gematik.de/fhir/sid/telematik-id"
}
