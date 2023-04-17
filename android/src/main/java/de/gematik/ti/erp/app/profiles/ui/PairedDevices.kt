/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.mini.ui.NoneEnrolledException
import de.gematik.ti.erp.app.cardwall.mini.ui.PromptAuthenticator
import de.gematik.ti.erp.app.cardwall.mini.ui.UserNotAuthenticatedException
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import io.github.aakira.napier.Napier
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProfileEditPairedDeviceSection(
    onShowPairedDevices: () -> Unit
) {
    SettingsMenuHeadline(stringResource(R.string.settings_paired_devices_title))

    // connected devices section
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onShowPairedDevices
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Rounded.Devices, null, tint = AppTheme.colors.primary600)
        Text(stringResource(R.string.settings_login_connected_devices), style = AppTheme.typography.body1)
    }
    SpacerLarge()
    Divider(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium))
    SpacerLarge()
}

@Composable
fun PairedDevicesScreen(
    selectedProfile: ProfilesUseCaseData.Profile,
    profilesController: ProfilesController,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.paired_devices_title),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) {
        PairedDevices(
            modifier = Modifier.padding(it),
            selectedProfile = selectedProfile,
            profilesController = profilesController,
            listState = listState
        )
    }
}

@Stable
private sealed interface RefreshState {
    @Stable
    object Loading : RefreshState

    @Stable
    class WithResults(val result: ProfilesUseCaseData.PairedDevices) : RefreshState

    @Stable
    object NoResults : RefreshState

    @Stable
    class Error(val throwable: Throwable) : RefreshState
}

@Stable
private sealed interface DeleteState {
    @Stable
    class Deleting(val device: ProfilesUseCaseData.PairedDevice, val isThisDevice: Boolean) : DeleteState

    @Stable
    object None : DeleteState

    @Stable
    object Error : DeleteState
}

