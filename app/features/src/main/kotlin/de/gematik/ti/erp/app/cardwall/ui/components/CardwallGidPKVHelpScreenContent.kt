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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString

@Composable
internal fun CardWallGidPKVHelpScreenContent(
    listState: LazyListState,
    onClickOpenSettings: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.XLarge)
    ) {
        CardWallGidPKVHelpScreenHeaderSection()
        CardWallGidPKVFirstTimeTipSection()
        CardWallGidPKVLockedOutTipSection()
        CardWallGidPKVInsuranceMissingTipSection()
        CardWallGidPKVNoIntentTipSection { onClickOpenSettings() }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidPKVHelpScreenHeaderSection() {
    item {
        Column(
            modifier = Modifier.padding(
                top = PaddingDefaults.Medium
            )
        ) {
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_header),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_body),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidPKVFirstTimeTipSection() {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_tip_1_header),
                style = AppTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_1_body_1))
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_1_body_2))
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_1_body_3))
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidPKVLockedOutTipSection() {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_tip_2_header),
                style = AppTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_2_body_1))
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidPKVInsuranceMissingTipSection() {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_tip_3_header),
                style = AppTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_3_body_1))
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.CardWallGidPKVNoIntentTipSection(
    onClickOpenSettings: () -> Unit
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
            Text(
                text = stringResource(R.string.cardwall_gid_pkv_help_screen_tip_4_header),
                style = AppTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            CardWallGidHelpScreenTip(stringResource(R.string.cardwall_gid_pkv_help_screen_tip_4_body_1))
            CardWallGidHelpScreenTip(
                stringResource(R.string.cardwall_gid_pkv_help_screen_tip_4_body_2),
                onClick = onClickOpenSettings,
                buttonText = stringResource(R.string.cardwall_gid_help_settings_button).toAnnotatedString()
            )
        }
    }
}

@LightDarkLongPreview
@Composable
internal fun CardWallGidPKVHelpScreenPreview() {
    PreviewAppTheme {
        CardWallGidPKVHelpScreenContent(
            listState = rememberLazyListState(),
            onClickOpenSettings = { }
        )
    }
}
