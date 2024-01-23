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

@file:OptIn(ExperimentalMaterialApi::class)

package de.gematik.ti.erp.app.prescription.detail.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackNavigationChangesAsync
import de.gematik.ti.erp.app.analytics.trackPrescriptionDetailPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentChip
import de.gematik.ti.erp.app.prescription.ui.FailureDetailsStatusChip
import de.gematik.ti.erp.app.prescription.ui.PrescriptionStateInfo
import de.gematik.ti.erp.app.prescription.ui.SubstitutionAllowedChip
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val MissingValue = "---"

@Composable
fun PrescriptionDetailsScreen(
    taskId: String,
    mainNavController: NavController
) {
    val prescriptionDetailsController = rememberPrescriptionDetailsController(taskId)

    var selectedMedication: PrescriptionData.Medication? by remember { mutableStateOf(null) }
    var selectedIngredient: SyncedTaskData.Ingredient? by remember { mutableStateOf(null) }

    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onBack: () -> Unit = {
        mainScope.launch {
            mainNavController.popBackStack() // TODO onBack instead of NavController
        }
    }
    val navController = rememberNavController()
    var previousNavEntry by remember { mutableStateOf("prescriptionDetail") }

    trackNavigationChangesAsync(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })

    NavHost(
        navController = navController,
        startDestination = PrescriptionDetailsNavigationScreens.Overview.route
    ) {
        composable(PrescriptionDetailsNavigationScreens.Overview.route) {
            PrescriptionDetailsWithScaffold(
                prescriptionDetailsController = prescriptionDetailsController,
                navController = navController,
                onClickMedication = {
                    selectedMedication = it
                    navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                },
                onBack = onBack
            )
        }
        composable(PrescriptionDetailsNavigationScreens.MedicationOverview.route) {
            MedicationOverviewScreen(
                prescriptionDetailsController = prescriptionDetailsController,
                onClickMedication = {
                    selectedMedication = it
                    navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                },
                onBack = onBack
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Medication.route) {
            SyncedMedicationDetailScreen(
                prescriptionDetailsController = prescriptionDetailsController,
                medication = requireNotNull(selectedMedication),
                onClickIngredient = {
                    selectedIngredient = it
                    navController.navigate(PrescriptionDetailsNavigationScreens.Ingredient.path())
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Ingredient.route) {
            IngredientScreen(
                ingredient = requireNotNull(selectedIngredient),
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Patient.route) {
            PatientScreen(
                prescriptionDetailsController = prescriptionDetailsController,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Prescriber.route) {
            PrescriberScreen(
                prescriptionDetailsController = prescriptionDetailsController,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Accident.route) {
            AccidentInformation(
                prescriptionDetailsController = prescriptionDetailsController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.Organization.route) {
            OrganizationScreen(
                prescriptionDetailsController = prescriptionDetailsController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.TechnicalInformation.route) {
            TechnicalInformation(
                taskId = taskId,
                prescriptionDetailsController = prescriptionDetailsController,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PrescriptionDetailsWithScaffold(
    prescriptionDetailsController: PrescriptionDetailsController,
    navController: NavHostController,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onBack: () -> Unit
) {
    val prescription by prescriptionDetailsController.prescriptionState

    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )

    val coroutineScope = rememberCoroutineScope()
    var infoBottomSheetContent: PrescriptionDetailBottomSheetContent? by remember { mutableStateOf(null) }

    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            infoBottomSheetContent?.let { analytics.trackPrescriptionDetailPopUps(it) }
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(navController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }
    LaunchedEffect(infoBottomSheetContent) {
        if (infoBottomSheetContent != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    ModalBottomSheetLayout(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Screen),
        sheetState = sheetState,
        sheetContent = {
            Box(
                Modifier
                    .heightIn(min = 56.dp)
                    .navigationBarsPadding()
            ) {
                infoBottomSheetContent?.let {
                    PrescriptionDetailInfoSheetContent(infoContent = it)
                }
            }
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        PrescriptionDetailsScaffold(
            prescription = prescription,
            scaffoldState = scaffoldState,
            listState = listState,
            prescriptionDetailsController = prescriptionDetailsController,
            navController = navController,
            onClickMedication = onClickMedication,
            onChangeSheetContent = {
                infoBottomSheetContent = it
                coroutineScope.launch {
                    sheetState.show()
                }
            },
            onBack = onBack
        )
    }
}

@Composable
fun SyncedHeader(
    prescription: PrescriptionData.Synced,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            prescription.name ?: stringResource(R.string.prescription_medication_default_name),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        when {
            prescription.isIncomplete -> {
                SpacerShortMedium()
                FailureDetailsStatusChip(
                    onClick = {
                        onShowInfo(PrescriptionDetailBottomSheetContent.Failure())
                    }
                )
            }

            prescription.isDirectAssignment -> {
                SpacerShortMedium()
                DirectAssignmentChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.DirectAssignment()
                        )
                    }
                )
            }

            prescription.isSubstitutionAllowed -> {
                SpacerShortMedium()
                SubstitutionAllowedChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.SubstitutionAllowed()
                        )
                    }
                )
            }
        }

        SpacerShortMedium()

        val onClick = when {
            !prescription.isDirectAssignment &&
                (
                    prescription.state is SyncedTaskData.SyncedTask.Ready ||
                        prescription.state is SyncedTaskData.SyncedTask.LaterRedeemable
                    ) -> {
                {
                    onShowInfo(
                        PrescriptionDetailBottomSheetContent.HowLongValid(
                            prescription
                        )
                    )
                }
            }

            else -> null
        }
        SyncedStatus(
            prescription = prescription,
            onClick = onClick
        )
        SpacerXLarge()
    }
}

@Composable
fun SyncedStatus(
    modifier: Modifier = Modifier,
    prescription: PrescriptionData.Synced,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = PaddingDefaults.Tiny)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prescription.isDirectAssignment) {
            val text = if (prescription.isDispensed) {
                stringResource(R.string.pres_details_direct_assignment_received_state)
            } else {
                stringResource(R.string.pres_details_direct_assignment_state)
            }
            Text(
                text,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
        } else {
            PrescriptionStateInfo(prescription.state, textAlign = TextAlign.Center)
        }
        if (onClick != null) {
            Spacer(Modifier.padding(2.dp))
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.primary600
            )
        }
    }
}
