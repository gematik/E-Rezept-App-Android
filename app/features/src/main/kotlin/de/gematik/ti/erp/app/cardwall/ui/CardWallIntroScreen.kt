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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.components.EnableNfcDialog
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.gid.ui.DomainsNotVerifiedDialog
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ClickText
import de.gematik.ti.erp.app.utils.compose.ClickableText
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

@Requirement(
    "O.Auth_3#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Selection of Authentication with health card or insurance App"
)
class CardWallIntroScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val profileId = remember { navBackStackEntry.arguments?.getString(CardWallRoutes.profileId) ?: "" }
        graphController.setProfileId(profileId)

        val lazyListState = rememberLazyListState()
        val cardWallController = rememberCardWallController()

        val activity = LocalActivity.current
        val dialog = LocalDialog.current
        val context = LocalContext.current

        val nfcDisabledEvent = ComposableEvent<Unit>()
        val domainsAreNotVerifiedEvent = ComposableEvent<Unit>()

        val isDomainVerified = cardWallController.isDomainVerified
        val isNfcAvailable by cardWallController.isNFCAvailable

        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.Intro.IntroScreen)
                .systemBarsPadding(),
            title = "",
            listState = lazyListState,
            backMode = null,
            actions = {
                TextButton(
                    onClick = {
                        graphController.reset()
                        navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onNext = null,
            nextText = "",
            onBack = { navController.popBackStack() }
        ) {
            CardWallIntroScreenContent(
                isNfcAvailable = isNfcAvailable,
                isDomainVerified = isDomainVerified,
                listState = lazyListState,
                checkNfcEnabled = { cardWallController.checkNfcEnabled() },
                showEnableNFCDialog = { nfcDisabledEvent.trigger(Unit) },
                onClickOrderNow = { navController.navigate(MainNavigationScreens.OrderHealthCard.path()) },
                onClickHealthCardAuth = { navController.navigate(CardWallRoutes.CardWallCanScreen.path()) },
                onClickInsuranceAuth = {
                    navController.navigate(CardWallRoutes.CardWallExternalAuthenticationScreen.path())
                },
                showVerifyDomainDialog = { domainsAreNotVerifiedEvent.trigger(Unit) },
                onClickDemoMode = {
                    DemoModeIntent.startAppWithDemoMode<MainActivity>(activity = activity)
                }
            )
        }
        nfcDisabledEvent.listen {
            dialog.show {
                EnableNfcDialog(
                    onClickAction = { it.dismiss() },
                    onCancel = { it.dismiss() }
                )
            }
        }
        domainsAreNotVerifiedEvent.listen {
            dialog.show {
                DomainsNotVerifiedDialog(
                    onClickSettingsOpen = {
                        context.openSettingsAsNewActivity()
                        it.dismiss()
                    },
                    onDismissRequest = { it.dismiss() }
                )
            }
        }
    }
}

@Composable
private fun CardWallIntroScreenContent(
    isNfcAvailable: Boolean,
    isDomainVerified: Boolean,
    listState: LazyListState,
    checkNfcEnabled: () -> Boolean,
    showEnableNFCDialog: () -> Unit,
    onClickHealthCardAuth: () -> Unit,
    onClickInsuranceAuth: () -> Unit,
    onClickOrderNow: () -> Unit,
    showVerifyDomainDialog: () -> Unit,
    onClickDemoMode: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HealthCardPhoneImage()
        Header()
        SubTitleHeader()
        HealthCardLoginSection(
            isNfcAvailable,
            checkNfcEnabled,
            onClickHealthCardAuth,
            showEnableNFCDialog
        )
        GidLoginSection(
            isDomainVerified = isDomainVerified,
            showVerifyDomainDialog = showVerifyDomainDialog,
            onClick = onClickInsuranceAuth
        )
        OrderHealthCardHintSection(onClickOrderNow)
        TryDemomodeSection(onClickDemoMode)
    }
}

