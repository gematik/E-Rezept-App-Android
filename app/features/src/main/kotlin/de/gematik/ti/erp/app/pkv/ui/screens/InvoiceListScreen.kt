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

package de.gematik.ti.erp.app.pkv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isNotGranted
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.currencyString
import de.gematik.ti.erp.app.launchedeffect.LaunchedEffectOnStart
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes.getPkvNavigationProfileId
import de.gematik.ti.erp.app.pkv.presentation.ConsentValidator
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.pkv.ui.components.GrantConsentDialog
import de.gematik.ti.erp.app.pkv.ui.components.InvoiceListLoading
import de.gematik.ti.erp.app.pkv.ui.components.InvoicesEmptyScreen
import de.gematik.ti.erp.app.pkv.ui.components.RevokeConsentDialog
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceListScreenPreviewData
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceListScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pulltorefresh.PullToRefresh
import de.gematik.ti.erp.app.pulltorefresh.extensions.trigger
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ConnectBottomBar
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.show
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class InvoiceListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val profileId = navBackStackEntry.arguments.getPkvNavigationProfileId()

        val context = LocalContext.current
        val snackbar = LocalSnackbarScaffold.current
        val dialog = LocalDialog.current
        val intentHandler = LocalIntentHandler.current

        val consentRevokedInfo = stringResource(R.string.consent_revoked_info)
        val consentGrantedInfo = stringResource(R.string.consent_granted_info)

        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        val pullToRefreshState = rememberPullToRefreshState()

        val invoiceController = rememberInvoiceController(profileId)
        val consentController = rememberConsentController()

        val invoicesState: UiState<Map<Year, List<InvoiceData.PKVInvoiceRecord>>> by invoiceController.invoices.collectAsStateWithLifecycle()
        val isSsoTokenValid by invoiceController.isSsoTokenValidForSelectedProfile.collectAsStateWithLifecycle()
        val isRefreshing by invoiceController.isRefreshing.collectAsStateWithLifecycle()

        val onGrantConsentEvent = ComposableEvent<Unit>()
        val onRevokeConsentEvent = ComposableEvent<Unit>()

        val consentState by consentController.consentState.collectAsStateWithLifecycle()
        val isConsentGranted by consentController.isConsentGranted.collectAsStateWithLifecycle()
        val isConsentNotGranted by consentController.isConsentNotGranted.collectAsStateWithLifecycle()

        var consentRecentlyRevoked by remember { mutableStateOf(false) } // required for back navigation

        val onBack: () -> Unit = {
            // when navigation came from prescription details and
            // the user removes the consent we need to skip the invoice detail screen
            if (isConsentGranted && !consentRecentlyRevoked) {
                navController.popBackStack()
            } else {
                navController.popBackStack(PkvRoutes.subGraphName(), true)
            }
        }

        BackHandler { onBack() }

        navBackStackEntry.onReturnAction(PkvRoutes.InvoiceListScreen) {
            invoiceController.refreshCombinedProfile()
            invoiceController.downloadInvoices()
            invoiceController.invoiceListScreenEvents.getConsentEvent.trigger(profileId)
        }

        with(invoiceController) {
            invoiceListScreenEvents.invoiceErrorEvent.listen { error ->
                snackbar.show(
                    message = invoiceController.invoiceErrorMessage(context, error.errorState),
                    scope = scope
                )
            }
            invoiceListScreenEvents.downloadCompletedEvent.listen {
                pullToRefreshState.endRefresh()
            }

            showCardWallEvent.listen { id ->
                navController.navigate(CardWallRoutes.CardWallIntroScreen.path(id))
            }
            showCardWallWithFilledCanEvent.listen { cardWallData ->
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = cardWallData.profileId,
                        can = cardWallData.can
                    )
                )
            }
            showGidEvent.listen { gidData ->
                navController.navigate(
                    CardWallRoutes.CardWallIntroScreen.pathWithGid(
                        profileIdentifier = gidData.profileId,
                        gidEventData = gidData
                    )
                )
            }

            invoiceListScreenEvents.getConsentEvent.listen { profileId ->
                consentController.getChargeConsent(profileId)
            }
        }

        AuthenticationFailureDialog(
            event = invoiceController.invoiceListScreenEvents.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )

        RevokeConsentDialog(
            dialogScaffold = dialog,
            onRevokeConsentEvent = onRevokeConsentEvent,
            onRevokeConsent = {
                consentRecentlyRevoked = true
                invoiceController.deleteLocalInvoices()
                consentController.revokeChargeConsent(profileId)
                consentController.saveConsentDrawerShown(profileId)
                navController.navigateAndClearStack(route = PrescriptionRoutes.PrescriptionsScreen.route)
            }
        )

        GrantConsentDialog(
            dialogScaffold = dialog,
            onGrantConsentEvent = onGrantConsentEvent,
            onShow = { pullToRefreshState.endRefresh() },
            onGrantConsent = { consentController.grantChargeConsent(profileId) }
        )

        LaunchedEffectOnStart {
            consentController.getChargeConsent(profileId)
        }

        LaunchedEffect(isConsentNotGranted) {
            if (isConsentNotGranted) {
                onGrantConsentEvent.trigger()
            }
        }

        HandleConsentState(
            consentState = consentState,
            dialog = dialog,
            onRetry = { consentContext ->
                when (consentContext) {
                    ConsentContext.GetConsent -> invoiceController.invoiceListScreenEvents.getConsentEvent.trigger(profileId)
                    ConsentContext.GrantConsent -> {
                        consentController.grantChargeConsent(profileId)
                    }

                    ConsentContext.RevokeConsent -> consentController.revokeChargeConsent(profileId)
                }
            },
            onShowCardWall = { invoiceController.chooseAuthenticationMethod(profileId) },
            onDeleteLocalInvoices = { invoiceController.deleteLocalInvoices() },
            onConsentGranted = { snackbar.show(consentGrantedInfo, scope) },
            onConsentRevoked = { snackbar.show(consentRevokedInfo, scope) }
        )

        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                invoiceController.refreshCombinedProfile()
                invoiceController.downloadInvoices()
            }

            if (isSsoTokenValid) {
                invoiceController.downloadInvoices()
            }
        }

        with(pullToRefreshState) {
            trigger(
                onStartRefreshing = { startRefresh() },
                block = {
                    ConsentValidator.validateAndExecute(
                        isSsoTokenValid = isSsoTokenValid,
                        consentState = consentState,
                        getChargeConsent = { invoiceController.invoiceListScreenEvents.getConsentEvent.trigger(profileId) },
                        onConsentGranted = invoiceController::downloadInvoices,
                        grantConsent = { onGrantConsentEvent.trigger() }
                    )
                },
                onNavigation = {
                    if (!isSsoTokenValid) {
                        endRefresh()
                        invoiceController.chooseAuthenticationMethod(profileId)
                    }
                }
            )
        }

        LaunchedEffect(isSsoTokenValid, consentState.isNotGranted()) {
            if (isSsoTokenValid && consentState.isNotGranted()) {
                onGrantConsentEvent.trigger()
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            InvoiceListScreenScaffold(
                profileId = profileId,
                isSsoTokenValid = isSsoTokenValid,
                listState = listState,
                scaffoldState = scaffoldState,
                isConsentGranted = isConsentGranted,
                consentState = consentState,
                invoicesState = invoicesState,
                onClickConnect = { _ ->
                    invoiceController.chooseAuthenticationMethod(profileId)
                },
                onClickInvoice = { id, taskId ->
                    navController.navigate(PkvRoutes.InvoiceDetailsScreen.path(taskId = taskId, profileId = id))
                },
                onClickGrantConsent = { onGrantConsentEvent.trigger() },
                onClickRevokeConsent = { onRevokeConsentEvent.trigger() },
                onBack = onBack
            )

            if (isRefreshing) {
                LoadingIndicator()
            }

            PullToRefresh(
                modifier = Modifier.align(Alignment.TopCenter),
                pullToRefreshState = pullToRefreshState
            )
        }
    }
}

