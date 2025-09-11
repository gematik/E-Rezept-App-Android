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

package de.gematik.ti.erp.app.pkv.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.PkvHtmlTemplate.joinMedicationInfo
import de.gematik.ti.erp.app.labels.TextLabel
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.model.InvoiceAction
import de.gematik.ti.erp.app.pkv.model.InvoiceState
import de.gematik.ti.erp.app.pkv.navigation.PkvNavigationArguments.Companion.getPkvNavigationArguments
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.pkv.ui.components.DeleteInvoiceDialog
import de.gematik.ti.erp.app.pkv.ui.components.InvoiceThreeDotMenu
import de.gematik.ti.erp.app.pkv.ui.components.InvoicesEmptyScreen
import de.gematik.ti.erp.app.pkv.ui.components.UserNotLoggedInDialog
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceDetailScreenPreviewData
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceDetailScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.show

class InvoiceDetailsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Suppress("CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val arguments = remember(navBackStackEntry.arguments) { navBackStackEntry.arguments?.getPkvNavigationArguments() }

        if (arguments == null) {
            ErrorScreenComponent(onClickRetry = { navController.popBackStack() })
            return
        }

        val dialog = LocalDialog.current
        val snackbar = LocalSnackbarScaffold.current

        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        val invoicesDeletedString = stringResource(R.string.invoices_deleted)

        val deleteInvoiceEvent = ComposableEvent<Unit>()

        val isFromPrescriptionDetails = remember(navController.previousBackStackEntry?.destination?.route) {
            navController.previousBackStackEntry?.destination?.route?.contains(PrescriptionDetailRoutes.PrescriptionDetailScreen.route) == true
        }
        val profileId = arguments.profileId
        val controller = rememberInvoiceController(profileId)
        val combinedProfileInformation by controller.combinedProfile.collectAsStateWithLifecycle()

        val invoiceState by produceState<InvoiceState>(InvoiceState.NoInvoice) {
            arguments.taskId?.let { it ->
                controller.getInvoiceForTaskId(it).collect {
                    value = if (it != null) {
                        InvoiceState.InvoiceLoaded(it)
                    } else {
                        InvoiceState.NoInvoice
                    }
                }
            }
        }

        controller.invoiceDetailScreenEvents.deleteSuccessfulEvent.listen {
            snackbar.show(invoicesDeletedString, scope)
            navController.navigateUp()
        }

        UserNotLoggedInDialog(
            onEvent = controller.invoiceDetailScreenEvents.askUserToLoginEvent,
            dialogScaffold = dialog,
            onConfirmRequest = navController::navigateUp
        )

        DeleteInvoiceDialog(
            onEvent = deleteInvoiceEvent,
            dialogScaffold = dialog,
            onDeleteInvoice = {
                arguments.taskId?.let {
                    controller.deleteInvoice(taskId = it)
                }
            }
        )

        UiStateMachine(
            state = combinedProfileInformation,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onError = {
                ErrorScreenComponent()
            }
        ) { (selectedProfile, _) ->
            InvoiceDetailScreenScaffold(
                listState = listState,
                scaffoldState = scaffoldState,
                invoiceState = invoiceState,
                isFromPrescriptionDetails = isFromPrescriptionDetails,
                onAction = { invoiceAction ->
                    when (invoiceAction) {
                        is InvoiceAction.Correct -> {
                            selectedProfile?.let {
                                navController.navigate(
                                    PkvRoutes.InvoiceLocalCorrectionScreen.path(
                                        taskId = invoiceAction.taskId,
                                        profileId = selectedProfile.id
                                    )
                                )
                            }
                        }

                        is InvoiceAction.Delete -> deleteInvoiceEvent.trigger()

                        is InvoiceAction.Share -> {
                            selectedProfile?.let {
                                navController.navigate(
                                    PkvRoutes.InvoiceShareScreen.path(
                                        taskId = invoiceAction.taskId,
                                        profileId = selectedProfile.id
                                    )
                                )
                            }
                        }

                        is InvoiceAction.Submit -> {
                            navController.navigate(
                                PkvRoutes.InvoiceShareScreen.path(
                                    taskId = invoiceAction.taskId,
                                    profileId = profileId
                                )
                            )
                        }

                        is InvoiceAction.ViewDetail -> {
                            selectedProfile?.let {
                                navController.navigate(
                                    PkvRoutes.InvoiceExpandedDetailsScreen.path(
                                        taskId = invoiceAction.taskId,
                                        profileId = selectedProfile.id
                                    )
                                )
                            }
                        }

                        InvoiceAction.ViewList -> {
                            if (isFromPrescriptionDetails) {
                                selectedProfile?.let {
                                    navController.navigate(PkvRoutes.InvoiceListScreen.path(profileId = selectedProfile.id))
                                }
                            }
                        }

                        is InvoiceAction.InAppCorrect -> {
                            if (BuildConfigExtension.isInternalDebug) {
                                snackbar.show(
                                    message = "InAppCorrect is not implemented yet",
                                    scope = scope
                                )
                            }
                        }

                        InvoiceAction.Back -> navController.navigateUp()
                    }
                }
            )
        }
    }
}

