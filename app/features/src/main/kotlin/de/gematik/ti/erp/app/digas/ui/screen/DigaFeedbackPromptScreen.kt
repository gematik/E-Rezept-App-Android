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

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.popup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.presentation.rememberDigaFeedbackPromptScreenController
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview

private const val RoundedCornerShapePercent = 50

class DigaFeedbackPromptScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val okButtonText = stringResource(R.string.diga_feedback_ok_button)
        val cancelButtonText = stringResource(R.string.diga_feedback_cancel_button)
        val accessibilityLabel = stringResource(R.string.a11y_diga_feedback_prompt_accessibility_label)

        val controller = rememberDigaFeedbackPromptScreenController()

        FeedbackPromptCard(
            okButtonText = okButtonText,
            cancelButtonText = cancelButtonText,
            accessibilityLabel = accessibilityLabel,
            onFeedbackClick = {
                controller.onFeedbackAccepted()
                navController.navigateUp()
            },
            onDismissClick = navController::navigateUp
        )
    }
}

@Composable
fun FeedbackPromptCard(
    okButtonText: String,
    cancelButtonText: String,
    accessibilityLabel: String,
    onFeedbackClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics {
                contentDescription = accessibilityLabel
                popup()
            }
            .padding(
                horizontal = SizeDefaults.triple,
                vertical = SizeDefaults.fourfold
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.femal_doctor_portrait),
            contentDescription = null,
            modifier = Modifier
                .size(SizeDefaults.twentyfold)
                .clip(CircleShape)
                .background(AppTheme.colors.primary200)
                .border(border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.primary200))
        )

        SpacerMedium()

        // Title
        Text(
            text = stringResource(R.string.diga_feedback_title),
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )

        SpacerSmall()

        // Subtitle
        Text(
            text = stringResource(R.string.diga_feedback_subtitle),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )

        SpacerMedium()

        val contentDescriptionSubmitButton = stringResource(R.string.a11y_diga_feedback_submit)
        // Buttons
        Column(verticalArrangement = Arrangement.spacedBy(SizeDefaults.oneHalf)) {
            Button(
                onClick = onFeedbackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = contentDescriptionSubmitButton
                    },
                shape = RoundedCornerShape(RoundedCornerShapePercent)
            ) {
                Text(okButtonText)
            }

            OutlinedButton(
                onClick = onDismissClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = cancelButtonText
                    },
                shape = RoundedCornerShape(RoundedCornerShapePercent),
                border = BorderStroke(SizeDefaults.eighth, MaterialTheme.colors.primary)
            ) {
                Text(cancelButtonText)
            }
        }
    }
}

@LightDarkPreview
@Composable
internal fun FeedbackPromptCardPreview() {
    PreviewTheme {
        FeedbackPromptCard(
            okButtonText = "Feedback geben",
            cancelButtonText = "Vielleicht später",
            accessibilityLabel = "Feedback zur digitalen Gesundheitsanwendung anfordern",
            onFeedbackClick = {},
            onDismissClick = {}
        )
    }
}
