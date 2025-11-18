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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

// Base
@Composable
fun GemAssistChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    labelColor: Color = AppTheme.colors.neutral900,
    iconColor: Color = AppTheme.colors.primary700,
    containerColor: Color = AppTheme.colors.neutral025,
    border: BorderStroke? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        elevation = null,
        shape = RoundedCornerShape(SizeDefaults.one),
        border = border,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor,
            leadingIconContentColor = iconColor,
            trailingIconContentColor = iconColor,
            disabledContainerColor = containerColor,
            disabledLabelColor = labelColor,
            disabledLeadingIconContentColor = iconColor,
            disabledTrailingIconContentColor = iconColor
        ),
        leadingIcon = leadingIcon,
        label = {
            Text(text, style = AppTheme.typography.subtitle2, overflow = TextOverflow.Ellipsis)
        },
        trailingIcon = trailingIcon
    )
}

@Composable
fun GemNonInteractiveAssistChip(
    modifier: Modifier = Modifier,
    text: String,
    labelColor: Color = AppTheme.colors.neutral900,
    iconColor: Color = AppTheme.colors.neutral500,
    containerColor: Color = AppTheme.colors.neutral200,
    border: BorderStroke? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    AssistChip(
        onClick = {},
        modifier = modifier.clearAndSetSemantics {
            contentDescription = text
        },
        enabled = false,
        elevation = null,
        shape = RoundedCornerShape(SizeDefaults.one),
        border = border,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor,
            leadingIconContentColor = iconColor,
            trailingIconContentColor = iconColor,
            disabledContainerColor = containerColor,
            disabledLabelColor = labelColor,
            disabledLeadingIconContentColor = iconColor,
            disabledTrailingIconContentColor = iconColor
        ),
        leadingIcon = leadingIcon,
        label = {
            Text(text, style = AppTheme.typography.subtitle2, overflow = TextOverflow.Ellipsis)
        },
        trailingIcon = trailingIcon
    )
}

// PrescriptionStatus
@Composable
fun ReadyStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_ready),
        trailingIcon = null,
        labelColor = AppTheme.colors.primary900,
        containerColor = AppTheme.colors.primary100,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionRedeemable)
    )

@Composable
fun SentStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_sent),
        trailingIcon = { Icon(Icons.AutoMirrored.Rounded.Send, null) },
        labelColor = AppTheme.colors.yellow900,
        containerColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500
    )

@Composable
fun PendingStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_pending),
        trailingIcon = {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = AppTheme.colors.neutral500,
                strokeWidth = 2.dp
            )
        },
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionWaitForResponse)
    )

@Composable
fun InProgressStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_in_progress),
        trailingIcon = { Icon(Icons.Rounded.HourglassTop, null) },
        labelColor = AppTheme.colors.yellow900,
        containerColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionInProgress)
    )

@Composable
fun FailureStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_failure),
        trailingIcon = { Icon(Icons.Rounded.WarningAmber, null) },
        labelColor = AppTheme.colors.red900,
        containerColor = AppTheme.colors.red100,
        iconColor = AppTheme.colors.red500
    )

@Composable
fun CompletedStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_completed),
        trailingIcon = { Icon(Icons.Rounded.DoneAll, null) },
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionRedeemed)
    )

@Composable
fun ProvidedStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.provided),
        trailingIcon = { Icon(Icons.Rounded.DoneAll, null) },
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionProvided)
    )

@Composable
fun DeletedStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_deleted),
        trailingIcon = { Icon(Icons.Outlined.Delete, null) },
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionDeleted)
    )

@Composable
fun ExpiredStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_expired),
        trailingIcon = { Icon(Icons.Rounded.EventBusy, null) },
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun LaterRedeemableStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_later_redeemable),
        trailingIcon = { Icon(Icons.Rounded.Today, null) },
        labelColor = AppTheme.colors.yellow900,
        containerColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500
    )

@Composable
fun NumeratorChip(numerator: String, denominator: String) {
    val text =
        annotatedStringResource(
            R.string.multiple_prescription_numbering,
            numerator,
            denominator
        ).toString()
    GemNonInteractiveAssistChip(
        text = text,
        containerColor = AppTheme.colors.neutral200,
        labelColor = AppTheme.colors.neutral600
    )
}

@Composable
fun UnknownStatusChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_unknown),
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral100,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun DirectAssignmentStatusChip(redeemed: Boolean) {
    val text = if (redeemed) {
        stringResource(R.string.prescription_status_direct_assignment_closed)
    } else {
        stringResource(R.string.prescription_status_direct_assignment_ready)
    }
    GemNonInteractiveAssistChip(
        text = text,
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500
    )
}

@Composable
fun DirectAssignmentChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    GemAssistChip(
        modifier = modifier,
        text = stringResource(R.string.prescription_detail_direct_assignment_chip),
        trailingIcon = { Icon(Icons.Outlined.Info, null) },
        labelColor = AppTheme.colors.primary900,
        containerColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary500,
        onClick = onClick
    )

@Composable
fun SubstitutionNotAllowedChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    GemAssistChip(
        modifier = modifier,
        text = stringResource(R.string.prescription_details_substitution_not_allowed),
        trailingIcon = { Icon(Icons.Outlined.Info, null) },
        labelColor = AppTheme.colors.primary900,
        containerColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary500,
        onClick = onClick
    )

@Composable
fun FailureDetailsStatusChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    GemAssistChip(
        modifier = modifier,
        text = stringResource(R.string.prescription_status_failure),
        trailingIcon = { Icon(Icons.Outlined.Info, null) },
        labelColor = AppTheme.colors.red900,
        containerColor = AppTheme.colors.red100,
        iconColor = AppTheme.colors.red500,
        onClick = onClick
    )

