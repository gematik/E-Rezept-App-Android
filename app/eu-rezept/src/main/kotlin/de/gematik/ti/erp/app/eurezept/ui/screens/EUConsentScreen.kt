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

package de.gematik.ti.erp.app.eurezept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuConsentScreenController
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall

class EUConsentScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberEuConsentScreenController()
        val lazyListState = rememberLazyListState()
        val taskId = navBackStackEntry.arguments?.getString(EuRoutes.EU_NAV_TASK_ID) ?: ""

        EuConsentScreenScaffold(
            listState = lazyListState,
            onBack = {
                navController.popBackStack()
            },
            onCancel = {
                navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                    popUpTo(EuRoutes.EuConsentScreen.route) { inclusive = true }
                }
            },
            onAccept = {
                controller.onConsentAccepted()
                val taskId = navBackStackEntry.arguments?.getString(EuRoutes.EU_NAV_TASK_ID)

                val routeToNavigate = if (taskId.isNullOrEmpty()) {
                    EuRoutes.EuRedeemScreen.path()
                } else {
                    EuRoutes.EuRedeemScreen.path(taskId)
                }
                navController.navigate(routeToNavigate)
            },
            onDecline = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun EuConsentScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    EuRedeemScaffold(
        modifier = modifier.navigationBarsPadding(),
        listState = listState,
        onBack = onBack,
        onCancel = onCancel,
        topBarTitle = "",
        bottomBar = {
            EuConsentBottomBar(
                onAccept = onAccept,
                onDecline = onDecline
            )
        }
    ) { paddingValues ->
        EuConsentScreenContent(
            paddingValues = paddingValues
        )
    }
}

@Composable
fun EuConsentScreenContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(AppTheme.colors.neutral000)
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Medium)
    ) {
        Text(
            text = stringResource(R.string.eu_consent_title),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.neutral900
        )

        SpacerMedium()

        Text(
            text = stringResource(R.string.eu_consent_description),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.neutral900
        )

        SpacerMedium()

        Text(
            text = stringResource(R.string.eu_consent_withdrawal),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.neutral900
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.eu_consent_profile_settings),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.neutral600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EuConsentBottomBar(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()

        PrimaryButtonSmall(
            onClick = onAccept,
            modifier = Modifier
                .widthIn(min = SizeDefaults.twentyfivefold)
                .heightIn(min = SizeDefaults.sixfold),
            shape = RoundedCornerShape(SizeDefaults.triple),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.primary700,
                contentColor = AppTheme.colors.neutral000
            )
        ) {
            Text(
                text = stringResource(R.string.eu_consent_accept),
                style = MaterialTheme.typography.button
            )
        }
        SpacerSmall()

        PrimaryButtonSmall(
            onClick = onDecline,
            modifier = Modifier
                .widthIn(min = SizeDefaults.twentyfivefold)
                .heightIn(min = SizeDefaults.sixfold),
            shape = RoundedCornerShape(SizeDefaults.triple),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.primary700,
                contentColor = AppTheme.colors.neutral000
            )
        ) {
            Text(
                text = stringResource(R.string.eu_consent_decline),
                style = MaterialTheme.typography.button
            )
        }
        SpacerLarge()
    }
}

@LightDarkPreview
@Composable
fun EUConsentScreenScaffoldPreview() {
    val lazyListState = rememberLazyListState()

    PreviewTheme {
        EuConsentScreenScaffold(
            listState = lazyListState,
            onBack = {},
            onCancel = {},
            onAccept = {},
            onDecline = {}
        )
    }
}
