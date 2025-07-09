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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.extensions.accessibility
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun OnboardingProgressIndicator(
    currentStep: Int?,
    totalSteps: Int = 3,
    modifier: Modifier = Modifier,
    activeColor: Color = AppTheme.colors.primary700,
    inactiveColor: Color = AppTheme.colors.primary200,
    size: Dp = SizeDefaults.oneHalf,
    spacing: Dp = SizeDefaults.fivefoldHalf
) {
    val indicatorHeight = size
    val indicatorWidth = size + SizeDefaults.tripleHalf
    val cornerRadius = indicatorHeight / 2

    val progressDescription = currentStep?.let { step ->
        stringResource(R.string.onboarding_progress_description, step, totalSteps)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .accessibility(
                contentDescriptionString = progressDescription
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCurrentStep = index == currentStep?.minus(1)

            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        color = if (isCurrentStep) activeColor else inactiveColor
                    )
                    .clearAndSetSemantics { }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun OnboardingDataProtectionAndTermsOfUseOverviewScreenContentPreview2() {
    PreviewAppTheme {
        OnboardingProgressIndicator(
            currentStep = 1,
            totalSteps = 3,
            modifier = Modifier.padding(PaddingDefaults.Medium),
            activeColor = AppTheme.colors.primary600

        )
    }
}
