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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedBoolean
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.utils.asFhirLocalDate
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Suppress("LongParameterList", "LongMethod")
fun <Organization, Patient, Practitioner, InsuranceInformation, MedicationRequest,
    Medication, Ingredient, MultiplePrescriptionInfo, Quantity, Ratio, Address> extractKBVBundleVersion102(
    bundle: JsonElement,
    processOrganization: OrganizationFn<Organization, Address>,
    processPatient: PatientFn<Patient, Address>,
    processPractitioner: PractitionerFn<Practitioner>,
    processInsuranceInformation: InsuranceInformationFn<InsuranceInformation>,
    processAddress: AddressFn<Address>,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    processIngredient: IngredientFn<Ingredient, Ratio>,
    processRatio: RatioFn<Ratio, Quantity>,
    processQuantity: QuantityFn<Quantity>,
    processMultiplePrescriptionInfo: MultiplePrescriptionInfoFn<MultiplePrescriptionInfo, Ratio>,
    processMedicationRequest: MedicationRequestFn<MedicationRequest, MultiplePrescriptionInfo>,

    savePVSIdentifier: (pvsId: String?) -> Unit,

    save: (
        organization: Organization,
        patient: Patient,
        practitioner: Practitioner,
        insuranceInformation: InsuranceInformation,
        medication: Medication,
        medicationRequest: MedicationRequest
    ) -> Unit
) {
    val pvsId = bundle
        .findAll("entry.resource.author.identifier")
        .filterWith("system", stringValue("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer"))
        .firstOrNull()
        ?.containedString("value")

    savePVSIdentifier(pvsId)

    val resources = bundle
        .findAll("entry.resource")

    var organization: Organization? = null
    var patient: Patient? = null
    var practitioner: Practitioner? = null
    var insuranceInformation: InsuranceInformation? = null
    var medication: Medication? = null
    var medicationRequest: MedicationRequest? = null

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization",
                "1.0.3"
            ) -> {
                organization = extractOrganization(
                    resource,
                    processOrganization,
                    processAddress
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient",
                "1.0.3"
            ) -> {
                patient = extractPatient(
                    resource,
                    processPatient,
                    processAddress
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner",
                "1.0.3"
            ) -> {
                practitioner = extractPractitioner(
                    resource,
                    processPractitioner
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage",
                "1.0.3"
            ) -> {
                insuranceInformation = extractInsuranceInformation(
                    resource,
                    processInsuranceInformation
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN",
                "1.0.2"
            ) -> {
                medication = extractPZNMedication(
                    resource,
                    processMedication,
                    processRatio,
                    processQuantity
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding",
                "1.0.2"
            ) -> {
                medication = extractMedicationCompounding(
                    resource,
                    processMedication,
                    processIngredient,
                    processRatio,
                    processQuantity
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient",
                "1.0.2"
            ) -> {
                medication = extractMedicationIngredient(
                    resource,
                    processMedication,
                    processIngredient,
                    processRatio,
                    processQuantity
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText",
                "1.0.2"
            ) -> {
                medication = extractMedicationFreetext(
                    resource,
                    processQuantity,
                    processRatio,
                    processMedication
                )
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription",
                "1.0.2"
            ) -> {
                medicationRequest = extractMedicationRequest(
                    resource,
                    processMedicationRequest,
                    processMultiplePrescriptionInfo,
                    processRatio,
                    processQuantity

                )
            }
        }
    }

    save(
        requireNotNull(organization),
        requireNotNull(patient),
        requireNotNull(practitioner),
        requireNotNull(insuranceInformation),
        requireNotNull(medication),
        requireNotNull(medicationRequest)
    )
}

fun <MedicationRequest, MultiplePrescriptionInfo, Ratio, Quantity> extractMedicationRequest(
    resource: JsonElement,
    processMedicationRequest: MedicationRequestFn<MedicationRequest, MultiplePrescriptionInfo>,
    processMultiplePrescriptionInfo: MultiplePrescriptionInfoFn<MultiplePrescriptionInfo, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MedicationRequest {
    val accidentInformation = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident")
        ).firstOrNull()
    val dateOfAccident = accidentInformation?.findAll("extension")?.filterWith(
        "url",
        stringValue("unfalltag")
    )?.firstOrNull()?.containedOrNull("valueDate")?.jsonPrimitive?.asFhirLocalDate()

    val location = accidentInformation?.findAll("extension")?.filterWith(
        "url",
        stringValue("unfallbetrieb")
    )?.firstOrNull()
        ?.containedString("valueString")

    val accidentTypeCode = accidentInformation?.findAll("extension")?.filterWith(
        "url",
        stringValue("unfallkennzeichen")
    )?.firstOrNull()
        ?.containedOrNull("valueCoding")
        ?.containedStringOrNull("code")

    val accidentType = when (accidentTypeCode) {
        "1" -> AccidentType.Unfall
        "2" -> AccidentType.Arbeitsunfall
        "4" -> AccidentType.Berufskrankheit
        else -> AccidentType.None
    }

    val emergencyFee = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee")
        ).firstOrNull()?.containedBoolean("valueBoolean")

    val substitutionAllowed = resource.contained("substitution").containedBoolean("allowedBoolean")

    val dosageInstruction = resource.containedOrNull("dosageInstruction")?.containedStringOrNull("text")
    val multiplePrescriptionInfo = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription")
        )
        .first()
        .extractMultiplePrescriptionInfo(processMultiplePrescriptionInfo, ratioFn, quantityFn)
    val note = resource.containedOrNull("note")?.containedStringOrNull("text")
    val quantity = resource.contained("dispenseRequest").contained("quantity")
        .containedDouble("value").toInt()
    val bvg = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG")
        ).firstOrNull()?.containedBoolean("valueBoolean") ?: false
    val additionalFee = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment")
        )
        .firstOrNull()
        ?.containedOrNull("valueCoding")
        ?.containedStringOrNull("code")

    return processMedicationRequest(
        null,
        dateOfAccident,
        location,
        accidentType,
        emergencyFee,
        substitutionAllowed,
        dosageInstruction,
        quantity,
        multiplePrescriptionInfo,
        note,
        bvg,
        additionalFee
    )
}

