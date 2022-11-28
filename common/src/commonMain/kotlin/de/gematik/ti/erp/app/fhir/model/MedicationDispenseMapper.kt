/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.asLocalDate
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedBooleanOrNull
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

typealias MedicationDispenseFn<R, Medication> = (
    dispenseId: String,
    patientIdentifier: String, // KVNR
    medication: Medication?,
    wasSubstituted: Boolean,
    dosageInstruction: String?,
    performer: String, // Telematik-ID
    whenHandedOver: LocalDate
) -> R

fun <MedicationDispense, Medication, Ingredient, Ratio, Quantity> extractMedicationDispense(
    resource: JsonElement,
    processMedicationDispense: MedicationDispenseFn<MedicationDispense, Medication>,
    processMedication: MedicationFn<Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MedicationDispense {
    val dispenseId = resource.containedString("id")
    val patientIdentifier = resource.contained("subject").contained("identifier").containedString("value")
    val medication = extractMedication(
        resource.containedArray("contained")[0],
        processMedication,
        ingredientFn,
        ratioFn,
        quantityFn
    )
    val wasSubstituted = resource.containedOrNull("substitution")
        ?.containedBooleanOrNull("wasSubstituted") ?: false
    val dosageInstruction = resource.containedOrNull("dosageInstruction")?.containedStringOrNull("text")
    val performer = resource.containedArray("performer")[0]
        .contained("actor").contained("identifier").containedString("value") // Telematik-ID
    val whenHandedOver = resource.contained("whenHandedOver").jsonPrimitive.asLocalDate()
        ?: error("error on parsing date of delivery")

    return processMedicationDispense(
        dispenseId,
        patientIdentifier,
        medication,
        wasSubstituted,
        dosageInstruction,
        performer,
        whenHandedOver
    )
}
