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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.DebugPushNotificationsViewModel
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.debugPushNotificationsViewModel
import de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components.DebugEncryptedPushSection
import de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components.DebugSendFcmSection
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import kotlinx.coroutines.launch

/**
 * For testing push notifications in debug mode:
 *  - Plain FCM push test (toggle permission + send via FCM HTTP v1 API)
 *  - Encrypted FCM push test (encrypt/decrypt per gemF_PushNotification spec A_27610)
 */
@Composable
fun DebugPushNotificationsScreen(
    viewModel: DebugPushNotificationsViewModel = debugPushNotificationsViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pushEnabled by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (permissionGranted) pushEnabled = true
        viewModel.fetchFcmToken()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        pushEnabled = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Permission denied. Enable in Settings > App > Notifications"
                )
            }
        }
    }

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        topBarTitle = "Push Notifications",
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            item {
                DebugSendFcmSection(
                    viewModel = viewModel,
                    pushEnabled = pushEnabled,
                    permissionGranted = permissionGranted,
                    onToggle = { enabled ->
                        when {
                            !enabled -> pushEnabled = false
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                                if (permissionGranted) pushEnabled = true
                                else permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else -> {
                                pushEnabled = true
                                permissionGranted = true
                            }
                        }
                    },
                    onFetchToken = { viewModel.fetchFcmToken() },
                    snackbarHostState = snackbarHostState
                )
            }

            item {
                DebugEncryptedPushSection(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun DebugPushNotificationsScreenPreview() {
    DebugPushNotificationsScreen(onBack = {})
}
