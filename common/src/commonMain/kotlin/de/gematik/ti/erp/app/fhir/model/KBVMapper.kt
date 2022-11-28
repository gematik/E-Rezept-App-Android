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
import de.gematik.ti.erp.app.fhir.parser.asTemporalAccessor
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedBoolean
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

typealias AddressFn<R> = (
    line: List<String>?,
    postalCode: String?,
    city: String?
) -> R

typealias OrganizationFn<R, Address> = (
    name: String?,
    address: Address,
    uniqueIdentifier: String?,
    phone: String?,
    mail: String?
) -> R

typealias PatientFn<R, Address> = (
    name: String?,
    address: Address,
    birthDate: LocalDate?,
    insuranceIdentifier: String?
) -> R

typealias PractitionerFn<R> = (
    name: String?,
    qualification: String?,
    practitionerIdentifier: String?
) -> R

typealias InsuranceInformationFn<R> = (
    name: String?,
    statusCode: String?
) -> R

typealias MedicationRequestFn<R, MultiplePrescriptionInfo> = (
    dateOfAccident: LocalDate?,
    location: String?,
    emergencyFee: Boolean?,
    substitutionAllowed: Boolean,
    dosageInstruction: String?,
    multiplePrescriptionInfo: MultiplePrescriptionInfo?,
    note: String?,
    bvg: Boolean,
    additionalFee: String?
) -> R

typealias MultiplePrescriptionInfoFn<R, Ratio> = (
    indicator: Boolean,
    numbering: Ratio?,
    start: LocalDate?
) -> R

typealias MedicationFn<R, Ingredient, Ratio> = (
    text: String?,
    medicationProfile: MedicationProfile,
    medicationCategory: MedicationCategory,
    form: String?,
    amount: Ratio?,
    vaccine: Boolean,
    manufacturingInstructions: String?,
    packaging: String?,
    normSizeCode: String?,
    uniqueIdentifier: String?,
    ingredients: List<Ingredient>,
    lotNumber: String?,
    expirationDate: TemporalAccessor?
) -> R

typealias IngredientFn<R, Ratio> = (
    text: String,
    form: String?,
    number: String?,
    amount: String?,
    strength: Ratio?
) -> R

typealias RatioFn<R, Quantity> = (
    numerator: Quantity?,
    denominator: Quantity?
) -> R

typealias QuantityFn<R> = (
    value: String,
    unit: String
) -> R

enum class MedicationCategory {
    ARZNEI_UND_VERBAND_MITTEL,
    BTM,
    AMVV
}

enum class MedicationProfile {
    PZN, COMPOUNDING, INGREDIENT, FREETEXT
}

