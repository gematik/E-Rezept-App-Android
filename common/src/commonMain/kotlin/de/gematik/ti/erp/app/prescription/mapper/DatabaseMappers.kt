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

package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.AccidentTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.CoverageTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.IdentifierEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationCategoryV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskOrganizationErpModel
import io.realm.kotlin.ext.toRealmList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

object DatabaseMappers {

    fun FhirTaskOrganizationErpModel.toDatabaseModel() = OrganizationEntityV1()
        .apply {
            this.name = this@toDatabaseModel.name
            this.uniqueIdentifier = bsnr
            this.phone = this@toDatabaseModel.phone
            this.mail = this@toDatabaseModel.mail
            this.address = this@toDatabaseModel.address?.toDatabaseModel()
        }

    fun FhirTaskKbvAddressErpModel.toDatabaseModel() = AddressEntityV1()
        .apply {
            this.line1 = streetName ?: ""
            this.line2 = houseNumber ?: ""
            this.postalCode = this@toDatabaseModel.postalCode ?: ""
            this.city = this@toDatabaseModel.city ?: ""
        }

    fun FhirTaskKbvPatientErpModel.toDatabaseModel() = PatientEntityV1()
        .apply {
            this.name = this@toDatabaseModel.name
            this.dateOfBirth = this@toDatabaseModel.birthDate
            this.insuranceIdentifier = insuranceInformation
            this.address = this@toDatabaseModel.address?.toDatabaseModel()
        }

    fun FhirTaskKbvPractitionerErpModel.toDatabaseModel() = PractitionerEntityV1()
        .apply {
            this.name = this@toDatabaseModel.name
            this.qualification = this@toDatabaseModel.qualification
            this.practitionerIdentifier = this@toDatabaseModel.practitionerIdentifier
        }

    fun FhirCoverageErpModel.toDatabaseModel() = InsuranceInformationEntityV1()
        .apply {
            this.name = this@toDatabaseModel.name
            this.statusCode = this@toDatabaseModel.statusCode
            this.coverageType = CoverageTypeV1.mapTo(this@toDatabaseModel.coverageType ?: "") // todo: fix to enum
        }

    fun FhirTaskKbvMedicationRequestErpModel.toDatabaseModel() = MedicationRequestEntityV1()
        .apply {
            this.authoredOn = this@toDatabaseModel.authoredOn
            this.dateOfAccident = this@toDatabaseModel.dateOfAccident?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
            this.location = this@toDatabaseModel.location
            this.accidentType = this@toDatabaseModel.accidentType.toDatabaseModel()
            this.emergencyFee = this@toDatabaseModel.emergencyFee
            this.substitutionAllowed = this@toDatabaseModel.substitutionAllowed
            this.dosageInstruction = this@toDatabaseModel.dosageInstruction
            this.quantity = this@toDatabaseModel.quantity
            this.multiplePrescriptionInfo = this@toDatabaseModel.multiplePrescriptionInfo?.toDatabaseModel()
            this.note = this@toDatabaseModel.note
            this.bvg = this@toDatabaseModel.bvg
            this.additionalFee = this@toDatabaseModel.additionalFee
        }

    fun FhirTaskKbvMedicationErpModel.toDatabaseModel() = MedicationEntityV1()
        .apply {
            this.text = this@toDatabaseModel.text ?: ""
            this.medicationCategory = this@toDatabaseModel.medicationCategory.toDatabaseModel()
            this.form = this@toDatabaseModel.form
            this.amount = this@toDatabaseModel.amount?.toDatabaseModel()
            this.vaccine = this@toDatabaseModel.isVaccine
            this.manufacturingInstructions = this@toDatabaseModel.compoundingInstructions
            this.packaging = this@toDatabaseModel.compoundingPackaging
            this.normSizeCode = this@toDatabaseModel.normSizeCode
            this.identifier = this@toDatabaseModel.identifier.toDatabaseModel()
            this.ingredients = this@toDatabaseModel.ingredients.map { it.toDatabaseModel() }.toRealmList()
        }

    private fun FhirMultiplePrescriptionInfoErpModel.toDatabaseModel() = MultiplePrescriptionInfoEntityV1()
        .apply {
            this.indicator = this@toDatabaseModel.indicator
            this.numbering = this@toDatabaseModel.numbering?.toDatabaseModel()
            this.start = this@toDatabaseModel.start?.toInstant(TimeZone.UTC)?.toRealmInstant()
            this.end = this@toDatabaseModel.end?.toInstant(TimeZone.UTC)?.toRealmInstant()
        }

    private fun FhirRatioErpModel.toDatabaseModel() = RatioEntityV1()
        .apply {
            this.numerator = this@toDatabaseModel.numerator?.toDatabaseModel()
            this.denominator = this@toDatabaseModel.denominator?.toDatabaseModel()
        }

    private fun FhirQuantityErpModel.toDatabaseModel() = QuantityEntityV1()
        .apply {
            this.value = this@toDatabaseModel.value ?: ""
            this.unit = this@toDatabaseModel.unit ?: ""
        }

    private fun FhirMedicationIdentifierErpModel.toDatabaseModel() = IdentifierEntityV1()
        .apply {
            this.pzn = this@toDatabaseModel.pzn
            this.atc = this@toDatabaseModel.atc
            this.ask = this@toDatabaseModel.ask
            this.snomed = this@toDatabaseModel.snomed
        }

    private fun FhirMedicationIngredientErpModel.toDatabaseModel() = IngredientEntityV1()
        .apply {
            this.text = this@toDatabaseModel.text ?: ""
            this.form = this@toDatabaseModel.form
            this.amount = this@toDatabaseModel.amount
            this.strength = this@toDatabaseModel.strengthRatio?.toDatabaseModel()
            this.identifier = this@toDatabaseModel.identifier.toDatabaseModel()
            this.number = "" // todo: fix parse from json
        }

    // the code for the accident-type is lost in this mapping due to DB constraints
    private fun FhirTaskAccidentType.toDatabaseModel() = when (this) {
        FhirTaskAccidentType.Unfall -> AccidentTypeV1.Unfall
        FhirTaskAccidentType.Arbeitsunfall -> AccidentTypeV1.Arbeitsunfall
        FhirTaskAccidentType.Berufskrankheit -> AccidentTypeV1.Berufskrankheit
        FhirTaskAccidentType.None -> AccidentTypeV1.None
    }

    // the code and description text for the medication category is lost in this mapping due to DB constraints
    private fun FhirTaskMedicationCategoryErpModel.toDatabaseModel() = when (this) {
        FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL -> MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL
        FhirTaskMedicationCategoryErpModel.BTM -> MedicationCategoryV1.BTM
        FhirTaskMedicationCategoryErpModel.AMVV -> MedicationCategoryV1.AMVV
        FhirTaskMedicationCategoryErpModel.SONSTIGES -> MedicationCategoryV1.SONSTIGES
        FhirTaskMedicationCategoryErpModel.UNKNOWN -> MedicationCategoryV1.UNKNOWN
    }
}
