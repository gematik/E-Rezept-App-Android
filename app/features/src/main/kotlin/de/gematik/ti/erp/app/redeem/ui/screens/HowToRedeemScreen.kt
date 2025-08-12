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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.rememberHowToRedeemController
import de.gematik.ti.erp.app.redeem.ui.components.TextFlatButton
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.ErezeptText.HeaderStyle
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import androidx.compose.runtime.getValue

class HowToRedeemScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val howToRedeemController = rememberHowToRedeemController()
        val hasEuRedeemablePrescriptions by howToRedeemController.hasEuRedeemablePrescriptions

        HowToRedeemScreenScaffold(
            listState = listState,
            hasEuRedeemablePrescriptions = hasEuRedeemablePrescriptions,
            onLocalClick = { navController.navigate(RedeemRoutes.RedeemLocal.path(taskId = "")) },
            onOnlineClick = {
                navController.navigate(
                    RedeemRoutes.RedeemOrderOverviewScreen.path(
                        pharmacy = null,
                        orderOption = null,
                        taskId = null
                    )
                )
            },
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun HowToRedeemScreenScaffold(
    listState: LazyListState,
    hasEuRedeemablePrescriptions: Boolean,
    onLocalClick: () -> Unit,
    onOnlineClick: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = "",
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Close,
        listState = listState,
        onBack = onBack
    ) {
        HowToRedeemScreenContent(
            listState = listState,
            hasEuRedeemablePrescriptions = hasEuRedeemablePrescriptions,
            onLocalClick = onLocalClick,
            onOnlineClick = onOnlineClick
        )
    }
}

@Composable
fun HowToRedeemScreenContent(
    listState: LazyListState,
    hasEuRedeemablePrescriptions: Boolean,
    onLocalClick: () -> Unit,
    onOnlineClick: () -> Unit
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
            Image(painterResource(R.drawable.order_onb_pharmacist_blue), null)
            Spacer(Modifier.height(SizeDefaults.tenfold))
        }
        item {
            ErezeptText.Title(
                stringResource(R.string.order_onb_how_to_title),
                style = HeaderStyle.H5,
                textAlignment = TextAlignment.Center
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.order_onb_how_to_desc),
                style = AppTheme.typography.subtitle2l,
                textAlign = TextAlign.Center
            )
            SpacerXXLarge()
        }
        item {
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.order_onb_how_to_local),
                description = stringResource(R.string.order_onb_how_to_local_desc),
                onClick = onLocalClick
            )
            SpacerMedium()
        }
        item {
            TextFlatButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.order_onb_how_to_online),
                description = stringResource(R.string.order_onb_how_to_online_desc),
                onClick = onOnlineClick
            )

            Spacer(Modifier.navigationBarsPadding())
        }

        if (hasEuRedeemablePrescriptions) {
            item {
                SpacerXLarge()
                Text(
                    text = stringResource(R.string.eu_prescription_available),
                    style = AppTheme.typography.subtitle2l,
                    textAlign = TextAlign.Center,
                    color = AppTheme.colors.primary700
                )
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
    SpacerMedium()
}

@LightDarkPreview
@Composable
fun HowToRedeemScaffoldScreenPreview() {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        HowToRedeemScreenScaffold(
            listState = listState,
            hasEuRedeemablePrescriptions = false,
            onLocalClick = {},
            onOnlineClick = {},
            onBack = {}
        )
    }
}
