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

package de.gematik.ti.erp.app.eurezept.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.EuConsentScreenController
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuConsentScreenController
import de.gematik.ti.erp.app.eurezept.ui.component.EuConsentBottomBar
import de.gematik.ti.erp.app.eurezept.ui.component.EuConsentScreenContent
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentNavigationEvent
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentScreenActions
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentViewState
import de.gematik.ti.erp.app.eurezept.ui.preview.EuConsentPreviewParameter
import de.gematik.ti.erp.app.eurezept.ui.preview.EuConsentPreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState

class EuConsentScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberEuConsentScreenController()
        val consentViewState by controller.consentViewState.collectAsStateWithLifecycle()

        val lazyListState = rememberLazyListState()
        val snackbar = remember { SnackbarHostState() }
        val taskId = navBackStackEntry.arguments?.getString(EuRoutes.EU_NAV_TASK_ID)

        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }

        ChooseAuthenticationNavigationEventsListener(
            controller = controller,
            navController = navController,
            dialogScaffold = LocalDialog.current
        )

        LaunchedEffect(Unit) {
            controller.navigationEvents.collect { event ->
                when (event) {
                    EuConsentNavigationEvent.NavigateToRedeem -> {
                        val route = if (taskId.isNullOrEmpty()) {
                            EuRoutes.EuRedeemScreen.path()
                        } else {
                            EuRoutes.EuRedeemScreen.path(taskId)
                        }
                        navController.navigate(route) {
                            popUpTo(EuRoutes.EuConsentScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    EuConsentNavigationEvent.NavigateBack -> onBack()
                    EuConsentNavigationEvent.CancelFlow -> {
                        navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                            popUpTo(EuRoutes.subGraphName()) { inclusive = true }
                        }
                    }
                }
            }
        }

        LaunchedEffect(consentViewState) {
            consentViewState.takeIf { it.isDataState }?.data?.grantConsentError?.message
                ?.let { snackbar.showSnackbar(it) }
        }

        val actions = createConsentActions(controller)

        EuConsentScaffold(
            lazyListState = lazyListState,
            snackbarHostState = snackbar,
            consentViewState = consentViewState,
            actions = actions
        )
    }
}

@Composable
private fun createConsentActions(
    controller: EuConsentScreenController
) = remember(controller) {
    EuConsentScreenActions(
        onBack = { controller.onBackPressed() },
        onCancel = { controller.onCancelFlow() },
        onAcceptEuConsent = { controller.onConsentAccepted() },
        onDeclineEuConsent = { controller.onDeclineConsent() },
        onRetry = { controller.retryLoadingConsent() }
    )
}

@Composable
private fun EuConsentScaffold(
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    consentViewState: UiState<EuConsentViewState>,
    actions: EuConsentScreenActions,
    modifier: Modifier = Modifier
) {
    EuRedeemScaffold(
        modifier = modifier.navigationBarsPadding(),
        listState = lazyListState,
        snackbarHostState = snackbarHostState,
        onBack = actions.onBack,
        onCancel = actions.onCancel,
        topBarTitle = "",
        bottomBar = {
            val data = consentViewState.data
            if (data != null && !data.consentData.isActive()) {
                EuConsentBottomBar(
                    isGrantingConsent = data.isGrantingConsent,
                    onAccept = actions.onAcceptEuConsent,
                    onDecline = actions.onDeclineEuConsent
                )
            }
        }
    ) { paddingValues ->
        UiStateMachine(
            state = consentViewState,
            onLoading = {
                Center {
                    FullScreenLoadingIndicator()
                }
            },
            onError = { error ->
                ErrorScreenComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    titleText = stringResource(R.string.eu_consent_error_title),
                    bodyText = error.message ?: stringResource(R.string.eu_consent_error_generic),
                    tryAgainText = stringResource(R.string.eu_consent_retry_button),
                    onClickRetry = actions.onRetry
                )
            },
            onContent = {
                EuConsentScreenContent(
                    paddingValues = paddingValues,
                    listState = lazyListState
                )
            }
        )
    }
}

@LightDarkPreview
@Composable
fun EuConsentScreenScaffoldPreview(
    @PreviewParameter(EuConsentPreviewParameterProvider::class)
    previewData: EuConsentPreviewParameter
) {
    val lazyListState = rememberLazyListState()
    val actions = EuConsentScreenActions(
        onBack = {},
        onCancel = {},
        onAcceptEuConsent = {},
        onDeclineEuConsent = {},
        onRetry = {}
    )

    PreviewTheme {
        EuConsentScaffold(
            lazyListState = lazyListState,
            snackbarHostState = remember { SnackbarHostState() },
            consentViewState = previewData.uiState,
            actions = actions
        )
    }
}
