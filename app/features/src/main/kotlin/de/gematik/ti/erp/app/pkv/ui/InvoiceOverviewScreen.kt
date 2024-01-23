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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.visualTestTag

@Composable
fun InvoiceOverviewScreen(
    selectedProfile: ProfilesUseCaseData.Profile,
    taskId: String,
    onBack: () -> Unit,
    onClickShowMore: () -> Unit,
    onClickSubmit: () -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val invoicesController = rememberInvoicesController(profileId = selectedProfile.id)
    val invoice by produceState<InvoiceData.PKVInvoice?>(null) {
        invoicesController.detailState(taskId).collect {
            value = it
        }
    }
    // var showDeleteInvoiceAlert by remember { mutableStateOf(false) }

    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .visualTestTag(TestTag.Profile.InvoicesDetailScreen),
        topBarTitle = "",
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        bottomBar = {
            invoice?.let {
                InvoiceDetailBottomBar(
                    it.invoice.totalBruttoAmount,
                    onClickSubmit = onClickSubmit
                )
            }
        },
        listState = listState,
        actions = {
            Row {
                // TODO: ??
            }
        },
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
                    LabeledText(
                        description = stringResource(R.string.invoice_prescribed_by),
                        content = it.practitioner.name
                    )
                }
                item {
                    LabeledText(
                        description = stringResource(R.string.invoice_redeemed_in),
                        content = it.pharmacyOrganization.name
                    )
                }
                item {
                    LabeledText(
                        description = stringResource(R.string.invoice_redeemed_on),
                        content = it.whenHandedOver?.formattedString()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TertiaryButton(onClick = onClickShowMore) {
                            Text(text = stringResource(R.string.invoice_show_more))
                        }
                    }
                }
            }
        }
    }
}
