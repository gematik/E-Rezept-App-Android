/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.currencyString
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.visualTestTag

@Composable
fun InvoiceDetailsScreen(
    invoicesController: InvoicesController,
    taskId: String,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()

    val invoice by produceState<InvoiceData.PKVInvoice?>(null) {
        invoicesController.detailState(taskId).collect {
            value = it
        }
    }

    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .visualTestTag(TestTag.Profile.InvoicesDetailScreen),
        topBarTitle = "",
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        listState = listState,
        actions = {},
        onBack = onBack
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = PaddingDefaults.Medium + innerPadding.calculateTopPadding(),
                    bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium
                ),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            invoice?.let {
                item {
                    InvoiceMedicationHeader(it)
                }
                item {
                    LabeledText(description = stringResource(R.string.invoice_task_id), content = it.taskId)
                }
                item {
                    PatientLabel(it.patient)
                }
                item {
                    PractitionerLabel(it.practitioner, it.practitionerOrganization)
                }
                item {
                    PharmacyLabel(it.pharmacyOrganization)
                }
                item {
                    LabeledText(
                        description = stringResource(R.string.invoice_redeemed_on),
                        content = it.whenHandedOver?.formattedString()
                    )
                }
                item {
                    PriceData(it.invoice)
                }
            }
        }
    }
}

@Composable
fun PriceData(invoice: InvoiceData.Invoice) {
    val (fees, articles) = invoice.chargeableItems.partition {
        (it.description as? InvoiceData.ChargeableItem.Description.PZN)?.isSpecialPZN() ?: false
    }

    articles.map {
        val article = when (it.description) {
            is InvoiceData.ChargeableItem.Description.HMNR ->
                stringResource(
                    R.string.invoice_description_hmknr,
                    (it.description as InvoiceData.ChargeableItem.Description.HMNR).hmnr
                )
            is InvoiceData.ChargeableItem.Description.PZN ->
                stringResource(
                    R.string.invoice_description_pzn,
                    (it.description as InvoiceData.ChargeableItem.Description.PZN).pzn
                )
            is InvoiceData.ChargeableItem.Description.TA1 ->
                stringResource(
                    R.string.invoice_description_ta1,
                    (it.description as InvoiceData.ChargeableItem.Description.TA1).ta1
                )
        }

        Text(stringResource(R.string.invoice_description_articel, article))
        Text(stringResource(R.string.invoice_description_factor, it.factor))
        Text(stringResource(R.string.invoice_description_tax, it.price.tax.currencyString()))
        Text(stringResource(R.string.invoice_description_brutto_price, it.price.value))

        SpacerMedium()
    }

    if (fees.isNotEmpty()) {
        Text(stringResource(R.string.invoice_description_additional_fees))
        fees.map {
            require(it.description is InvoiceData.ChargeableItem.Description.PZN)
            val article = when (
                InvoiceData.SpecialPZN.valueOfPZN(
                    (it.description as InvoiceData.ChargeableItem.Description.PZN).pzn
                )
            ) {
                InvoiceData.SpecialPZN.EmergencyServiceFee -> stringResource(R.string.invoice_details_emergency_fee)
                InvoiceData.SpecialPZN.BTMFee -> stringResource(R.string.invoice_details_narcotic_fee)
                InvoiceData.SpecialPZN.TPrescriptionFee -> stringResource(R.string.invoice_details_t_prescription_fee)
                InvoiceData.SpecialPZN.ProvisioningCosts -> stringResource(R.string.invoice_details_provisioning_costs)
                InvoiceData.SpecialPZN.DeliveryServiceCosts ->
                    stringResource(R.string.invoice_details_delivery_service_costs)
                null -> error("wrong mapping")
            }

            Text(stringResource(R.string.invoice_description_articel, article))
            Text(stringResource(R.string.invoice_description_brutto_price, it.price.value))

            SpacerMedium()
        }
    }

    Text(stringResource(R.string.invoice_description_total_brutto_amount, invoice.totalBruttoAmount))

    Text(stringResource(R.string.invoice_detail_dispense), style = AppTheme.typography.body2l)
    SpacerXXLarge()
}

@Composable
fun PharmacyLabel(pharmacyOrganization: SyncedTaskData.Organization) {
    LabeledTextItems(
        label = stringResource(R.string.invoice_redeemed_in),
        items = listOf(
            pharmacyOrganization.name,
            pharmacyOrganization.address?.joinToString(),
            pharmacyOrganization.uniqueIdentifier?.let { stringResource(R.string.invoice_pharmacy_id, it) }
        )
    )
}

@Composable
fun PractitionerLabel(
    practitioner: SyncedTaskData.Practitioner,
    practitionerOrganization: SyncedTaskData.Organization
) {
    LabeledTextItems(
        label = stringResource(R.string.invoice_prescribed_by),
        items = listOf(
            practitioner.name,
            practitionerOrganization.address?.joinToString(),
            practitioner.practitionerIdentifier?.let { stringResource(R.string.invoice_practitioner_id, it) }
        )
    )
}

@Composable
fun LabeledTextItems(label: String, items: List<String?>) {
    val showLabel = items.any { it != null }
    items.forEach {
        it?.let {
            Text(it, style = AppTheme.typography.body1)
        }
    }
    if (showLabel) {
        Text(label, style = AppTheme.typography.body2l)
    }
}

@Composable
fun PatientLabel(patient: SyncedTaskData.Patient) {
    LabeledTextItems(
        label = stringResource(R.string.invoice_prescribed_for),
        items = listOf(
            patient.name,
            patient.insuranceIdentifier?.let { stringResource(R.string.invoice_insurance_id, it) },
            patient.address?.joinToString(),
            patient.birthdate?.formattedString()?.let { stringResource(R.string.invoice_born_on, it) }
        )
    )
}
