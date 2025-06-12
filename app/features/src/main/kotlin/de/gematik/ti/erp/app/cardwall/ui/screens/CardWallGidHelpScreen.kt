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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLargeMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString

class CardWallGidHelpScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val context = LocalContext.current

        BackHandler {
            navController.popBackStack()
        }
        CardWallGidHelpScreenScaffold(
            listState = listState,
            onBack = {
                navController.popBackStack()
            },
            onClickOpenSettings = {
                context.openSettingsAsNewActivity(
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS

                        else -> Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                    }
                )
            }
        )
    }
}

@Composable
private fun CardWallGidHelpScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onClickOpenSettings: () -> Unit
) {
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.cardwall_gid_help_title),
        onBack = onBack,
        listState = listState
    ) {
        CardWallGidHelpScreenContent(
            listState = listState,
            onClickOpenSettings = onClickOpenSettings
        )
    }
}

@Composable
private fun CardWallGidHelpScreenContent(
    listState: LazyListState,
    onClickOpenSettings: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardWallGidHelpScreenHeaderSection()
        CardWallGidHelpScreenTipSection(
            onClickOpenSettings = onClickOpenSettings
        )
        item { SpacerXXLargeMedium() }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidHelpScreenHeaderSection() {
    item {
        Column(
            modifier = Modifier.padding(bottom = PaddingDefaults.Medium)
        ) {
            Text(
                text = stringResource(R.string.cardwall_gid_help_header),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
            Text(
                text = stringResource(R.string.cardwall_gid_help_body),
                style = AppTheme.typography.body2
            )
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidHelpScreenTipSection(
    onClickOpenSettings: () -> Unit
) {
    val tips = listOf(
        R.string.cardwall_gid_help_tip_1,
        R.string.cardwall_gid_help_tip_2,
        R.string.cardwall_gid_help_tip_3,
        R.string.cardwall_gid_help_tip_4,
        R.string.cardwall_gid_help_tip_5,
        R.string.cardwall_gid_help_tip_6
    )
    items(tips) {
        CardWallGidHelpScreenTip(stringResource(it))
    }
    item {
        CardWallGidHelpScreenTip(
            text = stringResource(R.string.cardwall_gid_help_tip_7),
            onClick = onClickOpenSettings,
            buttonText = stringResource(R.string.cardwall_gid_help_settings_button).toAnnotatedString()
        )
    }
}

@Composable
private fun CardWallGidHelpScreenTip(
    text: String,
    onClick: () -> Unit = {},
    buttonText: AnnotatedString? = null
) {
    Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
        Row(Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green600)
            SpacerMedium()
            Text(
                text = text,
                style = AppTheme.typography.body1
            )
        }
        buttonText?.let {
            TextButton(
                onClick = { onClick() }
            ) { Text(text = buttonText, style = AppTheme.typography.body2) }
        }
    }
}

@LightDarkPreview
@Composable
fun CardWallGidHelpScreenPreview() {
    PreviewAppTheme {
        CardWallGidHelpScreenScaffold(
            listState = rememberLazyListState(),
            onClickOpenSettings = { },
            onBack = { }
        )
    }
}

@LightDarkPreview
@Composable
fun CardWallGidHelpScreenScaffoldPreview() {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        TestScaffold(
            topBarTitle = stringResource(R.string.cardwall_gid_help_title),
            navigationMode = NavigationBarMode.Back
        ) {
            CardWallGidHelpScreenContent(
                listState = listState
            ) { }
        }
    }
}
