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

package de.gematik.ti.erp.app.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import de.gematik.ti.erp.app.appauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.core.LocalNavController
import de.gematik.ti.erp.app.database.datastore.featuretoggle.COMM_RES_V3
import de.gematik.ti.erp.app.mainscreen.navigation.NavigationGraph
import de.gematik.ti.erp.app.mainscreen.navigation.mainScreenBottomNavigationItems
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenBottomBar
import de.gematik.ti.erp.app.mainscreen.ui.OrderStateChangeOnSuccessSideEffect
import de.gematik.ti.erp.app.padding.ApplicationInnerPadding
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalUiScopeScaffold
import org.kodein.di.compose.rememberInstance

@Composable
fun ApplicationScaffold(
    authentication: AuthenticationModeAndMethod?,
    isDemoMode: Boolean
) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val appController = rememberAppController()
    val isFeatureToggleEnabledUseCase by rememberInstance<IsFeatureToggleEnabledUseCase>()

    val refreshState by appController.refreshState.collectAsStateWithLifecycle()
    val isCommResV3Enabled by isFeatureToggleEnabledUseCase(COMM_RES_V3).collectAsStateWithLifecycle(initialValue = false)

    val currentRoute by navController.currentBackStackEntryAsState()
    val activeProfile by appController.activeProfile.collectAsStateWithLifecycle()
    val orderEventState by appController.orderedEvent.collectAsStateWithLifecycle()
    val isNetworkConnected by appController.isNetworkConnected.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val unreadOrdersCount by appController.unreadOrders.collectAsStateWithLifecycle()

    LaunchedEffect(orderEventState) {
        OrderStateChangeOnSuccessSideEffect(
            context = context,
            scope = scope,
            snackbar = snackbarHostState,
            orderedEvent = orderEventState,
            resetOrdered = appController::resetOrderedEvent
        )
    }

    val bottomRoutes = remember(isCommResV3Enabled) {
        mainScreenBottomNavigationItems(isCommResV3Enabled).map { it.route }
    }

    val isBottomSheetScreen = remember(currentRoute, bottomRoutes) {
        currentRoute?.let {
            bottomRoutes.contains(it.destination.route)
        } ?: false
    }

    CompositionLocalProvider(
        LocalSnackbarScaffold provides snackbarHostState,
        LocalUiScopeScaffold provides scope
    ) {
        val snackbar = LocalSnackbarScaffold.current
        val layoutDirection = LocalLayoutDirection.current
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.systemBarsPadding()) },
            content = { innerPadding ->
                Column {
                    NetworkBanner(isNetworkConnected)
                    NavigationGraph(
                        authentication = authentication,
                        isDemoMode = isDemoMode,
                        digaPromptFeedback = appController.promptFeedback,
                        // needed for fab for screens which have scaffolds
                        padding = ApplicationInnerPadding(layoutDirection, innerPadding),
                        onDigaNavigationActivated = { appController.markNavigationTriggerConsumed() }
                    )
                }
            },
            bottomBar = {
                if (isBottomSheetScreen) {
                    MainScreenBottomBar(
                        mainNavController = navController,
                        unreadOrdersCount = unreadOrdersCount,
                        isCommResV3Enabled = isCommResV3Enabled
                    )
                }
            }
        )
    }
}
