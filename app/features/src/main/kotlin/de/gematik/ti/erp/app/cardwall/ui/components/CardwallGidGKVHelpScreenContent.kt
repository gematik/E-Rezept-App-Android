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

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString

@Composable
internal fun CardWallGidGKVHelpScreenContent(
    listState: LazyListState,
    onClickOpenSettings: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        CardWallGidGKVHelpScreenHeaderSection()
        CardWallGidGKVHelpScreenTipSection(
            onClickOpenSettings = onClickOpenSettings
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidGKVHelpScreenHeaderSection() {
    item {
        Column(
            modifier = Modifier.padding(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small
            )
        ) {
            Text(
                text = stringResource(R.string.cardwall_gid_help_header),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
            Text(
                text = stringResource(R.string.cardwall_gid_help_body),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidGKVHelpScreenTipSection(
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
internal fun CardWallGidHelpScreenTip(
    text: String,
    onClick: () -> Unit = {},
    buttonText: AnnotatedString? = null
) {
    Column {
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
            ) {
                Text(text = buttonText, style = AppTheme.typography.body1)
                SpacerTiny()
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
            }
        }
    }
}

@LightDarkLongPreview
@Composable
internal fun CardWallGidGKVHelpScreenPreview() {
    PreviewAppTheme {
        CardWallGidGKVHelpScreenContent(
            listState = rememberLazyListState(),
            onClickOpenSettings = { }
        )
    }
}
