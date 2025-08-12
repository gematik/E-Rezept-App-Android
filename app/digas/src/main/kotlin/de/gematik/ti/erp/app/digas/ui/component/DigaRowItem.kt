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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.animated.RotatingHourglassIcon
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.model.DigaStatusSteps
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

@Composable
fun RequestRowItem(current: DigaStatus) {
    DigaStepRowItem(
        currentStatus = current,
        process = DigaStatus.Ready,
        stepNumber = 1,
        defaultText = R.string.request_unlock_code,
        completedText = R.string.unlock_code_requested
    )
}

@Composable
fun InsuranceRowItem(currentProcess: DigaStatus, code: String, declineNote: String?, onClick: () -> Unit = {}, onRegisterFeedback: () -> Unit = {}) {
    when {
        currentProcess.step > DigaStatusSteps.InProgress.step || currentProcess == DigaStatus.CompletedSuccessfully -> {
            onRegisterFeedback()
            DigaRowItem(
                text = stringResource(R.string.code_received),
                contentDescr = stringResource(R.string.a11y_diga_code_received),
                copyCode = code,
                onClick = onClick
            )
        }

        currentProcess is DigaStatus.CompletedWithRejection -> {
            onRegisterFeedback()
            DigaRowItem(text = stringResource(R.string.insurance_notification))
            Row(
                modifier = Modifier.padding(start = PaddingDefaults.XLarge)
            ) {
                SpacerSmall()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.red100, shape = RoundedCornerShape(PaddingDefaults.Small))
                        .padding(vertical = PaddingDefaults.Small, horizontal = PaddingDefaults.ShortMedium)
                ) {
                    val note = declineNote ?: stringResource(R.string.insurance_not_responsible_text)
                    Text(
                        style = AppTheme.typography.body2,
                        text = note
                    )
                }
            }
        }

        currentProcess.step == DigaStatusSteps.InProgress.step -> {
            DigaRowItem(
                isWaiting = true,
                stepNumber = 2,
                backgroundColor = AppTheme.colors.yellow100,
                text = stringResource(R.string.waiting_for_insurance_code),
                contentColor = AppTheme.colors.yellow900
            )
        }

        else -> {
            DigaRowItem(stepNumber = 2, text = stringResource(R.string.insurance_unlocks_code))
        }
    }
}

@Composable
fun DownloadRowItem(current: DigaStatus) {
    DigaStepRowItem(
        currentStatus = current,
        process = if (current == DigaStatus.CompletedSuccessfully) current else DigaStatus.DownloadDigaApp,
        stepNumber = 3,
        defaultText = R.string.download_diga_app,
        completedText = R.string.diga_app_downloaded
    )
}

@Composable
fun ActivateRowItem(current: DigaStatus) {
    DigaStepRowItem(
        currentStatus = current,
        process = DigaStatus.OpenAppWithRedeemCode,
        stepNumber = 4,
        defaultText = R.string.enter_code_in_diga_app,
        completedText = R.string.code_entered_in_diga_app
    )
}

@Composable
fun DigaStepRowItem(
    currentStatus: DigaStatus,
    process: DigaStatus,
    stepNumber: Int,
    defaultText: Int,
    completedText: Int
) {
    val isCurrent = currentStatus.step == process.step
    val isAfter = currentStatus.step > process.step
    val chipStepNumber = if (!isAfter) stepNumber else 0

    val backgroundColor = if (isCurrent) AppTheme.colors.primary100 else AppTheme.colors.neutral000
    val textRes = when {
        isAfter -> completedText
        isCurrent -> defaultText
        else -> defaultText
    }

    DigaRowItem(
        stepNumber = chipStepNumber,
        isCurrent = isCurrent,
        backgroundColor = backgroundColor,
        text = stringResource(textRes)
    )
}

@Composable
fun DigaRowItem(
    text: String = "",
    contentDescr: String? = null,
    stepNumber: Int = 0,
    backgroundColor: Color = AppTheme.colors.neutral000,
    contentColor: Color = AppTheme.colors.neutral900,
    icon: ImageVector = Icons.Filled.Check,
    isWaiting: Boolean = false,
    isCurrent: Boolean = false,
    copyCode: String = "",
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(PaddingDefaults.Small))
            .padding(vertical = PaddingDefaults.Small, horizontal = PaddingDefaults.ShortMedium)
            .semantics(mergeDescendants = true) {}
    ) {
        Row {
            Column(
                Modifier
                    .width(SizeDefaults.doubleHalf)
                    .height(SizeDefaults.doubleHalf),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isWaiting -> RotatingHourglassIcon()

                    stepNumber > 0 -> {
                        val stepNumberText = stringResource(R.string.step, stepNumber)
                        val isActiveText = if (isCurrent) stringResource(R.string.active) else ""
                        Text(
                            modifier = Modifier.clearAndSetSemantics {
                                contentDescription = isActiveText
                                stateDescription = stepNumberText
                            },
                            text = "$stepNumber.",
                            style = AppTheme.typography.body2
                        )
                    }

                    else -> Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.step_completed)
                    )
                }
            }
            SpacerSmall()
            Text(
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = contentDescr ?: text
                },
                text = text,
                style = AppTheme.typography.body2,
                color = contentColor
            )
            if (onClick != null && copyCode.isNotBlank()) {
                @Suppress("MagicNumber")
                Text(
                    text = copyCode,
                    style = AppTheme.typography.body2,
                    fontWeight = FontWeight(800)
                )
                SpacerSmall()
                Icon(
                    modifier = Modifier.clickable(onClick = onClick::invoke, role = Role.Button),
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.copy_code)
                )
            }
        }
    }
}
