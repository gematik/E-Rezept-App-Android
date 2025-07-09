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

import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedBoolean
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

fun <Medication, Ingredient, Ratio, Quantity> extractEpaMedications(
    resource: JsonElement,
    quantityFn: QuantityFn<Quantity>,
    ratioFn: RatioFn<Ratio, Quantity>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>
): Medication {
    val containedMedications =
        resource.findAll("contained")
            .filterWith(
                "resourceType",
                stringValue("Medication")
            )
            .map {
                extractContainedMedication(it, quantityFn, ratioFn, ingredientFn, processMedication)
            }.toList()

    val text = resource.containedOrNull("code")?.containedStringOrNull("text")
        ?: resource.containedOrNull("code")?.contained("coding")?.containedStringOrNull("code")
    val medicationCategory = extractMedicationCategoryEpa(resource)
    val form = resource.containedOrNull("form")
        ?.findAll("coding")
        ?.filterWith(
            "system",
            stringValue(
                "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"
            )
        )
        ?.firstOrNull()
        ?.containedString("code")

    val amount = resource.containedOrNull("amount")?.extractRatio(ratioFn, quantityFn)
    val vaccine = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://gematik.de/fhir/epa-medication/StructureDefinition/medication-id-vaccine-extension")
        )
        .firstOrNull()
        ?.containedBoolean("valueBoolean") ?: false

    val normSizeCode = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("http://fhir.de/StructureDefinition/normgroesse")
        )
        .firstOrNull()
        ?.containedStringOrNull("valueCode")

    val identifier = parseIdentifier(resource)

    val manufacturingInstructions = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue(
                "https://gematik.de/fhir/epa-medication/StructureDefinition/" +
                    "medication-manufacturing-instructions-extension"
            )
        )
        .firstOrNull()
        ?.containedString("valueString")

    val packaging = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue(
                "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"
            )
        )
        .firstOrNull()
        ?.containedString("valueString")

    val ingredients = resource.findAll("ingredient").filter {
        it.containedOrNull("itemReference")?.containedStringOrNull("reference").isNullOrEmpty()
    }.map {
        it.extractEpaIngredient(ingredientFn, ratioFn, quantityFn)
    }.toList()

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.toFhirTemporal()

    return processMedication(
        text,
        medicationCategory,
        form,
        amount,
        vaccine,
        manufacturingInstructions,
        packaging,
        normSizeCode,
        identifier,
        containedMedications,
        ingredients,
        lotNumber,
        expirationDate
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractContainedMedication(
    resource: JsonElement,
    quantityFn: QuantityFn<Quantity>,
    ratioFn: RatioFn<Ratio, Quantity>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    processIngredientMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationCategory = extractMedicationCategoryEpa(resource)
    val form = resource.containedOrNull("form")
        ?.findAll("coding")
        ?.filterWith(
            "system",
            stringValue(
                "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"
            )
        )
        ?.firstOrNull()
        ?.containedString("code")

    val amount = resource.containedOrNull("amount")?.extractRatio(ratioFn, quantityFn)
    val vaccine = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://gematik.de/fhir/epa-medication/StructureDefinition/medication-id-vaccine-extension")
        )
        .firstOrNull()
        ?.containedBoolean("valueBoolean") ?: false

    val normSizeCode = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("http://fhir.de/StructureDefinition/normgroesse")
        )
        .firstOrNull()
        ?.containedStringOrNull("valueCode")

    val identifier = parseIdentifier(resource)

    val manufacturingInstructions = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue(
                "https://gematik.de/fhir/epa-medication/StructureDefinition/" +
                    "medication-manufacturing-instructions-extension"
            )
        )
        .firstOrNull()
        ?.containedString("valueString")

    val packaging = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue(
                "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"
            )
        )
        .firstOrNull()
        ?.containedString("valueString")

    val ingredients = resource.findAll("ingredient").filter {
        it.containedOrNull("itemReference")?.containedStringOrNull("reference").isNullOrEmpty()
    }.map {
        it.extractEpaIngredient(ingredientFn, ratioFn, quantityFn)
    }.toList()

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.toFhirTemporal()

    return processIngredientMedication(
        text,
        medicationCategory,
        form,
        amount,
        vaccine,
        manufacturingInstructions,
        packaging,
        normSizeCode,
        identifier,
        emptyList(),
        ingredients,
        lotNumber,
        expirationDate
    )
}

fun <Ingredient, Ratio, Quantity> JsonElement.extractEpaIngredient(
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Ingredient {
    val text = this.containedOrNull("itemCodeableConcept")?.contained("coding")?.containedStringOrNull("display") ?: ""
    val strength = this.contained("strength")
    val amount = strength.findAll("extension").filterWith(
        "url",
        stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount")
    ).firstOrNull()
        ?.containedStringOrNull("valueString")
    val form = this.findAll("extension").filterWith(
        "url",
        stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form")
    ).firstOrNull()
        ?.containedStringOrNull("valueString")

    val identifier = parseIdentifier(this)

    return ingredientFn(
        text,
        form,
        identifier,
        amount,
        strength.extractRatio(ratioFn, quantityFn)
    )
}

fun parseIdentifier(resource: JsonElement): Identifier {
    val pzn = extractCode(resource, "http://fhir.de/CodeSystem/ifa/pzn")
    val atc = extractCode(resource, "http://fhir.de/CodeSystem/bfarm/atc")
    val ask = extractCode(resource, "http://fhir.de/CodeSystem/ask")
    val snomed = extractCode(resource, "http://snomed.info/sct")

    return Identifier(pzn = pzn, atc = atc, ask = ask, snomed = snomed)
}

fun extractCode(resource: JsonElement, systemUrl: String): String? {
    return resource.containedOrNull("code")?.findAll("coding")
        ?.filterWith("system", stringValue(systemUrl))
        ?.firstOrNull()?.containedString("code")
        ?: resource.containedOrNull("itemCodeableConcept")?.findAll("coding")
            ?.filterWith("system", stringValue(systemUrl))
            ?.firstOrNull()?.containedString("code")
}

fun extractMedicationCategoryEpa(resource: JsonElement): MedicationCategory {
    val medicationCategoryCode = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://gematik.de/fhir/epa-medication/StructureDefinition/drug-category-extension")
        )
        .firstOrNull()
        ?.contained("valueCoding")
        ?.containedStringOrNull("code")

    return when (medicationCategoryCode) {
        "00" -> MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        "01" -> MedicationCategory.BTM
        "02" -> MedicationCategory.AMVV
        else -> MedicationCategory.UNKNOWN
    }
}
