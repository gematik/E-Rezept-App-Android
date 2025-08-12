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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun TranslateChip(
    isEnabled: Boolean,
    isTranslationInProgress: Boolean,
    onClick: () -> Unit
) {
    val translationText = if (isEnabled) {
        stringResource(R.string.offline_translation_translate)
    } else {
        stringResource(R.string.offline_translation_show_original)
    }
    TextButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Outlined.Translate,
            tint = AppTheme.colors.primary600,
            contentDescription = translationText
        )
        SpacerSmall()
        Text(
            text = translationText,
            style = AppTheme.typography.body2,
            color = AppTheme.colors.primary600
        )
        if (isTranslationInProgress) {
            SpacerMedium()
            CircularProgressIndicator(
                modifier = Modifier.size(SizeDefaults.double),
                color = AppTheme.colors.primary600,
                strokeWidth = SizeDefaults.fiveEighth
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun TranslateChipPreview() {
    PreviewAppTheme {
        Column {
            TranslateChip(
                isEnabled = false,
                isTranslationInProgress = false,
                onClick = {}
            )
            Divider()
            TranslateChip(
                isEnabled = true,
                isTranslationInProgress = true,
                onClick = {}
            )
        }
    }
}
