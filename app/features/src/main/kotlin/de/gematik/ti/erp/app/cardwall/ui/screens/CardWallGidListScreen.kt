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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.app.Dialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberExternalAuthenticatorListController
import de.gematik.ti.erp.app.cardwall.ui.components.GematikErrorDialog
import de.gematik.ti.erp.app.cardwall.ui.components.GidItem
import de.gematik.ti.erp.app.cardwall.ui.components.GidScreenHeaderSection
import de.gematik.ti.erp.app.cardwall.ui.preview.HealthInsuranceDataPreviewParameterProvider
import de.gematik.ti.erp.app.column.ColumnItems
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData.Companion.isPkv
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LoadingDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.imeHeight
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import kotlinx.coroutines.launch

class CardWallGidListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val profileId by graphController.profileId.collectAsStateWithLifecycle()
        val intentHandler = LocalIntentHandler.current
        val dialog = LocalDialog.current
        val snackbar = LocalSnackbarScaffold.current
        val scope = rememberCoroutineScope()
        var loadingDialog: Dialog? = remember { null }

        val listState = rememberLazyListState()

        val fastTrackClosedString = stringResource(R.string.gid_fast_track_closed_error)

        val controller = rememberExternalAuthenticatorListController()
        val healthInsuranceAppIdps by controller.healthInsuranceDataList.collectAsStateWithLifecycle()
        val authorizationWithExternalAppEvent = controller.authorizationWithExternalAppInBackgroundEvent
        val redirectUriEvent = controller.redirectUriEvent
        val redirectErrorEvent = controller.redirectUriErrorEvent
        val redirectGematikErrorEvent = controller.redirectUriGematikErrorEvent

        LaunchedEffect(Unit) {
            controller.getHealthInsuranceAppList()
        }

        authorizationWithExternalAppEvent.listen { isStarted ->
            if (isStarted) {
                dialog.show {
                    loadingDialog = it
                    LoadingDialog { it.dismiss() }
                }
            } else {
                loadingDialog?.dismiss()
            }
        }

        redirectUriEvent.listen { (redirectUri, healthInsuranceData) ->
            intentHandler.tryStartingExternalHealthInsuranceAuthenticationApp(
                redirect = redirectUri,
                onSuccess = {
                    if (healthInsuranceData.isPkv()) {
                        controller.switchToPKV(profileId)
                    }
                    navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                },
                onFailure = {
                    dialog.show {
                        ErezeptAlertDialog(
                            title = stringResource(R.string.gid_external_app_missing_title),
                            body = stringResource(R.string.gid_external_app_missing_description),
                            okText = stringResource(R.string.ok),
                            onDismissRequest = { it.dismiss() }
                        )
                    }
                }
            )
        }

        redirectErrorEvent.listen {
            dialog.show {
                ErezeptAlertDialog(
                    title = stringResource(R.string.main_fasttrack_error_title),
                    body = stringResource(R.string.main_fasttrack_error_info),
                    okText = stringResource(R.string.ok),
                    onDismissRequest = it::dismiss
                )
            }
        }

        redirectGematikErrorEvent.listen { responseError ->
            dialog.show {
                GematikErrorDialog(error = responseError) {
                    it.dismiss()
                }
            }
        }

        BackHandler {
            navController.navigateUp()
        }
        GidListScreenScaffold(
            profileId = profileId,
            listState = listState,
            healthInsuranceAppIdps = healthInsuranceAppIdps,
            filterList = controller::filterList,
            unFilterList = controller::unFilterList,
            reloadHealthInsuranceAppList = controller::getHealthInsuranceAppList,
            startAuthorizationWithExternal = controller::startAuthorizationWithExternal,
            onClickHealthInsuranceWithGidNotSupported = {
                scope.launch {
                    snackbar.showSnackbar(fastTrackClosedString)
                }
            },
            onCancel = {
                graphController.reset()
                navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
            },
            onBack = navController::navigateUp,
            onNavigateToHelp = {
                navController.navigate(CardWallRoutes.CardWallGidHelpScreen.route)
            }
        )
    }
}

