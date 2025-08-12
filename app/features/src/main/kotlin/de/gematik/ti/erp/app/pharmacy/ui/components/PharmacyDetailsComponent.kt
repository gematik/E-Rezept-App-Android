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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRouteBackStackEntryArguments
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.rememberPharmacyDetailsController
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyPortalText
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyPreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacySheetFromMessagesParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.gotoCoordinates
import de.gematik.ti.erp.app.utils.extensions.openEmailClient
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ScreenType {
    ForPharmacy,
    ForMessage,
}

@Composable
fun PharmacyDetailsComponent(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    graphController: PharmacyGraphController? = null,
    screenType: ScreenType
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val noRedeemableTaskDialogEvent = ComposableEvent<Unit>()

    NoRedeemableOrdersDialog(
        event = noRedeemableTaskDialogEvent,
        dialog = LocalDialog.current
    )
    // this can be opened while the keyboard is open on the list screen, this forces the keyboard to close
    LaunchedEffect(Unit) { keyboardController?.hide() }

    PharmacyRouteBackStackEntryArguments(navBackStackEntry).getPharmacy()?.let { pharmacy ->

        val faqUri: String = stringResource(R.string.pharmacy_detail_data_info_faqs_uri)

        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val urlText = PharmacyPortalText().urlText()
        val controller = rememberPharmacyDetailsController()

        LaunchedEffect(pharmacy) {
            controller.isPharmacyFavorite(pharmacy)
        }

        val isMarkedAsFavorite by controller.isPharmacyFavorite.collectAsStateWithLifecycle()

        val showTelematikId by controller.showTelematikId.collectAsStateWithLifecycle(false)

        val isDirectRedeemEnabled by (graphController?.isDirectRedeemEnabled() ?: remember { mutableStateOf(false) })

        val hasRedeemableOrders by (graphController?.hasRedeemableOrders() ?: remember { mutableStateOf(false) })

        BasePharmacyDetailsContent(
            pharmacy = pharmacy,
            clickableText = urlText,
            isMarkedAsFavorite = isMarkedAsFavorite,
            isDirectRedeemEnabled = isDirectRedeemEnabled,
            showTelematikId = showTelematikId,
            onChangeFavoriteState = {
                controller.changePharmacyAsFavorite(pharmacy, it)
            },
            onClickOrder = { selectedPharmacy, orderOption ->
                if (!hasRedeemableOrders) {
                    noRedeemableTaskDialogEvent.trigger()
                } else {
                    navController.navigate(
                        RedeemRoutes.RedeemOrderOverviewScreen.path(
                            pharmacy = selectedPharmacy,
                            orderOption = orderOption,
                            taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
                        )
                    )
                }
            },
            openExternalMap = {
                context.gotoCoordinates(it)
            },
            onClickPhone = {
                context.handleIntent(providePhoneIntent(it))
            },
            onClickMail = { emailAddress ->
                context.openEmailClient(emailAddress)
            },
            onClickUrl = {
                uriHandler.openUriWhenValid(it)
            },
            onClickWebsite = {
                urlText.getStringAnnotations("URL", it, it + 1).firstOrNull()?.let { annotation ->
                    uriHandler.openUriWhenValid(annotation.item)
                }
            },
            onClickHint = {
                uriHandler.openUriWhenValid(faqUri)
            },
            screenType = screenType
        )
    } ?: run {
        ErrorScreenComponent()
    }
}

