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

import java.io.File

const val ResourceBasePath = "src/commonTest/resources/"

val taskJson_vers_1_1_1 by lazy {
    File("$ResourceBasePath/fhir/task.json").readText()
}
val taskJson_vers_1_2 by lazy {
    File("$ResourceBasePath/fhir/task_vers_1_2.json").readText()
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

val task_bundle_version_1_2 by lazy {
    File("$ResourceBasePath/fhir/task_bundle_vers_1_2.json").readText()
}

val charge_item_bundle_version_1_2 by lazy {
    File("$ResourceBasePath/fhir/charge_item_bundle_vers_1_2.json").readText()
}

val pkvAbgabedatenJson_vers_1_1 by lazy {
    File("$ResourceBasePath/fhir/charge_item_by_id_bundle.json").readText()
}
