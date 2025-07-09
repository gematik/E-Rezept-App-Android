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

import java.io.File

const val ResourceBasePath = "src/commonTest/resources/"

val taskJson_vers_1_2 by lazy {
    File("$ResourceBasePath/fhir/task_vers_1_2.json").readText()
}
val taskJson_vers_1_3 by lazy {
    File("$ResourceBasePath/fhir/task_vers_1_3.json").readText()
}

val organizationJson by lazy {
    File("$ResourceBasePath/fhir/organization.json").readText()
}

val patientJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/patient.json").readText()
}

val patientJson_vers_1_0_2_with_incomplete_birthDate by lazy {
    File("$ResourceBasePath/fhir/patient_incomplete_birth_date.json").readText()
}

val patientJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/patient_vers_1_1_0.json").readText()
}

val practitionerJson by lazy {
    File("$ResourceBasePath/fhir/practitioner.json").readText()
}

val insuranceInformationJson by lazy {
    File("$ResourceBasePath/fhir/insurance_information.json").readText()
}

val quantityJson by lazy {
    File("$ResourceBasePath/fhir/quantity.json").readText()
}

val ratioJson by lazy {
    File("$ResourceBasePath/fhir/ratio.json").readText()
}

val ingredientJson by lazy {
    File("$ResourceBasePath/fhir/ingredient.json").readText()
}

val ingredientWithAmountJson by lazy {
    File("$ResourceBasePath/fhir/ingredient_amount.json").readText()
}

val multiPrescriptionInfoJson by lazy {
    File("$ResourceBasePath/fhir/multi_prescription_info.json").readText()
}

val medicationPznJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/medication_pzn.json").readText()
}
val medicationPznJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/medication_pzn_vers_1_1_0.json").readText()
}

val medicationIngredientJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/medication_ingredient.json").readText()
}
val medicationIngredientJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/medication_ingredient_vers_1_1_0.json").readText()
}

val medicationCompoundingJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/medication_compounding.json").readText()
}
val medicationCompoundingJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/medication_compounding_vers_1_1_0.json").readText()
}

val medicationFreetextJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/medication_freetext.json").readText()
}
val medicationFreetextJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/medication_freetext_vers_1_1_0.json").readText()
}

val medicationRequestJson_vers_1_0_2 by lazy {
    File("$ResourceBasePath/fhir/medication_request.json").readText()
}
val medicationRequestJson_vers_1_1_0 by lazy {
    File("$ResourceBasePath/fhir/medication_request_vers_1_1_0.json").readText()
}

val medicationDispenseJson by lazy {
    File("$ResourceBasePath/fhir/medication_dispense.json").readText()
}

val medicationDispenseWithoutCategoryJson by lazy {
    File("$ResourceBasePath/fhir/medication_dispense_without_category.json").readText()
}

val medicationDispenseWithUnknownMedicationProfileJson by lazy {
    File("$ResourceBasePath/fhir/medication_dispense_unknown_medication_profile.json").readText()
}

val medDispenseBundleVersion_1_2 by lazy {
    File("$ResourceBasePath/fhir/bundle_med_dispense_version_1_2.json").readText()
}

val dispenseSimpleMedication_1_4 by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/bundle_dispense_simple_medication.json").readText()
}

val simpleMedication_1_4 by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/resource_simple_medication.json").readText()
}

val pharmaceuticalProduct by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/pharmaceuticalProduct.json").readText()
}

val complexMedication_1_4 by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/Medication-Medication-Kombipackung.json").readText()
}

val dispenseMultipleMedication_1_4 by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/bundle_multiple_dispenses_simple_medications.json").readText()
}

val dispenseCompoundingMedication_1_4 by lazy {
    File("$ResourceBasePath/fhir/workflow_version_1_4/bundle_dispense_compounding_medication.json").readText()
}

val task_bundle_version_1_2 by lazy {
    File("$ResourceBasePath/fhir/task_bundle_vers_1_2.json").readText()
}
val task_bundle_version_1_3 by lazy {
    File("$ResourceBasePath/fhir/task_bundle_vers_1_3.json").readText()
}

// for PKV profile v1.2
val charge_item_bundle_version_1_2 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/charge_item_bundle_vers_1_2.json").readText()
}

val chargeItem_freetext by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/Freitext-Verordnung.json").readText()
}

val chargeItem_pzn_1 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_1.json").readText()
}

val chargeItem_pzn_2 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_2.json").readText()
}

val chargeItem_pzn_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_3.json").readText()
}

val chargeItem_pzn_5 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_5.json").readText()
}

val chargeItem_pzn_6 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_6.json").readText()
}

val chargeItem_pzn_7 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_7.json").readText()
}

val chargeItem_pzn_8 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/PZN-Verordnung_Nr_8.json").readText()
}

val chargeItem_compounding by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_2/Rezeptur-Verordnung_Nr_1.json").readText()
}

// for PKV profile v1.3

val chargeItem_pzn_1_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_1.json").readText()
}

val chargeItem_pzn_2_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_2.json").readText()
}

val chargeItem_pzn_3_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_3.json").readText()
}

val chargeItem_pzn_5_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_5.json").readText()
}

val chargeItem_pzn_6_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_6.json").readText()
}

val chargeItem_pzn_7_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_7.json").readText()
}

val chargeItem_pzn_8_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_8.json").readText()
}

val chargeItem_pzn_14_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_14.json").readText()
}

val chargeItem_pzn_15_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_15.json").readText()
}

val chargeItem_pzn_16_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_16.json").readText()
}

val erp_charge_bundle_1_4 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/erp_charge_bundle_1_4.json").readText()
}

val chargeItem_pzn_18_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/PZN-Verordnung_Nr_18.json").readText()
}

val chargeItem_freetext_v1_3 by lazy {
    File("$ResourceBasePath/fhir/pkv/pkv1_3/Freitext-Verordnung.json").readText()
}
