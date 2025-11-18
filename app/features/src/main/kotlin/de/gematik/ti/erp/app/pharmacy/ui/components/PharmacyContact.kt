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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.fhir.pharmacy.model.NotAvailablePeriodMetadata
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeMetadata
import de.gematik.ti.erp.app.pharmacy.ui.preview.mockDetailedInfoText
import de.gematik.ti.erp.app.pharmacy.ui.preview.mockOpeningHours
import de.gematik.ti.erp.app.pharmacy.ui.preview.mockSpecialClosingTimes
import de.gematik.ti.erp.app.pharmacy.ui.preview.mockSpecialOpeningTimes
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningHours
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningTime
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun PharmacyContact(
    openingHours: OpeningHours?,
    specialOpeningTimes: List<SpecialOpeningTimeMetadata>,
    specialClosingTimes: List<NotAvailablePeriodMetadata>,
    phone: String,
    mail: String,
    url: String,
    detailedInfoText: AnnotatedString,
    onPhoneClicked: (String) -> Unit,
    onMailClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onTextClicked: (Int) -> Unit,
    onHintClicked: () -> Unit,
    currentDateTime: LocalDateTime
) {
    Column {
        openingHours?.let {
            if (it.isNotEmpty()) {
                PharmacyOpeningHoursOverview(
                    openingHours = it,
                    currentDateTime = currentDateTime
                )
            }
        }
        SpacerLarge()

        if (specialOpeningTimes.isNotEmpty()) {
            SpecialOpeningTimes(specialOpeningTimes)
        }
        SpacerXXLarge()

        if (specialClosingTimes.isNotEmpty()) {
            SpecialClosingTimes(specialClosingTimes)
        }

        if (phone.isNotEmpty() || mail.isNotEmpty() || url.isNotEmpty()) {
            SpacerXXLarge()
            ErezeptText.Title(text = stringResource(id = R.string.legal_notice_contact_header))
            SpacerMedium()
        }

        if (phone.isNotEmpty()) {
            ContactLabel(
                text = phone,
                icon = R.drawable.phone,
                label = stringResource(R.string.pres_detail_organization_label_telephone),
                onClick = onPhoneClicked
            )
            SpacerMedium()
        }
        if (mail.isNotEmpty()) {
            ContactLabel(
                text = mail,
                icon = R.drawable.mail,
                label = stringResource(R.string.pres_detail_organization_label_email),
                onClick = onMailClicked
            )
            SpacerMedium()
        }
        if (url.isNotEmpty()) {
            ContactLabel(
                text = url,
                icon = R.drawable.website,
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
private fun ContactLabel(
    text: String,
    label: String,
    icon: Int,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                onClick(text)
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary700
            )
            SpacerTiny()
            Text(
                text = label,
                style = AppTheme.typography.body2l
            )
        }
        Image(
            painter = painterResource(icon),
            contentDescription = label,
            modifier = Modifier.size(SizeDefaults.triple)
        )
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
            onClick = onHintClicked,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.open_link),
                    contentDescription = null,
                    modifier = Modifier.size(SizeDefaults.triple)
                )
            }
        )
    }
}

@Composable
private fun PharmacyOpeningHoursOverview(
    openingHours: OpeningHours,
    currentDateTime: LocalDateTime
) {
    val todayRelevantDay = currentDateTime.dayOfWeek
    val tomorrowRelevantDay = todayRelevantDay.plus(1)

    val sortedOpeningHours = remember {
        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        val startIndex = todayRelevantDay.ordinal
        daysOfWeek.indices
            .map { daysOfWeek[(startIndex + it) % daysOfWeek.size] }
            .filter { openingHours.containsKey(it) }
            .associateWith { openingHours[it] ?: emptyList() }
    }

    Column {
        Text(
            text = stringResource(R.string.pharm_detail_opening_hours),
            style = AppTheme.typography.h6
        )

        SpacerMedium()

        sortedOpeningHours.forEach { (day, hours) ->
            val isOpenToday = remember(currentDateTime) { day == todayRelevantDay && hours.isNotEmpty() }
            PharmacyDayOpeningHoursDisplay(
                day = day,
                hours = hours,
                isOpenToday = isOpenToday,
                currentDateTime = currentDateTime,
                todayRelevantDay = todayRelevantDay,
                nextRelevantDay = tomorrowRelevantDay,
                isNextDayAfterToday = day == tomorrowRelevantDay
            )
            SpacerMedium()
        }
    }
}

@Composable
private fun PharmacyDayOpeningHoursDisplay(
    day: DayOfWeek,
    hours: List<OpeningTime>,
    isOpenToday: Boolean,
    currentDateTime: LocalDateTime,
    todayRelevantDay: DayOfWeek,
    nextRelevantDay: DayOfWeek,
    isNextDayAfterToday: Boolean
) {
    val formatter = rememberErpTimeFormatter()
    val dayLabel = generateDayLabel(day, todayRelevantDay, nextRelevantDay, isNextDayAfterToday, formatter.locale)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayLabel,
            color = AppTheme.colors.neutral900,
            fontWeight = if (isOpenToday) FontWeight.Medium else null
        )
        Column(horizontalAlignment = Alignment.End) {
            hours.sortedBy { it.openingTime }.forEach { hour ->
                val text = "${formatter.time(hour.openingTime)} - ${formatter.time(hour.closingTime)}"
                val isOpenNow = remember(currentDateTime) {
                    hour.isOpenAt(currentDateTime.time) && isOpenToday
                }
                Text(
                    text = text,
                    color = when {
                        isOpenNow -> AppTheme.colors.green700
                        isOpenToday -> AppTheme.colors.neutral600
                        else -> AppTheme.colors.neutral600
                    },
                    fontWeight = if (isOpenNow || isOpenToday) FontWeight.Medium else null
                )
            }
        }
    }
}

@Composable
private fun generateDayLabel(
    day: DayOfWeek,
    todayRelevantDay: DayOfWeek,
    nextRelevantDay: DayOfWeek,
    isNextDayAfterToday: Boolean,
    locale: Locale
): String {
    return when (day) {
        todayRelevantDay -> stringResource(id = R.string.pharmacy_day_today)
        nextRelevantDay -> if (isNextDayAfterToday) {
            stringResource(id = R.string.pharmacy_day_tommorow)
        } else {
            day.getDisplayName(TextStyle.FULL, locale)
        }

        else -> day.getDisplayName(TextStyle.FULL, locale)
    }
}

@Suppress("MagicNumber")
@LightDarkLongPreview
@Composable
fun PreviewPharmacyContact() {
    PreviewAppTheme {
        PharmacyContact(
            openingHours = mockOpeningHours,
            specialClosingTimes = mockSpecialClosingTimes,
            specialOpeningTimes = mockSpecialOpeningTimes,
            phone = "123-456-7890",
            mail = "pharmacy@example.com",
            url = "www.examplepharmacy.com",
            detailedInfoText = mockDetailedInfoText,
            onPhoneClicked = {},
            onMailClicked = {},
            onUrlClicked = {},
            onTextClicked = {},
            onHintClicked = {},
            currentDateTime = LocalDateTime(2024, 7, 31, 10, 0)
        )
    }
}
