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

package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.AccidentTypeV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CoverageTypeV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.DeviceRequestEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IdentifierEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IngredientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationCategoryV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationRequestEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.OrganizationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PatientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PractitionerEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.QuantityEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedEpaMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedIngredientMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedPznMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedCompoundingMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedFreeTextMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.AMVV
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.BTM
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.SONSTIGES
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.UNKNOWN
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import io.realm.kotlin.ext.toRealmList
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

object TaskDatabaseMappers {

    private fun FhirTemporal.toRealmInstant() = this.toInstant(TimeZone.UTC).toRealmInstant()

    private fun LocalDate.toRealmInstant() = this.atStartOfDayIn(TimeZone.UTC).toRealmInstant()

    private fun FhirMultiplePrescriptionInfoErpModel.toDatabaseModel() = MultiplePrescriptionInfoEntityV1()
        .apply {
            this.indicator = this@toDatabaseModel.indicator
            this.numbering = this@toDatabaseModel.numbering?.toDatabaseModel()
            this.start = this@toDatabaseModel.start?.toRealmInstant()
            this.end = this@toDatabaseModel.end?.toRealmInstant()
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
        FhirTaskAccidentType.Accident -> AccidentTypeV1.Unfall
        FhirTaskAccidentType.WorkAccident -> AccidentTypeV1.Arbeitsunfall
        FhirTaskAccidentType.OccupationalDisease -> AccidentTypeV1.Berufskrankheit
        else -> AccidentTypeV1.None
    }

    // the code and description text for the medication category is lost in this mapping due to DB constraints
    private fun FhirTaskMedicationCategoryErpModel.toDatabaseModel() = when (this) {
        ARZNEI_UND_VERBAND_MITTEL -> MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL
        BTM -> MedicationCategoryV1.BTM
        AMVV -> MedicationCategoryV1.AMVV
        SONSTIGES -> MedicationCategoryV1.SONSTIGES
        UNKNOWN -> MedicationCategoryV1.UNKNOWN
    }

    private fun medicationCategoryCodeToDatabaseModel(category: String?): MedicationCategoryV1 {
        return when (category) {
            ARZNEI_UND_VERBAND_MITTEL.code -> MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL
            BTM.code -> MedicationCategoryV1.BTM
            AMVV.code -> MedicationCategoryV1.AMVV
            else -> MedicationCategoryV1.UNKNOWN
        }
    }

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
            this.identifierNumber = this@toDatabaseModel.healthInsuranceIdentifierForDiga // added for diga identification (iknr)
            this.coverageType = CoverageTypeV1.mapTo(this@toDatabaseModel.coverageType ?: "")
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

    fun FhirTaskKbvDeviceRequestErpModel.toDatabaseModel(isNew: Boolean) = DeviceRequestEntityV1()
        .apply {
            this.id = this@toDatabaseModel.id ?: ""
            this.intent = this@toDatabaseModel.intent.code
            this.status = this@toDatabaseModel.status
            this.pzn = this@toDatabaseModel.pzn ?: ""
            this.appName = this@toDatabaseModel.appName ?: ""
            this.isSelfUse = this@toDatabaseModel.isSelfUse
            this.authoredOn = this@toDatabaseModel.authoredOn?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
            this.accidentType = this@toDatabaseModel.accident?.type?.code ?: ""
            this.accidentLocation = this@toDatabaseModel.accident?.location ?: ""
            this.accidentDate = this@toDatabaseModel.accident?.date?.value?.toRealmInstant()
            this.isNew = isNew
        }

    private fun MedicationEntityV1.fillCommonFieldsForDispensedMedication(
        model: DispensedMedicationErpModel,
        category: MedicationCategoryV1
    ): MedicationEntityV1 = apply {
        text = model.text ?: ""
        medicationCategory = category
        form = model.form
        amount = model.amount?.toDatabaseModel()
        vaccine = model.isVaccine ?: false
        lotNumber = model.lotNumber
        expirationDate = model.expirationDate
    }

