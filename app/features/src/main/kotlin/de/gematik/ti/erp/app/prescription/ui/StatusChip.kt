/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun StatusChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .background(backgroundColor, shape)
            .clip(shape)
            .then(modifier)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = AppTheme.typography.subtitle2, color = textColor, overflow = TextOverflow.Ellipsis)
        icon?.let {
            SpacerSmall()
            it()
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    icon: ImageVector?,
    textColor: Color,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) =
    StatusChip(
        text = text,
        icon = icon?.let {
            {
                Icon(it, tint = iconColor, contentDescription = null)
            }
        },
        textColor = textColor,
        backgroundColor = backgroundColor,
        modifier = modifier
    )

@Preview
@Composable
private fun StatusChipPreview() {
    AppTheme {
        StatusChip(
            text = "Einlösbar",
            icon = Icons.Rounded.Check,
            textColor = AppTheme.colors.green900,
            backgroundColor = AppTheme.colors.green100,
            iconColor = AppTheme.colors.green500
        )
    }
}

@Composable
fun ReadyStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_ready),
        icon = null,
        textColor = AppTheme.colors.primary900,
        backgroundColor = AppTheme.colors.primary100,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionRedeemable)
    )

@Composable
fun SentStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_sent),
        icon = null,
        textColor = AppTheme.colors.yellow900,
        backgroundColor = AppTheme.colors.yellow100
    )

@Composable
fun PendingStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_pending),
        icon = {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = AppTheme.colors.neutral500,
                strokeWidth = 2.dp
            )
        },
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionWaitForResponse)
    )

@Composable
fun InProgressStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_in_progress),
        icon = Icons.Rounded.HourglassTop,
        textColor = AppTheme.colors.yellow900,
        backgroundColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionInProgress)
    )

@Composable
fun FailureStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_failure),
        icon = Icons.Rounded.WarningAmber,
        textColor = AppTheme.colors.red900,
        backgroundColor = AppTheme.colors.red100,
        iconColor = AppTheme.colors.red500
    )

@Composable
fun CompletedStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_completed),
        icon = Icons.Rounded.DoneAll,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionRedeemed)
    )

@Composable
fun ProvidedStatusChip() =
    StatusChip(
        text = stringResource(R.string.provided),
        icon = Icons.Rounded.DoneAll,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionProvided)
    )

@Composable
fun DeletedStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_deleted),
        icon = Icons.Outlined.Delete,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500,
        modifier = Modifier.testTag(TestTag.Prescriptions.PrescriptionDeleted)
    )

@Composable
fun ExpiredStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_expired),
        icon = Icons.Rounded.EventBusy,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun LaterRedeemableStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_later_redeemable),
        icon = Icons.Rounded.Today,
        textColor = AppTheme.colors.yellow900,
        backgroundColor = AppTheme.colors.yellow100,
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

    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .background(AppTheme.colors.neutral200, shape)
            .clip(shape)
            .padding(vertical = PaddingDefaults.ShortMedium / 2, horizontal = PaddingDefaults.ShortMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = AppTheme.typography.subtitle2, color = AppTheme.colors.neutral600)
    }
}

@Composable
fun UnknownStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_unknown),
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral100
    )

@Composable
fun DirectAssignmentStatusChip(redeemed: Boolean) {
    val text = if (redeemed) {
        stringResource(R.string.prescription_status_direct_assignment_closed)
    } else {
        stringResource(R.string.prescription_status_direct_assignment_ready)
    }
    StatusChip(
        text = text,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral200
    )
}

@Composable
fun DirectAssignmentChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    StatusChip(
        modifier = modifier
            .clickable(role = Role.Button) {
                onClick()
            },
        text = stringResource(R.string.prescription_detail_direct_assignment_chip),
        icon = Icons.Outlined.Info,
        textColor = AppTheme.colors.primary900,
        backgroundColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary600
    )

@Composable
fun SubstitutionNotAllowedChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    StatusChip(
        modifier = modifier
            .clickable(role = Role.Button) {
                onClick()
            },
        text = stringResource(R.string.prescription_details_substitution_not_allowed),
        icon = Icons.Outlined.Info,
        textColor = AppTheme.colors.primary900,
        backgroundColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary600
    )

@Composable
fun FailureDetailsStatusChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    StatusChip(
        modifier = modifier
            .clickable(role = Role.Button) {
                onClick()
            },
        text = stringResource(R.string.prescription_status_failure),
        icon = Icons.Outlined.Info,
        textColor = AppTheme.colors.red900,
        backgroundColor = AppTheme.colors.red100,
        iconColor = AppTheme.colors.red500
    )

@LightDarkPreview
@Composable
fun SubstitutionNotallowedStatusChipPreview() {
    PreviewAppTheme {
        SubstitutionNotAllowedChip {}
    }
}

@LightDarkPreview
@Composable
fun PendingStatusChipPreview() {
    PreviewAppTheme {
        PendingStatusChip()
    }
}
