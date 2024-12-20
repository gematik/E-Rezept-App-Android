/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
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

fun <Organization, Address> extractOrganization(
    resource: JsonElement,
    processOrganization: OrganizationFn<Organization, Address>,
    processAddress: AddressFn<Address>
): Organization? {
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

    val iknr = resource
        .findAll("identifier")
        .filterWith("system", stringValue("http://fhir.de/sid/arge-ik/iknr"))
        .firstOrNull()
        ?.containedString("value")

    return processOrganization(
        name,
        resource.extractAddress(processAddress),
        bsnr,
        iknr,
        phone,
        mail
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

    val coverageType = resource.contained("type").contained("coding").containedString("code")

    return processInsuranceInformation(
        name,
        statusCode,
        coverageType
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

    val validityPeriod = this.findAll("extension").filterWith("url", stringValue("Zeitraum"))
        .firstOrNull()
        ?.contained("valuePeriod")

    val start = validityPeriod
        ?.containedOrNull("start")?.jsonPrimitive?.toFhirTemporal()

    val end = validityPeriod
        ?.containedOrNull("end")?.jsonPrimitive?.toFhirTemporal()

    return processMultiplePrescriptionInfo(
        indicator,
        numbering,
        start,
        end
    )
}
fun <Ingredient, Ratio, Quantity> JsonElement.extractIngredient(
    ingredientFn: IngredientFn<Ingredient, Ratio>,
    ratioFn: RatioFn<Ratio, Quantity>,
    quantityFn: QuantityFn<Quantity>
): Ingredient {
    val text = this.contained("itemCodeableConcept").containedString("text")
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