@Composable
private fun InvoiceDetailScreenScaffold(
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    invoiceState: InvoiceState,
    isFromPrescriptionDetails: Boolean,
    onAction: (InvoiceAction) -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.imePadding(),
        topBarTitle = stringResource(R.string.invoices_detail_title),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        scaffoldState = scaffoldState,
        actions = {
            invoiceState.OnInvoiceLoaded { invoices ->
                Row {
                    InvoiceThreeDotMenu(
                        taskId = invoices.record.taskId,
                        onClickShareInvoice = { taskId -> onAction(InvoiceAction.Share(taskId, invoices.record.profileId)) },
                        onClickRemoveInvoice = { taskId -> onAction(InvoiceAction.Delete(taskId)) },
                        onClickCorrectInvoiceLocally = { taskId -> onAction(InvoiceAction.Correct(taskId, invoices.record.profileId)) },
                        onClickCorrectInvoiceInApp = { taskId -> onAction(InvoiceAction.InAppCorrect(taskId, invoices.record.profileId)) }
                    )
                }
            }
        },
        bottomBar = {
            invoiceState.OnInvoiceLoaded { invoices ->
                InvoiceDetailBottomBar(
                    invoices.record.invoice.totalBruttoAmount,
                    onClickSubmit = {
                        onAction(
                            InvoiceAction.Submit(
                                invoices.record.taskId,
                                invoices.record.profileId
                            )
                        )
                    }
                )
            }
        },
        onBack = { onAction(InvoiceAction.Back) }
    ) { innerPadding ->
        InvoiceDetailsScreenContent(
            innerPadding = innerPadding,
            listState = listState,
            invoiceState = invoiceState,
            isFromPrescriptionDetails = isFromPrescriptionDetails,
            onClickInvoiceList = { onAction(InvoiceAction.ViewList) },
            onClickInvoiceDetail = { taskId -> onAction(InvoiceAction.ViewDetail(taskId)) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InvoiceDetailsScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState,
    invoiceState: InvoiceState,
    isFromPrescriptionDetails: Boolean,
    onClickInvoiceDetail: (String) -> Unit,
    onClickInvoiceList: () -> Unit
) {
    val padding by rememberContentPadding(innerPadding)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        if (invoiceState.hasInvoice()) {
            val invoice = invoiceState as InvoiceState.InvoiceLoaded
            item {
                InvoiceMedicationHeader(
                    modifier = Modifier.padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    ),
                    invoice = invoice.record
                )
            }
            item {
                TextLabel(
                    modifier = Modifier.padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    ),
                    description = stringResource(R.string.invoice_prescribed_by),
                    content = invoice.record.practitioner.name
                )
            }
            item {
                TextLabel(
                    modifier = Modifier.padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    ),
                    description = stringResource(R.string.invoice_redeemed_in),
                    content = invoice.record.pharmacyOrganization.name
                )
            }
            item {
                TextLabel(
                    modifier = Modifier.padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    ),
                    description = stringResource(R.string.invoice_redeemed_on),
                    content = invoice.record.whenHandedOver?.formattedString()
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(
                            start = padding.calculateStartPadding(LayoutDirection.Ltr),
                            end = padding.calculateEndPadding(LayoutDirection.Ltr)
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TertiaryButton(
                        onClick = {
                            onClickInvoiceDetail(invoice.record.taskId)
                        }
                    ) {
                        Text(text = stringResource(R.string.invoice_show_more))
                    }
                }
            }
            stickyHeader {
                if (isFromPrescriptionDetails && invoiceState.hasInvoice()) {
                    // check if navigation comes from PrescriptionDetailScreen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PaddingDefaults.XXLargeMedium)
                    ) {
                        Spacer(modifier = Modifier.weight(0.3f))
                        LinkToInvoiceList(
                            modifier = Modifier
                                .padding(
                                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                                )
                                .padding(vertical = PaddingDefaults.Medium)
                        ) {
                            onClickInvoiceList()
                        }
                        Spacer(modifier = Modifier.weight(0.3f))
                    }
                }
            }
        } else {
            item {
                Center {
                    InvoicesEmptyScreen()
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailBottomBar(
    totalGrossAmount: Double,
    onClickSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(color = AppTheme.colors.neutral100),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.invoice_details_cost, totalGrossAmount),
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
private fun LinkToInvoiceList(
    modifier: Modifier,
    onClickInvoiceList: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .padding(vertical = SizeDefaults.triple)
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.one))
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClickInvoiceList
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier,
            text = stringResource(R.string.link_to_invoice_list),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.primary700
        )
        SpacerTiny()
        Icon(
            modifier = modifier,
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.primary700
        )
    }
}

// TODO: move to components
@Composable
fun InvoiceMedicationHeader(
    modifier: Modifier = Modifier,
    invoice: InvoiceData.PKVInvoiceRecord
) {
    val medicationInfo = joinMedicationInfo(invoice.medicationRequest)
    Text(modifier = modifier, text = medicationInfo, style = AppTheme.typography.h5)
}

@LightDarkPreview
@Composable
fun InvoiceDetailsScreenPreview(
    @PreviewParameter(InvoiceDetailScreenPreviewParameterProvider::class) previewData: InvoiceDetailScreenPreviewData
) {
    PreviewAppTheme {
        InvoiceDetailScreenScaffold(
            listState = rememberLazyListState(),
            scaffoldState = rememberScaffoldState(),
            invoiceState = previewData.invoiceState,
            isFromPrescriptionDetails = previewData.isFromPrescriptionDetails,
            onAction = {}
        )
    }
}
