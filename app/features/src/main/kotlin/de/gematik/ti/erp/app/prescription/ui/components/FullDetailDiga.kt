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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.timeStateParser
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.ArchivedChip
import de.gematik.ti.erp.app.prescription.ui.CodeRejectChip
import de.gematik.ti.erp.app.prescription.ui.InReguestStatusChip
import de.gematik.ti.erp.app.prescription.ui.InsuranceCodeChip
import de.gematik.ti.erp.app.prescription.ui.NotLongerValidChip
import de.gematik.ti.erp.app.prescription.ui.PendingStatusChip
import de.gematik.ti.erp.app.prescription.ui.WaitingForCodeChip
import de.gematik.ti.erp.app.prescription.ui.preview.FullDetailDigaPreviewData
import de.gematik.ti.erp.app.prescription.ui.preview.FullDetailDigaPreviewProvider
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.timestate.getTimeState
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import de.gematik.ti.erp.app.utils.compose.preview.NamedPreviewBox
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.format
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun FullDetailDiga(
    modifier: Modifier = Modifier,
    prescription: SyncedPrescription,
    now: Instant = Clock.System.now(),
    onClick: () -> Unit
) {
    var showNew by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showNew = prescription.isNew
    }

    val newBadgeText = if (prescription.isNew) stringResource(R.string.diga_new) + ". " else ""
    val medicationName = prescription.name ?: stringResource(R.string.diga_default_name)
    val description = newBadgeText + medicationName
    Box(
        modifier = Modifier.graphicsLayer { clip = false }
    ) {
        Card(
            modifier = modifier
                .semantics { prescriptionId = prescription.taskId }
                .testTag(TestTag.Prescriptions.FullDetailPrescription),
            shape = RoundedCornerShape(SizeDefaults.double),
            border = BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.neutral300),
            backgroundColor = AppTheme.colors.neutral050,
            elevation = SizeDefaults.zero,
            onClick = onClick
        ) {
            val textColor = AppTheme.colors.neutral800
            Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.semantics {
                            contentDescription = description
                        },
                        text = medicationName,
                        color = textColor,
                        style = AppTheme.typography.subtitle1,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    SpacerTiny()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when {
                            prescription.deviceRequestState is DigaStatus.SelfArchiveDiga ||
                                prescription.isArchived -> {
                                Text(
                                    dateWithIntroductionString(
                                        R.string.code_received_on_date,
                                        prescription.acceptUntil ?: Clock.System.now()
                                    ),
                                    style = AppTheme.typography.body2,
                                    color = textColor
                                )
                                SpacerSmall()
                                ArchivedChip()
                            }

                            prescription.state is SyncedTaskData.SyncedTask.Pending -> DigaPendingChip()
                            prescription.deviceRequestState is DigaStatus.Ready -> DigaReadyChip(prescription, now)
                            prescription.deviceRequestState is DigaStatus.InProgress ->
                                (prescription.deviceRequestState as? DigaStatus.InProgress)
                                    ?.sentOn?.let { DigaInProgressChip(it) }

                            prescription.deviceRequestState is DigaStatus.CompletedSuccessfully ||
                                prescription.deviceRequestState is DigaStatus.OpenAppWithRedeemCode ||
                                prescription.deviceRequestState is DigaStatus.DownloadDigaApp ||
                                prescription.deviceRequestState is DigaStatus.ReadyForSelfArchiveDiga ->
                                prescription.acceptUntil?.let { DigaUserStateChip(it) }

                            prescription.deviceRequestState is DigaStatus.CompletedWithRejection ->
                                (prescription.deviceRequestState as? DigaStatus.CompletedWithRejection)
                                    ?.sentOn?.let { DigaCodeRejectedChip(it) }

                            else -> DigaInProgressChip()
                        }
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral400,
                    modifier = Modifier
                        .size(SizeDefaults.triple)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            AnimatedVisibility(
                visible = showNew,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 250)
                ),
                exit = scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 150)
                )
            ) {
                Box(
                    modifier = Modifier
                        .clearAndSetSemantics(properties = {})
                        .align(Alignment.TopEnd)
                        .padding(top = SizeDefaults.half, end = SizeDefaults.half)
                        .offset(x = -(SizeDefaults.half), y = -(SizeDefaults.one))
                        .background(
                            color = AppTheme.colors.red500,
                            shape = RoundedCornerShape(SizeDefaults.oneThreeQuarter)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = PaddingDefaults.ShortMedium, vertical = PaddingDefaults.Tiny),
                        text = stringResource(R.string.diga_new),
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral000
                    )
                }
            }
        }
    }
}

@Composable
private fun DigaPendingChip() {
    Text(
        stringResource(R.string.code_requested_just_now),
        style = AppTheme.typography.body2,
        color = AppTheme.colors.neutral800
    )
    SpacerSmall()
    PendingStatusChip()
}

@Composable
private fun DigaReadyChip(
    prescription: SyncedPrescription,
    now: Instant = Clock.System.now()
) {
    val expiresOn = (prescription.state as? SyncedTaskData.SyncedTask.Ready)?.expiresOn
    if (expiresOn?.let { now.daysUntil(it, TimeZone.currentSystemDefault()) } == 0) {
        Text(
            dateWithIntroductionString(
                R.string.pres_detail_medication_expired_on,
                expiresOn
            ),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral800
        )
        SpacerSmall()
        NotLongerValidChip()
    } else {
        PrescriptionStateInfo(
            state = prescription.state,
            now = now
        )
        SpacerSmall()
        InReguestStatusChip()
    }
}

@Composable
private fun ColumnScope.DigaInProgressChip(sentOn: Instant = Clock.System.now()) {
    val timeState = timeStateParser(timeState = getTimeState(timestamp = sentOn))
    Text(
        stringResource(R.string.code_requested_just_now, timeState),
        style = AppTheme.typography.body2,
        color = AppTheme.colors.neutral800
    )
    SpacerSmall()
    WaitingForCodeChip()
}

@Composable
private fun ColumnScope.DigaUserStateChip(acceptedUntil: Instant) {
    Text(
        dateWithIntroductionString(
            R.string.code_received_on_date,
            acceptedUntil
        ),
        style = AppTheme.typography.body2,
        color = AppTheme.colors.neutral800
    )
    SpacerSmall()
    InsuranceCodeChip()
}

@Composable
private fun ColumnScope.DigaCodeRejectedChip(rejectedOn: Instant) {
    Text(
        stringResource(R.string.code_reject_info, rejectedOn.format()),
        style = AppTheme.typography.body2,
        color = AppTheme.colors.neutral800
    )
    SpacerSmall()
    CodeRejectChip()
}

@LightDarkPreview
@Composable
internal fun FullDetailDigaPreview(
    @PreviewParameter(FullDetailDigaPreviewProvider::class) data: FullDetailDigaPreviewData
) {
    PreviewAppTheme {
        NamedPreviewBox(
            name = data.name
        ) {
            FullDetailDiga(
                now = data.now,
                prescription = data.prescription
            ) { }
        }
    }
}
