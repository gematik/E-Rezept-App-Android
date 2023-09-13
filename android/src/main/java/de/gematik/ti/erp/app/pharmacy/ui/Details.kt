/*
 * Copyright (c) 2023 gematik GmbH
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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.pharmacy.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.fhir.model.OpeningHours
import de.gematik.ti.erp.app.fhir.model.isOpenToday
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PharmacyDetailsSheetContent(
    orderState: PharmacyOrderState,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onClickOrder: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val controller = rememberPharmacyController()
    val scope = rememberCoroutineScope()

    val hasRedeemableTasks = orderState.hasRedeemableTasks
    var showNoRedeemableTasksDialog by remember { mutableStateOf(false) }

    val onClickOrderFn = { pharmacy: PharmacyUseCaseData.Pharmacy, option: PharmacyScreenData.OrderOption ->
        if (!hasRedeemableTasks.value) {
            showNoRedeemableTasksDialog = true
        } else {
            onClickOrder(pharmacy, option)
        }
    }

    if (showNoRedeemableTasksDialog) {
        AcceptDialog(
            header = stringResource(R.string.pharmacy_order_no_prescriptions_title),
            info = stringResource(R.string.pharmacy_order_no_prescriptions_desc),
            acceptText = stringResource(R.string.ok),
            onClickAccept = {
                showNoRedeemableTasksDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Large)
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .testTag(TestTag.PharmacySearch.OrderOptions.Content),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        role = Role.Button,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        pharmacy.location?.let {
                            navigateWithGoogleMaps(context, it) ?: launchMaps(context, it)
                        }
                    }
            ) {
                Text(
                    text = pharmacy.name,
                    style = AppTheme.typography.h6,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                SpacerTiny()
                Text(
                    text = pharmacy.singleLineAddress(),
                    style = AppTheme.typography.subtitle2,
                    color = MaterialTheme.colors.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val isMarkedAsFavorite by produceState(false, pharmacy) {
                controller.isPharmacyInFavorites(pharmacy).collect {
                    value = it
                }
            }
            SpacerMedium()
            FavoriteStarButton(
                isMarked = isMarkedAsFavorite,
                modifier = Modifier,
                onChange = {
                    scope.launch {
                        if (it) {
                            controller.markPharmacyAsFavorite(pharmacy)
                        } else {
                            controller.unmarkPharmacyAsFavorite(pharmacy)
                        }
                    }
                }
            )
        }
        SpacerXXLarge()

        OrderSelection(
            orderState = orderState,
            pharmacy = pharmacy,
            onClickOrder = onClickOrderFn
        )

        SpacerXXLarge()

        PharmacyInfo(pharmacy)
    }
}

private const val NrOfAllOrderOptions = 3

@Composable
private fun OrderSelection(
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onClickOrder: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit,
    orderState: PharmacyOrderState
) {
    val scope = rememberCoroutineScope()
    var directRedeemEnabled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            directRedeemEnabled = orderState.profile.lastAuthenticated == null
        }
    }
    val directPickUpServiceAvailable = directRedeemEnabled && pharmacy.contacts.pickUpUrl.isNotEmpty()
    val pickUpServiceVisible =
        pharmacy.pickupServiceAvailable() || directPickUpServiceAvailable
    val pickupServiceEnabled = directPickUpServiceAvailable ||
        !directRedeemEnabled && pharmacy.pickupServiceAvailable()

    val directDeliveryServiceAvailable = directRedeemEnabled && pharmacy.contacts.deliveryUrl.isNotEmpty()
    val deliveryServiceVisible =
        directDeliveryServiceAvailable || pharmacy.deliveryServiceAvailable()
    val deliveryServiceEnabled = directDeliveryServiceAvailable ||
        !directRedeemEnabled && pharmacy.deliveryServiceAvailable()

    val directOnlineServiceAvailable = directRedeemEnabled && pharmacy.contacts.onlineServiceUrl.isNotEmpty()
    val onlineServiceVisible =
        pharmacy.onlineServiceAvailable() || directOnlineServiceAvailable
    val onlineServiceEnabled = directOnlineServiceAvailable ||
        !directRedeemEnabled && pharmacy.onlineServiceAvailable()

    val nrOfServices = remember(pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible) {
        listOf(pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible).count { it }
    }
    val isSingle = nrOfServices == 1
    val isLarge = nrOfServices != NrOfAllOrderOptions

    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        val orderModifier = Modifier.weight(weight = 0.5f).fillMaxHeight()
        if (pickUpServiceVisible) {
            OrderButton(
                modifier = orderModifier.testTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton),
                enabled = pickupServiceEnabled,
                onClick = { onClickOrder(pharmacy, PharmacyScreenData.OrderOption.PickupService) },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_collect),
                image = painterResource(R.drawable.pharmacy_small)
            )
        }

        if (deliveryServiceVisible) {
            OrderButton(
                modifier = orderModifier.testTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton),
                enabled = deliveryServiceEnabled,
                onClick = { onClickOrder(pharmacy, PharmacyScreenData.OrderOption.CourierDelivery) },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_delivery),
                image = painterResource(R.drawable.delivery_car_small)
            )
        }
        if (onlineServiceVisible) {
            OrderButton(
                modifier = orderModifier
                    .testTag(TestTag.PharmacySearch.OrderOptions.OnlineDeliveryOptionButton),
                enabled = onlineServiceEnabled,
                onClick = { onClickOrder(pharmacy, PharmacyScreenData.OrderOption.MailDelivery) },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_mail),
                image = painterResource(R.drawable.truck_small)
            )
        }

        if (isSingle) {
            Spacer(Modifier.weight(weight = 0.5f))
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun OrderButton(
    modifier: Modifier,
    enabled: Boolean,
    isLarge: Boolean = true,
    text: String,
    image: Painter,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16.dp)
    val connectText = stringResource(R.string.connect_for_pharmacy_service)
    Column(
        modifier = modifier
            .background(AppTheme.colors.neutral100, shape)
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClick = {
                    if (enabled) {
                        onClick()
                    } else {
                        createToastShort(context, connectText)
                    }
                }
            )
            .padding(PaddingDefaults.Medium)
            .alpha(
                if (enabled) {
                    1f
                } else { 0.3f }
            )
    ) {
        val imgModifier = if (isLarge) {
            Modifier.align(Alignment.End)
        } else {
            Modifier.align(Alignment.CenterHorizontally)
        }
        Image(image, null, modifier = imgModifier)
        SpacerTiny()

        val txtModifier = if (isLarge) {
            Modifier.align(Alignment.Start)
        } else {
            Modifier.align(Alignment.CenterHorizontally)
        }
        Text(text, modifier = txtModifier, style = AppTheme.typography.subtitle2)
    }
}

@Composable
private fun FavoriteStarButton(
    isMarked: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Boolean) -> Unit
) {
    val color = if (isMarked) {
        AppTheme.colors.yellow500
    } else {
        AppTheme.colors.primary600
    }

    val icon = if (isMarked) {
        Icons.Rounded.Star
    } else {
        Icons.Rounded.StarBorder
    }

    val addedText = stringResource(R.string.pharmacy_detals_added_to_favorites)
    val removedText = stringResource(R.string.pharmacy_detalls_removed_from_favorites)

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    TertiaryButton(
        modifier = modifier.size(56.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onChange(!isMarked)
            createToastShort(
                context,
                if (!isMarked) {
                    addedText
                } else {
                    removedText
                }
            )
        },
        contentPadding = PaddingValues(PaddingDefaults.Medium)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color
        )
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
            text = stringResource(R.string.legal_notice_contact_header),
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
            text = stringResource(R.string.pharm_detail_opening_hours),
            style = AppTheme.typography.h6
        )

        SpacerMedium()

        val sortedOpeningHours = OpeningHours(openingHours.toSortedMap(compareBy { it }))

        for (h in sortedOpeningHours) {
            val (day, hours) = h
            val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
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
                        val opens = hour.openingTime?.toJavaLocalTime()?.format(dateTimeFormatter) ?: ""
                        val closes = hour.closingTime?.toJavaLocalTime()?.format(dateTimeFormatter) ?: ""
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
private fun PharmacyPhoneContact(context: Context, phone: String) {
    Label(
        text = phone,
        label = stringResource(R.string.pres_detail_organization_label_telephone),
        onClick = {
            context.handleIntent(providePhoneIntent(it))
        }
    )
}

@Composable
private fun PharmacyEmailContact(context: Context, mail: String) {
    Label(
        text = mail,
        label = stringResource(R.string.pres_detail_organization_label_email),
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
    val uriPharmacyPortal = stringResource(R.string.pharmacy_detail_pharmacy_portal_uri)
    val uriFaq = stringResource(R.string.pharmacy_detail_data_info_faqs_uri)
    val textPharmacyPortal = stringResource(R.string.pharmacy_detail_data_info_domain)
    val infoText = stringResource(R.string.pharmacy_detail_data_info)
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
    SpacerSmall()
    Row(modifier = modifier) {
        HintTextActionButton(text = stringResource(R.string.pharmacy_detail_data_info_btn)) {
            uriHandler.openUri(uriFaq)
        }
    }
}

@Composable
private fun PharmacyWebSite(url: String) {
    val uriHandler = LocalUriHandler.current
    Label(
        text = url,
        label = stringResource(R.string.pharm_detail_website),
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
        SpacerTiny()
        Text(
            text = label,
            style = AppTheme.typography.body2l
        )
    }
}

private fun launchMaps(context: Context, location: Location) {
    val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?z=16")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.resolveActivity(context.packageManager)?.let {
        context.startActivity(mapIntent)
    }
}

private fun navigateWithGoogleMaps(
    context: Context,
    location: Location
): Any? {
    val gmmIntentUri =
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    return mapIntent.resolveActivity(context.packageManager)?.let {
        context.startActivity(mapIntent)
    }
}
