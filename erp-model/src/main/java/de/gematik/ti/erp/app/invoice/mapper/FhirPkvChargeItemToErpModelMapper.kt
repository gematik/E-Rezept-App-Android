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

package de.gematik.ti.erp.app.invoice.mapper

import de.gematik.ti.erp.app.fhir.FhirPkvChargeItem
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.support.ChargeItemType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.invoice.model.ChargeableItemDescriptionErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PriceComponentErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.Ingredient
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import kotlinx.datetime.Instant

fun FhirPkvChargeItem.toErpModel(profileId: String): PKVInvoiceErpModel {
    val invoice = invoiceErpModel
    val kbvData = kbvDataErpModel
    val dispense = medicationDispenseErpModel

    return PKVInvoiceErpModel(
        profileId = profileId,
        taskId = taskId ?: "",
        accessCode = accessCode ?: "",
        timestamp = (invoice?.timestamp as? FhirTemporal.Instant)?.toInstant() ?: Instant.fromEpochMilliseconds(0),
        pharmacyOrganization = invoice?.organization?.let { org ->
            OrganizationErpModel(
                name = org.name,
                uniqueIdentifier = org.iknr,
                address = org.address?.let { addr ->
                    de.gematik.ti.erp.app.task.model.AddressErpModel(
                        line1 = addr.streetName ?: "",
                        line2 = addr.houseNumber ?: "",
                        postalCode = addr.postalCode ?: "",
                        city = addr.city ?: "",
                        additionalAddressInformation = addr.additionalAddressInformation
                    )
                }
            )
        },
        practitionerOrganization = kbvData?.organization?.let { org ->
            OrganizationErpModel(
                name = org.name,
                uniqueIdentifier = org.bsnr,
                address = org.address?.let { addr ->
                    de.gematik.ti.erp.app.task.model.AddressErpModel(
                        line1 = addr.streetName ?: "",
                        line2 = addr.houseNumber ?: "",
                        postalCode = addr.postalCode ?: "",
                        city = addr.city ?: "",
                        additionalAddressInformation = addr.additionalAddressInformation
                    )
                }
            )
        },
        practitioner = kbvData?.practitioner?.let { pr ->
            PractitionerErpModel(
                name = pr.name,
                qualification = pr.qualification,
                practitionerIdentifier = pr.doctorIdentifier,
                dentistIdentifier = pr.dentistIdentifier,
                telematikId = pr.telematikId
            )
        },
        patient = kbvData?.patient?.let { pat ->
            PatientErpModel(
                name = pat.name,
                insuranceIdentifier = pat.insuranceInformation,
                dateOfBirth = pat.birthDate,
                address = pat.address?.let { addr ->
                    de.gematik.ti.erp.app.task.model.AddressErpModel(
                        line1 = addr.streetName ?: "",
                        line2 = addr.houseNumber ?: "",
                        postalCode = addr.postalCode ?: "",
                        city = addr.city ?: "",
                        additionalAddressInformation = addr.additionalAddressInformation
                    )
                }
            )
        },
        medicationRequest = kbvData?.medicationRequest?.let { req ->
            MedicationRequestErpModel(
                authoredOn = req.authoredOn,
                quantity = req.quantity,
                substitutionAllowed = req.substitutionAllowed,
                dosageInstruction = req.dosageInstruction,
                note = req.note,
                medication = kbvData.medication?.toMedicationErpModel()
            )
        },
        whenHandedOver = invoice?.whenHandedOver ?: dispense?.whenHandedOver,
        consumed = false,
        invoice = invoice?.toInvoiceErpModel(),
        invoiceBinary = invoiceErpModel?.binary,
        kbvBinary = kbvBinaryErpModel?.binary,
        erpPrBinary = invoiceBinaryErpModel?.binary
    )
}

fun FhirPkvInvoiceErpModel.toInvoiceErpModel() = InvoiceErpModel(
    totalAdditionalFee = totalAdditionalFee?.value?.toDouble() ?: 0.0,
    totalBruttoAmount = totalGrossFee?.value?.toDouble() ?: 0.0,
    currency = totalGrossFee?.unit ?: "",
    chargeableItems = lineItems.mapNotNull { it.toChargeableItemErpModel() },
    additionalDispenseItems = additionalDispenseItems.mapNotNull { it.toChargeableItemErpModel() },
    additionalInformation = additionalInvoiceInformation
)

fun FhirPkvInvoiceChargeItemErpModel.toChargeableItemErpModel(): ChargeableItemErpModel? {
    val code = chargeItemCode ?: return null
    val desc = code.code ?: ""
    val description = when (code.type) {
        ChargeItemType.Pzn -> ChargeableItemDescriptionErpModel.PZN(desc)
        ChargeItemType.Ta1 -> ChargeableItemDescriptionErpModel.TA1(desc)
        ChargeItemType.Hmnr -> ChargeableItemDescriptionErpModel.HMNR(desc)
    }
    return ChargeableItemErpModel(
        description = description,
        text = code.text ?: "",
        factor = factor?.toDouble() ?: 0.0,
        price = PriceComponentErpModel(
            value = price?.toDouble() ?: 0.0,
            tax = tax?.toDouble() ?: 0.0
        )
    )
}

private fun FhirTaskKbvMedicationErpModel.toMedicationErpModel(): MedicationErpModel {
    return MedicationErpModel(
        category = when (medicationCategory) {
            FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL -> MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
            FhirTaskMedicationCategoryErpModel.BTM -> MedicationCategory.BTM
            FhirTaskMedicationCategoryErpModel.AMVV -> MedicationCategory.AMVV
            FhirTaskMedicationCategoryErpModel.SONSTIGES -> MedicationCategory.SONSTIGES
            FhirTaskMedicationCategoryErpModel.UNKNOWN -> MedicationCategory.UNKNOWN
        },
        medicationProfile = medicationProfile,
        isVaccine = isVaccine,
        text = text ?: "",
        form = form,
        normSizeCode = normSizeCode,
        amount = amount?.let { ratio ->
            RatioErpModel(
                numerator = ratio.numerator?.let { QuantityErpModel(it.value ?: "", it.unit ?: "") },
                denominator = ratio.denominator?.let { QuantityErpModel(it.value ?: "", it.unit ?: "") }
            )
        },
        identifier = Identifier(
            pzn = identifier.pzn,
            atc = identifier.atc,
            ask = identifier.ask,
            snomed = identifier.snomed
        ),
        manufacturingInstructions = compoundingInstructions,
        packaging = compoundingPackaging,
        ingredients = ingredients.map { ingredient ->
            Ingredient(
                text = ingredient.text ?: "",
                form = ingredient.form,
                amount = ingredient.amount,
                strength = ingredient.strengthRatio?.let { ratio ->
                    RatioErpModel(
                        numerator = ratio.numerator?.let { QuantityErpModel(it.value ?: "", it.unit ?: "") },
                        denominator = ratio.denominator?.let { QuantityErpModel(it.value ?: "", it.unit ?: "") }
                    )
                }
            )
        }
    )
}
