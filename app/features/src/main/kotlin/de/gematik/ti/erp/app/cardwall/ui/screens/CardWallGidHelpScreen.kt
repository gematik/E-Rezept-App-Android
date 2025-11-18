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

import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallGidGKVHelpScreenContent
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallGidPKVHelpScreenContent
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallGidHelpScreenPreviewData
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallGidHelpScreenPreviewParameterProvider
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class CardWallGidHelpScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val profileIsPkv by sharedViewModel.profileIsPkv.collectAsStateWithLifecycle(false)
        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }

        BackHandler {
            onBack()
        }
        CardWallGidHelpScreenScaffold(
            listState = listState,
            profileIsPkv = profileIsPkv,
            onBack = { onBack() },
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
    profileIsPkv: Boolean,
    onBack: () -> Unit,
    onClickOpenSettings: () -> Unit
) {
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = stringResource(R.string.cardwall_gid_help_title),
        onBack = onBack,
        listState = listState
    ) {
        if (profileIsPkv) {
            CardWallGidPKVHelpScreenContent(
                listState = listState,
                onClickOpenSettings = onClickOpenSettings
            )
        } else {
            CardWallGidGKVHelpScreenContent(
                listState = listState,
                onClickOpenSettings = onClickOpenSettings
            )
        }
    }
}

@LightDarkLongPreview
@Composable
fun CardWallGidHelpScreenPreview(
    @PreviewParameter(CardWallGidHelpScreenPreviewParameterProvider::class) previewData:
        CardWallGidHelpScreenPreviewData
) {
    PreviewAppTheme {
        CardWallGidHelpScreenScaffold(
            profileIsPkv = previewData.profileIsPkv,
            listState = rememberLazyListState(),
            onClickOpenSettings = { },
            onBack = { }
        )
    }
}
