/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.CardWallIntroScreen
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.NoInternetError
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.UserNotLoggedInWithBiometricsError
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilePairedDevicesScreenController
import de.gematik.ti.erp.app.profiles.ui.components.DeleteDeviceDialog
import de.gematik.ti.erp.app.profiles.ui.preview.PairedDevicesPreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.collectLatest

private const val VERTICAL_BIAS_ALIGNMENT = -0.33f
private const val HORIZONTAL_BIAS_ALIGNMENT = 0f

class ProfilePairedDevicesScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val profileId = remember { requireNotNull(navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)) }
        val intentHandler = LocalIntentHandler.current

        val deleteDeviceEvent = ComposableEvent<PairedDevice>()

        val controller = rememberProfilePairedDevicesScreenController(profileId)

        val pairedDevicesState by controller.pairedDevices.collectAsStateWithLifecycle()

        navBackStackEntry.onReturnAction(ProfileRoutes.ProfilePairedDevicesScreen) {
            controller.refreshPairedDevices()
        }

        with(controller) {
            showCardWallEvent.listen { id ->
                navController.navigate(CardWallIntroScreen.path(id))
            }
            showCardWallWithFilledCanEvent.listen { cardWallData ->
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = cardWallData.profileId,
                        can = cardWallData.can
                    )
                )
            }
            showGidEvent.listen { gidData ->
                navController.navigate(
                    CardWallIntroScreen.pathWithGid(gidData)
                )
            }
        }

        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                controller.refreshPairedDevices()
            }
        }

        AuthenticationFailureDialog(
            event = controller.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )

        DeleteDeviceDialog(
            event = deleteDeviceEvent,
            dialogScaffold = dialog,
            onClickAction = {
                controller.deletePairedDevice(device = it)
            }
        )

        ProfilePairedDevicesScreenScaffold(
            state = pairedDevicesState,
            listState = listState,
            onBack = { navController.popBackStack() },
            onDeleteDevice = { device -> deleteDeviceEvent.trigger(device) },
            onAuthenticate = {
                controller.chooseAuthenticationMethod(
                    profileId = profileId,
                    useBiometricPairingScope = true
                )
            },
            onRefresh = { controller.refreshPairedDevices() }
        )
    }
}

@Composable
private fun ProfilePairedDevicesScreenScaffold(
    state: UiState<List<PairedDevice>>,
    listState: LazyListState,
    onBack: () -> Unit,
    onDeleteDevice: (PairedDevice) -> Unit,
    onAuthenticate: () -> Unit,
    onRefresh: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.paired_devices_title),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) {
        UiStateMachine(
            state = state,
            onLoading = {
                EmptyScreenLoading(modifier = Modifier.fillMaxSize())
            },
            onEmpty = {
                EmptyScreenNoDevices(modifier = Modifier.fillMaxSize())
            },
            onError = { error ->
                val (title, description, retryText) = (error as ProfilePairedDevicesErrorState).errorParams()
                val isBiometricError = error is UserNotLoggedInWithBiometricsError
                EmptyScreenFailure(
                    modifier = Modifier.fillMaxSize(),
                    isBiometricError = isBiometricError,
                    title = title,
                    description = description,
                    retryText = retryText,
                    onClickRetry = { if (isBiometricError) onAuthenticate() else onRefresh() }
                )
            }
        ) { pairedDevices ->
            PairedDevicesSection(
                listState = listState,
                devices = pairedDevices,
                onDeleteDevice = onDeleteDevice
            )
        }
    }
}

// TODO: Move to components
@Composable
private fun PairedDevicesSection(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    devices: List<PairedDevice>,
    onDeleteDevice: (PairedDevice) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items = devices) { device ->
            PairedDevice(
                device = device,
                isOurDevice = device.isCurrentDevice,
                onDeleteDevice = {
                    onDeleteDevice(device)
                }
            )
        }
    }
}

@Composable
private fun EmptyScreenLoading(modifier: Modifier) {
    EmptyScreen(modifier) {
        CircularProgressIndicator(Modifier.size(SizeDefaults.sixfold))
        Text(
            stringResource(R.string.paired_devices_loading_description),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyScreenNoDevices(modifier: Modifier = Modifier) {
    EmptyScreen(modifier) {
        Text(
            stringResource(R.string.paired_devices_no_devices_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.paired_devices_no_devices_description),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyScreenFailure(
    modifier: Modifier,
    isBiometricError: Boolean,
    title: String,
    description: String,
    retryText: String = stringResource(R.string.paired_devices_error_retry),
    onClickRetry: () -> Unit
) {
    EmptyScreen(modifier) {
        if (isBiometricError) {
            Image(
                painterResource(R.drawable.card_wall_card_hand),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .size(SizeDefaults.twentyfivefold)
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            SpacerMedium()
        }
        Text(
            title,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Text(
            description,
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onClickRetry) {
            Icon(Icons.Rounded.Refresh, null)
            SpacerSmall()
            Text(retryText)
        }
    }
}

@Suppress("MagicNumber")
@Composable
internal fun EmptyScreen(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .align(BiasAlignment(HORIZONTAL_BIAS_ALIGNMENT, VERTICAL_BIAS_ALIGNMENT))
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            content()
        }
    }
}

@Composable
private fun PairedDevice(
    device: PairedDevice,
    isOurDevice: Boolean,
    onDeleteDevice: () -> Unit
) {
    Row(Modifier.padding(PaddingDefaults.Medium)) {
        Column(Modifier.weight(1f)) {
            Text(device.name, style = AppTheme.typography.body1)
            // val connectedOn = localizedDateString(device.connectedOn)
            if (isOurDevice) {
                Text(
                    stringResource(R.string.paired_device_subtitle_our_device, device.connectedOn),
                    style = AppTheme.typography.body2l
                )
            } else {
                Text(stringResource(R.string.paired_device_subtitle, device.connectedOn), style = AppTheme.typography.body2l)
            }
        }
        SpacerMedium()
        IconButton(onClick = onDeleteDevice) {
            Icon(Icons.Rounded.Delete, null, tint = AppTheme.colors.neutral500)
        }
    }
}

@Composable
private fun ProfilePairedDevicesErrorState.errorParams(): Triple<String, String, String> {
    return when (this) {
        is NoInternetError -> Triple(
            stringResource(R.string.paired_devices_error_no_network_title),
            stringResource(R.string.paired_devices_error_no_network_description),
            stringResource(R.string.paired_devices_error_retry)
        )

        is UserNotLoggedInWithBiometricsError -> Triple(
            stringResource(R.string.paired_devices_biometrics_error_login_title),
            stringResource(R.string.paired_devices_biometrics_error_login_description),
            stringResource(R.string.paired_devices_biometrics_error_login_button_text)
        )

        else -> Triple(
            stringResource(R.string.paired_devices_error_generic_title),
            stringResource(R.string.paired_devices_error_generic_description),
            stringResource(R.string.paired_devices_error_retry)
        )
    }
}

@LightDarkPreview
@Composable
fun ProfilePairedDevicesScreenScaffoldPreview(
    @PreviewParameter(PairedDevicesPreviewParameterProvider::class) state: UiState<List<PairedDevice>>
) {
    PreviewAppTheme {
        ProfilePairedDevicesScreenScaffold(
            state = state,
            listState = rememberLazyListState(),
            onBack = {},
            onDeleteDevice = {},
            onAuthenticate = {},
            onRefresh = {}
        )
    }
}
