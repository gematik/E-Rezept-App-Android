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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.fhir.model.OpeningHours
import de.gematik.ti.erp.app.fhir.model.isOpenToday
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.Title
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun PharmacyContact(
    openingHours: OpeningHours?,
    phone: String,
    mail: String,
    url: String,
    detailedInfoText: AnnotatedString,
    onPhoneClicked: (String) -> Unit,
    onMailClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onTextClicked: (Int) -> Unit,
    onHintClicked: () -> Unit
) {
    Column {
        openingHours?.let {
            if (it.isNotEmpty()) {
                PharmacyOpeningHours(it)
            }
            SpacerMedium()
        }
        if (phone.isNotEmpty() || mail.isNotEmpty() || url.isNotEmpty()) {
            Title(text = stringResource(id = R.string.legal_notice_contact_header))
            SpacerMedium()
        }
        if (phone.isNotEmpty()) {
            ContactLabel(
                text = phone,
                label = stringResource(R.string.pres_detail_organization_label_telephone),
                onClick = onPhoneClicked
            )
            SpacerMedium()
        }
        if (mail.isNotEmpty()) {
            ContactLabel(
                text = mail,
                label = stringResource(R.string.pres_detail_organization_label_email),
                onClick = onMailClicked
            )
            SpacerMedium()
        }
        if (url.isNotEmpty()) {
            ContactLabel(
                text = url,
                label = stringResource(R.string.pharm_detail_website),
                onClick = onUrlClicked
            )
            SpacerMedium()
        }
        SpacerMedium()
        DataInfoSection(
            modifier = Modifier.align(Alignment.End),
            detailedInfoText = detailedInfoText,
            onTextClicked = onTextClicked,
            onHintClicked = onHintClicked
        )
    }
}

@Composable
private fun PharmacyOpeningHours(openingHours: OpeningHours) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Column {
        Text(
            text = stringResource(R.string.pharm_detail_opening_hours),
            style = AppTheme.typography.h6
        )

        SpacerMedium()

        val sortedOpeningHours = OpeningHours(openingHours.toSortedMap(compareBy { it }))

        for (h in sortedOpeningHours) {
            val (day, hours) = h
            val now =
                remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
            val isOpenToday = remember(now) { h.isOpenToday(now) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    fontWeight = if (isOpenToday) FontWeight.Medium else null
                )
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    for (hour in hours.sortedBy { it.openingTime }) {
                        val opens =
                            hour.openingTime?.toJavaLocalTime()?.format(dateTimeFormatter) ?: ""
                        val closes =
                            hour.closingTime?.toJavaLocalTime()?.format(dateTimeFormatter) ?: ""
                        val text = "$opens - $closes"
                        val isOpenNow =
                            remember(now) { hour.isOpenAt(now.time) && isOpenToday }
                        when {
                            isOpenNow ->
                                Text(
                                    text = text,
                                    color = AppTheme.colors.green600,
                                    fontWeight = FontWeight.Medium
                                )

                            isOpenToday ->
                                Text(
                                    text = text,
                                    color = AppTheme.colors.neutral600,
                                    fontWeight = FontWeight.Medium
                                )

                            else ->
                                Text(
                                    text = text,
                                    color = AppTheme.colors.neutral600
                                )
                        }
                    }
                }
            }
            SpacerMedium()
        }
    }
}

@Composable
private fun DataInfoSection(
    modifier: Modifier,
    detailedInfoText: AnnotatedString,
    onTextClicked: (Int) -> Unit,
    onHintClicked: () -> Unit
) {
    ClickableText(
        modifier = modifier
            .fillMaxWidth(),
        text = detailedInfoText,
        style = AppTheme.typography.body2l,
        onClick = onTextClicked
    )
    SpacerSmall()
    Row(modifier = modifier) {
        HintTextActionButton(
            text = stringResource(R.string.pharmacy_detail_data_info_btn),
            onClick = onHintClicked
        )
    }
}

@Composable
private fun ContactLabel(
    text: String,
    label: String,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick(text)
            }
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = AppTheme.typography.body1,
            color = AppTheme.colors.primary600
        )
        SpacerTiny()
        Text(
            text = label,
            style = AppTheme.typography.body2l
        )
    }
}
