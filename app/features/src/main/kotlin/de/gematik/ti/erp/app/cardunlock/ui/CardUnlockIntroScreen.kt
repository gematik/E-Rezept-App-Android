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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes.CardUnlockCanScreen
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

class CardUnlockIntroScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {

    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()

        val unlockMethod = remember {
            requireNotNull(
                navBackStackEntry.arguments?.getString(CardUnlockRoutes.UnlockMethod)
            )
        }

        LaunchedEffect(Unit) {
            graphController.setUnlockMethodForGraph(UnlockMethod.valueOf(unlockMethod))
        }

        CardWallScaffold(
            modifier = Modifier.testTag("unlockEgk/unlock"),
            title = when (unlockMethod) {
                UnlockMethod.ChangeReferenceData.name -> stringResource(R.string.unlock_egk_top_bar_title_change_secret)
                UnlockMethod.ResetRetryCounterWithNewSecret.name -> stringResource(
                    R.string.unlock_egk_top_bar_title_forgot_pin
                )

                else -> stringResource(R.string.unlock_egk_top_bar_title)
            },
            onBack = {
                graphController.reset()
                navController.popBackStack(CardUnlockRoutes.CardUnlockIntroScreen.route, inclusive = true)
            },
            onNext = { navController.navigate(CardUnlockCanScreen.path()) },
            nextText = stringResource(R.string.unlock_egk_next),
            listState = lazyListState
        ) {
            CardUnlockIntroScreenContent(unlockMethod, lazyListState = lazyListState)
        }
    }
}

@Composable
private fun CardUnlockIntroScreenContent(
    unlockMethod: String,
    lazyListState: LazyListState
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    ) {
        item {
            Text(
                text = stringResource(R.string.unlock_egk_intro_what_you_need),
                style = AppTheme.typography.h5
            )
            SpacerLarge()
            SimpleCheck(stringResource(R.string.unlock_egk_intro_egk))
            if (unlockMethod == UnlockMethod.ChangeReferenceData.name) {
                SimpleCheck(stringResource(R.string.unlock_egk_intro_pin))
                SpacerSmall()
                Text(
                    text = stringResource(R.string.cdw_pin_info),
                    style = AppTheme.typography.caption1l
                )
            } else {
                SimpleCheck(stringResource(R.string.unlock_egk_intro_puk))
                SpacerSmall()
                Text(
                    text = stringResource(R.string.unlock_egk_puk_info),
                    style = AppTheme.typography.caption1l
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun CardUnlockIntroScreenContentResetRetryCounterPreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockIntroScreenContent(
            unlockMethod = UnlockMethod.ResetRetryCounter.name,
            lazyListState = lazyListState
        )
    }
}

@LightDarkPreview
@Composable
fun CardUnlockIntroScreenContentChangeReferenceDataPreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockIntroScreenContent(
            unlockMethod = UnlockMethod.ChangeReferenceData.name,
            lazyListState = lazyListState
        )
    }
}

@LightDarkPreview
@Composable
fun CardUnlockIntroScreenContentResetRetryCounterWithNewSecretPreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockIntroScreenContent(
            unlockMethod = UnlockMethod.ResetRetryCounterWithNewSecret.name,
            lazyListState = lazyListState
        )
    }
}

@LightDarkPreview
@Composable
fun CardUnlockIntroScreenContentNonePreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockIntroScreenContent(
            unlockMethod = UnlockMethod.None.name,
            lazyListState = lazyListState
        )
    }
}
