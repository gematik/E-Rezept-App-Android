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

package de.gematik.ti.erp.app.pkv.ui

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.currencyString
import de.gematik.ti.erp.app.mainscreen.ui.rememberMainScreenController
import de.gematik.ti.erp.app.pkv.ui.ConsentController.State.ChargeConsentNotGranted.isConsentGranted
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.rememberRefreshPrescriptionsController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ConnectBottomBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun InvoicesScreen(
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    onShowCardWall: () -> Unit,
    onClickInvoice: (String) -> Unit,
    invoicesController: InvoicesController
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val consentController = rememberConsentController(profile = selectedProfile)
    var consentGranted by remember { mutableStateOf(false) }
    var showGrantConsentDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val ssoTokenValid = rememberSaveable(selectedProfile.ssoTokenScope) {
        selectedProfile.ssoTokenValid()
    }

    CheckConsentState(consentController, ssoTokenValid, scaffoldState, context) {
        consentGranted = it
        showGrantConsentDialog = !consentGranted
    }

    var showRevokeConsentAlert by remember { mutableStateOf(false) }

    if (showGrantConsentDialog && selectedProfile.ssoTokenValid()) {
        GrantConsentDialog(
            onCancel = onBack
        ) {
            onGrantConsent(context, scope, consentController, scaffoldState) {
                consentGranted = it
                showGrantConsentDialog = false
            }
        }
    }

    if (showRevokeConsentAlert) {
        RevokeConsentDialog(
            onCancel = { showRevokeConsentAlert = false }
        ) {
            onRevokeConsent(context, scope, consentController, scaffoldState, { consentGranted = it }) {
                onBack()
            }
        }
    }

    var showDeleteInvoiceAlert by remember { mutableStateOf(false) }
    var invoiceToDeleteTaskID: String? by remember { mutableStateOf(null) }

    if (showDeleteInvoiceAlert) {
        DeleteInvoiceDialog(
            onCancel = {
                showDeleteInvoiceAlert = false
                invoiceToDeleteTaskID = null
            }
        ) {
            onDeleteInvoice(
                scope,
                invoiceToDeleteTaskID,
                invoicesController,
                selectedProfile,
                context,
                scaffoldState
            ) {
                showDeleteInvoiceAlert = false
                invoiceToDeleteTaskID = null
            }
        }
    }

    val mainScreenController = rememberMainScreenController()
    val refreshPrescriptionsController = rememberRefreshPrescriptionsController(mainScreenController)

    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .visualTestTag(TestTag.Profile.InvoicesScreen),
        topBarTitle = stringResource(R.string.profile_invoices),
        snackbarHost = {
            SnackbarHost(it, modifier = Modifier.systemBarsPadding())
        },
        bottomBar = {
            if (!ssoTokenValid) {
                ConnectBottomBar(
                    infoText = stringResource(R.string.invoices_connect_info)
                ) {
                    scope.launch {
                        refreshPrescriptionsController.refresh(
                            profileId = selectedProfile.id,
                            isUserAction = true,
                            onUserNotAuthenticated = {},
                            onShowCardWall = onShowCardWall
                        )
                    }
                }
            }
        },
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        listState = listState,
        actions = {
            Row {
                InvoicesHeaderThreeDotMenu(
                    consentGranted = remember(consentGranted) { consentGranted },
                    onClickRevokeConsent = { showRevokeConsentAlert = true }
                )
            }
        },
        onBack = onBack
    ) {
        RefreshInvoicesContent(
            profileIdentifier = selectedProfile.id,
            invoicesController = invoicesController,
            ssoTokenValid = ssoTokenValid,
            listState = listState,
            consentGranted = consentGranted,
            onRefreshInvoicesError = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            },
            onClickInvoice = onClickInvoice
        )
    }
}

@Composable
fun CheckConsentState(
    consentController: ConsentController,
    ssoTokenValid: Boolean,
    scaffoldState: ScaffoldState,
    context: Context,
    saveConsentState: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        if (ssoTokenValid) {
            val consentState = consentController.getChargeConsent().first()
            when (consentState) {
                is PrescriptionServiceErrorState -> {
                    consentErrorMessage(context, consentState)?.let {
                        scaffoldState.snackbarHostState.showSnackbar(it)
                    }
                }
            }
            saveConsentState(consentState.isConsentGranted())
        }
    }
}