@Composable
private fun InvoiceListScreenScaffold(
    profileId: ProfileIdentifier,
    isSsoTokenValid: Boolean,
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    consentState: ConsentState,
    isConsentGranted: Boolean,
    invoicesState: UiState<Map<Year, List<InvoiceData.PKVInvoiceRecord>>>,
    onClickConnect: (profileId: ProfileIdentifier) -> Unit,
    onClickInvoice: (profileId: ProfileIdentifier, taskId: String) -> Unit,
    onClickGrantConsent: () -> Unit,
    onClickRevokeConsent: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.imePadding(),
        topBarTitle = stringResource(R.string.profile_invoices),
        snackbarHost = { SnackbarHost(it, modifier = Modifier.systemBarsPadding()) },
        bottomBar = {
            when {
                !isSsoTokenValid -> {
                    ConnectBottomBar(
                        infoText = stringResource(R.string.invoices_connect_info)
                    ) {
                        onClickConnect(profileId)
                    }
                }

                isSsoTokenValid && !isConsentGranted -> {
                    // TODO: Need text for this case
                }
            }
        },
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        listState = listState,
        actions = {
            if (consentState !is ConsentState.ValidState.Loading && isSsoTokenValid) {
                Row {
                    InvoicesHeaderThreeDotMenu(
                        consentGranted = isConsentGranted,
                        onClickGrantConsent = { onClickGrantConsent() },
                        onClickRevokeConsent = { onClickRevokeConsent() }
                    )
                }
            }
        },
        onBack = onBack
    ) {
        RefreshInvoicesContent(
            invoicesState = invoicesState,
            listState = listState,
            onClickInvoice = { taskId -> onClickInvoice(profileId, taskId) }
        )
    }
}