@Suppress("FunctionName")
private fun LazyListScope.HealthCardPhoneImage() {
    item {
        Image(
            painterResource(R.drawable.card_wall_card_hand),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.Header() {
    item {
        Text(
            stringResource(R.string.cdw_intro_header),
            style = AppTheme.typography.h5,
            color = AppTheme.colors.neutral900,
            modifier = Modifier.testTag("cdw_txt_intro_header_bottom")
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.SubTitleHeader() {
    item { SpacerSmall() }
    item {
        Text(
            stringResource(R.string.cdw_intro_info),
            style = AppTheme.typography.subtitle2,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.neutral600
        )
    }
    item { SpacerXLarge() }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.HealthCardLoginSection(
    isNfcAvailable: Boolean,
    checkNfcEnabled: () -> Boolean,
    onClick: () -> Unit,
    showEnableNFCDialog: () -> Unit
) {
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.cdw_intro_auth_prior),
                modifier = Modifier.align(Alignment.Start).offset(x = PaddingDefaults.Medium),
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primary600
            )
            Card(
                modifier = CardPaddingModifier,
                shape = RoundedCornerShape(SizeDefaults.double),
                border = BorderStroke(SizeDefaults.quarter, color = AppTheme.colors.primary600),
                elevation = SizeDefaults.zero,
                backgroundColor = AppTheme.colors.neutral050,
                enabled = isNfcAvailable,
                onClick = {
                    if (checkNfcEnabled()) {
                        onClick()
                    } else {
                        showEnableNFCDialog()
                    }
                }
            ) {
                Row(
                    Modifier.padding(PaddingDefaults.Medium)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.cdw_intro_auth_health_card),
                            style = AppTheme.typography.subtitle1l,
                            color = when {
                                isNfcAvailable -> AppTheme.colors.neutral900
                                else -> AppTheme.colors.neutral400
                            }
                        )
                        SpacerTiny()
                        Text(
                            when {
                                isNfcAvailable -> stringResource(R.string.cdw_intro_auth_health_card_pin)
                                else -> stringResource(R.string.cdw_intro_auth_health_card_no_nfc_device)
                            },
                            style = AppTheme.typography.body2l,
                            color = when {
                                isNfcAvailable -> AppTheme.colors.neutral600
                                else -> AppTheme.colors.neutral400
                            }
                        )
                    }
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        null,
                        tint = if (isNfcAvailable) AppTheme.colors.primary600 else AppTheme.colors.neutral300,
                        modifier = Modifier
                            .size(SizeDefaults.triple)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.GidLoginSection(
    isDomainVerified: Boolean,
    showVerifyDomainDialog: () -> Unit,
    onClick: () -> Unit
) {
    item {
        Card(
            modifier = CardPaddingModifier,
            shape = RoundedCornerShape(SizeDefaults.double),
            border = BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.neutral300),
            elevation = SizeDefaults.zero,
            backgroundColor = AppTheme.colors.neutral050,
            onClick = {
                when {
                    isDomainVerified -> onClick()
                    else -> showVerifyDomainDialog()
                }
            }
        ) {
            Row(
                modifier = Modifier.padding(
                    PaddingDefaults.Medium
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        stringResource(R.string.cdw_intro_auth_gid),
                        style = AppTheme.typography.subtitle1l,
                        color = AppTheme.colors.neutral900
                    )
                    SpacerTiny()
                    Text(
                        stringResource(R.string.cdw_intro_auth_additional_app_required),
                        style = AppTheme.typography.body2l,
                        color = AppTheme.colors.neutral600
                    )
                }
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral400,
                    modifier = Modifier
                        .size(SizeDefaults.triple)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.OrderHealthCardHintSection(onClickOrderNow: () -> Unit) {
    item { SpacerSmall() }
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.cdw_have_no_card_with_pin),
                style = AppTheme.typography.body2l
            )
            HintTextActionButton(
                text = stringResource(R.string.cdw_intro_order_now),
                align = Alignment.End,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag(TestTag.CardWall.Intro.OrderEgkButton)
            ) {
                onClickOrderNow()
            }
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.TryDemomodeSection(onClickDemoMode: () -> Unit) {
    item { SpacerMedium() }
    item {
        ClickableText(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Large),
            textWithPlaceholdersRes = R.string.demo_mode_start_text,
            textStyle = AppTheme.typography.body1l,
            clickText = ClickText(
                text = stringResource(R.string.demo_mode_link_text),
                onClick = onClickDemoMode
            )
        )
    }
}

private val CardPaddingModifier = Modifier
    .padding(
        bottom = PaddingDefaults.Medium
    )
    .fillMaxWidth()
