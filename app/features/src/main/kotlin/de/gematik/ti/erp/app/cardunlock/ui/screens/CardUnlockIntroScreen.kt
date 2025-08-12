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

package de.gematik.ti.erp.app.cardunlock.ui.screens

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes.CardUnlockCanScreen
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold
import de.gematik.ti.erp.app.utils.compose.preview.UnlockMethodPreviewParameterProvider

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
                navBackStackEntry.arguments?.getString(CardUnlockRoutes.CARD_UNLOCK_NAV_UNLOCK_METHOD)
            )
        }

        LaunchedEffect(Unit) {
            graphController.setUnlockMethodForGraph(UnlockMethod.valueOf(unlockMethod))
        }

        CardWallScaffold(
            modifier = Modifier.testTag("unlockEgk/unlock"),
            title = getTitle(unlockMethod),
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

/**
 * This translation should be part of the UnlockMethod enum
 */
@Composable
private fun getTitle(unlockMethod: String) = when (unlockMethod) {
    UnlockMethod.ChangeReferenceData.name -> stringResource(R.string.unlock_egk_top_bar_title_change_secret)
    UnlockMethod.ResetRetryCounterWithNewSecret.name -> stringResource(R.string.unlock_egk_top_bar_title_forgot_pin)
    else -> stringResource(R.string.unlock_egk_top_bar_title)
}

@LightDarkPreview
@Composable
fun CardUnlockIntroScreenScaffoldPreview(
    @PreviewParameter(UnlockMethodPreviewParameterProvider::class) unlockMethod: UnlockMethod
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        TestScaffold(
            topBarTitle = getTitle(unlockMethod = unlockMethod.name),
            navigationMode = NavigationBarMode.Back
        ) {
            CardUnlockIntroScreenContent(unlockMethod.name, lazyListState)
        }
    }
}