@Composable
fun SelfPayerPrescriptionChip() =
    GemNonInteractiveAssistChip(
        text = stringResource(R.string.prescription_status_self_pay),
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral100
    )

@Composable
fun SelfPayPrescriptionDetailsChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    GemAssistChip(
        modifier = modifier,
        text = stringResource(R.string.pres_details_exp_sel_payer_prescription),
        trailingIcon = { Icon(Icons.Outlined.Info, null) },
        labelColor = AppTheme.colors.primary900,
        containerColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary500,
        onClick = onClick
    )

// Diga
@Composable
internal fun DigaStatusChip(
    text: String,
    icon: ImageVector? = null,
    labelColor: Color,
    containerColor: Color,
    iconColor: Color,
    showTrailingIcon: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        GemNonInteractiveAssistChip(
            text = text,
            trailingIcon = { icon?.let { Icon(icon, null) } },
            labelColor = labelColor,
            containerColor = containerColor,
            iconColor = iconColor
        )
        if (showTrailingIcon) {
            DigaStatusExtraIcon()
        }
    }
}

@Composable
private fun DigaStatusExtraIcon() {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(SizeDefaults.one),
        color = AppTheme.colors.neutral200,
        contentColor = AppTheme.colors.neutral500
    ) {
        Icon(
            modifier = Modifier.padding(PaddingValues(SizeDefaults.half)),
            imageVector = Icons.Rounded.Smartphone,
            tint = AppTheme.colors.neutral500,
            contentDescription = null
        )
    }
}

@Composable
fun InReguestStatusChip() = DigaStatusChip(
    text = stringResource(R.string.diga_status_request),
    labelColor = AppTheme.colors.primary900,
    containerColor = AppTheme.colors.primary100,
    iconColor = AppTheme.colors.neutral500
)

@Composable
fun ArchivedChip() =
    DigaStatusChip(
        text = stringResource(R.string.diga_status_archived),
        icon = Icons.Outlined.Archive,
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun NotLongerValidChip() =
    DigaStatusChip(
        text = stringResource(R.string.diga_status_not_valid),
        icon = Icons.Outlined.AccessTime,
        labelColor = AppTheme.colors.neutral600,
        containerColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun WaitingForCodeChip() =
    DigaStatusChip(
        text = stringResource(R.string.diga_status_waiting),
        icon = Icons.Outlined.HourglassTop,
        labelColor = AppTheme.colors.yellow900,
        containerColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500
    )

@Composable
fun CodeRejectChip() =
    DigaStatusChip(
        text = stringResource(R.string.diga_status_reject),
        icon = Icons.Outlined.Close,
        labelColor = AppTheme.colors.red900,
        containerColor = AppTheme.colors.red100,
        iconColor = AppTheme.colors.red500
    )

@Composable
fun InsuranceCodeChip() =
    DigaStatusChip(
        text = stringResource(R.string.diga_status_insurance_code),
        icon = Icons.Outlined.Check,
        labelColor = AppTheme.colors.green900,
        containerColor = AppTheme.colors.green100,
        iconColor = AppTheme.colors.green900
    )

@LightDarkPreview
@Composable
fun StatusChipsPreview() {
    PreviewAppTheme {
        Column {
            ReadyStatusChip()
            SentStatusChip()
            PendingStatusChip()
            InProgressStatusChip()
            FailureStatusChip()
            CompletedStatusChip()
            ProvidedStatusChip()
            DeletedStatusChip()
            ExpiredStatusChip()
            LaterRedeemableStatusChip()
            SelfPayerPrescriptionChip()
        }
    }
}

@LightDarkPreview
@Composable
fun StatusChipsPreviewExtended() {
    PreviewAppTheme {
        Column {
            NumeratorChip("1", "2")
            UnknownStatusChip()
            DirectAssignmentStatusChip(true)
            DirectAssignmentStatusChip(false)
            DirectAssignmentChip { }
            SubstitutionNotAllowedChip { }
            FailureDetailsStatusChip { }
            SelfPayPrescriptionDetailsChip { }
        }
    }
}

@LightDarkPreview
@Composable
fun DigaStatusChipsPreview() {
    PreviewAppTheme {
        Column {
            InReguestStatusChip()
            ArchivedChip()
            NotLongerValidChip()
            WaitingForCodeChip()
            CodeRejectChip()
            InsuranceCodeChip()
        }
    }
}

@LightDarkPreview
@Composable
fun AssistChipsPreview() {
    PreviewAppTheme {
        Column {
            GemAssistChip(
                text = "Einlösbar",
                trailingIcon = { Icon(Icons.Rounded.Check, null) },
                labelColor = AppTheme.colors.green900,
                containerColor = AppTheme.colors.green100,
                iconColor = AppTheme.colors.green500,
                onClick = {}
            )
            GemAssistChip(
                text = stringResource(R.string.prescription_status_deleted),
                trailingIcon = null,
                labelColor = AppTheme.colors.neutral600,
                containerColor = AppTheme.colors.neutral200,
                iconColor = AppTheme.colors.neutral500,
                modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionDeleted),
                onClick = {}
            )
            GemNonInteractiveAssistChip(
                text = "Einlösbar",
                trailingIcon = { Icon(Icons.Rounded.Check, null) },
                labelColor = AppTheme.colors.green900,
                containerColor = AppTheme.colors.green100,
                iconColor = AppTheme.colors.green500
            )
            GemNonInteractiveAssistChip(
                text = stringResource(R.string.prescription_status_deleted),
                trailingIcon = null,
                labelColor = AppTheme.colors.neutral600,
                containerColor = AppTheme.colors.neutral200,
                iconColor = AppTheme.colors.neutral500,
                modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionDeleted)
            )
        }
    }
}
