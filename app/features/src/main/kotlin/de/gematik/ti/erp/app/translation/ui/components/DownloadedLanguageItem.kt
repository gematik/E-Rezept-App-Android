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

package de.gematik.ti.erp.app.translation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.translation.domain.model.DownloadedLanguage
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
internal fun DownloadedLanguageItem(
    language: DownloadedLanguage,
    onTargetClick: (DownloadedLanguage) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(SizeDefaults.one),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = SizeDefaults.double, vertical = SizeDefaults.oneHalf),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = language.isTarget,
                onClick = { onTargetClick(language) }
            )
            Text(
                text = language.displayName,
                color = if (language.deletable) AppTheme.colors.neutral800 else AppTheme.colors.neutral400,
                style = AppTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )
            if (language.deletable) {
                IconButton(
                    onClick = {
                        onDeleteClick(language.code)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        tint = AppTheme.colors.red400,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .padding(PaddingDefaults.Medium)
                            .size(SizeDefaults.triple)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    tint = AppTheme.colors.neutral400,
                    contentDescription = "Not deletable",
                    modifier = Modifier
                        .padding(PaddingDefaults.Medium)
                        .size(SizeDefaults.triple)
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun ClickableTextFieldNonDeletablePreview() {
    PreviewAppTheme {
        DownloadedLanguageItem(
            language = DownloadedLanguage(
                isTarget = false,
                code = "de",
                displayName = "Deutsch",
                deletable = false
            ),
            onTargetClick = {},
            onDeleteClick = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun ClickableTextFieldDeletablePreview() {
    PreviewAppTheme {
        DownloadedLanguageItem(
            language = DownloadedLanguage(
                isTarget = true,
                code = "de",
                displayName = "Deutsch",
                deletable = true
            ),
            onTargetClick = {},
            onDeleteClick = {}
        )
    }
}
