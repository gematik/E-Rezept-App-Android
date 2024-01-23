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

package de.gematik.ti.erp.app.orders.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackOrderPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orders.presentation.rememberMessageController
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrderOverviewScreen(
    orderId: String,
    mainNavController: NavController
) {
    val listState = rememberLazyListState()

    val messageController = rememberMessageController(orderId = orderId)

    val order by messageController.order

    val activeProfile by messageController.activeProfileState

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            analytics.trackOrderPopUps()
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(mainNavController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }

    val scope = rememberCoroutineScope()
    var selectedMessage: OrderUseCaseData.Message? by remember { mutableStateOf(null) }
// Todo: It should not be inside bottomsheet
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            MessageSheetContent(
                order = order,
                message = selectedMessage,
                onClickClose = { scope.launch { sheetState.hide() } }
            )
        },
        sheetShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) }
    ) {
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
            topBarTitle = stringResource(R.string.orders_details_title),
            listState = listState,
            navigationMode = NavigationBarMode.Back,
            onBack = {
                scope.launch(Dispatchers.Main) {
                    messageController.consumeAllMessages()
                    mainNavController.popBackStack()
                }
            }
        ) {
            Messages(
                listState = listState,
                messageController = messageController,
                onClickMessage = {
                    selectedMessage = it
                    scope.launch { sheetState.show() }
                },
                onClickPrescription = {
                    mainNavController.navigate(
                        PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId = it)
                    )
                },
                onClickInvoiceMessage = { taskId ->
                    activeProfile?.let { profile ->
                        mainNavController.navigate(
                            PkvRoutes.InvoiceDetailsScreen
                                .path(
                                    taskId = taskId,
                                    profileId = profile.id
                                )
                        )
                    }
                }
            )
        }
    }

    BackHandler {
        scope.launch {
            messageController.consumeAllMessages()
            mainNavController.popBackStack()
        }
    }
}
