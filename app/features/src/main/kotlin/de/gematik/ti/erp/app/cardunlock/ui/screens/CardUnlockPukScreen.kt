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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.settings.navigation.SettingsRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.PukPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus

private const val PUK_LENGTH = 8

class CardUnlockPukScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {

    @Composable
    override fun Content() {
        val unlockMethod by graphController.unlockMethod.collectAsStateWithLifecycle()
        val puk by graphController.puk.collectAsStateWithLifecycle()

        val listState = rememberLazyListState()

        CardWallScaffold(
            modifier = Modifier.testTag("cardWall/secretScreen"),
            title = if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret) {
                stringResource(R.string.unlock_egk_top_bar_title_forgot_pin)
            } else {
                stringResource(R.string.unlock_egk_top_bar_title)
            },
            listState = listState,
            nextEnabled = puk.length == PUK_LENGTH,
            onBack = {
                navController.popBackStack()
            },
            onNext = {
                if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret) {
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockNewSecretScreen.path()
                    )
                } else {
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockEgkScreen.path()
                    )
                }
            },
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(onClick = {
                    graphController.reset()
                    navController.navigate(SettingsRoutes.SettingsScreen.route)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            PukScreenContent(
                listState = listState,
                innerPadding = innerPadding,
                puk = puk,
                onPukChange = graphController::setPersonalUnblockingKey,
                onNext = {
                    if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret) {
                        navController.navigate(
                            CardUnlockRoutes.CardUnlockNewSecretScreen.path()
                        )
                    } else {
                        navController.navigate(
                            CardUnlockRoutes.CardUnlockEgkScreen.path()
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun PukScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    puk: String,
    onPukChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val contentPadding by rememberContentPadding(innerPadding)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            Text(
                stringResource(R.string.unlock_egk_enter_puk),
                style = AppTheme.typography.h5
            )
            SpacerMedium()
        }
        item {
            PukInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollOnFocus(1, listState),
                onPukChange = onPukChange,
                puk = puk,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun PukInputField(
    modifier: Modifier,
    puk: String,
    onPukChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val pukRegex = """^\d{0,$PUK_LENGTH}$""".toRegex()
    ErezeptOutlineText(
        modifier = modifier
            .fillMaxWidth(),
        value = puk,
        onValueChange = {
            if (it.matches(pukRegex)) {
                onPukChange(it)
            }
        },
        label = stringResource(R.string.unlock_egk_puk_label),
        placeholder = stringResource(R.string.unlock_egk_puk_label),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(SizeDefaults.one),
        keyboardActions = KeyboardActions {
            if (puk.length == PUK_LENGTH) {
                onNext()
            }
        }
    )
    SpacerTiny()
    Text(
        stringResource(R.string.unlock_egk_puk_info),
        style = AppTheme.typography.caption1l
    )
}

@LightDarkPreview
@Composable
fun CardUnlockPukScreenPreview(
    @PreviewParameter(PukPreviewParameterProvider::class) puk: String
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        PukScreenContent(
            listState = lazyListState,
            puk = puk,
            innerPadding = PaddingValues(PaddingDefaults.Medium),
            onPukChange = {},
            onNext = {}
        )
    }
}
