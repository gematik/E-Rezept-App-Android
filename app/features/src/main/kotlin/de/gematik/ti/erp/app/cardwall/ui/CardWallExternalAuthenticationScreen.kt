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

package de.gematik.ti.erp.app.cardwall.ui

import android.app.Dialog
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberExternalAuthenticatorListController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallExternalAuthenticationScreenBannerSection
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallExternalAuthenticationScreenHeaderSection
import de.gematik.ti.erp.app.cardwall.ui.components.GematikErrorDialog
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData.Companion.isPkv
import de.gematik.ti.erp.app.mainscreen.ui.LoadingDialog
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

class CardWallExternalAuthenticationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val profileId by graphController.profileId.collectAsStateWithLifecycle()
        val intentHandler = LocalIntentHandler.current
        val dialog = LocalDialog.current
        var loadingDialog: Dialog? = remember { null }

        val listState = rememberLazyListState()

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
                            onConfirmRequest = { it.dismiss() },
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
                    onConfirmRequest = { it.dismiss() },
                    onDismissRequest = { it.dismiss() }
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
        ExternalAuthenticationListScreenScaffold(
            profileId = profileId,
            listState = listState,
            healthInsuranceAppIdps = healthInsuranceAppIdps,
            filterList = controller::filterList,
            unFilterList = controller::unFilterList,
            reloadHealthInsuranceAppList = controller::getHealthInsuranceAppList,
            startAuthorizationWithExternal = controller::startAuthorizationWithExternal,
            onClickBanner = {
                context.openSettingsAsNewActivity(
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS

                        else -> Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                    }
                )
            },
            onCancel = {
                graphController.reset()
                navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
            },
            onBack = navController::popBackStack

        )
    }
}

@Composable
private fun ExternalAuthenticationListScreenScaffold(
    profileId: ProfileIdentifier,
    listState: LazyListState,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    filterList: (String) -> Unit,
    unFilterList: () -> Unit,
    reloadHealthInsuranceAppList: () -> Unit,
    startAuthorizationWithExternal: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickBanner: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.cdw_fasttrack_title),
        onBack = onBack,
        listState = listState,
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        ExternalAuthenticationScreenContent(
            profileId = profileId,
            listState = listState,
            healthInsuranceAppIdps = healthInsuranceAppIdps,
            onClickBanner = onClickBanner,
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
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ExternalAuthenticationScreenContent(
    profileId: ProfileIdentifier,
    listState: LazyListState,
    onSearch: (String) -> Unit,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    onClickHealthInsuranceIdp: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickBanner: () -> Unit,
    onClickRetry: () -> Unit
) {
    val fastTrackClosedString = stringResource(R.string.gid_fast_track_closed_error)

    // focus maintenance
    val contentFocusRequester = remember { FocusRequester() }
    val emptyFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val focusContentEvent = ComposableEvent<Unit>()
    val focusEmptyEvent = ComposableEvent<Unit>()

    var showBanner by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf(TextFieldValue("")) }

    val snackbar = LocalSnackbar.current

    focusContentEvent.listen {
        coroutineScope.launch {
            contentFocusRequester.requestFocus()
            awaitFrame()
            keyboard?.show()
        }
    }

    focusEmptyEvent.listen {
        coroutineScope.launch {
            emptyFocusRequester.requestFocus()
            awaitFrame()
            keyboard?.show()
        }
    }

    UiStateMachine(
        state = healthInsuranceAppIdps,
        onLoading = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingDefaults.Medium)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(PaddingDefaults.Large),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(PaddingDefaults.XLarge)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        },
        onError = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingDefaults.Medium),
                state = listState
            ) {
                item {
                    ErrorScreen(
                        modifier = Modifier.fillParentMaxSize(),
                        onClickRetry = onClickRetry
                    )
                }
            }
        },
        onEmpty = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingDefaults.Medium),
                state = listState
            ) {
                focusEmptyEvent.trigger()
                CardWallExternalAuthenticationScreenBannerSection(
                    showBanner = showBanner,
                    onClickBanner = onClickBanner,
                    onClose = { showBanner = false }
                )
                CardWallExternalAuthenticationScreenHeaderSection(
                    modifier = Modifier.fillMaxWidth(),
                    searchValue = search,
                    focusRequester = emptyFocusRequester,
                    onValueChange = {
                        onSearch(it.text)
                        search = it.copy(selection = TextRange(it.text.length))
                    }
                )
            }
        },
        onContent = { healthInsuranceAppIdpValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingDefaults.Medium),
                state = listState,
                contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
            ) {
                focusContentEvent.trigger()
                CardWallExternalAuthenticationScreenBannerSection(
                    showBanner = showBanner,
                    onClickBanner = onClickBanner,
                    onClose = { showBanner = false }
                )
                CardWallExternalAuthenticationScreenHeaderSection(
                    modifier = Modifier.fillMaxWidth(),
                    searchValue = search,
                    focusRequester = contentFocusRequester,
                    onValueChange = {
                        onSearch(it.text)
                        search = it.copy(selection = TextRange(it.text.length))
                    }
                )
                items(healthInsuranceAppIdpValues) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (it.isGid) {
                                onClickHealthInsuranceIdp(profileId, it)
                            } else {
                                snackbar.show(fastTrackClosedString)
                            }
                        }
                    ) {
                        Text(text = it.name)
                    }
                }
            }
        }
    )
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
