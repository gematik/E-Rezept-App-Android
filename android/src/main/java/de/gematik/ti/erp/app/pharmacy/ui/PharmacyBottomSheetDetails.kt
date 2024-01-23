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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.pharmacy.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import kotlinx.coroutines.launch

@Composable
fun PharmacyBottomSheetDetails(
    orderState: PharmacyOrderState,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    pharmacyPortalUri: String = stringResource(R.string.pharmacy_detail_pharmacy_portal_uri),
    pharmacyPortalText: String = stringResource(R.string.pharmacy_detail_data_info_domain),
    infoText: String = stringResource(R.string.pharmacy_detail_data_info),
    faqUri: String = stringResource(R.string.pharmacy_detail_data_info_faqs_uri),
    onClickOrder: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val controller = rememberPharmacyController()
    val scope = rememberCoroutineScope()
    val styledText = buildStyledTextForPharmacy(
        infoText = infoText,
        pharmacyPortalUri = pharmacyPortalUri,
        start = infoText.indexOf(pharmacyPortalText),
        end = infoText.indexOf(pharmacyPortalText) + pharmacyPortalText.length
    )
    val hasRedeemableTasks = orderState.hasRedeemableTasks
    var showNoRedeemableTasksDialog by remember { mutableStateOf(false) }

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
                controller.isPharmacyInFavorites(pharmacy).collect { value = it }
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
            onOrderClicked = { pharmacy: PharmacyUseCaseData.Pharmacy, option: PharmacyScreenData.OrderOption ->
                if (!hasRedeemableTasks.value) {
                    showNoRedeemableTasksDialog = true
                } else {
                    onClickOrder(pharmacy, option)
                }
            }
        )
        SpacerXXLarge()
        PharmacyContact(
            openingHours = pharmacy.openingHours,
            phone = pharmacy.contacts.phone,
            mail = pharmacy.contacts.mail,
            url = pharmacy.contacts.url,
            detailedInfoText = styledText,
            onPhoneClicked = { context.handleIntent(providePhoneIntent(it)) },
            onMailClicked = { emailAddress ->
                val intent = provideEmailIntent(emailAddress)
                if (canHandleIntent(intent, context.packageManager)) {
                    context.startActivity(intent)
                } else {
                    // Should we do something here?
                }
            },
            onUrlClicked = { url -> uriHandler.openUri(url) },
            onTextClicked = {
                styledText
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            },
            onHintClicked = { uriHandler.openUri(faqUri) }
        )
    }
}

@Composable
private fun buildStyledTextForPharmacy(
    infoText: String,
    pharmacyPortalUri: String,
    start: Int,
    end: Int
) = with(AnnotatedString.Builder()) {
    append(infoText)
    addStringAnnotation(
        tag = "URL",
        annotation = pharmacyPortalUri,
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