@Composable
private fun RefreshInvoicesContent(
    listState: LazyListState,
    invoicesState: UiState<Map<Year, List<InvoiceData.PKVInvoiceRecord>>>,
    onClickInvoice: (String) -> Unit
) {
    UiStateMachine(
        state = invoicesState,
        onLoading = { InvoiceListLoading() }
    ) { invoices ->
        Invoices(
            listState = listState,
            invoices = invoices,
            onClickInvoice = onClickInvoice
        )
    }
}

@Composable
private fun InvoicesHeaderThreeDotMenu(
    consentGranted: Boolean,
    onClickGrantConsent: () -> Unit,
    onClickRevokeConsent: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true }
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(SizeDefaults.triple, SizeDefaults.zero)
    ) {
        DropdownMenuItem(
            onClick = {
                if (consentGranted) {
                    onClickRevokeConsent()
                } else {
                    onClickGrantConsent()
                }
                expanded = false
            }
        ) {
            Text(
                text =
                if (consentGranted) {
                    stringResource(R.string.profile_revoke_consent)
                } else {
                    stringResource(R.string.profile_grant_consent)
                },
                color =
                if (consentGranted) {
                    AppTheme.colors.red600
                } else {
                    AppTheme.colors.neutral900
                }
            )
        }
    }
}

@Composable
private fun Invoices(
    listState: LazyListState,
    invoices: Map<Year, List<InvoiceData.PKVInvoiceRecord>>,
    onClickInvoice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
        state = listState
    ) {
        if (invoices.entries.isEmpty()) {
            item {
                InvoicesEmptyScreen()
            }
        } else {
            invoices.entries.forEach { entry ->
                item {
                    val formattedYear =
                        remember {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy")
                            if (entry.value.isNotEmpty()) {
                                entry.value[0]
                                    .timestamp
                                    .toLocalDateTime(currentSystemDefault())
                                    .toJavaLocalDateTime()
                                    .format(dateFormatter)
                            } else {
                                ""
                            }
                        }

                    val totalSumOfInvoices =
                        entry.value
                            .sumOf {
                                it.invoice.totalBruttoAmount
                            }.currencyString()
                    SpacerMedium()
                    HeadingPerYear(formattedYear, totalSumOfInvoices, entry.value[0].invoice.currency)
                }
                entry.value.forEach { invoice ->
                    item {
                        val formattedDate =
                            remember(invoice) {
                                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                invoice.timestamp
                                    .toLocalDateTime(currentSystemDefault())
                                    .toJavaLocalDateTime()
                                    .format(dateFormatter)
                            }
                        Invoice(
                            invoice = invoice,
                            formattedDate = formattedDate,
                            onClickInvoice = onClickInvoice
                        )
                        Divider(modifier = Modifier.padding(start = PaddingDefaults.Medium))
                    }
                }
                item { SpacerXLarge() }
            }
        }
    }
}

@Composable
private fun Invoice(
    invoice: InvoiceData.PKVInvoiceRecord,
    formattedDate: String,
    onClickInvoice: (String) -> Unit
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable {
                onClickInvoice(invoice.taskId)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier =
            Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Medium
                )
                .weight(1f)
        ) {
            val itemName = invoice.medicationRequest.medication?.name() ?: ""

            Text(
                itemName,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900
            )
            SpacerTiny()
            Text(
                formattedDate,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body2l
            )
            SpacerSmall()
            TotalBruttoAmountChip(
                text = invoice.invoice.totalBruttoAmount.currencyString() + " " + invoice.invoice.currency
            )
        }
        Row(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium), horizontalArrangement = Arrangement.End) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
        }
    }
}

@Composable
private fun TotalBruttoAmountChip(
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        Modifier
            .background(AppTheme.colors.neutral025, shape)
            .border(1.dp, AppTheme.colors.neutral300, shape)
            .clip(shape)
            .then(modifier)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = AppTheme.typography.subtitle2, color = AppTheme.colors.neutral900)
    }
}

@Composable
private fun HeadingPerYear(
    formattedYear: String,
    totalSumOfInvoices: String,
    currency: String
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedYear,
            style = AppTheme.typography.h6
        )
        Text(
            text =
            stringResource(
                R.string.pkv_invoices_total_of_year,
                totalSumOfInvoices,
                currency
            ),
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.primary600
        )
    }
}

@LightDarkPreview
@Composable
fun InvoiceListScreenScaffoldPreview(
    @PreviewParameter(InvoiceListScreenPreviewParameterProvider::class) previewData: InvoiceListScreenPreviewData
) {
    PreviewAppTheme {
        InvoiceListScreenScaffold(
            profileId = "profileId",
            isSsoTokenValid = previewData.isSsoTokenValid,
            listState = rememberLazyListState(),
            scaffoldState = rememberScaffoldState(),
            isConsentGranted = previewData.isConsentGranted,
            consentState = ConsentState.ValidState.Loading,
            invoicesState = UiState.Data(previewData.invoices),
            onClickConnect = {},
            onClickInvoice = { _, _ -> },
            onClickGrantConsent = {},
            onClickRevokeConsent = {},
            onBack = {}
        )
    }
}
