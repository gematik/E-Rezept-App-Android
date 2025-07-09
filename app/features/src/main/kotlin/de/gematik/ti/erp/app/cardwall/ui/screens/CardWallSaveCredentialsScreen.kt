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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.authentication.presentation.deviceSecurityStatus
import de.gematik.ti.erp.app.authentication.ui.components.EnrollBiometricDialog
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallSaveCredentialsPreviewParameterProvider
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallSaveCredentialsScreenPreviewData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

enum class AuthenticationMethod {
    None,
    Alternative, // e.g. biometrics
    HealthCard
}

class CardWallSaveCredentialsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val dialog = LocalDialog.current
        val showEnrollBiometricEvent = ComposableEvent<Unit>()
        var selectedAuthMode by remember { mutableStateOf(AuthenticationMethod.None) }
        var showFutureLogOutHint by remember { mutableStateOf(false) }
        val lazyListState = rememberLazyListState()
        CardWallScaffold(
            modifier = Modifier
                .testTag("cardWall/authenticationSelection"),
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = selectedAuthMode != AuthenticationMethod.None,
            onNext = {
                navController.navigate(
                    CardWallRoutes.CardWallReadCardScreen.path()
                )
            },
            onBack = {
                navController.popBackStack()
            },
            listState = lazyListState,
            nextText = stringResource(R.string.cdw_next),
            actions = {
                TextButton(onClick = {
                    graphController.reset()
                    navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            @Requirement(
                "O.Resi_1#3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Selection of secure save credentials opens a info screen with more details"
            )
            CardWallSaveCredentialsScreenContent(
                listState = lazyListState,
                innerPadding = innerPadding,
                selectedAuthMode = selectedAuthMode,
                context = context,
                showFutureLogOutHint = showFutureLogOutHint,
                onShowBiometricDialog = { showEnrollBiometricEvent.trigger(Unit) },
                onShowFutureLogOutHint = { showFutureLogOutHint = it },
                onSelectAlternativeOption = { selectedAuthMode = it },
                onOpenInfoScreen = {
                    navController.navigate(
                        CardWallRoutes.CardWallSaveCredentialsInfoScreen.path()
                    )
                }
            )
        }
        EnrollBiometricDialog(
            context = context,
            dialog = dialog,
            event = showEnrollBiometricEvent
        )
    }
}

@Composable
private fun CardWallSaveCredentialsScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    selectedAuthMode: AuthenticationMethod,
    context: Context,
    showFutureLogOutHint: Boolean,
    onSelectAlternativeOption: (AuthenticationMethod) -> Unit,
    onShowBiometricDialog: () -> Unit,
    onShowFutureLogOutHint: (Boolean) -> Unit,
    onOpenInfoScreen: () -> Unit
) {
    val contentPadding by rememberContentPadding(innerPadding)

    LazyColumn(
        state = listState,
        contentPadding = contentPadding
    ) {
        item {
            Text(
                stringResource(R.string.cdw_selection_title),
                style = AppTheme.typography.h5,
                modifier = Modifier.padding(PaddingDefaults.Medium)
            )
            SpacerXXLarge()
        }
        item {
            SelectableCard(
                modifier = Modifier.testTag(TestTag.CardWall.StoreCredentials.Save),
                selected = selectedAuthMode == AuthenticationMethod.Alternative,
                startIcon = Icons.Rounded.Check,
                text = stringResource(R.string.cdw_selection_save)
            ) {
                onShowFutureLogOutHint(false)
                if (context.deviceSecurityStatus() == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    onShowBiometricDialog()
                } else {
                    onSelectAlternativeOption(AuthenticationMethod.Alternative)
                    onOpenInfoScreen()
                }
            }
            SpacerMedium()
        }
        item {
            SelectableCard(
                modifier = Modifier
                    .testTag(TestTag.CardWall.StoreCredentials.DontSave),
                selected = selectedAuthMode == AuthenticationMethod.HealthCard,
                startIcon = Icons.Rounded.Close,
                text = stringResource(R.string.cdw_selection_save_not)
            ) {
                onSelectAlternativeOption(AuthenticationMethod.HealthCard)
                onShowFutureLogOutHint(true)
            }
            SpacerXXLarge()
        }
        item {
            if (showFutureLogOutHint) {
                SpacerMedium()
                HintCard(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    image = {
                        HintSmallImage(painterResource(R.drawable.information), null, it)
                    },
                    title = { Text(stringResource(R.string.cdw_selection_hint_title)) },
                    body = { Text(stringResource(R.string.cdw_selection_hint_info_)) }
                )
                SpacerXXLarge()
            }
        }
    }
}

@Composable
private fun SelectableCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    startIcon: ImageVector,
    text: String,
    onCardSelected: () -> Unit = {}
) {
    val checkIcon = if (selected) {
        Icons.Rounded.CheckCircle
    } else {
        Icons.Rounded.RadioButtonUnchecked
    }

    val checkIconTint = if (selected) {
        AppTheme.colors.primary700
    } else {
        AppTheme.colors.neutral400
    }

    val cardBorderStroke = if (selected) {
        BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary700)
    } else {
        BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
    }

    Card(
        border = cardBorderStroke,
        backgroundColor = AppTheme.colors.neutral000,
        modifier = modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .fillMaxWidth(),
        shape = RoundedCornerShape(SizeDefaults.one)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = true,
                    onClick = onCardSelected
                )
                .padding(PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                startIcon,
                null,
                tint = AppTheme.colors.primary700
            )

            Text(
                text,
                style = AppTheme.typography.subtitle1,
                modifier = modifier
                    .weight(1f)
                    .padding(start = PaddingDefaults.Medium)
            )

            Icon(
                checkIcon,
                null,
                tint = checkIconTint
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CardWallSaveCredentialsScreenPreview(
    @PreviewParameter(CardWallSaveCredentialsPreviewParameterProvider::class) previewData:
        CardWallSaveCredentialsScreenPreviewData
) {
    val lazyListState = rememberLazyListState()
    val sampleContext = LocalContext.current

    var selectedAuthMode by remember { mutableStateOf(previewData.selectedAuthMode) }
    var showFutureLogOutHint by remember { mutableStateOf(previewData.showFutureLogOutHint) }

    PreviewAppTheme {
        CardWallScaffold(
            title = stringResource(R.string.cdw_top_bar_title),
            onBack = { },
            onNext = {},
            nextEnabled = selectedAuthMode != AuthenticationMethod.None,
            nextText = stringResource(R.string.cdw_next),
            backMode = NavigationBarMode.Back,
            listState = lazyListState,
            content = { innerPadding ->
                CardWallSaveCredentialsScreenContent(
                    listState = lazyListState,
                    innerPadding = innerPadding,
                    selectedAuthMode = selectedAuthMode,
                    context = sampleContext,
                    showFutureLogOutHint = showFutureLogOutHint,
                    onShowBiometricDialog = {},
                    onShowFutureLogOutHint = { showFutureLogOutHint = it },
                    onSelectAlternativeOption = { selectedAuthMode = it },
                    onOpenInfoScreen = { }
                )
            }
        )
    }
}
