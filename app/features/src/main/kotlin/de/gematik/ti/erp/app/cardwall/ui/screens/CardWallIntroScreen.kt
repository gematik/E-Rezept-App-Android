/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.app.Dialog
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.model.GidNavigationData.Companion.isPkv
import de.gematik.ti.erp.app.base.fold
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.base.requireNonNull
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.processGidEventData
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.components.EnableNfcDialog
import de.gematik.ti.erp.app.cardwall.ui.components.GematikErrorDialog
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallIntroScreenContentPreviewParameterProvider
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallIntroScreenPreviewData
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.gid.ui.components.DomainsNotVerifiedDialog
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.semantics.semanticsButton
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.semantics.semanticsMergeDescendants
import de.gematik.ti.erp.app.semantics.semanticsMergedButton
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.SpacerXXXLarge
import de.gematik.ti.erp.app.utils.compose.ClickText
import de.gematik.ti.erp.app.utils.compose.ClickableText
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LoadingDialog
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

@Requirement(
    "O.Resi_1#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Selection of login methods for the user"
)
class CardWallIntroScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        requireNonNull(
            navBackStackEntry.arguments?.getString(
                CardWallRoutes.CARD_WALL_NAV_PROFILE_ID
            )
        ).fold(
            onSuccess = { profileId ->
                // this information is available on the screen only when the process wants gid authentication
                val gidEventData = navBackStackEntry.processGidEventData()

                var loadingDialog: Dialog? = remember { null }

                graphController.setProfileId(profileId)

                val lazyListState = rememberLazyListState()
                val cardWallController = rememberCardWallController()

                val activity = LocalActivity.current
                val dialog = LocalDialog.current
                val context = LocalContext.current
                val intentHandler = LocalIntentHandler.current

                val nfcDisabledEvent = ComposableEvent<Unit>()
                val domainsAreNotVerifiedEvent = ComposableEvent<Unit>()

                val isDomainVerified = cardWallController.isDomainVerified
                val isNfcAvailable by cardWallController.isNFCAvailable

                with(graphController) {
                    authorizationWithExternalAppInBackgroundEvent.listen { isStarted ->
                        if (isStarted) {
                            dialog.show {
                                loadingDialog = it
                                LoadingDialog { it.dismiss() }
                            }
                        } else {
                            loadingDialog?.dismiss()
                        }
                    }
                    redirectUriEvent.listen { (redirectUri, gidEventData) ->
                        intentHandler.tryStartingExternalHealthInsuranceAuthenticationApp(
                            redirect = redirectUri,
                            onSuccess = {
                                if (gidEventData.isPkv()) {
                                    graphController.switchToPKV(profileId)
                                }
                                navController.popBackStack()
                            },
                            onFailure = {
                                dialog.show {
                                    ErezeptAlertDialog(
                                        title = stringResource(R.string.gid_external_app_missing_title),
                                        body = stringResource(R.string.gid_external_app_missing_description),
                                        okText = stringResource(R.string.ok),
                                        onDismissRequest = { it.dismiss() }
                                    )
                                }
                            }
                        )
                    }
                    redirectUriErrorEvent.listen {
                        dialog.show {
                            ErezeptAlertDialog(
                                title = stringResource(R.string.main_fasttrack_error_title),
                                body = stringResource(R.string.main_fasttrack_error_info),
                                okText = stringResource(R.string.ok),
                                onDismissRequest = it::dismiss
                            )
                        }
                    }
                    redirectUriGematikErrorEvent.listen { responseError ->
                        dialog.show {
                            GematikErrorDialog(error = responseError) {
                                it.dismiss()
                            }
                        }
                    }
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
                                context.openSettingsAsNewActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                it.dismiss()
                            },
                            onDismissRequest = { it.dismiss() }
                        )
                    }
                }

                BackHandler {
                    navController.popBackStack()
                }
                CardWallScaffold(
                    modifier = Modifier
                        .testTag(TestTag.CardWall.Intro.IntroScreen)
                        .systemBarsPadding(),
                    listState = lazyListState,
                    title = "",
                    backMode = null,
                    onNext = null,
                    nextText = "",
                    actions = {
                        TextButton(
                            onClick = {
                                graphController.reset()
                                navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    onBack = { navController.popBackStack() }
                ) {
                    CardWallIntroScreenContent(
                        isNfcAvailable = isNfcAvailable,
                        isNfcEnabled = { cardWallController.isNfcEnabled() },
                        isDomainVerified = isDomainVerified,
                        listState = lazyListState,
                        showEnableNFCDialog = { nfcDisabledEvent.trigger(Unit) },
                        insuranceName = gidEventData?.authenticatorName,
                        onClickOrderNow = {
                            navController.navigate(
                                OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.path()
                            )
                        },
                        onClickHealthCardAuth = { navController.navigate(CardWallRoutes.CardWallCanScreen.path()) },
                        onClickInsuranceAuth = {
                            gidEventData?.let {
                                graphController.startAuthorizationWithExternal(gidEventData)
                            } ?: navController.navigate(CardWallRoutes.CardWallGidListScreen.path())
                        },
                        showVerifyDomainDialog = { domainsAreNotVerifiedEvent.trigger() },
                        onClickDemoMode = {
                            DemoModeIntent.startAppWithDemoMode<MainActivity>(activity = activity)
                        }
                    )
                }
            }
        )
    }
}