    private fun DispensedMedicationErpModel.toDatabaseModel(): MedicationEntityV1 = MedicationEntityV1().apply {
        fillCommonFieldsForDispensedMedication(
            model = this@toDatabaseModel,
            category = medicationCategoryCodeToDatabaseModel(category)
        )
        when (this@toDatabaseModel) {
            is FhirDispensedCompoundingMedicationErpModel -> {
                this.identifier = this@toDatabaseModel.contextualData.identifier?.toDatabaseModel()
                this.manufacturingInstructions = this@toDatabaseModel.contextualData.manufacturingInstructions
                this.packaging = this@toDatabaseModel.contextualData.packaging
                this.ingredients = this@toDatabaseModel.contextualData.ingredients.map { it.toDatabaseModel() }.toRealmList()
            }

            is DispensedEpaMedicationErpModel -> {
                this.identifier = this@toDatabaseModel.contextualData.identifier?.toDatabaseModel()
                this.normSizeCode = this@toDatabaseModel.contextualData.normSizeCode
                this.manufacturingInstructions = this@toDatabaseModel.contextualData.manufacturingInstructions
                this.packaging = this@toDatabaseModel.contextualData.packaging
                this.ingredients = this@toDatabaseModel.contextualData.ingredients.map { it.toDatabaseModel() }.toRealmList()
                this.ingredientMedications = this@toDatabaseModel.contextualData.internalMedication.map { it.toDatabaseModel() }.toRealmList()
            }

            is FhirDispensedFreeTextMedicationErpModel -> {
                // nothing to persist that is specific here, everything is available from the common data
            }

            is DispensedIngredientMedicationErpModel -> {
                this.identifier = this@toDatabaseModel.contextualData.identifier?.toDatabaseModel()
                this.normSizeCode = this@toDatabaseModel.contextualData.normSizeCode
                this.ingredients = this@toDatabaseModel.contextualData.ingredients.map { it.toDatabaseModel() }.toRealmList()
            }

            is DispensedPznMedicationErpModel -> {
                this.identifier = this@toDatabaseModel.contextualData.identifier?.toDatabaseModel()
                this.normSizeCode = this@toDatabaseModel.contextualData.normSizeCode
            }
        }
    }

    private fun FhirDispenseDeviceRequestErpModel.toDatabaseModel(): DeviceRequestDispenseEntityV1 = DeviceRequestDispenseEntityV1().apply {
        this.deepLink = this@toDatabaseModel.deepLink
        this.redeemCode = this@toDatabaseModel.redeemCode
        this.declineCode = this@toDatabaseModel.declineCode
        this.note = this@toDatabaseModel.note
        this.modifiedDate = this@toDatabaseModel.modifiedDate?.toRealmInstant()
        this.referencePzn = this@toDatabaseModel.referencePzn
        this.display = this@toDatabaseModel.display
        this.status = this@toDatabaseModel.status
    }

    fun FhirMedicationDispenseErpModel.toDatabaseModel() = MedicationDispenseEntityV1()
        .apply {
            this.dispenseId = this@toDatabaseModel.dispenseId
            this.patientIdentifier = this@toDatabaseModel.kvnrNumber
            this.wasSubstituted = this@toDatabaseModel.substitutionAllowed
            this.dosageInstruction = this@toDatabaseModel.dosageInstruction
            this.performer = this@toDatabaseModel.telematikId ?: ""
            // issue, not converting to realm instant while we convert others
            this.handedOverOn = this@toDatabaseModel.handedOver
            // from fhir rules, we have dispense -- medication as a 1-1 relation
            this.medication = this@toDatabaseModel.dispensedMedication.firstOrNull()?.toDatabaseModel()
            this.deviceRequest = this@toDatabaseModel.dispensedDeviceRequest?.toDatabaseModel()
        }
}