fun <Patient, Address> extractPatient(
    resource: JsonElement,
    processPatient: PatientFn<Patient, Address>,
    processAddress: AddressFn<Address>
): Patient {
    val name = resource.extractHumanName()

    val birthDate = resource.containedOrNull("birthDate")?.jsonPrimitive?.toFhirTemporal()

    val kvnr = resource
        .findAll("identifier")
        .filterWith("system", stringValue("http://fhir.de/NamingSystem/gkv/kvid-10"))
        .firstOrNull()
        ?.containedString("value")

    return processPatient(
        name,
        resource.extractAddress(processAddress),
        birthDate,
        kvnr
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractPZNMedication(
    resource: JsonElement,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationCategory = extractMedicationCategory(resource)

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
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine")
        )
        .first()
        .containedBoolean("valueBoolean")

    val normSizeCode = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("http://fhir.de/StructureDefinition/normgroesse")
        )
        .firstOrNull()
        ?.containedStringOrNull("valueCode")

    val identifier = parseIdentifier(resource)

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.toFhirTemporal()

    return processMedication(
        text,
        medicationCategory,
        form,
        amount,
        vaccine,
        null,
        null,
        normSizeCode,
        identifier,
        emptyList(),
        emptyList(),
        lotNumber,
        expirationDate
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractMedicationCompounding(
    resource: JsonElement,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationCategory = extractMedicationCategory(resource)
    val form = resource.contained("form").containedString("text")
    val amount = resource.containedOrNull("amount")?.extractRatio(ratioFn, quantityFn)
    val vaccine = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine")
        )
        .first()
        .containedBoolean("valueBoolean")
    val manufacturingInstructions = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction")
        )
        .firstOrNull()
        ?.containedString("valueString")

    val packaging = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging")
        )
        .firstOrNull()
        ?.containedString("valueString")

    val ingredients = resource.findAll("ingredient").map {
        it.extractIngredient(ingredientFn, ratioFn, quantityFn)
    }.toList()

    val identifier = parseIdentifier(resource)

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
        null,
        identifier,
        emptyList(),
        ingredients,
        lotNumber,
        expirationDate
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractMedicationFreetext(
    resource: JsonElement,
    quantityFn: QuantityFn<Quantity>, // needed for medication alias
    ratioFn: RatioFn<Ratio, Quantity>, // needed for medication alias
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationCategory = extractMedicationCategory(resource)
    val form = resource.containedOrNull("form")?.containedStringOrNull("text")
    val vaccine = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine")
        )
        .first()
        .containedBoolean("valueBoolean")

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.toFhirTemporal()

    return processMedication(
        text,
        medicationCategory,
        form,
        null,
        vaccine,
        null,
        null,
        null,
        Identifier(),
        emptyList(),
        listOf(),
        lotNumber,
        expirationDate
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractMedicationIngredient(
    resource: JsonElement,
    processMedication: MedicationFn<Medication, Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationCategory = extractMedicationCategory(resource)
    val form = resource.containedOrNull("form")?.containedStringOrNull("text")

    val amount = resource.containedOrNull("amount")?.extractRatio(ratioFn, quantityFn)

    val vaccine = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine")
        )
        .first()
        .containedBoolean("valueBoolean")

    val normSizeCode = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("http://fhir.de/StructureDefinition/normgroesse")
        )
        .firstOrNull()
        ?.containedStringOrNull("valueCode")

    val ingredients = resource.findAll("ingredient").map {
        it.extractIngredient(ingredientFn, ratioFn, quantityFn)
    }.toList()

    val identifier = parseIdentifier(resource)

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.toFhirTemporal()

    return processMedication(
        text,
        medicationCategory,
        form,
        amount,
        vaccine,
        null,
        null,
        normSizeCode,
        identifier,
        emptyList(),
        ingredients,
        lotNumber,
        expirationDate
    )
}
fun extractMedicationCategory(resource: JsonElement): MedicationCategory {
    val medicationCategoryCode = resource
        .findAll("extension")
        .filterWith(
            "valueCoding.system",
            stringValue("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category")
        )
        .first()
        .contained("valueCoding")
        .containedString("code")

    return when (medicationCategoryCode) {
        "00" -> MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        "01" -> MedicationCategory.BTM
        "02" -> MedicationCategory.AMVV
        else -> MedicationCategory.UNKNOWN
    }
}
