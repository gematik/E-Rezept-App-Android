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

package de.gematik.ti.erp.app.cardunlock.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.CAN_LENGTH
import de.gematik.ti.erp.app.cardwall.ui.CanScreenContent
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

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
                    navController.navigate(MainNavigationScreens.OrderHealthCard.path())
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
