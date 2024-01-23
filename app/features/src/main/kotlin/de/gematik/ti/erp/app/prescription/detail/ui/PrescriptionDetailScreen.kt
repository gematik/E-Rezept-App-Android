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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackPrescriptionDetailPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import kotlinx.coroutines.launch

const val MissingValue = "---"

@OptIn(ExperimentalMaterialApi::class)
class PrescriptionDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId = remember {
            requireNotNull(
                navBackStackEntry.arguments?.getString(PrescriptionDetailRoutes.TaskId)
            )
        }
        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId)
        val prescription by prescriptionDetailsController.prescriptionState
        val activeProfile by prescriptionDetailsController.activeProfileState
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
            activeProfile?.let {
                PrescriptionDetailScreenScaffold(
                    activeProfile = it,
                    prescription = prescription,
                    scaffoldState = scaffoldState,
                    listState = listState,
                    // TODO: Hoist it out
                    prescriptionDetailController = prescriptionDetailsController,
                    // TODO: Hoist it out
                    navController = navController,
                    onClickMedication = { medication ->
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.path(
                                taskId = taskId,
                                selectedMedication = medication.toNavigationString()
                            )
                        )
                    },
                    onChangeSheetContent = {
                        infoBottomSheetContent = it
                        coroutineScope.launch {
                            sheetState.show()
                        }
                    },
                    onGrantConsent = {
                        prescriptionDetailsController.grantConsent()
                    },
                    onBack = navController::popBackStack
                )
            }
        }
    }
}
