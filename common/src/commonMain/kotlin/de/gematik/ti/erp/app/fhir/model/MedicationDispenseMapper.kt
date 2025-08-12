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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedBooleanOrNull
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.toFhirTemporal
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Requirement(
    "O.Source_2#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Sanitization is also done for all FHIR mapping."
)
typealias MedicationDispenseFn<R, Medication> = (
    dispenseId: String,
    patientIdentifier: String, // KVNR
    medication: Medication?,
    wasSubstituted: Boolean,
    dosageInstruction: String?,
    performer: String, // Telematik-ID
    whenHandedOver: FhirTemporal
) -> R

fun <MedicationDispense, Medication, Ingredient, Ratio, Quantity> extractMedicationDispense(
    resource: JsonElement,
    processMedicationDispense: MedicationDispenseFn<MedicationDispense, Medication>,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MedicationDispense {
    val dispenseId = resource.containedString("id")
    val patientIdentifier = resource.contained("subject").contained("identifier").containedString("value")
    val medication = extractDispenseMedication(
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
    val whenHandedOver = resource.contained("whenHandedOver").jsonPrimitive.toFhirTemporal()
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

fun <MedicationDispense, Medication, Ingredient, Ratio, Quantity> extractMedicationDispenseWithMedication(
    medicationDispense: JsonElement,
    medication: JsonElement,
    processMedicationDispense: MedicationDispenseFn<MedicationDispense, Medication>,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MedicationDispense {
    val dispenseId = medicationDispense.containedString("id")
    val patientIdentifier = medicationDispense.contained("subject").contained("identifier").containedString("value")
    val dispenseMedication = extractDispenseMedication(
        medication,
        processMedication,
        ingredientFn,
        ratioFn,
        quantityFn
    )

    val wasSubstituted = medicationDispense.containedOrNull("substitution")
        ?.containedBooleanOrNull("wasSubstituted") ?: false
    val dosageInstruction = medicationDispense.containedOrNull("dosageInstruction")?.containedStringOrNull("text")
    val performer = medicationDispense.containedArray("performer")[0]
        .contained("actor").contained("identifier").containedString("value") // Telematik-ID
    val whenHandedOver = medicationDispense.contained("whenHandedOver").jsonPrimitive.toFhirTemporal()
        ?: error("error on parsing date of delivery")

    return processMedicationDispense(
        dispenseId,
        patientIdentifier,
        dispenseMedication,
        wasSubstituted,
        dosageInstruction,
        performer,
        whenHandedOver
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractDispenseMedication(
    resource: JsonElement,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Medication {
    val profileString = resource
        .contained("meta")
        .contained("profile")
        .contained()

    return when {
        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN",
            "1.0.2"
        ) -> extractPZNMedication(resource, processMedication, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN",
            "1.1.0"
        ) -> extractPZNMedicationVersion110(resource, processMedication, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding",
            "1.0.2"
        ) -> extractMedicationCompounding(resource, processMedication, ingredientFn, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding",
            "1.1.0"
        ) -> extractMedicationCompoundingVersion110(resource, processMedication, ingredientFn, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient",
            "1.0.2"
        ) -> extractMedicationIngredient(resource, processMedication, ingredientFn, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient",
            "1.1.0"
        ) -> extractMedicationIngredientVersion110(resource, processMedication, ingredientFn, ratioFn, quantityFn)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText",
            "1.0.2"
        ) -> extractMedicationFreetext(resource, quantityFn, ratioFn, processMedication)

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText",
            "1.1.0"
        ) -> extractMedicationFreetextVersion110(resource, quantityFn, ratioFn, processMedication)

        profileString.isProfileValue(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Medication",
            "1.4"
        ) -> extractEpaMedications(resource, quantityFn, ratioFn, ingredientFn, processMedication)

        profileString.isProfileValue(
            "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pharmaceutical-product"
        ) -> extractEpaMedications(resource, quantityFn, ratioFn, ingredientFn, processMedication)

        else ->
            processMedication(
                "",
                MedicationCategory.UNKNOWN,
                null,
                null,
                false,
                null,
                null,
                null,
                Identifier(),
                listOf(),
                listOf(),
                null,
                null
            )
    }
}

fun extractMedicationDispensePairs(bundle: JsonElement): List<Pair<JsonElement, JsonElement>> {
    val resources = bundle.findAll("entry.resource").toList()
    val medicationDispenses = resources.filter {
        it.containedString("resourceType") == "MedicationDispense"
    }
    val medications = resources.filter {
        it.containedString("resourceType") == "Medication"
    }

    return medicationDispenses.mapNotNull { dispense ->
        val medicationReference = dispense
            .contained("medicationReference")
            .containedString("reference")
            .substringAfter("urn:uuid:")
        val medication = medications.find {
            it.contained("id").containedString() == medicationReference
        }
        if (medication != null) {
            Pair(dispense, medication)
        } else {
            null
        }
    }
}