@Composable
private fun GidListScreenScaffold(
    profileId: ProfileIdentifier,
    listState: LazyListState,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    filterList: (String) -> Unit,
    unFilterList: () -> Unit,
    reloadHealthInsuranceAppList: () -> Unit,
    startAuthorizationWithExternal: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickHealthInsuranceWithGidNotSupported: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.cardwall_gid_title),
        onBack = onBack,
        listState = listState,
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        GidListScreenContent(
            profileId = profileId,
            healthInsuranceAppIdps = healthInsuranceAppIdps,
            onClickHealthInsuranceWithGidNotSupported = onClickHealthInsuranceWithGidNotSupported,
            onSearch = { searchWord ->
                if (searchWord.isNotEmpty()) {
                    filterList(searchWord)
                } else {
                    unFilterList()
                }
            },
            onClickRetry = {
                reloadHealthInsuranceAppList()
            },
            onClickHealthInsuranceIdp = { profileId, heathInsuranceIdp ->
                startAuthorizationWithExternal(
                    profileId,
                    heathInsuranceIdp
                )
            },
            onNavigateToHelp = onNavigateToHelp
        )
    }
}

@Composable
fun GidListScreenContent(
    profileId: ProfileIdentifier,
    onSearch: (String) -> Unit,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    @Requirement(
        "O.Auth_4#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "The user selects the insurance company and the authentication process starts"
    )
    onClickHealthInsuranceIdp: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickHealthInsuranceWithGidNotSupported: () -> Unit,
    onClickRetry: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    var search by remember { mutableStateOf(TextFieldValue("")) }

    val columnArrangement by remember(healthInsuranceAppIdps) {
        derivedStateOf {
            when {
                healthInsuranceAppIdps.isLoadingState || healthInsuranceAppIdps.isErrorState -> Arrangement.Center
                else -> Arrangement.Top
            }
        }
    }

    // column in a column to make the sticky header behaviour like a lazy column
    Column {
        GidScreenHeaderSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium),
            searchValue = search,
            focusRequester = focusRequester,
            onValueChange = {
                onSearch(it.text)
                search = it.copy(selection = TextRange(it.text.length))
            },
            onNavigateToHelp = onNavigateToHelp
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = PaddingDefaults.Medium),
            verticalArrangement = columnArrangement,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UiStateMachine(
                state = healthInsuranceAppIdps,
                onLoading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(PaddingDefaults.Large),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(PaddingDefaults.XLarge)
                                .align(Alignment.Center)
                        )
                    }
                },
                onError = {
                    ErrorScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClickRetry = onClickRetry
                    )
                }
            ) {
                ColumnItems(
                    items = it,
                    itemContent = { _, healthInsuranceAppData ->
                        GidItem(
                            profileId = profileId,
                            healthInsuranceData = healthInsuranceAppData,
                            onClickHealthInsuranceIdp = onClickHealthInsuranceIdp
                        ) {
                            onClickHealthInsuranceWithGidNotSupported()
                        }
                    },
                    lastItemExtraContent = { _, _ ->
                        Spacer(
                            modifier = Modifier
                                .size(PaddingDefaults.XXXLarge)
                                .imeHeight()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) =
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.align(BiasAlignment(horizontalBias = 0f, verticalBias = -0.33f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Text(
                stringResource(R.string.cdw_fasttrack_error_title),
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.cdw_fasttrack_error_info),
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onClickRetry
            ) {
                Icon(Icons.Rounded.Refresh, null)
                SpacerSmall()
                Text(stringResource(R.string.cdw_fasttrack_try_again))
            }
        }
    }

@LightDarkPreview
@Composable
fun GidListScreenScaffoldPreview(
    @PreviewParameter(HealthInsuranceDataPreviewParameterProvider::class) healthInsuranceAppIds:
        UiState<List<HealthInsuranceData>>
) {
    PreviewAppTheme {
        GidListScreenContent(
            profileId = "profile-id",
            healthInsuranceAppIdps = healthInsuranceAppIds,
            onNavigateToHelp = {},
            onClickHealthInsuranceWithGidNotSupported = {},
            onClickHealthInsuranceIdp = { _, _ -> },
            onClickRetry = {},
            onSearch = {}
        )
    }
}
