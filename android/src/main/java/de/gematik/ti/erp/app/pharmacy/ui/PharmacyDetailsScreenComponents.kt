/*
 * Copyright (c) 2022 gematik GmbH
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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.repository.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningHours
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningTime
import de.gematik.ti.erp.app.pharmacy.repository.model.RoleCode
import de.gematik.ti.erp.app.pharmacy.repository.model.isOpenToday
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PharmacyDetailsScreen(
    navController: NavController,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    showRedeemOptions: Boolean
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                title = stringResource(R.string.pharmacy_detail_title),
                onBack = { navController.popBackStack() }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .verticalScroll(rememberScrollState())
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.navigationBars,
                        applyBottom = true
                    )
                )
        ) {
            SpacerMedium()

            if (pharmacy.ready) {
                ReadyFlag()
                SpacerSmall()
            }

            Text(
                text = pharmacy.name,
                style = MaterialTheme.typography.h5
            )
            SpacerSmall()
            Row(
                modifier = Modifier
                    .clickable {
                        pharmacy.location?.let {
                            launchMaps(context, it, pharmacy.name)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pharmacy.removeLineBreaksFromAddress(),
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.secondary,
                )
                Spacer8()
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "",
                    tint = MaterialTheme.colors.secondary
                )
            }

            Spacer24()

            if (pharmacy.ready) {
                if (showRedeemOptions) {
                    OrderOptions(navController, pharmacy)
                    Spacer16()
                    HintCard(
                        properties = HintCardDefaults.properties(
                            backgroundColor = AppTheme.colors.primary100,
                            border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
                            elevation = 0.dp
                        ),
                        image = {
                            HintSmallImage(
                                painterResource(R.drawable.ic_info),
                                innerPadding = it
                            )
                        },
                        title = { Text(stringResource(R.string.pharm_detail_hint_header)) },
                        body = { Text(stringResource(R.string.pharm_detail_hint)) }
                    )
                }
            } else {
                HintCard(
                    properties = HintCardDefaults.properties(
                        backgroundColor = AppTheme.colors.red100,
                        contentColor = AppTheme.colors.neutral999,
                        border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
                        elevation = 0.dp
                    ),
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.medical_hand_out_circle_red),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.pharmacy_detail_not_ready_header)) },
                    body = { Text(stringResource(R.string.pharmacy_detail_not_ready_info)) }
                )
            }
            Spacer(modifier = Modifier.size(PaddingDefaults.XXLarge))

            PharmacyInfo(pharmacy)

            SpacerMedium()
        }
    }
}

@Composable
private fun OrderOptions(navController: NavController, pharmacy: PharmacyUseCaseData.Pharmacy) {
    if (pharmacy.roleCode.any { it == RoleCode.OUT_PHARM }) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = { navController.navigate("reserveInPharmacy") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                text = stringResource(id = R.string.pharm_detail_preorder_prescription).uppercase(
                    Locale.getDefault()
                )
            )
        }
    }

    if (pharmacy.provides.any { it is DeliveryPharmacyService }) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = { navController.navigate("courierDelivery") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.neutral050,
                contentColor = AppTheme.colors.primary700
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                text = stringResource(id = R.string.pharm_detail_messanger_delivered).uppercase(
                    Locale.getDefault()
                )
            )
        }
    }
    if (pharmacy.roleCode.any { it == RoleCode.MOBL }) {
        Button(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = { navController.navigate("mailDelivery") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.neutral050,
                contentColor = AppTheme.colors.primary700
            )
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                text = stringResource(id = R.string.pharm_detail_mail_delivered).uppercase(Locale.getDefault())
            )
        }
    }
}

@Composable
private fun PharmacyInfo(pharmacy: PharmacyUseCaseData.Pharmacy) {
    Column {
        pharmacy.openingHours?.let {
            if (it.isNotEmpty()) {
                PharmacyOpeningHours(it)
            }
            SpacerMedium()
        }
        Text(
            text = stringResource(id = R.string.legal_notice_contact_header),
            style = MaterialTheme.typography.h6
        )
        SpacerMedium()
        val context = LocalContext.current
        PharmacyPhoneContact(context, pharmacy.contacts.phone)
        SpacerMedium()
        PharmacyEmailContact(context = context, pharmacy.contacts.mail)
        SpacerMedium()
        if (pharmacy.contacts.url.isNotEmpty()) {
            PharmacyWebSite(pharmacy.contacts.url)
            SpacerMedium()
        }
        SpacerMedium()
        DataInfoSection(modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun PharmacyOpeningHours(openingHours: OpeningHours) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Column {
        Text(
            text = stringResource(id = R.string.pharm_detail_opening_hours),
            style = MaterialTheme.typography.h6
        )

        SpacerMedium()

        val sortedOpeningHours = OpeningHours(openingHours.toSortedMap(compareBy { it }))

        for (h in sortedOpeningHours) {
            val (day, hours) = h
            val now = remember { OffsetDateTime.now() }
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
                        val opens = hour.openingTime.format(dateTimeFormatter)
                        val closes = hour.closingTime.format(dateTimeFormatter)
                        val text = "$opens - $closes"
                        val isOpenNow =
                            remember(now) { hour.isOpenAt(now.toLocalTime()) && isOpenToday }
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
            Spacer16()
        }
    }
}

@Preview
@Composable
private fun PharmacyOpeningHoursPreview() {
    val now = OffsetDateTime.now()
    AppTheme {
        PharmacyOpeningHours(
            OpeningHours(
                mapOf(
                    DayOfWeek.MONDAY to listOf(
                        OpeningTime(
                            LocalTime.of(12, 1),
                            LocalTime.of(14, 1)
                        )
                    ),
                    DayOfWeek.TUESDAY to listOf(
                        OpeningTime(
                            LocalTime.of(8, 0),
                            LocalTime.of(18, 0)
                        )
                    ),
                    DayOfWeek.WEDNESDAY to listOf(
                        OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                        OpeningTime(LocalTime.of(14, 0), LocalTime.of(18, 0)),
                    ),
                    now.dayOfWeek to listOf(
                        OpeningTime(
                            now.toLocalTime() - Duration.ofHours(2),
                            now.toLocalTime() + Duration.ofHours(2)
                        ),
                        OpeningTime(
                            now.toLocalTime() + Duration.ofHours(4),
                            now.toLocalTime() + Duration.ofHours(6)
                        ),
                    )
                )
            )
        )
    }
}

@Composable
private fun PharmacyPhoneContact(context: Context, phone: String) {
    Label(
        text = phone,
        label = stringResource(id = R.string.pres_detail_organization_label_telephone),
        onClick = {
            context.handleIntent(providePhoneIntent(it))
        }
    )
}

@Composable
private fun PharmacyEmailContact(context: Context, mail: String) {
    Label(
        text = mail,
        label = stringResource(id = R.string.pres_detail_organization_label_email),
        onClick = {
            val intent = provideEmailIntent(it)
            if (canHandleIntent(intent, context.packageManager)) {
                context.startActivity(intent)
            }
        }
    )
}

@Composable
private fun DataInfoSection(modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    val uriPharmacyPortal = stringResource(id = R.string.pharmacy_detail_pharmacy_portal_uri)
    val uriFaq = stringResource(id = R.string.pharmacy_detail_data_info_faqs_uri)
    val textPharmacyPortal = stringResource(id = R.string.pharmacy_detail_data_info_domain)
    val infoText = stringResource(id = R.string.pharmacy_detail_data_info)
    val start = infoText.indexOf(textPharmacyPortal)
    val end = start + textPharmacyPortal.length
    val styledText = with(AnnotatedString.Builder()) {
        append(infoText)
        addStringAnnotation(
            tag = "URL",
            annotation = uriPharmacyPortal,
            start = start,
            end = end
        )
        addStyle(
            SpanStyle(color = AppTheme.colors.primary600),
            start,
            end
        )
        toAnnotatedString()
    }
    ClickableText(
        modifier = modifier
            .fillMaxWidth(),
        text = styledText,
        style = AppTheme.typography.body2l,
        onClick = {
            styledText
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }
    )
    Spacer8()
    Row(modifier = modifier) {
        HintTextActionButton(text = stringResource(id = R.string.pharmacy_detail_data_info_btn)) {
            uriHandler.openUri(uriFaq)
        }
    }
}

@Composable
private fun PharmacyWebSite(url: String) {
    val uriHandler = LocalUriHandler.current
    Label(
        text = url,
        label = stringResource(id = R.string.pharm_detail_website),
        onClick = {
            uriHandler.openUri(it)
        }
    )
}

@Composable
private fun Label(
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
            style = MaterialTheme.typography.body1,
            color = AppTheme.colors.primary600
        )
        Spacer4()
        Text(
            text = label,
            style = AppTheme.typography.body2l,
        )
    }
}

private fun launchMaps(context: Context, location: Location, pharmacyName: String) {
    val gmmIntentUri =
        Uri.parse("geo:${location.latitude},${location.longitude}?q=$pharmacyName&z=16")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.resolveActivity(context.packageManager)?.let {
        context.startActivity(mapIntent)
    }
}
