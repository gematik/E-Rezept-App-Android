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

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenController
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.pkv.ui.ConsentController.State.ChargeConsentNotGranted.isConsentGranted
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.rememberRefreshPrescriptionsController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun InvoiceInformationScreen(
    mainScreenController: MainScreenController,
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    onShowCardWall: () -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val consentController = rememberConsentController(profile = selectedProfile)

    val ssoTokenValid by remember(selectedProfile) {
        derivedStateOf {
            selectedProfile.ssoTokenValid()
        }
    }

    var consentGranted by remember { mutableStateOf(false) }

    var showGrantConsentDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
            consentGranted = consentState.isConsentGranted()
            showGrantConsentDialog = !consentGranted
        }
    }

    var connectBottomBarVisible by remember { mutableStateOf(!ssoTokenValid) }

    var showRevokeConsentAlert by remember { mutableStateOf(false) }

    if (showGrantConsentDialog && selectedProfile.ssoTokenValid()) {
        GrantConsentDialog(
            onCancel = onBack,
            onGrantConsent = {
                scope.launch {
                    val consentState = consentController.grantChargeConsent().first()
                    when (consentState) {
                        is PrescriptionServiceErrorState -> {
                            consentErrorMessage(context, consentState)?.let {
                                scaffoldState.snackbarHostState.showSnackbar(it)
                            }
                        }
                    }
                    consentGranted = consentState.isConsentGranted()
                    showGrantConsentDialog = false
                }
            }
        )
    }

    if (showRevokeConsentAlert) {
        RevokeConsentDialog(
            onCancel = { showRevokeConsentAlert = false },
            onRevokeConsent = {
                scope.launch {
                    val consentState = consentController.revokeChargeConsent().first()
                    when (consentState) {
                        is PrescriptionServiceErrorState -> {
                            consentErrorMessage(context, consentState)?.let {
                                scaffoldState.snackbarHostState.showSnackbar(it)
                            }
                        }
                    }
                    consentGranted = consentState.isConsentGranted()
                    onBack()
                }
            }
        )
    }

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
            if (connectBottomBarVisible) {
                ConnectBottomBar {
                    scope.launch {
                        refreshPrescriptionsController.refresh(
                            profileId = selectedProfile.id,
                            isUserAction = true,
                            onUserNotAuthenticated = { connectBottomBarVisible = true },
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
            InvoicesThreeDotMenu(
                consentGranted = consentGranted,
                onClickRevokeConsent = { showRevokeConsentAlert = true }
            )
        },
        onBack = onBack
    ) {
        RefreshScaffold(
            profileId = selectedProfile.id,
            onUserNotAuthenticated = { connectBottomBarVisible = true },
            mainScreenController = mainScreenController,
            onShowCardWall = {}
        ) { _ ->
            Invoices(
                listState = listState
            )
        }
    }
}

@Composable
fun RevokeConsentDialog(onCancel: () -> Unit, onRevokeConsent: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.profile_revoke_consent_header),
        info = stringResource(R.string.profile_revoke_consent_info),
        cancelText = stringResource(R.string.profile_invoices_cancel),
        actionText = stringResource(R.string.profile_revoke_consent),
        onCancel = onCancel,
        onClickAction = onRevokeConsent
    )
}

@Composable
fun GrantConsentDialog(onCancel: () -> Unit, onGrantConsent: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.profile_grant_consent_header),
        info = stringResource(R.string.profile_grant_consent_info),
        cancelText = stringResource(R.string.profile_invoices_cancel),
        actionText = stringResource(R.string.profile_grant_consent),
        onCancel = onCancel,
        onClickAction = onGrantConsent
    )
}

@Composable
fun InvoicesThreeDotMenu(consentGranted: Boolean, onClickRevokeConsent: () -> Unit) {
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
                text =
                stringResource(R.string.profile_revoke_consent)
            )
        }
    }
}

@Composable
fun Invoices(
    listState: LazyListState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState
    ) {
        item {
            InvoicesEmptyScreen()
        }
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
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ConnectBottomBar(onClickConnect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                color = AppTheme.colors.primary100
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.invoices_connect_info),
            modifier = Modifier
                .padding(PaddingDefaults.Medium)
                .weight(1f),
            style = AppTheme.typography.body2
        )
        Button(
            onClick = onClickConnect,
            modifier = Modifier.padding(end = PaddingDefaults.Medium)
        ) {
            Text(text = stringResource(R.string.invoices_connect_btn))
        }
    }
}
