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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.PharmacyStartScreenModal
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PrescriptionInOrder
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.prescriptionIds
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemSharedViewModel
import de.gematik.ti.erp.app.redeem.ui.preview.PrescriptionSelectionPreview
import de.gematik.ti.erp.app.redeem.ui.preview.PrescriptionSelectionPreviewParameter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalUiScopeScaffold
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton

class PrescriptionSelectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val sharedViewModel: OnlineRedeemSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val snackbar = LocalSnackbarScaffold.current
        val isOrderOverviewMode = navBackStackEntry.arguments?.getBoolean(RedeemRoutes.REDEEM_NAV_MODAL_BEHAVIOUR) == true
        val selectedOrderState by sharedViewModel.selectedOrderState
        val orderState by sharedViewModel.redeemableOrderState
        val scope = LocalUiScopeScaffold.current
        val closeText = stringResource(R.string.cdw_troubleshooting_close_button)

        val listState = rememberLazyListState()
        val snackbarText = stringResource(R.string.pharmacy_order_no_selected_prescriptions_desc)

        val isEmptySelectionStateOnOrderOverview = isOrderOverviewMode && selectedOrderState.prescriptionsInOrder.isEmpty()

        val orderOverviewModeOnEmptyNavigation: () -> Unit by rememberUpdatedState {
            sharedViewModel.updatePrescriptionSelectionFailureFlag()
            snackbar.showWithDismissButton(message = snackbarText, scope = scope, actionLabel = closeText)
            navController.popBackStack()
        }

        val normalBackNavigation: () -> Unit by rememberUpdatedState {
            sharedViewModel.onResetPrescriptionSelection()
            navController.popBackStack()
        }

        val onBack: () -> Unit by rememberUpdatedState {
            when {
                !isOrderOverviewMode -> normalBackNavigation()
                isEmptySelectionStateOnOrderOverview -> orderOverviewModeOnEmptyNavigation()
                else -> navController.popBackStack()
            }
        }
        val onNext: () -> Unit by rememberUpdatedState {
            when {
                isEmptySelectionStateOnOrderOverview -> orderOverviewModeOnEmptyNavigation()
                isOrderOverviewMode -> navController.popBackStack()
                else -> navController.navigate(PharmacyStartScreenModal.path(taskId = ""))
            }
        }

        BackHandler(onBack = onBack)
        PrescriptionSelectionScreenScaffold(
            topBarTitle = stringResource(R.string.pharmacy_order_select_prescriptions),
            listState = listState,
            onBack = onBack,
            onNext = onNext,
            isOrderOverviewMode = isOrderOverviewMode,
            orderState = orderState,
            selectedOrderState = selectedOrderState,
            onPrescriptionSelectionChanged = { prescription, selected -> sharedViewModel.onPrescriptionSelectionChanged(prescription, selected) }
        )
    }
}

@Composable
fun PrescriptionSelectionScreenScaffold(
    topBarTitle: String,
    listState: LazyListState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    isOrderOverviewMode: Boolean,
    orderState: List<PrescriptionInOrder>,
    selectedOrderState: PharmacyUseCaseData.OrderState,
    onPrescriptionSelectionChanged: (prescriptionInOrder: PrescriptionInOrder, select: Boolean) -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Screen),
        topBarTitle = topBarTitle,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack,
        bottomBar = {
            NextButton(
                enabled = when {
                    isOrderOverviewMode -> true
                    else -> selectedOrderState.prescriptionsInOrder.isNotEmpty()
                },
                onNext = onNext
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .semantics {
                    prescriptionIds = orderState.map { it.taskId }
                },
            state = listState
        ) {
            orderState.forEach { prescriptionInOrder ->
                item(key = "prescription-${prescriptionInOrder.taskId}") {
                    PrescriptionItem(
                        modifier = Modifier,
                        prescription = prescriptionInOrder,
                        checked = prescriptionInOrder in selectedOrderState.prescriptionsInOrder,
                        onCheckedChange = { isChanged ->
                            onPrescriptionSelectionChanged(prescriptionInOrder, isChanged)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrescriptionItem(
    modifier: Modifier,
    prescription: PrescriptionInOrder,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val formatter = rememberErpTimeFormatter()
    val prescriptionDateTime = remember(prescription) {
        formatter.date(prescription.timestamp)
    }
    ListItem(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .semantics {
                selected = checked
                prescriptionId = prescription.taskId
            },
        colors = GemListItemDefaults.gemListItemColors(),
        headlineContent = {
            Text(
                prescription.title
                    ?: "",
                style = AppTheme.typography.body1
            )
        },
        supportingContent = {
            Text(
                prescriptionDateTime,
                style = AppTheme.typography.body2l
            )
        },
        trailingContent = {
            if (checked) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    tint = AppTheme.colors.primary700
                )
            } else {
                Icon(
                    Icons.Rounded.RadioButtonUnchecked,
                    null,
                    tint = AppTheme.colors.neutral400
                )
            }
        }
    )
}

@Composable
private fun NextButton(
    enabled: Boolean,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = SizeDefaults.half
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(vertical = SizeDefaults.double)
        ) {
            PrimaryButtonLarge(
                enabled = enabled,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton),
                onClick = onNext
            ) {
                Text(stringResource(R.string.rx_selection_next))
            }
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionSelectionScreenPreview(
    @PreviewParameter(PrescriptionSelectionPreviewParameter::class) previewData: PrescriptionSelectionPreview
) {
    val mockListState = rememberLazyListState()

    PreviewAppTheme {
        PrescriptionSelectionScreenScaffold(
            topBarTitle = stringResource(R.string.pharmacy_order_select_prescriptions),
            listState = mockListState,
            onBack = {},
            onNext = {},
            selectedOrderState = previewData.selectedOrders,
            orderState = previewData.orders,
            isOrderOverviewMode = false,
            onPrescriptionSelectionChanged = { _, _ -> }
        )
    }
}
