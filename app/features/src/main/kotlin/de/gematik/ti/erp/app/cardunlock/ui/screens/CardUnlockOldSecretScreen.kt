/*
 * Copyright 2025, gematik GmbH
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.screens.PIN_RANGE
import de.gematik.ti.erp.app.cardwall.ui.screens.PinInputField
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.Pin
import de.gematik.ti.erp.app.utils.compose.preview.PinPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus

class CardUnlockOldSecretScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {

    @Composable
    override fun Content() {
        val oldPin by graphController.oldPin.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        CardWallScaffold(
            modifier = Modifier.testTag("cardWall/secretScreen"),
            backMode = NavigationBarMode.Back,
            title = stringResource(R.string.unlock_egk_top_bar_title_change_secret),
            nextEnabled = oldPin.length in PIN_RANGE,
            listState = lazyListState,
            onNext = {
                navController.navigate(
                    CardUnlockRoutes.CardUnlockNewSecretScreen.path()
                )
            },
            onBack = {
                navController.popBackStack()
            },
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
            CardUnlockOldSecretScreenContent(
                lazyListState = lazyListState,
                innerPadding = innerPadding,
                pinRange = PIN_RANGE,
                oldPin = oldPin,
                onOldPinChange = graphController::setOldPin,
                onNext = { navController.navigate(CardUnlockRoutes.CardUnlockNewSecretScreen.path()) }
            )
        }
    }
}

@Composable
private fun CardUnlockOldSecretScreenContent(
    lazyListState: LazyListState,
    pinRange: IntRange,
    innerPadding: PaddingValues,
    oldPin: String,
    onOldPinChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val contentPadding by rememberContentPadding(innerPadding)

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Text(
                stringResource(R.string.unlock_egk_enter_old_secret),
                style = AppTheme.typography.h5
            )
            SpacerMedium()
        }
        item {
            PinInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollOnFocus(1, lazyListState),
                pinRange = pinRange,
                onPinChange = onOldPinChange,
                pin = oldPin,
                onNext = onNext,
                infoText = stringResource(R.string.unlock_egk_pin_info).plus(
                    " " +
                        annotatedStringResource(
                            R.string.cdw_pin_length_info,
                            PIN_RANGE.first.toString(),
                            PIN_RANGE.last.toString()
                        ).text
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CardUnlockOldSecretScreenPreview(
    @PreviewParameter(PinPreviewParameterProvider::class) pin: Pin
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockOldSecretScreenContent(
            lazyListState = lazyListState,
            pinRange = PIN_RANGE,
            innerPadding = PaddingValues(PaddingDefaults.Medium),
            oldPin = pin.pin,
            onOldPinChange = {},
            onNext = {}
        )
    }
}
