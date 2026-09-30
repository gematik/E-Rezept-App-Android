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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.DebugPushNotificationsViewModel
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.debugPushNotificationsViewModel
import de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components.DebugEncryptedPushSection
import de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components.DebugPushRegistrationSection
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

/**
 * For testing push notifications in debug mode:
 *  - Pusher registration and channel status inspection
 *  - Encrypted FCM and Push Gateway delivery testing
 */
@Composable
fun DebugPushNotificationsScreen(
    viewModel: DebugPushNotificationsViewModel = debugPushNotificationsViewModel(),
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchFcmToken()
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
                DebugPushRegistrationSection(viewModel)
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
