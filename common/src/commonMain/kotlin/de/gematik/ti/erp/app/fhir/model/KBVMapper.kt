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

import de.gematik.ti.erp.app.db.entities.v1.task.IdentifierEntityV1
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

typealias AddressFn<R> = (
    line: List<String>?,
    postalCode: String?,
    city: String?
) -> R

typealias OrganizationFn<R, Address> = (
    name: String?,
    address: Address,
    bsnr: String?,
    iknr: String?,
    phone: String?,
    mail: String?
) -> R

typealias PatientFn<R, Address> = (
    name: String?,
    address: Address,
    birthDate: FhirTemporal?,
    insuranceIdentifier: String?
) -> R

typealias PractitionerFn<R> = (
    name: String?,
    qualification: String?,
    practitionerIdentifier: String?
) -> R

typealias InsuranceInformationFn<R> = (
    name: String?,
    statusCode: String?,
    typeCode: String
) -> R

typealias MedicationRequestFn<R, MultiplePrescriptionInfo> = (
    authoredOn: FhirTemporal.LocalDate?,
    dateOfAccident: FhirTemporal.LocalDate?,
    location: String?,
    accidentType: AccidentType,
    emergencyFee: Boolean?,
    substitutionAllowed: Boolean,
    dosageInstruction: String?,
    quantity: Int,
    multiplePrescriptionInfo: MultiplePrescriptionInfo?,
    note: String?,
    bvg: Boolean,
    additionalFee: String?
) -> R

typealias MultiplePrescriptionInfoFn<R, Ratio> = (
    indicator: Boolean,
    numbering: Ratio?,
    start: FhirTemporal?,
    end: FhirTemporal?
) -> R

typealias MedicationFn<R, Medication, Ingredient, Ratio> = (
    text: String?,
    medicationCategory: MedicationCategory,
    form: String?,
    amount: Ratio?,
    vaccine: Boolean,
    manufacturingInstructions: String?,
    packaging: String?,
    normSizeCode: String?,
    uniqueIdentifier: Identifier,
    ingredientMedications: List<Medication>,
    ingredients: List<Ingredient>,
    lotNumber: String?,
    expirationDate: FhirTemporal?
) -> R

typealias IngredientFn<R, Ratio> = (
    text: String,
    form: String?,
    identifier: Identifier,
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
    AMVV,
    SONSTIGES,
    UNKNOWN
}

enum class AccidentType {
    Unfall,
    Arbeitsunfall,
    Berufskrankheit,
    None
}

enum class MedicationProfile {
    PZN, COMPOUNDING, INGREDIENT, FREETEXT, UNKNOWN, EPA
}

data class Identifier(
    val pzn: String? = null,
    val atc: String? = null,
    val ask: String? = null,
    val snomed: String? = null
) {
    fun toIdentifierEntityV1(): IdentifierEntityV1 {
        return IdentifierEntityV1().apply {
            pzn = this@Identifier.pzn
            atc = this@Identifier.atc
            ask = this@Identifier.ask
            snomed = this@Identifier.snomed
        }
    }
}

fun cleanJsonFile(jsonElement: JsonElement): String {
    // Use a temporary directory (app-specific, no context required)
    val outputDir = File(System.getProperty("java.io.tmpdir"), "cleaned_json")
    if (!outputDir.exists()) outputDir.mkdirs() // ✅ Ensure directory exists

    // Generate a timestamp-based unique file name
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val outputFile = File(outputDir, "kbv_cleaned_$timestamp.json") // ✅ Unique file name

    // Convert JsonElement to formatted JSON
    val cleanedJson = SafeJson.value.encodeToString(JsonElement.serializer(), jsonElement)

    // Write the cleaned JSON to the file
    outputFile.writeText(cleanedJson)

    println("✅ Cleaned JSON saved to: ${outputFile.absolutePath}")

    return outputFile.absolutePath // ✅ Return file path for later use
}

@Deprecated(
    "Use de.gematik.ti.erp.app.fhir.prescription.parser.TaskKBVParser.kt",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.fhir.prescription.parser.TaskKBVParser.kt"),
    level = DeprecationLevel.WARNING
)
@Suppress("LongParameterList")
fun <Organization, Patient, Practitioner, InsuranceInformation, MedicationRequest,
    Medication, Ingredient, MultiplePrescriptionInfo, Quantity, Ratio, Address> extractKBVBundle(
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
    /* try {
        Napier.e { "Extracting KBV Bundle" }
        Napier.e { "$bundle" }
        cleanJsonFile(bundle)
    } catch (e: Exception) {
        Napier.e {
            "Error parsing KBV Bundle: ${e.message}"
        }
    } */

    val profileString = bundle.contained("meta").contained("profile").contained()
    when {
        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
            "1.0.2"
        ) -> extractKBVBundleVersion102(
            bundle = bundle,
            processOrganization = processOrganization,
            processPatient = processPatient,
            processPractitioner = processPractitioner,
            processInsuranceInformation = processInsuranceInformation,
            processAddress = processAddress,
            processMedication = processMedication,
            processIngredient = processIngredient,
            processRatio = processRatio,
            processQuantity = processQuantity,
            processMultiplePrescriptionInfo = processMultiplePrescriptionInfo,
            processMedicationRequest = processMedicationRequest,
            savePVSIdentifier = savePVSIdentifier,
            save = save
        )

        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
            "1.1.0"
        ) ->
            extractKBVBundleVersion110(
                bundle,
                processOrganization,
                processPatient,
                processPractitioner,
                processInsuranceInformation,
                processAddress,
                processMedication,
                processIngredient,
                processRatio,
                processQuantity,
                processMultiplePrescriptionInfo,
                processMedicationRequest,
                savePVSIdentifier,
                save
            )
    }
}
