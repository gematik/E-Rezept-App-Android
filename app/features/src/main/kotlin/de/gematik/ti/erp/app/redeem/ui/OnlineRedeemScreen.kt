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

package de.gematik.ti.erp.app.redeem.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyOrderController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

@Composable
fun OnlineRedeemScreen(
    orderState: PharmacyOrderController,
    onClickSelectPrescriptions: () -> Unit,
    onClickAllPrescriptions: () -> Unit,
    onBack: () -> Unit
) {
    val prescriptions by orderState.prescriptionsState
    val scrollState = rememberScrollState()
    val elevated by remember {
        derivedStateOf { scrollState.value > 0 }
    }
    AnimatedElevationScaffold(
        topBarTitle = "",
        navigationMode = NavigationBarMode.Back,
        elevated = elevated,
        onBack = onBack,
        actions = {}
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Image(painterResource(R.drawable.order_onb_blue_handoutmedicine), null)
            Spacer(Modifier.height(80.dp))
            Text(
                stringResource(R.string.online_redeem_choose_rx_title),
                style = AppTheme.typography.h5,
                textAlign = TextAlign.Center
            )
            SpacerSmall()
            Text(
                pluralStringResource(R.plurals.online_redeem_rx_desc, prescriptions.size, prescriptions.size),
                style = AppTheme.typography.subtitle2l,
                textAlign = TextAlign.Center
            )
            SpacerXXLarge()
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.online_redeem_choose_rx),
                onClick = onClickSelectPrescriptions
            )
            SpacerMedium()
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.online_redeem_choose_all_rx),
                onClick = onClickAllPrescriptions
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
