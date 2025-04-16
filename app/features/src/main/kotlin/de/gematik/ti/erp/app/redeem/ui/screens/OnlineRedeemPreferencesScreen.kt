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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewParameterProvider
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.ui.components.TextFlatButton
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class OnlineRedeemPreferencesScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val controller: OnlineRedeemGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val prescriptions by controller.redeemableOrderState
        val listState = rememberLazyListState()
        AnimatedElevationScaffold(
            topBarTitle = "",
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            onBack = { navController.popBackStack() },
            actions = {}
        ) {
            OnlineRedeemPreferencesScreenScaffold(
                listState = listState,
                prescriptions = prescriptions,
                onNavigateToRedeemSelection = {
                    navController.navigate(
                        RedeemRoutes.RedeemPrescriptionSelection.path(isModal = false)
                    )
                },
                onNavigateToPharmacyStart = {
                    navController.navigate(PharmacyRoutes.PharmacyStartScreenModal.path(taskId = ""))
                },
                onResetPrescriptionSelection = { controller.onResetPrescriptionSelection() }
            )
        }
    }
}

@Composable
fun OnlineRedeemPreferencesScreenScaffold(
    listState: LazyListState,
    prescriptions: List<PharmacyUseCaseData.PrescriptionInOrder>,
    onNavigateToRedeemSelection: () -> Unit,
    onNavigateToPharmacyStart: () -> Unit,
    onResetPrescriptionSelection: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(SizeDefaults.fivefold))
            Image(painterResource(R.drawable.order_onb_blue_handoutmedicine), null)
            Spacer(Modifier.height(SizeDefaults.tenfold))
        }
        item {
            Text(
                stringResource(R.string.online_redeem_choose_rx_title),
                style = AppTheme.typography.h5,
                textAlign = TextAlign.Center
            )
            SpacerSmall()
        }
        item {
            Text(
                pluralStringResource(R.plurals.online_redeem_rx_desc, prescriptions.size, prescriptions.size),
                style = AppTheme.typography.subtitle2l,
                textAlign = TextAlign.Center
            )
            SpacerXXLarge()
        }
        item {
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.online_redeem_choose_rx),
                onClick = onNavigateToRedeemSelection
            )
            SpacerMedium()
        }
        item {
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.online_redeem_choose_all_rx),
                onClick = {
                    onResetPrescriptionSelection()
                    onNavigateToPharmacyStart()
                }
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@LightDarkPreview
@Composable
fun OnlineRedeemPreferencesScreenPreview(
    @PreviewParameter(OnlineRedeemPreferencesScreenPreviewParameterProvider::class)
    prescriptions: List<PharmacyUseCaseData.PrescriptionInOrder>
) {
    val listState = rememberLazyListState()

    PreviewAppTheme {
        OnlineRedeemPreferencesScreenScaffold(
            listState = listState,
            prescriptions = prescriptions,
            onNavigateToRedeemSelection = {},
            onNavigateToPharmacyStart = {},
            onResetPrescriptionSelection = {}
        )
    }
}
