/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import kotlinx.serialization.json.JsonElement
import java.time.LocalDate
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
    AMVV,
    UNKNOWN
}

enum class AccidentType {
    Unfall,
    Arbeitsunfall,
    Berufskrankheit,
    None
}

enum class MedicationProfile {
    PZN, COMPOUNDING, INGREDIENT, FREETEXT, UNKNOWN
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
    val profileString = bundle.contained("meta").contained("profile").contained()
    when {
        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
            "1.0.2"
        ) -> extractkbvbundleversion102(
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
        profileString.isProfileValue(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
            "1.1.0"
        ) ->
            extractkbvbundleversion110(
                bundle,
                processOrganization,
                processPatient,
                processPractitioner,
                processInsuranceInformation,
                processAddress,
                processMedication,
                processIngredient,
                processRatio,
                processQuantity, processMultiplePrescriptionInfo,
                processMedicationRequest,
                savePVSIdentifier,
                save
            )
    }
}