// tag::PairedDevicesUI[]
@Composable
private fun PairedDevices(
    modifier: Modifier,
    selectedProfile: ProfilesUseCaseData.Profile,
    profilesController: ProfilesController,
    listState: LazyListState
) {
    val authenticator = LocalAuthenticator.current

    val refreshFlow = remember { MutableSharedFlow<Unit>() }
    var state by remember { mutableStateOf<RefreshState>(RefreshState.NoResults) }
    LaunchedEffect(selectedProfile) {
        refreshFlow
            .onStart { emit(Unit) } // emit once to start the flow directly
            .collectLatest {
                state = RefreshState.Loading
                profilesController
                    .pairedDevices(selectedProfile.id)
                    .retry(1) { throwable ->
                        Napier.e("Couldn't get paired devices", throwable)
                        if (throwable is RefreshFlowException && throwable.userActionRequired) {
                            authenticator
                                .authenticateForPairedDevices(selectedProfile.id)
                                .first()
                                .let {
                                    when (it) {
                                        PromptAuthenticator.AuthResult.Authenticated -> true
                                        PromptAuthenticator.AuthResult.Cancelled -> false
                                        PromptAuthenticator.AuthResult.NoneEnrolled ->
                                            throw NoneEnrolledException()
                                        PromptAuthenticator.AuthResult.UserNotAuthenticated ->
                                            throw UserNotAuthenticatedException()
                                    }
                                }
                        } else {
                            false
                        }
                    }
                    .catch {
                        Napier.d("Couldn't get paired devices", it)

                        state = RefreshState.Error(it)
                    }
                    .collect {
                        state = if (it.devices.isEmpty()) {
                            RefreshState.NoResults
                        } else {
                            RefreshState.WithResults(it)
                        }
                    }
            }
    }

    val keyStoreAlias = remember(selectedProfile) {
        (selectedProfile.ssoTokenScope as? IdpData.TokenWithKeyStoreAliasScope)
            ?.aliasOfSecureElementEntryBase64()
    }

    val mutex = MutatorMutex()
    val coroutineScope = rememberCoroutineScope()

    var deleteState by remember(state) { mutableStateOf<DeleteState?>(null) }

    (deleteState as? DeleteState.Deleting)?.let {
        DeleteDeviceDialog(
            device = it.device,
            isThisDevice = it.isThisDevice,
            onCancel = {
                deleteState = DeleteState.None
            },
            onClickAction = {
                coroutineScope.launch {
                    mutex.mutate {
                        profilesController
                            .deletePairedDevice(selectedProfile.id, it.device)
                            .onFailure {
                                deleteState = DeleteState.Error
                            }
                            .onSuccess {
                                deleteState = DeleteState.None
                            }

                        // no matter if we received an error or not, we need to refresh this list
                        refreshFlow.emit(Unit)
                    }
                }
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        when (state) {
            RefreshState.Loading -> item { EmptyScreenLoading(Modifier.fillParentMaxSize()) }
            RefreshState.NoResults -> item { EmptyScreenNoDevices(Modifier.fillParentMaxSize()) }
            is RefreshState.Error -> item {
                val (title, desc) = errorMessageFromException((state as RefreshState.Error).throwable)
                EmptyScreenFailure(
                    modifier = Modifier.fillParentMaxSize(),
                    title = title,
                    description = desc,
                    onClickRetry = {
                        coroutineScope.launch {
                            refreshFlow.emit(Unit)
                        }
                    }
                )
            }
            is RefreshState.WithResults -> {
                items((state as RefreshState.WithResults).result.devices) { device: ProfilesUseCaseData.PairedDevice ->
                    val isThisDevice = keyStoreAlias?.let {
                        device.isOurDevice(it)
                    } ?: false
                    PairedDevice(
                        device = device,
                        isOurDevice = isThisDevice,
                        onDeleteDevice = {
                            deleteState = DeleteState.Deleting(device, isThisDevice)
                        }
                    )
                }
            }
        }
    }
}
// end::PairedDevicesUI[]

@Composable
private fun DeleteDeviceDialog(
    device: ProfilesUseCaseData.PairedDevice,
    isThisDevice: Boolean,
    onCancel: () -> Unit,
    onClickAction: () -> Unit
) {
    if (isThisDevice) {
        DeleteThisDeviceDialog(
            device = device,
            onCancel = onCancel,
            onClickAction = onClickAction
        )
    } else {
        DeleteOtherDeviceDialog(
            device = device,
            onCancel = onCancel,
            onClickAction = onClickAction
        )
    }
}

@Composable
private fun DeleteOtherDeviceDialog(
    device: ProfilesUseCaseData.PairedDevice,
    onCancel: () -> Unit,
    onClickAction: () -> Unit
) {
    CommonAlertDialog(
        header = stringResource(R.string.paired_devices_delete_title).toAnnotatedString(),
        info = annotatedStringResource(R.string.paired_devices_delete_description, annotatedStringBold(device.name)),
        cancelText = stringResource(R.string.paired_devices_delete_cancel),
        actionText = stringResource(R.string.paired_devices_delete_remove),
        onCancel = onCancel,
        onClickAction = onClickAction
    )
}

@Composable
private fun DeleteThisDeviceDialog(
    device: ProfilesUseCaseData.PairedDevice,
    onCancel: () -> Unit,
    onClickAction: () -> Unit
) {
    CommonAlertDialog(
        header = stringResource(R.string.paired_devices_delete_this_title).toAnnotatedString(),
        info = annotatedStringResource(
            R.string.paired_devices_delete_this_description,
            annotatedStringBold(device.name)
        ),
        cancelText = stringResource(R.string.paired_devices_delete_cancel),
        actionText = stringResource(R.string.paired_devices_delete_remove),
        onCancel = onCancel,
        onClickAction = onClickAction
    )
}

@Composable
private fun EmptyScreenLoading(modifier: Modifier) {
    EmptyScreen(modifier) {
        CircularProgressIndicator(Modifier.size(48.dp))
        Text(
            stringResource(R.string.paired_devices_loading_description),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyScreenNoDevices(modifier: Modifier) {
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
    title: String,
    description: String,
    onClickRetry: () -> Unit
) {
    EmptyScreen(modifier) {
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
            Text(stringResource(R.string.paired_devices_error_retry))
        }
    }
}

@Composable
private fun EmptyScreen(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .align(BiasAlignment(0f, -0.33f))
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
    device: ProfilesUseCaseData.PairedDevice,
    isOurDevice: Boolean,
    onDeleteDevice: () -> Unit
) {
    Row(Modifier.padding(PaddingDefaults.Medium)) {
        Column(Modifier.weight(1f)) {
            Text(device.name, style = AppTheme.typography.body1)
            val connectedOn = localizedDateString(device.connectedOn)
            if (isOurDevice) {
                Text(
                    stringResource(R.string.paired_device_subtitle_our_device, connectedOn),
                    style = AppTheme.typography.body2l
                )
            } else {
                Text(stringResource(R.string.paired_device_subtitle, connectedOn), style = AppTheme.typography.body2l)
            }
        }
        SpacerMedium()
        IconButton(onClick = onDeleteDevice) {
            Icon(Icons.Rounded.Delete, null, tint = AppTheme.colors.neutral500)
        }
    }
}

@Composable
fun localizedDateString(timestamp: Instant, format: FormatStyle = FormatStyle.LONG): String {
    val config = LocalConfiguration.current
    return remember(config, format) {
        val fmt = DateTimeFormatter.ofLocalizedDate(format)
        timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime().format(fmt)
    }
}

@Composable
fun errorMessageFromException(t: Throwable): Pair<String, String> {
    val other = stringResource(R.string.paired_devices_error_generic_title) to
        stringResource(R.string.paired_devices_error_generic_description)
    val network = stringResource(R.string.paired_devices_error_no_network_title) to
        stringResource(R.string.paired_devices_error_no_network_description)

    return when (t) {
        is IOException -> when {
            t.cause is UnknownHostException -> network
            else -> other
        }
        else -> other
    }
}
