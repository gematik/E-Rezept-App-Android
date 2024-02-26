/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.Strength
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.io.Resource
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme

private const val TEN_PERCENT = 0.1f
private const val THIRTY_PERCENT = 0.3f
private const val SIXTY_PERCENT = 0.6f
private const val ONE_HUNDRED_PERCENT = 1.0f
private const val FIVE_PERCENT = 0.05f
private const val MINIMAL_PASSWORD_SCORE = 2

private enum class PasswordEvaluation {
    VeryWeak, Weak, Strong, VeryStrong
}

@Requirement(
    "O.Pass_1",
    "O.Pass_2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "To determine the strength of the password, we use Zxcvbn with an additional german dictionary" +
        "The strength of the password is shown with bars and colors. " +
        "The minimum acceptable value of the score must be > 2."
)
@Composable
fun PasswordStrength(
    modifier: Modifier,
    password: String,
    onScoreChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val assetManager = context.assets
    val germanDictionaryFile = assetManager.open("german_dictionary.txt")
    val germanDictionaryRessource = Resource { germanDictionaryFile }

    val passwordStrengthEvaluator = ZxcvbnBuilder().dictionaries(
        StandardDictionaries.loadAllDictionaries()
    )
        .dictionary(DictionaryLoader("german_dictionary", germanDictionaryRessource).load())
        .build()

    val strength = remember(password) { passwordStrengthEvaluator.measure(password) }
    val barLength by animateFloatAsState(
        when (strength.score) {
            PasswordEvaluation.VeryWeak.ordinal -> TEN_PERCENT
            PasswordEvaluation.Weak.ordinal -> THIRTY_PERCENT
            PasswordEvaluation.Strong.ordinal -> SIXTY_PERCENT
            PasswordEvaluation.VeryStrong.ordinal -> ONE_HUNDRED_PERCENT
            else -> FIVE_PERCENT
        },
        label = ""
    )
    val barColor by animateColorAsState(
        when (strength.score) {
            PasswordEvaluation.VeryWeak.ordinal -> AppTheme.colors.red400
            PasswordEvaluation.Weak.ordinal -> AppTheme.colors.red300
            PasswordEvaluation.Strong.ordinal -> AppTheme.colors.yellow500
            PasswordEvaluation.VeryStrong.ordinal -> AppTheme.colors.green500
            else -> AppTheme.colors.red500
        },
        label = ""
    )

    DisposableEffect(strength) {
        onScoreChange(strength.score)
        onDispose { }
    }

    Column(
        modifier = modifier
            .semantics(true) {
                stateDescription = if (validatePasswordScore(strength.score)) "sufficient" else "insufficient"
            }
    ) {
        Suggestion(strength)
        PasswordStrengthIndicator(barLength, barColor)
        PasswordStrengthDescription(strength)
    }
}

@Composable
fun Suggestion(strength: Strength) {
    val suggestions = strength.feedback.suggestions.joinToString("\n").trim()
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
fun PasswordStrengthIndicator(barLength: Float, barColor: Color) {
    SpacerMedium()
    Box(
        modifier = Modifier
            .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(6.dp))
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .background(color = barColor, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth(barLength)
                .height(6.dp)
        )
    }
    SpacerTiny()
}

@Composable
fun PasswordStrengthDescription(strength: Strength) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            strength.score == PasswordEvaluation.VeryStrong.ordinal -> {
                Text(
                    stringResource(R.string.settings_password_strength_very_good),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.green600)
            }
            strength.score > MINIMAL_PASSWORD_SCORE -> {
                Text(
                    stringResource(R.string.settings_password_strength_sufficient),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.green600)
            }
            else -> {
                Text(
                    stringResource(R.string.settings_password_strength_not_sufficient),
                    style = AppTheme.typography.body2l
                )
                SpacerTiny()
                Icon(Icons.Rounded.Close, null, tint = AppTheme.colors.red600)
            }
        }
    }
}

fun validatePasswordScore(score: Int): Boolean =
    score > MINIMAL_PASSWORD_SCORE

@LightDarkPreview
@Composable
fun PasswordStrengthPreview() {
    PreviewAppTheme {
        PasswordStrength(
            Modifier,
            "",
            onScoreChange = {}
        )
    }
}
