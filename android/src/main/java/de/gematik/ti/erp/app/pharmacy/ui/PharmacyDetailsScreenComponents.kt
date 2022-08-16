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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.navigationBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.fhir.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.fhir.model.OnlinePharmacyService
import de.gematik.ti.erp.app.fhir.model.OpeningHours
import de.gematik.ti.erp.app.fhir.model.PickUpPharmacyService
import de.gematik.ti.erp.app.fhir.model.isOpenToday
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PharmacyDetailsScreen(
    navController: NavController,
    viewModel: PharmacySearchViewModel
) {
    val context = LocalContext.current

    val state by viewModel.detailScreenState().collectAsState(null)
    val pharmacy = state?.selectedPharmacy

    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.pharmacy_detail_title),
        elevated = scrollState.value > 0,
        actions = {},
        navigationMode = NavigationBarMode.Back,
        onBack = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            if (pharmacy != null) {
                SpacerMedium()

                Text(
                    text = pharmacy.name,
                    style = AppTheme.typography.h5
                )
                SpacerSmall()
                Row(
                    modifier = Modifier
                        .clickable {
                            pharmacy.location?.let {
                                launchMaps(context, it, pharmacy.name)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pharmacy.removeLineBreaksFromAddress(),
                        style = AppTheme.typography.subtitle2,
                        color = MaterialTheme.colors.secondary
                    )
                    SpacerSmall()
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary
                    )
                }

                SpacerLarge()

                if (pharmacy.ready) {
                    val hasRedeemableTasks by produceState(false) {
                        viewModel.hasRedeemableTasks().collect { value = it }
                    }

                    OrderOptions(hasRedeemableTasks, pharmacy) {
                        viewModel.onSelectOrderOption(it)
                        navController.navigate(PharmacyNavigationScreens.OrderPrescription.path())
                    }

                    if (!hasRedeemableTasks) {
                        Text(
                            text = stringResource(R.string.pharmacy_detail_no_redeemable_prescription_info),
                            style = AppTheme.typography.caption1l,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    SpacerMedium()
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
}

@Composable
private fun OrderOptions(
    hasRedeemableTasks: Boolean,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onClickOrder: (PharmacyScreenData.OrderOption) -> Unit
) {
    if (pharmacy.provides.any { it is PickUpPharmacyService }) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = hasRedeemableTasks,
            onClick = { onClickOrder(PharmacyScreenData.OrderOption.ReserveInPharmacy) },
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
            enabled = hasRedeemableTasks,
            onClick = { onClickOrder(PharmacyScreenData.OrderOption.CourierDelivery) },
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
    if (pharmacy.provides.any { it is OnlinePharmacyService }) {
        Button(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = hasRedeemableTasks,
            onClick = { onClickOrder(PharmacyScreenData.OrderOption.MailDelivery) },
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
            style = AppTheme.typography.h6
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
            style = AppTheme.typography.h6
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
            style = AppTheme.typography.body1,
            color = AppTheme.colors.primary600
        )
        Spacer4()
        Text(
            text = label,
            style = AppTheme.typography.body2l
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
