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

package de.gematik.ti.erp.app.redeem.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyNavigation
import de.gematik.ti.erp.app.pharmacy.ui.PrescriptionSelection
import de.gematik.ti.erp.app.pharmacy.ui.rememberPharmacyOrderState
import de.gematik.ti.erp.app.redeem.ui.model.RedeemNavigation
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState

@Composable
fun RedeemNavigation(
    onFinish: () -> Unit
) {
    val orderState = rememberPharmacyOrderState()

    val navController = rememberNavController()
    val navigationMode by navController.navigationModeState(RedeemNavigation.MethodSelection.route)

    var previousNavEntry by remember { mutableStateOf("redeem_methodSelection") }
    TrackNavigationChanges(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })

    NavHost(
        navController,
        startDestination = RedeemNavigation.MethodSelection.route
    ) {
        composable(RedeemNavigation.MethodSelection.route) {
            NavigationAnimation(mode = navigationMode) {
                val prescriptions by orderState.prescriptions
                HowToRedeem(
                    onClickLocalRedeem = {
                        navController.navigate(RedeemNavigation.LocalRedeem.path())
                    },
                    onClickOnlineRedeem = {
                        if (prescriptions.size > 1) {
                            navController.navigate(RedeemNavigation.OnlineRedeem.path())
                        } else {
                            navController.navigate(RedeemNavigation.PharmacySearch.path())
                        }
                    },
                    onCancel = onFinish
                )
            }
        }
        composable(RedeemNavigation.LocalRedeem.route) {
            NavigationAnimation(mode = navigationMode) {
                LocalRedeemScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onFinished = onFinish,
                    orderState = orderState
                )
            }
        }
        composable(RedeemNavigation.OnlineRedeem.route) {
            NavigationAnimation(mode = navigationMode) {
                OnlineRedeemScreen(
                    orderState = orderState,
                    onClickSelectPrescriptions = {
                        orderState.onResetPrescriptionSelection()
                        navController.navigate(RedeemNavigation.PrescriptionSelection.path())
                    },
                    onClickAllPrescriptions = {
                        orderState.onResetPrescriptionSelection()
                        navController.navigate(RedeemNavigation.PharmacySearch.path())
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(RedeemNavigation.PrescriptionSelection.route) {
            NavigationAnimation(mode = navigationMode) {
                PrescriptionSelection(
                    orderState = orderState,
                    onFinishSelection = {
                        navController.navigate(RedeemNavigation.PharmacySearch.path())
                    },
                    showNextButton = true,
                    backIsFinish = false,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(RedeemNavigation.PharmacySearch.route) {
            PharmacyNavigation(
                isNestedNavigation = true,
                orderState = orderState,
                onBack = {
                    navController.popBackStack()
                },
                onFinish = onFinish
            )
        }
    }
}
