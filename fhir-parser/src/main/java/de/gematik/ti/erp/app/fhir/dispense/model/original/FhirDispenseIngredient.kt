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

package de.gematik.ti.erp.app.fhir.dispense.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtensionReduced.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationIngredient.Companion.MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationIngredient.Companion.MEDICATION_INGREDIENT_FORM_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.serializer.MedicationReferenceSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class FhirDispenseIngredient

@Serializable
@SerialName("itemReference")
internal data class FhirReferenceIngredient(
    @SerialName("itemReference")
    @Serializable(with = MedicationReferenceSerializer::class)
    val itemReference: FhirMedicationDispenseReference?
) : FhirDispenseIngredient()

@Serializable
@SerialName("itemCodeableConcept")
internal data class FhirCodeableIngredient(
    val itemCodeableConcept: FhirCodeableConcept?,
    @SerialName("strength") val strength: FhirRatio? = null
) : FhirDispenseIngredient() {
    val text = itemCodeableConcept?.text
    val amount = strength?.extensions?.findExtensionByUrl(MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL)?.valueString
    val form = strength?.extensions?.findExtensionByUrl(MEDICATION_INGREDIENT_FORM_EXTENSION_URL)?.valueString
}
