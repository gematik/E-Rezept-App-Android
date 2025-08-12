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

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.data.bundle_speciality_complex
import de.gematik.ti.erp.app.data.bundle_speciality_simple
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdSpecialty.Companion.getSpecialtyTypes
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Test
import kotlin.test.assertEquals

class FhirVzdSpecialtyTest {

    @Test
    fun `parse simple speciality bundle`() = runTest {
        val bundle = Json.parseToJsonElement(bundle_speciality_simple)
        val specialties = SafeJson.value.decodeFromJsonElement<List<FhirVzdSpecialty>>(bundle)
        val types = specialties.getSpecialtyTypes()
        val expected = listOf(
            FhirVzdSpecialtyType.Pickup, // "10"
            FhirVzdSpecialtyType.Delivery, // "30"
            FhirVzdSpecialtyType.Shipment, // "40"
            FhirVzdSpecialtyType.Others // "koerperwerte"
        )
        assertEquals(expected.sortedBy { it.name }, types.sortedBy { it.name })
    }

    @Test
    fun `parse complex speciality bundle`() = runTest {
        val bundle = Json.parseToJsonElement(bundle_speciality_complex)
        val specialties = SafeJson.value.decodeFromJsonElement<List<FhirVzdSpecialty>>(bundle)
        val types = specialties.getSpecialtyTypes()
        val expected = listOf(
            FhirVzdSpecialtyType.Pickup, // "10"
            FhirVzdSpecialtyType.Delivery, // "30"
            FhirVzdSpecialtyType.Shipment, // "40"
            FhirVzdSpecialtyType.Others // "koerperwerte"
        )
        assertEquals(expected.sortedBy { it.name }, types.sortedBy { it.name })
    }
}
