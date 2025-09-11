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

import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.DescriptionTypeV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItem
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceErpModel
import de.gematik.ti.erp.app.fhir.support.ChargeItemType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.prescription.mapper.TaskDatabaseMappers.toDatabaseModel

object InvoiceDatabaseMappers {

    private inline fun <T, R> R.setIfNotNull(value: T?, crossinline set: R.(T) -> Unit): R =
        apply { value?.let { set(it) } }

    internal fun FhirPkvChargeItem.toInvoiceDatabaseModel(
        profile: ProfileEntityV1,
        target: PKVInvoiceEntityV1
    ) = target
        .apply { parent = profile }
        .setIfNotNull(taskId) { this.taskId = it }
        .setIfNotNull(accessCode) { this.accessCode = it }
        .setIfNotNull(kbvBinaryErpModel?.binary) { this.kbvBinary = it }
        .setIfNotNull(invoiceBinaryErpModel?.binary) { this.erpPrBinary = it }
        .setIfNotNull(invoiceErpModel?.binary) { this.invoiceBinary = it }
        .apply {
            val invoice = this@toInvoiceDatabaseModel.invoiceErpModel
            val kbvData = this@toInvoiceDatabaseModel.kbvDataErpModel
            val dispense = this@toInvoiceDatabaseModel.medicationDispenseErpModel

            // metadata
            (invoice?.timestamp as? FhirTemporal.Instant)?.value?.toRealmInstant()
                ?.let { this.timestamp = it }

            // invoice information
            this.invoice = invoice?.toDatabaseModel()

            // dispense date
            this.whenHandedOver = invoice?.whenHandedOver ?: dispense?.whenHandedOver

            // kbv data
            this.pharmacyOrganization = invoice?.organization?.toDatabaseModel()
            this.patient = kbvData?.patient?.toDatabaseModel()
            this.practitionerOrganization = kbvData?.organization?.toDatabaseModel()
            this.practitioner = kbvData?.practitioner?.toDatabaseModel()
            this.medicationRequest = kbvData?.medicationRequest?.toDatabaseModel().apply {
                this?.medication = kbvData?.medication?.toDatabaseModel()
            }
        }

    internal fun FhirPkvInvoiceErpModel.toDatabaseModel() = InvoiceEntityV1()
        .apply {
            this.totalAdditionalFee =
                this@toDatabaseModel.totalAdditionalFee?.value?.toDouble() ?: 0.0
            this.totalBruttoAmount = this@toDatabaseModel.totalGrossFee?.value?.toDouble() ?: 0.0
            this.currency = this@toDatabaseModel.totalGrossFee?.unit ?: ""
            this@toDatabaseModel.lineItems.map { it.toDatabaseModelOrNull() }.forEach { lineItem ->
                lineItem?.let { this.chargeableItems.add(it) }
            }
            this@toDatabaseModel.additionalDispenseItems.map { it.toDatabaseModelOrNull() }
                .forEach { lineItem ->
                    lineItem?.let { this.additionalDispenseItems.add(it) }
                }
            this@toDatabaseModel.additionalInvoiceInformation.forEach { additionalInformation ->
                this.additionalInformation.add(additionalInformation)
            }
        }

    internal fun FhirPkvInvoiceChargeItemErpModel.toDatabaseModelOrNull(): ChargeableItemV1? {
        val code = chargeItemCode ?: return null

        val factorValue = this.factor?.toDouble() ?: 0.0
        val taxValue = this.tax?.toDouble() ?: 0.0
        val priceValue = this.price?.toDouble() ?: 0.0

        val type = when (code.type) {
            ChargeItemType.Pzn -> DescriptionTypeV1.PZN
            ChargeItemType.Ta1 -> DescriptionTypeV1.TA1
            ChargeItemType.Hmnr -> DescriptionTypeV1.HMNR
        }

        val description = code.code?.takeIf { it.isNotBlank() } ?: return null

        return ChargeableItemV1().apply {
            this.description = description
            this.descriptionTypeV1 = type
            this.text = chargeItemCode?.text ?: ""
            this.factor = factorValue
            this.price = PriceComponentV1().apply {
                this.value = priceValue
                this.tax = taxValue
            }
        }
    }
}
