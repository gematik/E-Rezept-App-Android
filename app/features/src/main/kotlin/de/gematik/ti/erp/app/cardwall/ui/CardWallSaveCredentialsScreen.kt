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

package de.gematik.ti.erp.app.cardwall.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.domain.biometric.deviceStrongBiometricStatus
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

private enum class AuthenticationMethod {
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
        var selectedAuthMode by remember { mutableStateOf(AuthenticationMethod.None) }
        val context = LocalContext.current
        val biometricMode by produceState(initialValue = BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
            value = deviceStrongBiometricStatus(context)
        }
        var showEnrollBiometricDialog by remember { mutableStateOf(false) }
        var showFutureLogOutHint by remember { mutableStateOf(false) }
        val enrollBiometricIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        } else {
            Intent(Settings.ACTION_APPLICATION_SETTINGS)
        }

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
                    navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            CardWallSaveCredentialsScreenContent(
                listState = lazyListState,
                innerPadding = innerPadding,
                selectedAuthMode = selectedAuthMode,
                biometricMode = biometricMode,
                showFutureLogOutHint = showFutureLogOutHint,
                onShowBiometricDialog = { showEnrollBiometricDialog = it },
                onShowFutureLogOutHint = { showFutureLogOutHint = it },
                onSelectAlternativeOption = { selectedAuthMode = it },
                onOpenInfoScreen = {
                    navController.navigate(
                        CardWallRoutes.CardWallSaveCredentialsInfoScreen.path()
                    )
                }
            )
        }
        if (showEnrollBiometricDialog) {
            CommonAlertDialog(
                icon = Icons.Rounded.Fingerprint,
                header = stringResource(R.string.enroll_biometric_dialog_header),
                info = stringResource(R.string.enroll_biometric_dialog_info),
                cancelText = stringResource(R.string.enroll_biometric_dialog_cancel),
                actionText = stringResource(R.string.enroll_biometric_dialog_settings),
                onCancel = { showEnrollBiometricDialog = false }
            ) {
                ContextCompat.startActivity(context, enrollBiometricIntent, null)
            }
        }
    }
}

@Composable
private fun CardWallSaveCredentialsScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    selectedAuthMode: AuthenticationMethod,
    biometricMode: Int,
    showFutureLogOutHint: Boolean,
    onSelectAlternativeOption: (AuthenticationMethod) -> Unit,
    onShowBiometricDialog: (Boolean) -> Unit,
    onShowFutureLogOutHint: (Boolean) -> Unit,
    onOpenInfoScreen: () -> Unit
) {
    val contentPadding by remember(innerPadding) {
        derivedStateOf {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
    }
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
                if (biometricMode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    onShowBiometricDialog(true)
                } else {
                    onSelectAlternativeOption(AuthenticationMethod.Alternative)
                    onOpenInfoScreen()
                }
            }
            SpacerMedium()
        }
        item {
            SelectableCard( // TODO fix
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
        AppTheme.colors.primary600
    } else {
        AppTheme.colors.neutral400
    }

    val cardBorderStroke = if (selected) {
        BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
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
                tint = AppTheme.colors.primary600
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

@Preview("SelectableCard")
@Composable
private fun PreviewSelectableCard() {
    AppTheme {
        SelectableCard(
            startIcon = Icons.Rounded.Check,
            text = "Info here",
            selected = true
        )
    }
}
