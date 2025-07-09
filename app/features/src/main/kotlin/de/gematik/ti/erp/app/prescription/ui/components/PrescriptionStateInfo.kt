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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.model.SentOrCompletedPhrase
import de.gematik.ti.erp.app.prescription.ui.model.sentOrCompleted
import de.gematik.ti.erp.app.prescription.ui.preview.prescriptionStatePreviews
import de.gematik.ti.erp.app.prescription.ui.preview.prescriptionStatePreviewsNearDayEnd
import de.gematik.ti.erp.app.prescription.ui.screen.ONE_DAY_LEFT
import de.gematik.ti.erp.app.prescription.ui.screen.TWO_DAYS_LEFT
import de.gematik.ti.erp.app.prescription.ui.screen.ZERO_DAYS_LEFT
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

// todo: split into smaller functions ready/laterRedeemable/inProgress/pending/deleted/expired/other
@Suppress("CyclomaticComplexMethod")
@Composable
fun PrescriptionStateInfo(
    state: SyncedTaskData.SyncedTask.TaskState,
    now: Instant = Clock.System.now(),
    textColor: Color = AppTheme.colors.neutral800,
    textAlign: TextAlign = TextAlign.Left
) {
    val warningAmber =
        mapOf(
            "warningAmber" to
                InlineTextContent(
                    Placeholder(
                        width = 0.em,
                        height = 0.em,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        modifier = Modifier.padding(end = PaddingDefaults.Tiny),
                        contentDescription = null,
                        tint = AppTheme.colors.red600
                    )
                }
        )

    when (state) {
        is SyncedTaskData.SyncedTask.LaterRedeemable -> {
            Text(
                text =
                dateWithIntroductionString(
                    R.string.pres_detail_medication_redeemable_on,
                    state.redeemableOn
                ),
                color = textColor,
                style = AppTheme.typography.body2l,
                textAlign = textAlign
            )
        }

        is SyncedTaskData.SyncedTask.Ready -> {
            val acceptDaysLeft = state.acceptDaysLeft(now)
            val expiryDaysLeft = state.expiryDaysLeft(now)
            val text = readyPrescriptionStateInfo(acceptDaysLeft, expiryDaysLeft)

            when {
                acceptDaysLeft in ZERO_DAYS_LEFT..TWO_DAYS_LEFT ||
                    expiryDaysLeft in ZERO_DAYS_LEFT..TWO_DAYS_LEFT ->
                    text?.let {
                        DynamicText(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.body2,
                            color = AppTheme.colors.red600,
                            inlineContent = warningAmber,
                            textAlign = textAlign
                        )
                    }

                acceptDaysLeft > TWO_DAYS_LEFT || expiryDaysLeft > TWO_DAYS_LEFT ->
                    text?.let { Text(it, style = AppTheme.typography.body2, color = textColor, textAlign = textAlign) }

                else -> {}
            }
        }

        is SyncedTaskData.SyncedTask.Provided -> {
            val text = sentOrCompletedPhrase(state.lastMedicationDispense, now, state = state)
            Text(text, style = AppTheme.typography.body2, color = textColor, textAlign = textAlign)
        }

        is SyncedTaskData.SyncedTask.InProgress -> {
            val text = sentOrCompletedPhrase(state.lastModified, now, state = state)
            Text(text, style = AppTheme.typography.body2, color = textColor, textAlign = textAlign)
        }

        is SyncedTaskData.SyncedTask.Pending -> {
            val text = sentOrCompletedPhrase(state.sentOn, now, state = state)
            Text(text, style = AppTheme.typography.body2, color = textColor, textAlign = textAlign)
        }

        is SyncedTaskData.SyncedTask.Deleted -> {
            Text(
                dateWithIntroductionString(
                    R.string.pres_detail_medication_deleted,
                    state.lastModified
                ),
                style = AppTheme.typography.body2,
                color = textColor,
                textAlign = textAlign
            )
        }

        is SyncedTaskData.SyncedTask.Expired -> {
            Text(
                dateWithIntroductionString(
                    R.string.pres_detail_medication_expired_on,
                    state.expiredOn
                ),
                style = AppTheme.typography.body2,
                color = textColor,
                textAlign = textAlign
            )
        }

        is SyncedTaskData.SyncedTask.Other -> {
            if (state.state == SyncedTaskData.TaskStatus.Completed) {
                val text = sentOrCompletedPhrase(state.lastModified, now, true, state = state)
                Text(text, style = AppTheme.typography.body2, color = textColor, textAlign = textAlign)
            }
        }
    }
}

