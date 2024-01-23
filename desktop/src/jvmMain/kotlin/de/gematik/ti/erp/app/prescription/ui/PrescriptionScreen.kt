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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.Dialog
import de.gematik.ti.erp.app.common.HorizontalDivider
import de.gematik.ti.erp.app.common.HorizontalSplittable
import de.gematik.ti.erp.app.common.SpacerSmall
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.main.ui.MainNavigation
import de.gematik.ti.erp.app.navigation.ui.Navigation
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.rememberScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.bind
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun PrescriptionScreen(
    navigation: Navigation
) {
    val scope = rememberScope()

    subDI(diBuilder = {
        bind { scoped(scope).singleton { PrescriptionViewModel(instance(), instance()) } }
    }) {
        val prescriptionViewModel by rememberInstance<PrescriptionViewModel>()
        val state by produceState(prescriptionViewModel.defaultState) {
            prescriptionViewModel.screenState().collect {
                value = it
            }
        }

        DeleteAlertDialog(prescriptionViewModel)

        val coScope = rememberCoroutineScope()
        val selectedPrescription = state.selectedPrescription

        LaunchedEffect(navigation.currentBackStackEntry) {
            when (navigation.currentBackStackEntry) {
                is MainNavigation.PrescriptionsUnredeemed ->
                    coScope.launch { prescriptionViewModel.onSelectNotDispensed() }
                is MainNavigation.PrescriptionsRedeemed ->
                    coScope.launch { prescriptionViewModel.onSelectDispensed() }
            }
        }

        if (state.prescriptions.isNotEmpty() && selectedPrescription != null) {
            HorizontalSplittable(
                split = 0.3f,
                contentLeft = {
                    PrescriptionList(
                        state.prescriptions,
                        selectedPrescription = selectedPrescription.prescription,
                        onClickPrescription = {
                            coScope.launch {
                                prescriptionViewModel.onSelectPrescription(
                                    it
                                )
                            }
                        }
                    )
                },
                contentRight = {
                    PrescriptionDetailsScreen(
                        navigation = navigation,
                        prescription = selectedPrescription,
                        audits = state.selectedPrescriptionAudits,
                        onClickDelete = {
                            prescriptionViewModel.deletePrescription(selectedPrescription.prescription)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DeleteAlertDialog(
    prescriptionViewModel: PrescriptionViewModel
) {
    var deleteState by remember { mutableStateOf<PrescriptionScreenData.DeleteState?>(null) }

    LaunchedEffect(Unit) {
        prescriptionViewModel.deleteState().collect {
            deleteState = it
        }
    }
    deleteState?.let {
        Dialog(
            title = it.error ?: "",
            confirmButton = {
                TextButton(onClick = {
                    deleteState = null
                }) {
                    Text(App.strings.cancel().uppercase(Locale.getDefault()))
                }
            },
            onDismissRequest = { deleteState = null }
        )
    }
}

@Composable
private fun PrescriptionList(
    prescriptions: List<PrescriptionUseCaseData.Prescription>,
    selectedPrescription: PrescriptionUseCaseData.Prescription,
    onClickPrescription: (PrescriptionUseCaseData.Prescription) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState
        ) {
            itemsIndexed(prescriptions, key = { _, it -> it }) { _, it ->
                Prescription(
                    modifier = Modifier.fillMaxWidth(),
                    name = it.name ?: App.strings.desktopPrescriptionNoData(),
                    selected = it.taskId == selectedPrescription.taskId,
                    expiresOnText = expiresOrAcceptedUntil(it),
                    prescribedOnText = App.strings.desktopPrescriptionPrescribedOn(
                        count = 1,
                        it.authoredOn.format(formatter)
                    ),
                    onClick = { onClickPrescription(it) }
                )
                HorizontalDivider()
            }
        }
        VerticalScrollbar(
            scrollbarAdapter,
            modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 1.dp).fillMaxHeight()
        )
    }
}

@Composable
fun expiresOrAcceptedUntil(
    prescription: PrescriptionUseCaseData.Prescription
): String {
    val now = remember { LocalDate.now().toEpochDay() }
    val expiryDaysLeft = prescription.expiresOn.toEpochDay() - now
    val acceptDaysLeft = prescription.acceptUntil.toEpochDay() - now

    return when {
        prescription.redeemedOn != null -> {
            val dtFormatter =
                remember(prescription) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
            prescription.redeemedOn.format(dtFormatter)
        }
        acceptDaysLeft >= 0 -> App.strings.desktopPrescriptionAcceptUntil(
            count = acceptDaysLeft.toInt(),
            acceptDaysLeft
        )
        expiryDaysLeft >= 0 -> App.strings.desktopPrescriptionExpiresOn(count = expiryDaysLeft.toInt(), expiryDaysLeft)
        else -> App.strings.desktopPrescriptionExpired()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Prescription(
    modifier: Modifier,
    name: String,
    selected: Boolean,
    expiresOnText: String,
    prescribedOnText: String,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(if (hovered) 1f else 0f)
    val color = if (selected) AppTheme.colors.primary100 else AppTheme.colors.neutral100.copy(alpha = alpha)

    Column(
        modifier
            .fillMaxSize()
            .background(color)
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .pointerMoveFilter(
                onEnter = {
                    hovered = true
                    false
                },
                onExit = {
                    hovered = false
                    false
                }
            )
    ) {
        Text(name, style = MaterialTheme.typography.subtitle1)
        Text(expiresOnText, style = AppTheme.typography.body2l)
        SpacerSmall()
        Text(prescribedOnText, style = AppTheme.typography.captionl)
    }
}
