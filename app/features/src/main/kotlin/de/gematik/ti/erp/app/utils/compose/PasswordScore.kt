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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

private const val TEN_PERCENT = 0.1f
private const val THIRTY_PERCENT = 0.3f
private const val SIXTY_PERCENT = 0.6f
private const val ONE_HUNDRED_PERCENT = 1.0f
private const val FIVE_PERCENT = 0.05f

enum class PasswordScore {
    Uninitialised, VeryWeak, Weak, Strong, VeryStrong
}

data class PasswordEvaluation(
    val score: PasswordScore,
    val feedback: String
) {
    val isStrongEnough = score >= PasswordScore.Strong
}

@Requirement(
    "O.Pass_1#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Implementation of password strength validation using Zxcvbn with an additional german dictionary"
)
@Requirement(
    "O.Pass_2#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Shows password strength with a colored bar and a description"
)
@Composable
fun PasswordStrength(
    modifier: Modifier,
    passwordEvaluation: PasswordEvaluation
) {
    val sufficientText = stringResource(id = R.string.settings_password_sufficient)
    val notSufficientText = stringResource(id = R.string.settings_password_not_sufficient)
    Column(
        modifier = modifier
            .semantics(true) {
                stateDescription = if (passwordEvaluation.isStrongEnough) sufficientText else notSufficientText
            }
    ) {
        Suggestion(passwordEvaluation.feedback)
        PasswordStrengthIndicator(passwordEvaluation.score)
        PasswordStrengthDescription(passwordEvaluation.score)
    }
}

@Composable
fun Suggestion(suggestions: String) {
    if (suggestions.isNotEmpty()) {
        Text(
            text = annotatedStringResource(
                R.string.settings_password_suggestions,
                suggestions
            ),
            style = AppTheme.typography.caption1l
        )
    }
}

@Composable
fun PasswordStrengthIndicator(passwordScore: PasswordScore) {
    val barLength by animateFloatAsState(
        when (passwordScore) {
            PasswordScore.Uninitialised -> FIVE_PERCENT
            PasswordScore.VeryWeak -> TEN_PERCENT
            PasswordScore.Weak -> THIRTY_PERCENT
            PasswordScore.Strong -> SIXTY_PERCENT
            PasswordScore.VeryStrong -> ONE_HUNDRED_PERCENT
        },
        label = ""
    )
    val barColor by animateColorAsState(
        when (passwordScore) {
            PasswordScore.Uninitialised -> AppTheme.colors.red500
            PasswordScore.VeryWeak -> AppTheme.colors.red400
            PasswordScore.Weak -> AppTheme.colors.red300
            PasswordScore.Strong -> AppTheme.colors.yellow500
            PasswordScore.VeryStrong -> AppTheme.colors.green500
        },
        label = ""
    )

    SpacerMedium()
    Box(
        modifier = Modifier
            .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(SizeDefaults.threeQuarter))
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .background(color = barColor, shape = RoundedCornerShape(SizeDefaults.threeQuarter))
                .fillMaxWidth(barLength)
                .height(SizeDefaults.threeQuarter)
        )
    }
    SpacerTiny()
}

@Composable
fun PasswordStrengthDescription(passwordScore: PasswordScore) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (passwordScore) {
            PasswordScore.VeryStrong -> {
                Text(
                    stringResource(R.string.settings_password_very_good),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.green600)
            }
            PasswordScore.Strong -> {
                Text(
                    stringResource(R.string.settings_password_sufficient),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.green600)
            }
            else -> {
                Text(
                    stringResource(R.string.settings_password_not_sufficient),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Close, null, tint = AppTheme.colors.red600)
            }
        }
    }
}

@LightDarkPreview
@Composable
fun PasswordStrengthPreview() {
    PreviewAppTheme {
        PasswordStrength(
            modifier = Modifier,
            passwordEvaluation = PasswordEvaluation(
                PasswordScore.Strong,
                "This is a strong password"
            )
        )
    }
}
