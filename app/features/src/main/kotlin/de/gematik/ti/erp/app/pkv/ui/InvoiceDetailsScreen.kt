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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.PkvHtmlTemplate.joinMedicationInfo
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.navigation.PkvNavigationArguments
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.profileById
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.visualTestTag

class InvoiceDetailsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val pkvNavigationArguments = remember {
            val arguments = requireNotNull(navBackStackEntry.arguments)
            PkvNavigationArguments(
                taskId = requireNotNull(arguments.getString(PkvRoutes.TaskId)),
                profileId = requireNotNull(arguments.getString(PkvRoutes.ProfileId))
            )
        }
        val invoiceController = rememberInvoiceController(pkvNavigationArguments.profileId)
        val profiles by invoiceController.getProfilesState()
        profiles.profileById(pkvNavigationArguments.profileId)?.let { selectedProfile ->
            val listState = rememberLazyListState()
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val invoice by produceState<InvoiceData.PKVInvoice?>(null) {
                invoiceController.detailState(pkvNavigationArguments.taskId).collect {
                    value = it
                }
            }
            var showDeleteInvoiceAlert by remember { mutableStateOf(false) }

            if (showDeleteInvoiceAlert) {
                DeleteInvoiceDialog(
                    onCancel = {
                        showDeleteInvoiceAlert = false
                    }
                ) {
                    onDeleteInvoice(
                        scope,
                        pkvNavigationArguments.taskId,
                        invoiceController,
                        selectedProfile,
                        context,
                        scaffoldState
                    ) {
                        showDeleteInvoiceAlert = false
                        navController.popBackStack()
                    }
                }
            }

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
                            onClickSubmit = {
                                navController.navigate(
                                    PkvRoutes.InvoiceShareScreen.path(
                                        taskId = it.taskId,
                                        profileId = selectedProfile.id
                                    )
                                )
                            }
                        )
                    }
                },
                listState = listState,
                actions = {
                    Row {
                        invoice?.let { invoice ->
                            InvoiceThreeDotMenu(
                                invoice.taskId,
                                onClickShareInvoice = {
                                    navController.navigate(
                                        PkvRoutes.InvoiceShareScreen.path(
                                            taskId = invoice.taskId,
                                            profileId = selectedProfile.id
                                        )
                                    )
                                },
                                onClickRemoveInvoice = { showDeleteInvoiceAlert = true },
                                onClickCorrectInvoiceLocally = {
                                    navController.navigate(
                                        PkvRoutes.InvoiceLocalCorrectionScreen.path(
                                            taskId = invoice.taskId,
                                            profileId = selectedProfile.id
                                        )
                                    )
                                }
                            )
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            ) { innerPadding ->
                InvoiceDetailsScreenContent(
                    navController,
                    innerPadding,
                    listState,
                    invoice,
                    selectedProfile
                )
            }
        }
    }
}

@Composable
private fun InvoiceDetailsScreenContent(
    navController: NavController,
    innerPadding: PaddingValues,
    listState: LazyListState,
    invoice: InvoiceData.PKVInvoice?,
    selectedProfile: ProfilesUseCaseData.Profile
) {
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
                    TertiaryButton(onClick = {
                        navController.navigate(
                            PkvRoutes.InvoiceExpandedDetailsScreen.path(
                                taskId = it.taskId,
                                profileId = selectedProfile.id
                            )
                        )
                    }) {
                        Text(text = stringResource(R.string.invoice_show_more))
                    }
                }
            }
        }
        item {
            if (
                (navController.previousBackStackEntry?.id)
                == PrescriptionDetailRoutes.PrescriptionDetailScreen.route
            ) {
                LinkToInvoiceList(navController, profileIdentifier = selectedProfile.id)
            }
        }
    }
}

@Composable
private fun InvoiceDetailBottomBar(totalBruttoAmount: Double, onClickSubmit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                color = AppTheme.colors.neutral100
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.invoice_details_cost, totalBruttoAmount),
                style = AppTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.invoice_detail_total_brutto_amount), style = AppTheme.typography.body2l)
        }

        Button(
            onClick = onClickSubmit,
            modifier = Modifier.padding(end = PaddingDefaults.Medium)
        ) {
            Text(text = stringResource(R.string.invoice_details_submit))
        }
    }
}

@Composable
fun InvoiceMedicationHeader(invoice: InvoiceData.PKVInvoice) {
    val medicationInfo = joinMedicationInfo(invoice.medicationRequest)
    Text(text = medicationInfo, style = AppTheme.typography.h5)
}

@Composable
private fun LinkToInvoiceList(navController: NavController, profileIdentifier: ProfileIdentifier) {
    Row(
        modifier = Modifier
            .clickable { navController.navigate(PkvRoutes.InvoiceListScreen.path(profileId = profileIdentifier)) }
            .padding(PaddingDefaults.Medium)
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.link_to_invoice_list),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.primary600
        )
        SpacerTiny()
        Icon(Icons.Rounded.KeyboardArrowRight, null, tint = AppTheme.colors.primary600)
    }
}
