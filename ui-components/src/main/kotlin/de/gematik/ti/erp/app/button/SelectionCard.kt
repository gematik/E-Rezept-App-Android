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

package de.gematik.ti.erp.app.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.extensions.accessibility
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

data class SelectionCard(
    val title: String,
    val description: String,
    val isRecommended: Boolean = false,
    val showIcon: Boolean = true
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectionCard(
    modifier: Modifier = Modifier,
    data: SelectionCard,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .accessibility(
                role = Role.Button
            ),
        shape = RoundedCornerShape(SizeDefaults.oneHalf),
        backgroundColor = Color.Transparent,
        contentColor = AppTheme.colors.neutral900,
        border = BorderStroke(
            width = if (data.isRecommended) SizeDefaults.quarter else SizeDefaults.eighth,
            color = AppTheme.colors.primary600
        ),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(ButtonDefaults.ContentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = SizeDefaults.one),
                verticalArrangement = Arrangement.spacedBy(SizeDefaults.half),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = data.title,
                    style = AppTheme.typography.subtitle1,
                    color = AppTheme.colors.neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = data.description,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral600
                )
            }

            if (data.showIcon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun SelectionCardButtonPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.padding(SizeDefaults.double),
            verticalArrangement = Arrangement.spacedBy(SizeDefaults.oneHalf)
        ) {
            SelectionCard(
                data = SelectionCard(
                    title = "Biometrie",
                    description = "Es wird die sicherste verfügbare biometrische Methode genutzt. " +
                        "\nDennoch könnten Angreifende bei Überwindung des lokalen biometrischen \n" +
                        "Systems Zugang zu Ihren medizinischen Daten erhalten.",
                    isRecommended = true,
                    showIcon = true
                )
            ) { }

            SelectionCard(
                data = SelectionCard(
                    title = "Passwort",
                    description = "Geben Sie bei jedem Appstart das Passwort ein.",
                    isRecommended = false,
                    showIcon = false
                )
            ) { }
        }
    }
}
