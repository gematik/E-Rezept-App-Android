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

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.cardwall.ui.screens

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallSelectInsuranceTypeBottomSheetScreenController
import de.gematik.ti.erp.app.prescription.ui.components.DefaultDrawerScreenContent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class CardWallSelectInsuranceTypeBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val profileId = remember {
            navBackStackEntry.arguments?.getString(
                CardWallRoutes.CARD_WALL_NAV_PROFILE_ID
            )
        }
        val controller = rememberCardWallSelectInsuranceTypeBottomSheetScreenController(
            profileId
        )
        val profileUiState by controller.profileState.collectAsStateWithLifecycle()
        UiStateMachine(
            state = profileUiState,
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onError = {
                ErrorScreenComponent()
            }
        ) { profile ->
            CardWallSelectInsuranceTypeBottomSheetScreenContent(
                onClickGKV = {
                    controller.setProfileInsuranceTypeAsGKV()
                    navController.navigate(
                        CardWallRoutes.CardWallIntroScreen.path(profile.id),
                        navOptions = navOptions {
                            popUpTo(CardWallRoutes.CardWallSelectInsuranceTypeBottomSheetScreen.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                onClickPKV = {
                    controller.setProfileInsuranceTypeAsPKV()
                    navController.navigate(
                        CardWallRoutes.CardWallGidListScreen.path(profile.id),
                        navOptions = navOptions {
                            popUpTo(CardWallRoutes.CardWallSelectInsuranceTypeBottomSheetScreen.route) {
                                inclusive = true
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun CardWallSelectInsuranceTypeBottomSheetScreenContent(
    onClickGKV: () -> Unit,
    onClickPKV: () -> Unit
) {
    DefaultDrawerScreenContent(
        modifierOutlinedButton = Modifier
            .wrapContentSize()
            .testTag(TestTag.Main.MainScreenBottomSheet.ConnectLaterButton),
        info = stringResource(R.string.cardwall_select_insurance_type_drawer_body),
        header = stringResource(R.string.cardwall_select_insurance_type_drawer_title),
        image = painterResource(R.drawable.man_phone_blue_circle),
        primaryButtonText = stringResource(R.string.cardwall_select_insurance_type_drawer_public_insurance_button),
        outlinedButtonText = stringResource(R.string.cardwall_select_insurance_type_drawer_private_insurance_button),
        onClickPrimary = onClickGKV,
        onClickOutlined = onClickPKV
    )
}

@LightDarkPreview
@Composable
fun CardWallSelectInsuranceTypeBottomSheetScreenPreview() {
    PreviewAppTheme {
        CardWallSelectInsuranceTypeBottomSheetScreenContent(
            onClickGKV = {},
            onClickPKV = {}
        )
    }
}
