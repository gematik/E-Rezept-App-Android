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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData.TaskDetailedBundle
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedLinkUnderlined
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun DispenseMessage(
    pharmacyName: String,
    orderSentOn: Instant,
    dateFormatter: DateTimeFormatter = DateTimeUtils.dateFormatter,
    timeFormatter: DateTimeFormatter = DateTimeUtils.timeFormatter,
    taskDetails: List<TaskDetailedBundle>?,
    onClickPharmacy: () -> Unit,
    isOnlyMessage: Boolean
) {
    val date = remember(orderSentOn) {
        dateFormatter.format(orderSentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    val time = remember(orderSentOn) {
        timeFormatter.format(orderSentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }

    // State for dynamically tracking the circle position
    val circleYPositionState = remember { mutableFloatStateOf(0f) }

    Row(
        Modifier
            .drawConnectedLine(
                drawFilledTop = !isOnlyMessage,
                drawFilledBottom = false, // dispense is always the last message in the list
                circleYPosition = { circleYPositionState.floatValue }
            )
            .clickable(
                onClick = onClickPharmacy
            )
    ) {
        Spacer(Modifier.width(SizeDefaults.triple))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()
            Text(
                stringResource(R.string.orders_timestamp, date, time),
                style = AppTheme.typography.subtitle2,
                modifier = Modifier.calculateVerticalCenter(
                    onCenterCalculated = { circleYPositionState.floatValue = it }
                )
            )
            taskDetails?.let {
                Column(modifier = Modifier.padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.Tiny)) {
                    if (it.size > 1) {
                        InfoChip(stringResource(R.string.all_prescriptions_of_order))
                    } else {
                        it.first().prescription?.name?.let { it1 -> InfoChip(it1) }
                    }
                }
            }
            PrescriptionSentTo(pharmacyName)
        }
    }
}

@Composable
fun PrescriptionSentTo(
    pharmacyName: String
) {
    val fullText = when {
        pharmacyName.isEmpty() -> stringResource(
            R.string.orders_prescription_sent_to,
            stringResource(R.string.pharmacy_order_pharmacy)
        )

        else -> stringResource(R.string.orders_prescription_sent_to, pharmacyName)
    }
    val annotatedText = annotatedLinkUnderlined(fullText, pharmacyName, "PharmacyNameClickable")

    Text(
        text = annotatedText,
        style = AppTheme.typography.body2l
    )
}
