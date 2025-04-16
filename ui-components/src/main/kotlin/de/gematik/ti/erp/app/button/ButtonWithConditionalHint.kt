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

package de.gematik.ti.erp.app.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

data class ButtonWithConditionalHintData(
    val buttonTitleText: String,
    val errorTitleText: String,
    val buttonTexts: List<String>,
    val hintText: String
)

@Composable
fun ButtonWithConditionalHint(
    modifier: Modifier = Modifier,
    data: ButtonWithConditionalHintData,
    isError: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isError) AppTheme.colors.red300 else AppTheme.colors.neutral300
    val contentColor = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral900
    val hintColor = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral600

    val hasContent = data.buttonTexts.isNotEmpty()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.half)
    ) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(SizeDefaults.oneHalf),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(SizeDefaults.eighth, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SizeDefaults.quarter)
            ) {
                // title text
                if (!isError && hasContent) {
                    Text(
                        text = data.buttonTitleText,
                        style = AppTheme.typography.caption1,
                        color = AppTheme.colors.neutral600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // list of content
                if (hasContent) {
                    data.buttonTexts.forEach { text ->
                        Text(
                            text = text,
                            style = AppTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                } else {
                    // error or empty scenario when there is no content
                    Text(
                        text = data.errorTitleText,
                        color = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral700,
                        style = AppTheme.typography.subtitle1l,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }

        if (isError) {
            Text(
                text = data.hintText,
                style = AppTheme.typography.caption1,
                color = hintColor,
                modifier = Modifier.padding(start = SizeDefaults.half)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun ButtonWithConditionalHintWithErrorPreview() {
    PreviewTheme {
        ButtonWithConditionalHint(
            data = ButtonWithConditionalHintData(
                buttonTitleText = "Rezepte",
                errorTitleText = "Rezept wählen",
                buttonTexts = emptyList(),
                hintText = "Bitte wählen Sie ein Rezept"
            ),
            isError = true
        ) { }
    }
}

@LightDarkPreview
@Composable
fun ButtonWithConditionalHintWithoutErrorNoPrescriptionPreview() {
    PreviewTheme {
        ButtonWithConditionalHint(
            data = ButtonWithConditionalHintData(
                buttonTitleText = "Rezepte",
                errorTitleText = "Rezept wählen",
                buttonTexts = emptyList(),
                hintText = "Bitte wählen Sie ein Rezept"
            ),
            isError = false
        ) { }
    }
}

@LightDarkPreview
@Composable
fun ButtonWithConditionalHintWithoutErrorPreview() {
    PreviewTheme {
        ButtonWithConditionalHint(
            data = ButtonWithConditionalHintData(
                buttonTitleText = "Rezepte",
                errorTitleText = "Rezept wählen",
                buttonTexts = listOf("Zampa-Zok mitte 47,5 mg, Zampa-Zok mitte 22,5 mg"),
                hintText = "Bitte wählen Sie ein Rezept"
            ),
            isError = false
        ) { }
    }
}
