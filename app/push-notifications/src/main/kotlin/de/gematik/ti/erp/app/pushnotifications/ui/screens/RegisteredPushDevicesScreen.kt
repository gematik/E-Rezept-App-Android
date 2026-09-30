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

package de.gematik.ti.erp.app.pushnotifications.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.pushnotifications.presentation.rememberRegisteredDevicesController
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredDeviceUiModel
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.CannotLoadRegisteredPushDevicesError
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.NoInternetErrorPush
import de.gematik.ti.erp.app.pushnotifications.ui.preview.RegisteredDevicesPreviewParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState

private const val VERTICAL_BIAS_ALIGNMENT = -0.33f
private const val HORIZONTAL_BIAS_ALIGNMENT = 0f

class RegisteredPushDevicesScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val profileId = remember {
            navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)
        }

        if (profileId == null) {
            BackHandler { navController.popBackStack() }
            RegisteredPushDevicesScreenScaffold(
                state = UiState.Error(CannotLoadRegisteredPushDevicesError),
                isDeleting = { false },
                listState = rememberLazyListState(),
                onBack = { navController.popBackStack() },
                onRefresh = { navController.popBackStack() },
                onRequestDeleteDevice = {}
            )
            return
        }

        val controller = rememberRegisteredDevicesController(profileId)
        val registeredDevicesState by controller.registeredDevices.collectAsStateWithLifecycle()
        val deletingPushKeys by controller.deletingPushKeys.collectAsStateWithLifecycle()
        val devicePendingDeletion by controller.devicePendingDeletion.collectAsStateWithLifecycle()
        val showDeletionError by controller.showDeletionError.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        BackHandler { navController.popBackStack() }
        RegisteredPushDevicesScreenScaffold(
            state = registeredDevicesState,
            isDeleting = { device -> device.pushKey in deletingPushKeys },
            listState = listState,
            onBack = { navController.popBackStack() },
            onRefresh = { controller.refreshRegisteredDevices() },
            onRequestDeleteDevice = { controller.requestDeleteDevice(it) }
        )

        devicePendingDeletion?.let { device ->
            DeleteRegisteredPushDeviceDialog(
                device = device,
                onConfirm = { controller.deleteDevice(device) },
                onDismiss = { controller.dismissDeleteDeviceDialog() }
            )
        }

        if (showDeletionError) {
            DeleteRegisteredPushDeviceErrorDialog(
                onDismiss = { controller.dismissDeletionError() }
            )
        }
    }
}

@Composable
private fun DeleteRegisteredPushDeviceDialog(
    device: RegisteredDeviceUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val body = stringResource(device.deleteDialogBodyRes, device.name)
    ErezeptAlertDialog(
        title = stringResource(R.string.registered_push_devices_delete_dialog_title),
        bodyText = body,
        confirmText = stringResource(R.string.registered_push_devices_delete_dialog_confirm),
        dismissText = stringResource(R.string.cancel),
        confirmTextColor = AppTheme.colors.red600,
        onConfirmRequest = onConfirm,
        onDismissRequest = onDismiss
    )
}

@Composable
private fun DeleteRegisteredPushDeviceErrorDialog(
    onDismiss: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.registered_push_devices_delete_error_title),
        body = stringResource(R.string.registered_push_devices_delete_error_description),
        onDismissRequest = onDismiss
    )
}

@Composable
private fun RegisteredPushDevicesScreenScaffold(
    state: UiState<List<RegisteredDeviceUiModel>>,
    isDeleting: (RegisteredDeviceUiModel) -> Boolean,
    listState: LazyListState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRequestDeleteDevice: (RegisteredDeviceUiModel) -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.registered_push_devices_title),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
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
                val (title, description) = (error as RegisteredPushDevicesErrorState).errorParams()
                EmptyScreenFailure(
                    modifier = Modifier.fillMaxSize(),
                    title = title,
                    description = description,
                    onClickRetry = onRefresh
                )
            }
        ) { registeredDevices ->
            RegisteredPushDevicesSection(
                listState = listState,
                devices = registeredDevices,
                isDeleting = isDeleting,
                onRequestDeleteDevice = onRequestDeleteDevice
            )
        }
    }
}

@Composable
private fun RegisteredPushDevicesSection(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    devices: List<RegisteredDeviceUiModel>,
    isDeleting: (RegisteredDeviceUiModel) -> Boolean,
    onRequestDeleteDevice: (RegisteredDeviceUiModel) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        item {
            Text(
                text = stringResource(R.string.registered_push_devices_top_text),
                modifier = Modifier.padding(PaddingDefaults.Medium),
                style = AppTheme.typography.body2l
            )
        }

        items(items = devices) { device ->
            RegisteredDevice(
                device = device,
                isDeleting = isDeleting(device),
                onRequestDelete = { onRequestDeleteDevice(device) }
            )
        }
    }
}

@Composable
private fun RegisteredDevice(
    device: RegisteredDeviceUiModel,
    isDeleting: Boolean,
    onRequestDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Small,
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(device.name, style = AppTheme.typography.body1)
            if (device.isCurrentDevice) {
                Text(
                    stringResource(R.string.registered_push_devices_current_device),
                    style = AppTheme.typography.body2l
                )
            }
        }
        if (isDeleting) {
            CircularProgressIndicator(
                Modifier.size(SizeDefaults.triple),
                color = AppTheme.colors.primary400
            )
        } else {
            IconButton(onClick = onRequestDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.registered_push_devices_delete_action),
                    tint = AppTheme.colors.red600
                )
            }
        }
    }
}

@Composable
private fun EmptyScreenLoading(modifier: Modifier) {
    EmptyScreen(modifier) {
        CircularProgressIndicator(Modifier.size(SizeDefaults.sixfold))
        Text(
            stringResource(R.string.registered_push_devices_loading_description),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyScreenNoDevices(modifier: Modifier = Modifier) {
    EmptyScreen(modifier) {
        Text(
            stringResource(R.string.registered_push_devices_empty_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.registered_push_devices_empty_description),
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
            Text(stringResource(R.string.registered_push_devices_error_retry))
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun EmptyScreen(
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
private fun RegisteredPushDevicesErrorState.errorParams(): Pair<String, String> =
    when (this) {
        is NoInternetErrorPush -> Pair(
            stringResource(R.string.registered_push_devices_error_no_internet_title),
            stringResource(R.string.registered_push_devices_error_no_internet_description)
        )

        else -> Pair(
            stringResource(R.string.registered_push_devices_error_generic_title),
            stringResource(R.string.registered_push_devices_error_generic_description)
        )
    }

@LightDarkPreview
@Composable
fun RegisteredPushDevicesScreenScaffoldPreview(
    @PreviewParameter(RegisteredDevicesPreviewParameterProvider::class) state: UiState<List<RegisteredDeviceUiModel>>
) {
    PreviewTheme {
        RegisteredPushDevicesScreenScaffold(
            state = state,
            isDeleting = { false },
            listState = rememberLazyListState(),
            onBack = {},
            onRefresh = {},
            onRequestDeleteDevice = {}
        )
    }
}
