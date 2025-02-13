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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.prescriptionIds
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.ui.preview.PrescriptionSelectionPreview
import de.gematik.ti.erp.app.redeem.ui.preview.PrescriptionSelectionPreviewParameter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils.dateFormatter
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

class PrescriptionSelectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val controller: OnlineRedeemGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val snackbar = LocalSnackbar.current
        val isOrderOverviewMode = navBackStackEntry.arguments?.getBoolean(RedeemRoutes.REDEEM_NAV_MODAL_BEHAVIOUR) ?: false
        val selectedOrderState by controller.selectedOrderState()
        val orderState by controller.redeemableOrderState()
        val emptyOrdersCheckEvent = ComposableEvent<Unit>()

        val listState = rememberLazyListState()
        val snackbarText = stringResource(R.string.pharmacy_order_no_selected_prescriptions_desc)

        emptyOrdersCheckEvent.listen {
            if (selectedOrderState.prescriptionOrders.isEmpty()) {
                snackbar.show(snackbarText)
            }
        }

        val onBack: () -> Unit = {
            when {
                !isOrderOverviewMode -> {
                    controller.onResetPrescriptionSelection()
                    navController.popBackStack()
                }

                isOrderOverviewMode && selectedOrderState.prescriptionOrders.isEmpty() -> {
                    snackbar.show(snackbarText)
                }

                else -> {
                    navController.popBackStack()
                }
            }
        }

        BackHandler {
            onBack()
        }

        PrescriptionSelectionScreenScaffold(
            topBarTitle = stringResource(R.string.pharmacy_order_select_prescriptions),
            listState = listState,
            onBack = onBack
        ) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Content)
                        .semantics {
                            prescriptionIds = orderState.map { it.taskId }
                        },
                    state = listState
                ) {
                    orderState.forEach { prescriptionOrder ->
                        item(key = "prescription-${prescriptionOrder.taskId}") {
                            PrescriptionItem(
                                modifier = Modifier,
                                prescription = prescriptionOrder,
                                checked = prescriptionOrder in selectedOrderState.prescriptionOrders,
                                onCheckedChange = { isChanged ->
                                    controller.onPrescriptionSelectionChanged(prescriptionOrder, isChanged)
                                    emptyOrdersCheckEvent.trigger()
                                }
                            )
                        }
                    }
                }
                NextButton(
                    enabled = selectedOrderState.prescriptionOrders.isNotEmpty(),
                    onNext = {
                        when {
                            isOrderOverviewMode -> navController.popBackStack()
                            else -> navController.navigate(
                                PharmacyRoutes.PharmacyStartScreenModal.path(taskId = "")
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PrescriptionSelectionScreenScaffold(
    topBarTitle: String,
    listState: LazyListState,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Screen),
        topBarTitle = topBarTitle,
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) {
        content()
    }
}

@Composable
private fun PrescriptionItem(
    modifier: Modifier,
    prescription: PharmacyUseCaseData.PrescriptionOrder,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(PaddingDefaults.Medium)
            .semantics {
                selected = checked
                prescriptionId = prescription.taskId
            }
    ) {
        val prescriptionDateTime = remember(prescription) {
            dateFormatter.format(
                prescription.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                prescription.title
                    ?: "",
                style = AppTheme.typography.body1
            )
            Text(
                prescriptionDateTime,
                style = AppTheme.typography.body2l
            )
        }
        SpacerMedium()
        Box(
            contentAlignment = Alignment.Center
        ) {
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
    }
}

@Composable
private fun NextButton(
    enabled: Boolean,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            SpacerMedium()
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
    val selectedOrders = remember {
        mutableStateListOf<PharmacyUseCaseData.PrescriptionOrder>().apply {
            addAll(previewData.selectedOrders)
        }
    }

    val onCheckedChange = { order: PharmacyUseCaseData.PrescriptionOrder, isChecked: Boolean ->
        if (isChecked) {
            selectedOrders.add(order)
        } else {
            selectedOrders.remove(order)
        }
    }

    PreviewAppTheme {
        PrescriptionSelectionScreenScaffold(
            topBarTitle = stringResource(R.string.pharmacy_order_select_prescriptions),
            listState = mockListState,
            onBack = { }
        ) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                    state = mockListState
                ) {
                    previewData.orders.forEach { prescriptionOrder ->
                        item {
                            PrescriptionItem(
                                modifier = Modifier,
                                prescription = prescriptionOrder,
                                checked = prescriptionOrder in selectedOrders,
                                onCheckedChange = { isChecked ->
                                    onCheckedChange(prescriptionOrder, isChecked)
                                }
                            )
                        }
                    }
                }
                NextButton(
                    enabled = selectedOrders.isNotEmpty(),
                    onNext = {}
                )
            }
        }
    }
}
