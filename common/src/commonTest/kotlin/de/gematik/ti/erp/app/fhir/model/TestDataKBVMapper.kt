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

import java.io.File

val taskJson by lazy {
    File("$ResourceBasePath/fhir/task.json").readText()
}
val organizationJson by lazy {
    File("$ResourceBasePath/fhir/organization.json").readText()
}
val patientJson by lazy {
    File("$ResourceBasePath/fhir/patient.json").readText()
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
val multiPrescriptionInfoJson by lazy {
    File("$ResourceBasePath/fhir/multi_prescription_info.json").readText()
}
val medicationPznJson by lazy {
    File("$ResourceBasePath/fhir/medication_pzn.json").readText()
}
val medicationIngredientJson by lazy {
    File("$ResourceBasePath/fhir/medication_ingredient.json").readText()
}
val medicationCompoundingJson by lazy {
    File("$ResourceBasePath/fhir/medication_compounding.json").readText()
}
val medicationFreetextJson by lazy {
    File("$ResourceBasePath/fhir/medication_freetext.json").readText()
}
val medicationRequestJson by lazy {
    File("$ResourceBasePath/fhir/medication_request.json").readText()
}
val medicationDispenseJson by lazy {
    File("$ResourceBasePath/fhir/medication_dispense.json").readText()
}
