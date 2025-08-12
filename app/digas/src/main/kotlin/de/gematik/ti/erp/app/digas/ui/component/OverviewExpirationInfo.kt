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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.preview.digaMainScreenUiModel
import de.gematik.ti.erp.app.extensions.roundedCornerShape
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import kotlinx.datetime.Instant

@Composable
fun ColumnScope.OverviewExpirationInfo(
    data: DigaMainScreenUiModel,
    current: DigaStatus?,
    displayBottomSheet: () -> Unit
) {
    when {
        data.lifeCycleTimestamps.expiresOn != null && data.lifeCycleTimestamps.isExpired -> {
            Row(
                modifier = Modifier.roundedCornerShape(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = ""
                )
                SpacerTiny()
                Text(
                    text = stringResource(
                        R.string.pres_detail_medication_expired_on,
                        data.lifeCycleTimestamps.expiresOnTimeState
                    ),
                    style = AppTheme.typography.body1
                )
            }
        }

        current is DigaStatus.Ready -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .roundedCornerShape()
                    .clickable(
                        onClick = displayBottomSheet,
                        onClickLabel = stringResource(R.string.open),
                        role = Role.Button
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = ""
                )
                SpacerTiny()
                Text(
                    text = stringResource(
                        R.string.redeemable_until_date,
                        data.lifeCycleTimestamps.expiresOnTimeState
                    ),
                    style = AppTheme.typography.body1
                )
                SpacerSmall()
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "",
                    tint = AppTheme.colors.primary700
                )
            }
            SpacerMedium()
        }

        (current?.step ?: 0) >= DigaStatus.CompletedSuccessfully.step -> {
            Row(
                modifier = Modifier
                    .roundedCornerShape()
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "")
                Text(
                    text = stringResource(
                        R.string.code_received_on_date,
                        data.lifeCycleTimestamps.issuedOnTimeState.lowercase()
                    ),
                    style = AppTheme.typography.body1
                )
            }
        }

        current is DigaStatus.InProgress -> {
            Row(
                modifier = Modifier
                    .roundedCornerShape()
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "")
                SpacerTiny()
                Text(
                    text = stringResource(
                        R.string.code_requested_just_now,
                        data.lifeCycleTimestamps.sentOnTimeState.lowercase()
                    ),
                    style = AppTheme.typography.body1
                )
            }
        }

        current is DigaStatus.CompletedWithRejection -> {
            Row(
                modifier = Modifier
                    .roundedCornerShape()
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "")
                SpacerTiny()
                Text(
                    text = stringResource(
                        R.string.code_reject_info,
                        data.lifeCycleTimestamps.sentOnTimeState.lowercase()
                    ),
                    style = AppTheme.typography.body1
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewExpirationInfoExpiredPreview() {
    PreviewTheme {
        Column {
            OverviewExpirationInfo(
                data = digaMainScreenUiModel.copy(
                    lifeCycleTimestamps = digaMainScreenUiModel
                        .lifeCycleTimestamps.copy(
                            now = Instant.parse("2026-08-01T10:00:00Z")
                        )
                ),
                current = DigaStatus.Ready,
                displayBottomSheet = {}
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewExpirationInfoReadyPreview() {
    PreviewTheme {
        Column {
            OverviewExpirationInfo(
                data = digaMainScreenUiModel.copy(
                    lifeCycleTimestamps = digaMainScreenUiModel
                        .lifeCycleTimestamps.copy(
                            now = Instant.parse("2024-08-01T10:00:00Z")
                        )
                ),
                current = DigaStatus.Ready,
                displayBottomSheet = {}
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewExpirationInfoCompletedPreview() {
    PreviewTheme {
        Column {
            OverviewExpirationInfo(
                data = digaMainScreenUiModel.copy(
                    lifeCycleTimestamps = digaMainScreenUiModel
                        .lifeCycleTimestamps.copy(
                            now = Instant.parse("2024-08-01T10:00:00Z")
                        )
                ),
                current = DigaStatus.CompletedSuccessfully,
                displayBottomSheet = {}
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewExpirationInfoCompletedWithRejectionPreview() {
    PreviewTheme {
        Column {
            OverviewExpirationInfo(
                data = digaMainScreenUiModel.copy(
                    lifeCycleTimestamps = digaMainScreenUiModel
                        .lifeCycleTimestamps.copy(
                            now = Instant.parse("2024-08-01T10:00:00Z")
                        )
                ),
                current = DigaStatus.CompletedWithRejection(Instant.parse("2024-08-01T10:00:00Z")),
                displayBottomSheet = {}
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewExpirationInfoInProgressPreview() {
    PreviewTheme {
        Column {
            OverviewExpirationInfo(
                data = digaMainScreenUiModel.copy(
                    lifeCycleTimestamps = digaMainScreenUiModel
                        .lifeCycleTimestamps.copy(
                            now = Instant.parse("2024-08-01T10:00:00Z")
                        )
                ),
                current = DigaStatus.InProgress(
                    Instant.parse("2024-07-01T10:00:00Z")
                ),
                displayBottomSheet = {}
            )
        }
    }
}
