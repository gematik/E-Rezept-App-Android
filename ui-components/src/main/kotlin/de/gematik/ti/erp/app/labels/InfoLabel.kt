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

package de.gematik.ti.erp.app.labels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@Suppress("MagicNumber")
@Composable
fun InfoLabel(
    modifier: Modifier = Modifier,
    text: String,
    infoIcon: ImageVector = Icons.Outlined.AutoAwesome,
    closeIcon: ImageVector = Icons.Outlined.Close,
    contentColor: Color = AppTheme.colors.primary900,
    containerColor: Color = AppTheme.colors.primary100,
    onClose: () -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            contentColor = contentColor,
            containerColor = containerColor
        ),
        border = BorderStroke(SizeDefaults.sixteenth, contentColor)
    ) {
        Row(
            modifier = Modifier
                .height(SizeDefaults.sevenfoldAndHalf) // 60.dp
                .padding(PaddingDefaults.Small)
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .weight(0.07f),
                imageVector = infoIcon,
                contentDescription = null
            )
            SpacerMedium()
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .weight(0.8f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text
                )
            }
            SpacerMedium()
            IconButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .weight(0.07f),
                onClick = onClose
            ) {
                Icon(
                    imageVector = closeIcon,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun BoxScope.InfoLabelInBox(
    text: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = PaddingDefaults.Medium)
            .padding(horizontal = PaddingDefaults.Large)
            .fillMaxWidth()
            .align(Alignment.TopCenter),
        contentAlignment = Alignment.TopCenter
    ) {
        InfoLabel(
            text = text,
            onClose = onClose
        )
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun InfoLabelPreview() {
    PreviewTheme {
        InfoLabel(
            text = """
                Information that is to be shown in the info label. More text to make the text bigger
            """.trimIndent()
        ) {}
    }
}
