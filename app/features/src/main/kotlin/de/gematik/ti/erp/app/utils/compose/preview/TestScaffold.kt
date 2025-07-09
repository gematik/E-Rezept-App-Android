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

package de.gematik.ti.erp.app.utils.compose.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar

@Composable
fun TestScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode? = NavigationBarMode.Close,
    bottomBar: @Composable () -> Unit = {},
    topBarTitle: String,
    elevated: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            NavigationTopAppBar(
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                title = topBarTitle,
                elevation = if (elevated) {
                    AppBarDefaults.TopAppBarElevation
                } else {
                    SizeDefaults.zero
                },
                onBack = onBack,
                actions = actions
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun TestScaffoldPreview(
    @PreviewParameter(NavigationBarModePreviewParameters::class) modes: NavigationBarMode
) {
    PreviewAppTheme {
        TestScaffold(
            topBarTitle = "TestTitle",
            navigationMode = modes
        ) {
        }
    }
}

class NavigationBarModePreviewParameters : PreviewParameterProvider<NavigationBarMode?> {
    override val values: Sequence<NavigationBarMode?>
        get() = NavigationBarMode.entries.asSequence().plus(null)
}
