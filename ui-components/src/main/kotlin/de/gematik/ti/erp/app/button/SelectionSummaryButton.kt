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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

data class SelectionSummaryButtonData(
    val buttonTitleText: String,
    val errorTitleText: String,
    val errorHintText: String,
    val buttonTexts: List<SelectionSummaryButtonText>
)

data class SelectionSummaryButtonText(
    val text: String,
    val style: TextStyle,
    val color: Color,
    val maxLines: Int
)

/**
 * A helper composable function to create a [SelectionSummaryButtonText] instance with consistent styling,
 * using the current Compose theme.
 *
 * This function wraps the creation in [remember] to avoid unnecessary recompositions when the text does not change,
 * which is particularly useful when passing repeated values in recomposing components like [SelectionSummaryButton].
 *
 * ### Usage:
 * ```
 * val summaryText = selectionSummaryButtonText("Muster Apotheke")
 * SelectionSummaryButton(
 *     data = SelectionSummaryButtonData(
 *         buttonTitleText = "Apotheke",
 *         errorTitleText = "Apotheke wählen",
 *         errorHintText = "Bitte wählen Sie eine Apotheke",
 *         buttonTexts = listOf(summaryText)
 *     ),
 *     isError = false,
 *     onClick = { /* ... */ }
 * )
 * ```
 *
 * @param text The visible text to display in the button content area.
 * @param style The text style to apply (default is [AppTheme.typography.subtitle1]).
 * @param color The color of the text (default is [AppTheme.colors.neutral600]).
 * @param maxLines The maximum number of lines this text may occupy.
 *
 * @return A [SelectionSummaryButtonText] model used in [SelectionSummaryButtonData].
 */
@Composable
fun selectionSummaryButtonText(
    text: String,
    style: TextStyle = AppTheme.typography.body1,
    color: Color = AppTheme.colors.neutral900,
    maxLines: Int = 1
): SelectionSummaryButtonText {
    return remember(text) {
        SelectionSummaryButtonText(
            text = text,
            style = style,
            color = color,
            maxLines = maxLines
        )
    }
}

/**
 * A configurable button component that displays a title, a list of optional content lines,
 * and optionally highlights an error state when no content is provided. Typically used for
 * summary selection cards like prescriptions or pharmacies.
 *
 * ### Example usage:
 * ```
 * SelectionSummaryButton(
 *     data = SelectionSummaryButtonData(
 *         buttonTitleText = "Apotheke",
 *         errorTitleText = "Apotheke wählen",
 *         buttonTexts = listOf(
 *             selectionSummaryButtonText("Apotheke Mustermann"),
 *             selectionSummaryButtonText("Berlin")
 *         ),
 *         errorHintText = "Bitte wählen Sie eine Apotheke"
 *     ),
 *     isError = false,
 *     onClick = { /* navigate to selection screen */ }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the button container.
 * @param data Holds the title, content lines, error title, and error hint text. See [SelectionSummaryButtonData].
 * @param isError Whether the button is in an error state (e.g., required selection not made).
 * @param errorContentDescription Localized prefix (e.g. "Fehler:") to be included in screen reader descriptions when [isError] is true.
 * @param leadingContent Optional composable (e.g. icon or image) displayed at the start of the button.
 * @param bottomContent Optional additional UI displayed beneath the main content area (inside the button).
 * @param onClick Lambda triggered when the user taps the button.
 *
 * ### Accessibility:
 * - Uses [errorContentDescription] + [data.errorHintText] to provide a readable description for screen readers in error state.
 * - Ensure [buttonTitleText] and each [buttonTexts] item are user-understandable for assistive technologies.
 */
@Composable
fun SelectionSummaryButton(
    modifier: Modifier = Modifier,
    data: SelectionSummaryButtonData,
    overrideIcon: Boolean = false,
    isError: Boolean,
    errorContentDescription: String = "",
    leadingContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
    onClick: () -> Unit
) {
    val borderColor = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral300
    val contentColor = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral900
    val hintColor = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral600

    val hasContent = data.buttonTexts.isNotEmpty()

    val semanticsErrorDescription = buildString {
        if (isError) {
            append(", $errorContentDescription ${data.errorHintText}")
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(SizeDefaults.double),
            contentPadding = PaddingValues(SizeDefaults.double),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(SizeDefaults.eighth, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingContent?.let {
                        Box(modifier = Modifier.padding(end = PaddingDefaults.Medium)) {
                            it()
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(SizeDefaults.quarter)
                    ) {
                        if (hasContent) {
                            // title text
                            Text(
                                text = data.buttonTitleText,
                                style = AppTheme.typography.caption1,
                                color = AppTheme.colors.neutral600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // list of content
                            data.buttonTexts.forEach { text ->
                                Text(
                                    text = text.text,
                                    style = text.style,
                                    color = text.color,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = text.maxLines
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
                    Spacer(modifier = Modifier.weight(0.02f))
                    if (!overrideIcon) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                tint = if (isError) AppTheme.colors.red900 else AppTheme.colors.neutral600,
                                contentDescription = null
                            )
                        }
                    }
                }

                // bottom content if we are extending this
                bottomContent?.invoke(this)
            }
        }

        if (isError) {
            Text(
                text = data.errorHintText,
                style = AppTheme.typography.caption1,
                color = hintColor,
                modifier = Modifier
                    .padding(start = SizeDefaults.half)
                    .semantics {
                        contentDescription = semanticsErrorDescription
                    }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun SelectionSummaryButtonPrescriptionsWithErrorPreview() {
    PreviewTheme {
        SelectionSummaryButton(
            data = SelectionSummaryButtonData(
                buttonTitleText = "Rezepte",
                errorTitleText = "Rezept wählen",
                buttonTexts = emptyList(),
                errorHintText = "Bitte wählen Sie ein Rezept"
            ),
            isError = true
        ) { }
    }
}

@LightDarkPreview
@Composable
fun SelectionSummaryButtonPrescriptionsPreview() {
    PreviewTheme {
        SelectionSummaryButton(
            data = SelectionSummaryButtonData(
                buttonTitleText = "Rezepte",
                errorTitleText = "Rezept wählen",
                buttonTexts = listOf(
                    selectionSummaryButtonText(
                        "Zampa-Zok mitte 47,5 mg, Zampa-Zok mitte 22,5 mg"
                    )
                ),
                errorHintText = "Bitte wählen Sie ein Rezept"
            ),
            isError = false
        ) { }
    }
}

@LightDarkPreview
@Composable
fun SelectionSummaryButtonPharmacyPreview() {
    PreviewTheme {
        SelectionSummaryButton(
            data = SelectionSummaryButtonData(
                buttonTitleText = "Apotheke",
                errorTitleText = "Apotheke wählen",
                buttonTexts = listOf(
                    selectionSummaryButtonText(
                        text = "Albrecht Apotheke ULMENDORFER Test Only",
                        style = AppTheme.typography.body1l,
                        color = AppTheme.colors.neutral900,
                        maxLines = 5
                    ),
                    selectionSummaryButtonText(
                        text = "Hubertus Strasse 12",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral400
                    ),
                    selectionSummaryButtonText(
                        text = "12099 Berlin",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral400
                    )
                ),
                errorHintText = "Bitte wählen Sie ein Apotheke"
            ),
            isError = false
        ) {
            // extra area for more composable view
        }
    }
}

@LightDarkPreview
@Composable
fun SelectionSummaryButtonProfileImagePreview() {
    PreviewTheme {
        SelectionSummaryButton(
            data = SelectionSummaryButtonData(
                buttonTitleText = "Profil",
                errorTitleText = "",
                buttonTexts = listOf(
                    selectionSummaryButtonText(
                        text = "Ada Muster",
                        style = AppTheme.typography.body1l,
                        color = AppTheme.colors.neutral900,
                        maxLines = 1
                    )
                ),
                errorHintText = "Bitte geben Sie eine Telefonnummer an"
            ),
            isError = true,
            overrideIcon = true,
            leadingContent = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                    contentDescription = "Profile icon",
                    modifier = Modifier.padding(end = SizeDefaults.half)
                )
            }
        ) {}
    }
}
