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

package de.gematik.ti.erp.app.cardunlock.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallBottomBar
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.screens.CAN_LENGTH
import de.gematik.ti.erp.app.cardwall.ui.screens.CanScreenContent
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.CanBoolean
import de.gematik.ti.erp.app.utils.compose.preview.CanBooleanPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold

class CardUnlockCanScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {
    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()

        val unlockMethod by graphController.unlockMethod.collectAsStateWithLifecycle()
        val can by graphController.can.collectAsStateWithLifecycle()

        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.CAN.CANScreen),
            backMode = NavigationBarMode.Back,
            onBack = {
                navController.popBackStack()
            },
            title = when (unlockMethod) {
                UnlockMethod.ChangeReferenceData -> stringResource(R.string.unlock_egk_top_bar_title_change_secret)
                UnlockMethod.ResetRetryCounterWithNewSecret -> stringResource(
                    R.string.unlock_egk_top_bar_title_forgot_pin
                )
                else -> stringResource(R.string.unlock_egk_top_bar_title)
            },
            nextEnabled = can.length == CAN_LENGTH,
            onNext = {
                if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockOldSecretScreen.path()
                    )
                } else {
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockPukScreen.path()
                    )
                }
            },
            listState = lazyListState,
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(onClick = {
                    graphController.reset()
                    navController.popBackStack(CardUnlockRoutes.CardUnlockIntroScreen.route, inclusive = true)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            CanScreenContent(
                lazyListState = lazyListState,
                innerPadding = innerPadding,
                onClickLearnMore = {
                    navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.path())
                },
                can = can,
                onCanChange = graphController::setCardAccessNumber,
                onNext = {
                    if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                        navController.navigate(
                            CardUnlockRoutes.CardUnlockOldSecretScreen.path()
                        )
                    } else {
                        navController.navigate(
                            CardUnlockRoutes.CardUnlockPukScreen.path()
                        )
                    }
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CardUnlockCanScreenScaffoldPreview(
    @PreviewParameter(CanBooleanPreviewParameterProvider::class) canNext: CanBoolean
) {
    val lazyListState = rememberLazyListState()

    PreviewAppTheme {
        TestScaffold(
            navigationMode = NavigationBarMode.Back,
            topBarTitle = stringResource(R.string.unlock_egk_top_bar_title_change_secret),

            bottomBar = {
                CardWallBottomBar(
                    onNext = {},
                    nextEnabled = canNext.boolean,
                    nextText = stringResource(R.string.cdw_next)
                )
            }
        ) {
            CanScreenContent(
                lazyListState = lazyListState,
                innerPadding = it,
                onClickLearnMore = { },
                onNext = { },
                can = canNext.can,
                onCanChange = { }
            )
        }
    }
}