@Requirement(
    "A_25416#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Selection of login methods for the user",
    codeLines = 36
)
@Composable
private fun CardWallIntroScreenContent(
    isNfcAvailable: Boolean,
    isDomainVerified: Boolean,
    listState: LazyListState,
    isNfcEnabled: () -> Boolean,
    insuranceName: String?,
    showEnableNFCDialog: () -> Unit,
    onClickHealthCardAuth: () -> Unit,
    onClickInsuranceAuth: () -> Unit,
    onClickOrderNow: () -> Unit,
    showVerifyDomainDialog: () -> Unit,
    onClickDemoMode: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HealthCardPhoneImage()
        Header()
        SubTitleHeader()
        HealthCardLoginSection(
            isNfcAvailable = isNfcAvailable,
            isNfcEnabled = isNfcEnabled,
            onClickWithEnabledNfc = onClickHealthCardAuth,
            onClickWithDisabledNfc = showEnableNFCDialog
        )
        @Requirement(
            "O.Auth_4#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "The UI component that allows the user to login via GID"
        )
        GidLoginSection(
            isDomainVerified = isDomainVerified,
            insuranceName = insuranceName,
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
            contentDescription = stringResource(R.string.a11y_card_wall_card_hand),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
private fun LazyListScope.HealthCardLoginSection(
    isNfcAvailable: Boolean,
    isNfcEnabled: () -> Boolean,
    onClickWithEnabledNfc: () -> Unit,
    onClickWithDisabledNfc: () -> Unit
) {
    item {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isNfcAvailable) {
                Text(
                    text = stringResource(R.string.cdw_intro_auth_prior),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .offset(x = PaddingDefaults.Medium)
                        .semanticsHeading(),
                    style = AppTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.primary700
                )
            }
            Card(
                modifier = Modifier
                    .padding(bottom = PaddingDefaults.Medium)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(SizeDefaults.double),
                border = if (isNfcAvailable) BorderStroke(SizeDefaults.quarter, color = AppTheme.colors.primary700) else null,
                elevation = SizeDefaults.zero,
                backgroundColor = AppTheme.colors.neutral050,
                enabled = isNfcAvailable,
                onClick = {
                    if (isNfcEnabled()) {
                        onClickWithEnabledNfc()
                    } else {
                        onClickWithDisabledNfc()
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(PaddingDefaults.Medium)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semanticsMergeDescendants {
                                if (isNfcAvailable) {
                                    role = Role.Button
                                } else {
                                    disabled()
                                    invisibleToUser()
                                }
                            }
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
                                else -> stringResource(R.string.cdw_health_card_no_nfc_device)
                            },
                            style = AppTheme.typography.body2l,
                            color = when {
                                isNfcAvailable -> AppTheme.colors.neutral600
                                else -> AppTheme.colors.neutral400
                            }
                        )
                    }
                    Icon(
                        modifier = Modifier
                            .size(SizeDefaults.triple)
                            .align(Alignment.CenterVertically),
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (isNfcAvailable) AppTheme.colors.primary700 else AppTheme.colors.neutral300
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
    insuranceName: String?,
    showVerifyDomainDialog: () -> Unit,
    onClick: () -> Unit
) {
    item {
        Card(
            modifier = Modifier
                .padding(bottom = PaddingDefaults.Medium)
                .fillMaxWidth(),
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
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .semanticsMergedButton(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.cdw_intro_auth_gid),
                        style = AppTheme.typography.subtitle1l,
                        color = AppTheme.colors.neutral900
                    )
                    SpacerTiny()
                    Text(
                        text = if (insuranceName != null) {
                            stringResource(R.string.cdw_intro_auth_gid_app_required, insuranceName)
                        } else {
                            stringResource(R.string.cdw_intro_auth_additional_app_required)
                        },
                        style = AppTheme.typography.body2l,
                        color = AppTheme.colors.neutral600
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
private fun LazyListScope.OrderHealthCardHintSection(
    onClickOrderNow: () -> Unit
) {
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
                    .semanticsButton()
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
    item {
        Column(
            modifier = Modifier.semanticsMergedButton()
        ) {
            SpacerMedium()
            ClickableText(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Large),
                textWithPlaceholdersRes = R.string.demo_mode_start_text,
                textStyle = AppTheme.typography.body1l,
                clickText = ClickText(
                    text = stringResource(R.string.demo_mode_link_text),
                    onClick = onClickDemoMode
                )
            )
            SpacerXXXLarge()
        }
    }
}

@LightDarkPreview
@Composable
fun CardWallIntroScreenContentPreview(
    @PreviewParameter(CardWallIntroScreenContentPreviewParameterProvider::class) state:
        CardWallIntroScreenPreviewData
) {
    PreviewAppTheme {
        CardWallIntroScreenContent(
            listState = rememberLazyListState(),
            isNfcAvailable = state.isNfcAvailable,
            isNfcEnabled = { state.isNfcEnabled },
            isDomainVerified = state.isDomainVerified,
            insuranceName = null,
            showEnableNFCDialog = { },
            onClickHealthCardAuth = { },
            onClickInsuranceAuth = { },
            onClickOrderNow = { },
            showVerifyDomainDialog = { },
            onClickDemoMode = { }
        )
    }
}
