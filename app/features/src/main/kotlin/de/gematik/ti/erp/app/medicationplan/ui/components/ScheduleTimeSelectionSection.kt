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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoDisturbOn
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.temporal.formattedStringShort
import de.gematik.ti.erp.app.fhir.temporal.isBeforeCurrentDate
import de.gematik.ti.erp.app.fhir.temporal.toHourMinuteString
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessage
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosage
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.datetime.LocalDate

internal fun LazyListScope.scheduleTimeSelectionSection(
    medicationSchedule: MedicationSchedule,
    currentDate: LocalDate,
    onClickChangeDateRange: () -> Unit,
    onAddNewItem: () -> Unit,
    onRemoveNotificationTime: (MedicationScheduleNotification) -> Unit,
    onNotificationTimeClick: (MedicationScheduleNotification) -> Unit,
    onDosageClicked: (MedicationScheduleNotification) -> Unit
) {
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            Text(
                modifier = Modifier.semanticsHeading(),
                text = stringResource(R.string.plan_notification_times_header),
                style = AppTheme.typography.h6
            )
            ScheduleDateRangeCard(
                medicationSchedule = medicationSchedule,
                currentDate = currentDate,
                onClickChangeDateRange = onClickChangeDateRange
            )
            ScheduleTimeCard(
                medicationSchedule = medicationSchedule,
                onRemoveNotificationTime = onRemoveNotificationTime,
                onAddNewItem = onAddNewItem,
                onNotificationTimeClick = onNotificationTimeClick,
                onDosageClicked = onDosageClicked
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScheduleDateRangeCard(
    medicationSchedule: MedicationSchedule,
    currentDate: LocalDate,
    onClickChangeDateRange: () -> Unit
) {
    Card(
        backgroundColor = AppTheme.colors.neutral000,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
        shape = RoundedCornerShape(SizeDefaults.doubleHalf),
        elevation = SizeDefaults.quarter
    ) {
        Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
            val text = getScheduleDurationString(medicationSchedule, currentDate)
            ListItem(
                modifier = Modifier.clickable {
                    onClickChangeDateRange()
                },
                text = {
                    Text(
                        stringResource(R.string.medication_schedule_repeat, ""),
                        style = AppTheme.typography.body1
                    )
                },
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
                    ) {
                        Text(
                            text = text,
                            style = AppTheme.typography.body1,
                            color = AppTheme.colors.neutral600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun ScheduleDateRangeCardPreview() {
    PreviewAppTheme {
        ScheduleDateRangeCard(
            medicationSchedule = MedicationSchedule(
                isActive = true,
                profileId = "profileId",
                taskId = "taskId",
                amount = Ratio(
                    numerator = Quantity(value = "10", unit = "Stk"),
                    denominator = Quantity(value = "1", unit = "Tag")
                ),
                duration = MedicationScheduleDuration.Personalized(
                    startDate = LocalDate(2024, 1, 1),
                    endDate = LocalDate(2024, 12, 31)
                ),
                interval = MedicationScheduleInterval.Daily,
                message = MedicationNotificationMessage(title = "Title", body = "Body"),
                notifications = emptyList()
            ),
            currentDate = LocalDate(2024, 6, 15),
            onClickChangeDateRange = {}
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScheduleTimeCard(
    medicationSchedule: MedicationSchedule,
    onAddNewItem: () -> Unit,
    onRemoveNotificationTime: (MedicationScheduleNotification) -> Unit,
    onNotificationTimeClick: (MedicationScheduleNotification) -> Unit,
    onDosageClicked: (MedicationScheduleNotification) -> Unit
) {
    Card(
        backgroundColor = AppTheme.colors.neutral000,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
        shape = RoundedCornerShape(SizeDefaults.doubleHalf),
        elevation = SizeDefaults.quarter
    ) {
        Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
            medicationSchedule.notifications.forEach { notification ->
                ListItem(
                    modifier = Modifier,
                    icon = {
                        Icon(
                            modifier = Modifier.clickable {
                                onRemoveNotificationTime(notification)
                            },
                            imageVector = Icons.Filled.DoDisturbOn,
                            contentDescription = null,
                            tint = AppTheme.colors.red600
                        )
                    },
                    text = {
                        Text(
                            text = notification.time.toHourMinuteString(),
                            modifier = Modifier
                                .background(
                                    shape = RoundedCornerShape(SizeDefaults.one),
                                    color = AppTheme.colors.neutral200
                                )
                                .clip(RoundedCornerShape(SizeDefaults.one))
                                .clickable {
                                    onNotificationTimeClick(notification)
                                }
                                .padding(
                                    horizontal = PaddingDefaults.Small,
                                    vertical = PaddingDefaults.Tiny
                                ),
                            style = AppTheme.typography.body1
                        )
                    },
                    trailing = {
                        TextButton(
                            onClick = {
                                onDosageClicked(notification)
                            }
                        ) {
                            Text("${notification.dosage.ratio} ${notification.dosage.form}")
                        }
                    }
                )
            }
            ListItem(
                modifier = Modifier
                    .clickable { onAddNewItem() },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.medication_schedule_add_notification_time),
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.primary700
                    )
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ScheduleTimeCardPreview() {
    PreviewAppTheme {
        ScheduleTimeCard(
            medicationSchedule = MedicationSchedule(
                isActive = true,
                profileId = "profileId",
                taskId = "taskId",
                amount = Ratio(
                    numerator = Quantity(value = "10", unit = "Stk"),
                    denominator = Quantity(value = "1", unit = "Tag")
                ),
                duration = MedicationScheduleDuration.Personalized(
                    startDate = LocalDate(2024, 1, 1),
                    endDate = LocalDate(2024, 12, 31)
                ),
                interval = MedicationScheduleInterval.Daily,
                message = MedicationNotificationMessage(title = "Title", body = "Body"),
                notifications = listOf(
                    MedicationScheduleNotification(
                        time = kotlinx.datetime.LocalTime(8, 0),
                        dosage = MedicationScheduleNotificationDosage(form = "Tablette", ratio = "1")
                    ),
                    MedicationScheduleNotification(
                        time = kotlinx.datetime.LocalTime(18, 0),
                        dosage = MedicationScheduleNotificationDosage(form = "Tablette", ratio = "1")
                    )
                )
            ),
            onAddNewItem = {}, onRemoveNotificationTime = {}, onNotificationTimeClick = {}, onDosageClicked = {})
    }
}

@Composable
private fun getScheduleDurationString(
    medicationSchedule: MedicationSchedule,
    currentDate: LocalDate
): String {
    val text = when {
        medicationSchedule.duration is MedicationScheduleDuration.Endless -> stringResource(R.string.medication_plan_endless)
        medicationSchedule.duration.endDate.isBeforeCurrentDate(currentDate) ->
            stringResource(R.string.medication_plan_ended)

        else -> stringResource(
            R.string.medication_plan_ends,
            medicationSchedule.duration.endDate.formattedStringShort()
        )
    }
    return text
}