@Suppress("LongParameterList")
fun <Organization, Patient, Practitioner, InsuranceInformation, MedicationRequest,
    Medication, Ingredient, MultiplePrescriptionInfo, Quantity, Ratio, Address> extractKBVBundle(
    bundle: JsonElement,
    processOrganization: OrganizationFn<Organization, Address>,
    processPatient: PatientFn<Patient, Address>,
    processPractitioner: PractitionerFn<Practitioner>,
    processInsuranceInformation: InsuranceInformationFn<InsuranceInformation>,
    processAddress: AddressFn<Address>,
    processMedication: MedicationFn<Medication, Ingredient, Ratio>,
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

            profileString.isProfileValueOfMedication("1.0.2")
            -> {
                medication = extractMedication(
                    resource,
                    processMedication,
                    processIngredient,
                    processRatio,
                    processQuantity
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

fun <Organization, Address> extractOrganization(
    resource: JsonElement,
    processOrganization: OrganizationFn<Organization, Address>,
    processAddress: AddressFn<Address>
): Organization {
    val name = resource.containedStringOrNull("name")

    val telecom = resource.containedArrayOrNull("telecom")

    var phone: String? = null
    var mail: String? = null

    telecom?.forEach {
        when (it.containedString("system")) {
            "phone" -> phone = it.containedStringOrNull("value")
            "email" -> mail = it.containedStringOrNull("value")
        }
    }

    val bsnr = resource
        .findAll("identifier")
        .filterWith("system", stringValue("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"))
        .firstOrNull()
        ?.containedString("value")

    return processOrganization(
        name,
        resource.extractAddress(processAddress),
        bsnr,
        phone,
        mail
    )
}

fun <MedicationRequest, MultiplePrescriptionInfo, Ratio, Quantity> extractMedicationRequest(
    resource: JsonElement,
    processMedicationRequest: MedicationRequestFn<MedicationRequest, MultiplePrescriptionInfo>,
    processMultiplePrescriptionInfo: MultiplePrescriptionInfoFn<MultiplePrescriptionInfo, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MedicationRequest {
    val dateOfAccident = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident")
        ).firstOrNull()?.findAll("extension")?.filterWith(
            "url",
            stringValue("unfalltag")
        )?.firstOrNull()?.containedOrNull("valueDate")?.jsonPrimitive?.asLocalDate()

    val location = resource
        .findAll("extension")
        .filterWith(
            "url",
            stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident")
        ).firstOrNull()?.findAll("extension")?.filterWith(
            "url",
            stringValue("unfallbetrieb")
        )?.firstOrNull()
        ?.containedString("valueString")

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
        ?.contained("valueCoding")
        ?.containedString("code")

    return processMedicationRequest(
        dateOfAccident,
        location,
        emergencyFee,
        substitutionAllowed,
        dosageInstruction,
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

    val birthDate = try {
        resource.containedOrNull("birthDate")?.jsonPrimitive?.asLocalDate()
    } catch (expected: DateTimeParseException) {
        Napier.e("Could not parse birthdate.", expected)
        null
    }

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

fun <Practitioner> extractPractitioner(
    resource: JsonElement,
    processPractitioner: PractitionerFn<Practitioner>
): Practitioner {
    val name = resource.extractHumanName()

    val qualification = resource
        .containedArray("qualification")
        .find { it.containedOrNull("code")?.containedOrNull("text") != null }
        ?.contained("code")?.containedString("text")

    val lanr = resource
        .findAll("identifier")
        .filterWith("system", stringValue("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR"))
        .firstOrNull()
        ?.containedString("value")

    return processPractitioner(
        name,
        qualification,
        lanr
    )
}

fun <InsuranceInformation> extractInsuranceInformation(
    resource: JsonElement,
    processInsuranceInformation: InsuranceInformationFn<InsuranceInformation>
): InsuranceInformation {
    val name = resource.containedOrNull("payor")?.containedStringOrNull("display")
    val statusCode = resource
        .findAll("extension")
        .filterWith(
            "valueCoding.system",
            stringValue("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS")
        )
        .firstOrNull()
        ?.contained("valueCoding")
        ?.containedString("code")

    return processInsuranceInformation(
        name,
        statusCode
    )
}

fun <R> JsonElement.extractAddress(addressFn: AddressFn<R>): R {
    val address = this
        .containedOrNull("address")

    val line = address
        ?.containedArrayOrNull("line")
        ?.map {
            it.containedString()
        }

    val postalCode = address
        ?.containedStringOrNull("postalCode")

    val city = address
        ?.containedStringOrNull("city")

    return addressFn(line, postalCode, city)
}

fun JsonElement.extractHumanName(): String? {
    return this
        .findAll("name")
        .filterWith("use", stringValue("official"))
        .firstOrNull()
        ?.let { name ->
            val family = name.containedString("family")
            val given = name.containedArray("given").joinToString(" ") {
                it.containedString()
            }
            val prefix = name.containedArrayOrNull("prefix")?.joinToString(" ") {
                it.containedString()
            }
            listOfNotNull(prefix, given, family).joinToString(" ")
        }
}

fun <MultiplePrescriptionInfo, Ratio, Quantity> JsonElement.extractMultiplePrescriptionInfo(
    processMultiplePrescriptionInfo: MultiplePrescriptionInfoFn<MultiplePrescriptionInfo, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): MultiplePrescriptionInfo {
    val indicator = this.findAll("extension").filterWith("url", stringValue("Kennzeichen"))
        .first().containedBoolean("valueBoolean")
    val numbering = this.findAll("extension").filterWith("url", stringValue("Nummerierung"))
        .firstOrNull()
        ?.contained("valueRatio")
        ?.extractRatio(ratioFn, quantityFn)
    val start = this.findAll("extension").filterWith("url", stringValue("Zeitraum"))
        .firstOrNull()
        ?.contained("valuePeriod")
        ?.containedOrNull("start")?.jsonPrimitive?.asLocalDate()

    return processMultiplePrescriptionInfo(
        indicator,
        numbering,
        start
    )
}

fun <Medication, Ingredient, Ratio, Quantity> extractMedication(
    resource: JsonElement,
    processMedication: MedicationFn<Medication, Ingredient, Ratio>,
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Medication {
    val text = resource.contained("code").containedStringOrNull("text")
    val medicationProfile = when (
        resource.contained("meta").containedArray("profile")[0]
            .containedString().split("|").first()
    ) {
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN" -> MedicationProfile.PZN
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding" -> MedicationProfile.COMPOUNDING
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient" -> MedicationProfile.INGREDIENT
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText" -> MedicationProfile.FREETEXT
        else -> error("empty medication profile")
    }
    val medicationCategoryCode = resource
        .findAll("extension")
        .filterWith(
            "valueCoding.system",
            stringValue("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category")
        )
        .first()
        .contained("valueCoding")
        .containedString("code")

    val medicationCategory = when (medicationCategoryCode) {
        "00" -> MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        "01" -> MedicationCategory.BTM
        "02" -> MedicationCategory.AMVV
        else -> error("unknown medication category")
    }
    val form = resource.containedOrNull("form")
        ?.findAll("coding")
        ?.filterWith(
            "system",
            stringValue(
                "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"
            )
        )
        ?.firstOrNull()
        ?.containedString("code") ?: resource.containedOrNull("form")?.containedStringOrNull("text")

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

    val normSizeCode = resource.findAll("extension")
        .filterWith(
            "url",
            stringValue("http://fhir.de/StructureDefinition/normgroesse")
        )
        .firstOrNull()
        ?.containedString("valueCode")

    val uniqueIdentifier =
        resource.contained("code").findAll("coding")
            .filterWith("system", stringValue("http://fhir.de/CodeSystem/ifa/pzn"))
            .firstOrNull()?.containedString("code")

    val ingredients = resource.findAll("ingredient").map {
        it.extractIngredient(ingredientFn, ratioFn, quantityFn)
    }.toList()

    val lotNumber = resource.containedOrNull("batch")?.containedStringOrNull("lotNumber")
    val expirationDate = resource.containedOrNull("batch")
        ?.containedOrNull("expirationDate")?.jsonPrimitive?.asTemporalAccessor()

    return processMedication(
        text,
        medicationProfile,
        medicationCategory,
        form,
        amount,
        vaccine,
        manufacturingInstructions,
        packaging,
        normSizeCode,
        uniqueIdentifier,
        ingredients,
        lotNumber,
        expirationDate
    )
}

fun <Ingredient, Ratio, Quantity> JsonElement.extractIngredient(
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Ingredient {
    val text = this.contained("itemCodeableConcept").containedString("text")
    val strength = this.contained("strength")
    // FIXME
    val amount = strength.findAll("extension").filterWith(
        "url",
        stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount")
    ).firstOrNull()
        ?.containedStringOrNull()
    // FIXME
    val form = this.findAll("extension").filterWith(
        "url",
        stringValue("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form")
    ).firstOrNull()
        ?.containedStringOrNull()

    val number = this.contained("itemCodeableConcept").containedOrNull("coding")?.containedString("code")

    return ingredientFn(
        text,
        form,
        number,
        amount,
        strength.extractRatio(ratioFn, quantityFn)
    )
}

fun <Ratio, Quantity> JsonElement.extractRatio(
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Ratio {
    val numerator = this.containedOrNull("numerator")
    val denominator = this.containedOrNull("denominator")

    return ratioFn(
        numerator?.extractQuantity(quantityFn),
        denominator?.extractQuantity(quantityFn)
    )
}

fun <Quantity> JsonElement.extractQuantity(quantityFn: QuantityFn<Quantity>): Quantity {
    val value = this.containedStringOrNull("value") ?: ""
    val unit = this.containedStringOrNull("unit") ?: ""

    return quantityFn(value, unit)
}

private fun JsonElement.isProfileValueOfMedication(vararg versions: String): Boolean {
    return isProfileValue(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN",
        *versions
    ) || isProfileValue(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding",
        *versions
    ) || isProfileValue(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient",
        *versions
    ) || isProfileValue(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText",
        *versions
    )
}