fun onGrantConsent(
    context: Context,
    scope: CoroutineScope,
    consentController: ConsentController,
    scaffoldState: ScaffoldState,
    saveConsentState: (Boolean) -> Unit
) {
    scope.launch {
        val consentState = consentController.grantChargeConsent().first()
        when (consentState) {
            is PrescriptionServiceErrorState -> {
                consentErrorMessage(context, consentState)?.let {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
        saveConsentState(consentState.isConsentGranted())
    }
}

fun onRevokeConsent(
    context: Context,
    scope: CoroutineScope,
    consentController: ConsentController,
    scaffoldState: ScaffoldState,
    saveConsentState: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    scope.launch {
        val consentState = consentController.revokeChargeConsent().first()
        when (consentState) {
            is PrescriptionServiceErrorState -> {
                consentErrorMessage(context, consentState)?.let {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
        saveConsentState(consentState.isConsentGranted())
        onBack()
    }
}
fun onDeleteInvoice(
    scope: CoroutineScope,
    invoiceToDeleteTaskID: String?,
    invoicesController: InvoicesController,
    selectedProfile: ProfilesUseCaseData.Profile,
    context: Context,
    scaffoldState: ScaffoldState,
    finally: () -> Unit
) {
    scope.launch {
        val invoiceState =
            invoiceToDeleteTaskID?.let {
                invoicesController.deleteInvoice(
                    profileId = selectedProfile.id,
                    taskId = it
                )
            }
        when (invoiceState) {
            is PrescriptionServiceErrorState -> {
                refreshInvoicesErrorMessage(context, invoiceState)?.let {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
        finally()
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun RefreshInvoicesContent(
    profileIdentifier: ProfileIdentifier,
    invoicesController: InvoicesController,
    listState: LazyListState,
    ssoTokenValid: Boolean,
    consentGranted: Boolean,
    onRefreshInvoicesError: (String) -> Unit,
    onClickInvoice: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isRefreshing by invoicesController.isRefreshing

    fun refresh() = scope.launch {
        when (val state = invoicesController.downloadInvoices(profileIdentifier).first()) {
            is PrescriptionServiceErrorState -> {
                refreshInvoicesErrorMessage(context, state)?.let {
                    onRefreshInvoicesError(it)
                }
            }
        }
    }

    val refreshState = rememberPullRefreshState(isRefreshing, ::refresh)

    LaunchedEffect(ssoTokenValid, consentGranted) {
        if (ssoTokenValid && consentGranted) {
            refresh()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pullRefresh(refreshState, enabled = ssoTokenValid && consentGranted)
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = AppTheme.colors.primary600,
            scale = true
        )
        Invoices(
            listState = listState,
            invoicesController = invoicesController,
            onClickInvoice = onClickInvoice
        )
    }
}

@Composable
fun InvoicesHeaderThreeDotMenu(consentGranted: Boolean, onClickRevokeConsent: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true }
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            onClick = {
                onClickRevokeConsent()
                expanded = false
            },
            enabled = consentGranted
        ) {
            Text(
                text = stringResource(R.string.profile_revoke_consent),
                color = if (consentGranted) {
                    AppTheme.colors.red600
                } else {
                    AppTheme.colors.neutral900
                }
            )
        }
    }
}

@Composable
fun Invoices(
    listState: LazyListState,
    invoicesController: InvoicesController,
    onClickInvoice: (String) -> Unit
) {
    val invoiceState by invoicesController.state

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
        state = listState
    ) {
        invoiceState?.let { state ->
            if (state.entries.isEmpty()) {
                item {
                    InvoicesEmptyScreen()
                }
            } else {
                state.entries.forEach { entry ->
                    item {
                        val formattedYear = remember {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy")
                            if (entry.value.isNotEmpty()) {
                                entry.value[0].timestamp
                                    .toLocalDateTime(currentSystemDefault())
                                    .toJavaLocalDateTime().format(dateFormatter)
                            } else {
                                ""
                            }
                        }

                        val totalSumOfInvoices = entry.value.sumOf {
                            it.invoice.totalBruttoAmount
                        }.currencyString()
                        SpacerMedium()
                        HeadingPerYear(formattedYear, totalSumOfInvoices, entry.value[0].invoice.currency)
                    }
                    entry.value.forEach { invoice ->
                        item {
                            val formattedDate = remember {
                                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                invoice.timestamp
                                    .toLocalDateTime(currentSystemDefault())
                                    .toJavaLocalDateTime().format(dateFormatter)
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
}

@Composable
private fun Invoice(
    invoice: InvoiceData.PKVInvoice,
    formattedDate: String,
    onClickInvoice: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClickInvoice(invoice.taskId)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium
            ).weight(1f)

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
            Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
        }
    }
}

@Composable
fun TotalBruttoAmountChip(
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
        modifier = Modifier
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
            text = stringResource(
                R.string.pkv_invoices_total_of_year,
                totalSumOfInvoices,
                currency
            ),
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.primary600
        )
    }
}

@Composable
fun LazyItemScope.InvoicesEmptyScreen() {
    Column(
        modifier = Modifier
            .fillParentMaxSize()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(R.drawable.girl_red_oh_no),
            contentDescription = null
        )
        Text(
            stringResource(R.string.invoices_no_invoices),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = -(PaddingDefaults.Large))
        )
    }
}