@Composable
private fun BasePharmacyDetailsContent(
    pharmacy: PharmacyUseCaseData.Pharmacy,
    clickableText: AnnotatedString,
    isMarkedAsFavorite: Boolean,
    isDirectRedeemEnabled: Boolean,
    screenType: ScreenType,
    currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    onClickOrder: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit,
    showTelematikId: Boolean,
    openExternalMap: (Coordinates) -> Unit,
    onChangeFavoriteState: (Boolean) -> Unit,
    onClickPhone: (String) -> Unit,
    onClickMail: (String) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickWebsite: (Int) -> Unit,
    onClickHint: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag(TestTag.PharmacySearch.OrderOptions.Content),
        containerColor = AppTheme.colors.neutral000

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    bottom = innerPadding.calculateBottomPadding()
                )

        ) {
            item {
                SpacerLarge()
            }
            item {
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                role = Role.Button,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                pharmacy.coordinates?.let { openExternalMap(it) }
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
                            color = AppTheme.colors.primary700,
                            textDecoration = TextDecoration.Underline,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SpacerMedium()
                    if (screenType == ScreenType.ForPharmacy) {
                        FavoriteStarButton(
                            isMarked = isMarkedAsFavorite,
                            modifier = Modifier,
                            onChange = {
                                onChangeFavoriteState(it)
                            }
                        )
                    }
                }
                SpacerXXLarge()
            }

            item {
                if (screenType == ScreenType.ForPharmacy) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OrderSelection(
                            pharmacy = pharmacy,
                            onOrderClicked = onClickOrder
                        )
                    }
                    SpacerWithTelematikId(showTelematikId)
                } else if (screenType == ScreenType.ForMessage) {
                    PharmacyContactSelection(
                        pharmacy = pharmacy,
                        onPhoneClicked = onClickPhone,
                        onMailClicked = onClickMail
                    )
                    SpacerWithTelematikId(showTelematikId)
                }
            }

            if (BuildConfigExtension.isInternalDebug && showTelematikId) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(SizeDefaults.one))
                            .clickable {
                                ClipBoardCopy.copyToClipboard(
                                    context = context,
                                    text = pharmacy.telematikId
                                )
                            }
                            .padding(SizeDefaults.one)
                    ) {
                        Text(
                            textAlign = TextAlign.Justify,
                            text = "Telematik-Id: ${pharmacy.telematikId}",
                            style = AppTheme.typography.body2l
                        )
                    }
                }
            }

            item {
                PharmacyContact(
                    openingHours = pharmacy.openingHours,
                    phone = pharmacy.contact.phone,
                    mail = pharmacy.contact.mail,
                    url = pharmacy.contact.url,
                    detailedInfoText = clickableText,
                    onPhoneClicked = onClickPhone,
                    onMailClicked = onClickMail,
                    onUrlClicked = onClickUrl,
                    onTextClicked = onClickWebsite,
                    onHintClicked = onClickHint,
                    currentDateTime = currentDateTime
                )
            }
            item {
                SpacerMedium()
            }
        }
    }
}

@Composable
private fun SpacerWithTelematikId(show: Boolean) {
    if (show) {
        SpacerXXLarge()
    } else {
        SpacerLarge()
    }
}

@Suppress("MagicNumber")
@LightDarkPreview
@Composable
fun PharmacyDetailsScreenFromPharmacyPreview(
    @PreviewParameter(
        PharmacyPreviewParameterProvider::class
    ) pharmacy: PharmacyUseCaseData.Pharmacy
) {
    PreviewAppTheme {
        BasePharmacyDetailsContent(
            pharmacy = pharmacy,
            clickableText = PharmacyPortalText().urlText(),
            isMarkedAsFavorite = true,
            isDirectRedeemEnabled = false,
            showTelematikId = false,
            screenType = ScreenType.ForPharmacy,
            currentDateTime = LocalDateTime(2024, 7, 31, 10, 0),
            onClickOrder = { _, _ -> },
            openExternalMap = {},
            onChangeFavoriteState = {},
            onClickPhone = {},
            onClickMail = {},
            onClickUrl = {},
            onClickWebsite = {},
            onClickHint = {}

        )
    }
}

@Suppress("MagicNumber")
@LightDarkPreview
@Composable
fun PharmacyDetailsScreenFromMessagePreview(
    @PreviewParameter(
        PharmacySheetFromMessagesParameterProvider::class
    ) pharmacy: PharmacyUseCaseData.Pharmacy
) {
    PreviewAppTheme {
        BasePharmacyDetailsContent(
            pharmacy = pharmacy,
            clickableText = PharmacyPortalText().urlText(),
            isMarkedAsFavorite = true,
            isDirectRedeemEnabled = true,
            showTelematikId = false,
            screenType = ScreenType.ForMessage,
            currentDateTime = LocalDateTime(2024, 7, 31, 10, 0),
            onClickOrder = { _, _ -> },
            openExternalMap = {},
            onChangeFavoriteState = {},
            onClickPhone = {},
            onClickMail = {},
            onClickUrl = {},
            onClickWebsite = {},
            onClickHint = {}
        )
    }
}