@Composable
fun readyPrescriptionStateInfo(
    acceptDaysLeft: Int,
    expiryDaysLeft: Int
): AnnotatedString? =
    when {
        acceptDaysLeft == ZERO_DAYS_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(stringResource(R.string.prescription_item_accept_only_today))
            }

        expiryDaysLeft == ZERO_DAYS_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(stringResource(R.string.prescription_item_expiration_only_today))
            }

        acceptDaysLeft == ONE_DAY_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(stringResource(R.string.prescription_item_accept_only_tomorrow))
            }

        expiryDaysLeft == ONE_DAY_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(
                    stringResource(R.string.prescription_item_expiration_only_tomorrow)
                )
            }

        acceptDaysLeft == TWO_DAYS_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(
                    annotatedStringResource(
                        R.string.prescription_item_two_accept_days_left,
                        AnnotatedString(acceptDaysLeft.toString())
                    )
                )
            }

        expiryDaysLeft == TWO_DAYS_LEFT ->
            buildAnnotatedString {
                appendInlineContent(
                    id = "warningAmber",
                    alternateText = stringResource(R.string.prescription_item_warning_amber)
                )
                append(
                    annotatedStringResource(
                        R.string.prescription_item_two_expiration_days_left,
                        AnnotatedString(expiryDaysLeft.toString())
                    )
                )
            }

        acceptDaysLeft > TWO_DAYS_LEFT ->
            annotatedStringResource(
                R.string.prescription_item_accept_days_left,
                AnnotatedString((acceptDaysLeft).toString())
            )

        expiryDaysLeft > TWO_DAYS_LEFT ->
            annotatedStringResource(
                R.string.prescription_item_expiration_days_left,
                AnnotatedString((expiryDaysLeft).toString())
            )

        else -> null
    }

@Suppress("CyclomaticComplexMethod")
@Composable
private fun sentOrCompletedPhrase(
    lastModified: Instant,
    now: Instant,
    completed: Boolean = false,
    state: SyncedTaskData.SyncedTask.TaskState
): String {
    val formatter = rememberErpTimeFormatter()
    return when (
        val phrase = sentOrCompleted(
            lastModified = lastModified,
            now = now,
            completed = completed,
            provided = state is SyncedTaskData.SyncedTask.Provided
        )
    ) {
        SentOrCompletedPhrase.RedeemedJustNow -> stringResource(R.string.received_now)
        SentOrCompletedPhrase.SentJustNow -> {
            if (state is SyncedTaskData.SyncedTask.Pending) {
                stringResource(R.string.sent_now)
            } else {
                stringResource(R.string.accept_now)
            }
        }

        is SentOrCompletedPhrase.ProvidedHoursAgo -> {
            annotatedStringResource(
                R.string.provided_at_hour,
                remember { formatter.time(lastModified) }
            ).toString()
        }
        SentOrCompletedPhrase.ProvidedJustNow -> stringResource(R.string.provided_now)
        is SentOrCompletedPhrase.ProvidedMinutesAgo ->
            annotatedStringResource(R.string.provided_minutes_ago, phrase.minutes).toString()

        is SentOrCompletedPhrase.ProvidedOn -> annotatedStringResource(
            R.string.provided_on_date,
            remember { formatter.date(phrase.on) }
        ).toString()

        is SentOrCompletedPhrase.RedeemedMinutesAgo -> {
            annotatedStringResource(R.string.received_x_min_ago, phrase.minutes).toString()
        }

        is SentOrCompletedPhrase.SentMinutesAgo -> {
            val resourceId =
                if (state is SyncedTaskData.SyncedTask.Pending) {
                    R.string.sent_x_min_ago
                } else {
                    R.string.accept_x_min_ago
                }
            annotatedStringResource(resourceId, phrase.minutes).toString()
        }

        is SentOrCompletedPhrase.RedeemedHoursAgo -> {
            annotatedStringResource(
                R.string.received_on_minute,
                remember { formatter.time(lastModified) }
            ).toString()
        }

        is SentOrCompletedPhrase.SentHoursAgo -> {
            val resourceId =
                if (state is SyncedTaskData.SyncedTask.Pending) {
                    R.string.sent_on_minute
                } else {
                    R.string.accept_on_minute
                }
            annotatedStringResource(
                resourceId,
                remember { formatter.time(lastModified) }
            ).toString()
        }

        is SentOrCompletedPhrase.RedeemedOn -> {
            annotatedStringResource(
                R.string.received_on_day,
                remember { formatter.date(phrase.on) }
            ).toString()
        }

        is SentOrCompletedPhrase.SentOn -> {
            val resourceId =
                if (state is SyncedTaskData.SyncedTask.Pending) {
                    R.string.sent_on_day
                } else {
                    R.string.accept_on_day
                }
            annotatedStringResource(
                resourceId,
                remember { formatter.date(phrase.on) }
            ).toString()
        }
    }
}

@Preview
@Composable
fun PrescriptionStateInfosCombinedPreview() {
    PreviewAppTheme {
        Column {
            prescriptionStatePreviews.forEach { previewData ->
                Text(
                    previewData.name,
                    style = AppTheme.typography.caption2,
                    color = AppTheme.colors.neutral800,
                    textAlign = TextAlign.Start
                )
                SpacerTiny()
                PrescriptionStateInfo(
                    state = previewData.prescriptionState,
                    now = previewData.now,
                    textAlign = TextAlign.Center
                )
                SpacerTiny()
                Divider()
                SpacerMedium()
            }
        }
    }
}

@Preview
@Composable
fun PrescriptionStateInfoNearDayEndPreview() {
    PreviewAppTheme {
        Column {
            prescriptionStatePreviewsNearDayEnd.forEach { previewData ->
                Text(
                    previewData.name,
                    style = AppTheme.typography.caption2,
                    color = AppTheme.colors.neutral800,
                    textAlign = TextAlign.Start
                )
                SpacerTiny()
                PrescriptionStateInfo(
                    state = previewData.prescriptionState,
                    now = previewData.now,
                    textAlign = TextAlign.Center
                )
                SpacerTiny()
                Divider()
                SpacerMedium()
            }
        }
    }
}
