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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData.Companion.isPkv
import de.gematik.ti.erp.app.mainscreen.ui.LoadingDialog
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.uistate.UiState

@Composable
fun ExternalAuthenticatorListScreen(
    profileId: ProfileIdentifier,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    val dialog = LocalDialog.current

    var loadingDialog: Dialog? = remember { null }

    val intentHandler = LocalIntentHandler.current

    val listState = rememberLazyListState()

    val controller = rememberExternalAuthenticatorListController()

    val healthInsuranceAppIdps by controller
        .healthInsuranceDataList.collectAsStateWithLifecycle()

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
                onNext()
            },
            onFailure = {
                dialog.show {
                    AcceptDialog(
                        header = stringResource(R.string.gid_external_app_missing_title),
                        info = stringResource(R.string.gid_external_app_missing_description),
                        acceptText = stringResource(R.string.ok)
                    ) {
                        it.dismiss()
                    }
                }
            }
        )
    }

    redirectErrorEvent.listen {
        dialog.show {
            AcceptDialog(
                header = stringResource(R.string.main_fasttrack_error_title),
                info = stringResource(R.string.main_fasttrack_error_info),
                acceptText = stringResource(R.string.ok)
            ) {
                it.dismiss()
            }
        }
    }

    redirectGematikErrorEvent.listen { responseError ->
        dialog.show {
            GematikErrorDialog(error = responseError) {
                it.dismiss()
            }
        }
    }

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.cdw_fasttrack_title),
        onBack = onBack,
        listState = listState,
        actions = @Composable {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        AuthenticatorList(
            profileId = profileId,
            listState = listState,
            healthInsuranceAppIdps = healthInsuranceAppIdps,
            onSearch = { searchWord ->
                if (searchWord.isNotEmpty()) {
                    controller.filterList(searchWord)
                } else {
                    controller.unFilterList()
                }
            },
            onClickRetry = {
                controller.getHealthInsuranceAppList()
            },
            onClickHealthInsuranceIdp = { profileId, heathInsuranceIdp ->
                controller
                    .startAuthorizationWithExternal(
                        profileId = profileId,
                        healthInsuranceData = heathInsuranceIdp
                    )
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AuthenticatorList(
    profileId: ProfileIdentifier,
    listState: LazyListState,
    onSearch: (String) -> Unit,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    onClickHealthInsuranceIdp: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickRetry: () -> Unit
) {
    val fastTrackClosedString = stringResource(R.string.gid_fast_track_closed_error)
    var search by remember { mutableStateOf("") }
    val snackbar = LocalSnackbar.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        Text(stringResource(R.string.cdw_fasttrack_choose_insurance), style = MaterialTheme.typography.h6)
        SpacerSmall()
        Text(
            stringResource(R.string.cdw_fasttrack_help_info),
            style = AppTheme.typography.body2l
        )
        UiStateMachine(
            state = healthInsuranceAppIdps,
            onLoading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingDefaults.Medium)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(PaddingDefaults.XLarge)
                            .align(Alignment.Center)
                    )
                }
            },
            onError = {
                ErrorScreen(onClickRetry = onClickRetry)
            },
            onEmpty = {
                ListSearchField(
                    searchValue = search,
                    onValueChange = {
                        onSearch(it)
                        search = it
                    }
                )
            },
            onContent = { healthInsuranceAppIdps ->
                ListSearchField(
                    searchValue = search,
                    onValueChange = {
                        onSearch(it)
                        search = it
                    }
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                ) {
                    items(healthInsuranceAppIdps) {
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
                            Row {
                                Text(
                                    text = it.name,
                                    modifier = Modifier
                                        .padding(PaddingDefaults.Medium)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

// Needs ColumnScope
@Composable
private fun ListSearchField(
    searchValue: String,
    onValueChange: (String) -> Unit
) {
    SpacerLarge()
    SearchField(
        value = searchValue,
        onValueChange = {
            onValueChange(it)
        }
    )
    SpacerMedium()
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) =
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        placeholder = {
            Text(
                stringResource(R.string.cdw_fasttrack_search_placeholder),
                style = AppTheme.typography.body1l
            )
        },
        shape = RoundedCornerShape(PaddingDefaults.Medium),
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = AppTheme.colors.neutral100,
            placeholderColor = AppTheme.colors.neutral600,
            leadingIconColor = AppTheme.colors.neutral600,
            focusedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified
        )
    )

@Composable
private fun ErrorScreen(
    onClickRetry: () -> Unit
) =
    Box(
        Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
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
