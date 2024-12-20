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

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.prescription.ui.screen

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.prescription.presentation.rememberWelcomeDrawerController
import de.gematik.ti.erp.app.prescription.ui.components.CommonDrawerScreenContent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

/**
 * This drawer is a one time show, once it is shown it will be hidden from the user
 */
class WelcomeDrawerBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val controller = rememberWelcomeDrawerController()
        val profileData by controller.activeProfile.collectAsStateWithLifecycle()

        controller.onWelcomeDrawerShown()
        UiStateMachine(
            state = profileData,
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onError = {
                ErrorScreenComponent()
            }
        ) { profile ->
            WelcomeDrawerScreenContent(
                onClickConnect = {
                    navController.navigate(
                        CardWallRoutes.CardWallIntroScreen.path(profile.id)
                    )
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun WelcomeDrawerScreenContent(
    onClickConnect: () -> Unit,
    onCancel: () -> Unit
) {
    CommonDrawerScreenContent(
        modifierText = Modifier
            .wrapContentSize()
            .testTag(TestTag.Main.MainScreenBottomSheet.ConnectLaterButton),
        header = stringResource(R.string.mainscreen_welcome_drawer_header),
        info = stringResource(R.string.mainscreen_welcome_drawer_info),
        image = painterResource(R.drawable.man_phone_blue_circle),
        connectButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect),
        cancelButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect_later),
        onClickConnect = onClickConnect,
        onCancel = onCancel
    )
}

@LightDarkPreview
@Composable
fun WelcomeDrawerScreenContentPreview() {
    PreviewAppTheme {
        WelcomeDrawerScreenContent(
            onClickConnect = {},
            onCancel = {}
        )
    }
}
