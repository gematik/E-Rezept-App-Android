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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

sealed interface OnboardingButtonStyle {
    object Filled : OnboardingButtonStyle
    data class Outline(val showLeadingIcon: Boolean = false) : OnboardingButtonStyle
}

@Composable
fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    info: String?,
    buttonText: String,
    buttonEnabled: Boolean,
    buttonModifier: Modifier = Modifier,
    buttonStyle: OnboardingButtonStyle = OnboardingButtonStyle.Filled,
    includeBottomSpacer: Boolean = true,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .padding(horizontal = PaddingDefaults.Medium)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()
        if (info != null) {
            Text(info, style = AppTheme.typography.caption1l, textAlign = TextAlign.Center)
            SpacerShortMedium()
        }

        when (buttonStyle) {
            is OnboardingButtonStyle.Outline -> {
                SecondaryButton(
                    modifier = buttonModifier
                        .widthIn(min = SizeDefaults.twentyfivefold)
                        .heightIn(min = SizeDefaults.sixfold),
                    onClick = onButtonClick,
                    enabled = buttonEnabled,
                    shape = RoundedCornerShape(SizeDefaults.triple),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = AppTheme.colors.neutral000,
                        contentColor = AppTheme.colors.primary700
                    ),
                    contentPadding = PaddingValues(horizontal = PaddingDefaults.XXLargePlus, vertical = PaddingDefaults.MediumSmall)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonText,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (buttonStyle.showLeadingIcon) {
                            val textMeasurer = rememberTextMeasurer()
                            val currentTextStyle = LocalTextStyle.current
                            val density = LocalDensity.current

                            val textMeasurement = remember(buttonText, currentTextStyle) {
                                textMeasurer.measure(buttonText, currentTextStyle)
                            }

                            val buttonTextWidth = with(density) {
                                textMeasurement.size.width.toDp()
                            }

                            val leftIconOffset = -(buttonTextWidth / 2 + PaddingDefaults.Medium)

                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = leftIconOffset)
                                    .size(PaddingDefaults.Large),
                                tint = AppTheme.colors.primary700
                            )
                        }
                    }
                }
            }

            is OnboardingButtonStyle.Filled -> {
                PrimaryButtonSmall(
                    modifier = buttonModifier
                        .widthIn(min = SizeDefaults.twentyfivefold)
                        .heightIn(min = SizeDefaults.sixfold),
                    shape = RoundedCornerShape(SizeDefaults.triple),
                    enabled = buttonEnabled,
                    onClick = onButtonClick
                ) {
                    Text(buttonText)
                }
            }
        }
        if (includeBottomSpacer) {
            SpacerLarge()
        }
    }
}

@LightDarkPreview
@Composable
fun OnboardingBottomBarPreview() {
    PreviewAppTheme {
        Column {
            OnboardingBottomBar(
                info = null,
                buttonText = "Continue",
                buttonEnabled = true,
                buttonStyle = OnboardingButtonStyle.Filled,
                onButtonClick = { }
            )

            OnboardingBottomBar(
                info = "This is test info text",
                buttonText = "Skip",
                buttonEnabled = true,
                buttonStyle = OnboardingButtonStyle.Outline(),
                onButtonClick = { }
            )

            OnboardingBottomBar(
                info = "Back button with icon",
                buttonText = "Back",
                buttonEnabled = true,
                buttonStyle = OnboardingButtonStyle.Outline(showLeadingIcon = true),
                onButtonClick = { }
            )

            OnboardingBottomBar(
                info = "Disabled transparent button",
                buttonText = "Disabled",
                buttonEnabled = false,
                buttonStyle = OnboardingButtonStyle.Filled,
                onButtonClick = { }
            )
        }
    }
}
