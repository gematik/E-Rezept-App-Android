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

// Added to allow composable extension functions
@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton

@Composable
fun CardWallScaffold(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit),
    onNext: (() -> Unit)?,
    nextEnabled: Boolean = true,
    nextText: String = stringResource(R.string.cdw_next),
    backMode: NavigationBarMode? = NavigationBarMode.Back,
    actions: @Composable RowScope.() -> Unit = {},
    listState: LazyListState,
    content: @Composable (PaddingValues) -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = title,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = backMode,
        actions = actions,
        onBack = onBack,
        bottomBar = {
            if (onNext != null) {
                CardWallBottomBar(onNext = onNext, nextEnabled = nextEnabled, nextText = nextText)
            }
        },
        modifier = modifier,
        topBarColor = MaterialTheme.colors.surface,
        listState = listState,
        content = content
    )
}

@Composable
fun CardWallBottomBar(
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextText: String
) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = SizeDefaults.half
    ) {
        Column(
            Modifier
                .imePadding()
                .navigationBarsPadding()
                .fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier
                    .testTag(TestTag.CardWall.ContinueButton)
                    .padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.ShortMedium
                    )
                    .align(Alignment.End)
            ) {
                Text(
                    nextText
                )
            }
        }
    }
}
