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

package de.gematik.ti.erp.app.database.realm.v1.invoice.mapper

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.DescriptionTypeV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.toErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemDescriptionErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PriceComponentErpModel

fun PKVInvoiceEntityV1.toErpModel(): PKVInvoiceErpModel =
    PKVInvoiceErpModel(
        profileId = this.parent?.id ?: "",
        taskId = this.taskId,
        accessCode = this.accessCode,
        timestamp = this.timestamp.toInstant(),
        pharmacyOrganization = this.pharmacyOrganization?.toErpModel(),
        practitionerOrganization = this.practitionerOrganization?.toErpModel(),
        practitioner = this.practitioner?.toErpModel(),
        patient = this.patient?.toErpModel(),
        medicationRequest = this.medicationRequest?.toErpModel(),
        whenHandedOver = this.whenHandedOver,
        consumed = this.consumed,
        invoice = this.invoice?.toErpModel(),
        invoiceBinary = this.invoiceBinary,
        kbvBinary = this.kbvBinary,
        erpPrBinary = this.erpPrBinary
    )

fun InvoiceEntityV1.toErpModel(): InvoiceErpModel =
    InvoiceErpModel(
        totalAdditionalFee = this.totalAdditionalFee,
        totalBruttoAmount = this.totalBruttoAmount,
        currency = this.currency,
        chargeableItems = this.chargeableItems.map { it.toErpModel() },
        additionalDispenseItems = this.additionalDispenseItems.map { it.toErpModel() },
        additionalInformation = this.additionalInformation.toList()
    )

fun ChargeableItemV1.toErpModel(): ChargeableItemErpModel =
    ChargeableItemErpModel(
        description = this.descriptionTypeV1.toErpModel(this.description),
        text = this.text,
        factor = this.factor,
        price = this.price?.toErpModel()
    )

fun DescriptionTypeV1.toErpModel(value: String): ChargeableItemDescriptionErpModel =
    when (this) {
        DescriptionTypeV1.PZN -> ChargeableItemDescriptionErpModel.PZN(value)
        DescriptionTypeV1.TA1 -> ChargeableItemDescriptionErpModel.TA1(value)
        DescriptionTypeV1.HMNR -> ChargeableItemDescriptionErpModel.HMNR(value)
    }

fun PriceComponentV1.toErpModel(): PriceComponentErpModel =
    PriceComponentErpModel(
        value = this.value,
        tax = this.tax
    )
