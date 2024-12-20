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

package de.gematik.ti.erp.app.mlkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mlkit.navigation.MlKitRoutes
import de.gematik.ti.erp.app.mlkit.presentation.rememberMlKitController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold

class MlKitScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val mlKitController = rememberMlKitController()
        AnimatedElevationScaffold(
            modifier = Modifier
                .systemBarsPadding(),
            topBarTitle = "",
            bottomBar = {
                MlKitBottomBar(
                    onAccept = {
                        mlKitController.acceptMLKit()
                        navController.navigate(PrescriptionRoutes.PrescriptionScanScreen.path())
                    },
                    onClickReadMore = { navController.navigate(MlKitRoutes.MlKitInformationScreen.path()) }
                )
            },
            listState = listState,
            navigationMode = NavigationBarMode.Close,
            onBack = navController::popBackStack
        ) {
            MlKitScreenContent(
                it,
                listState
            )
        }
    }
}

@Composable
private fun MlKitScreenContent(
    contentPadding: PaddingValues,
    listState: LazyListState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium),
        state = listState,
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        item {
            Image(
                painterResource(R.drawable.woman_smartphone_circle_blue),
                null
            )
        }
        item {
            SpacerXXLarge()
            Text(
                text = stringResource(R.string.mlkit_intro_header),
                color = AppTheme.colors.neutral900,
                style = AppTheme.typography.h5,
                textAlign = TextAlign.Center
            )
        }
        item {
            SpacerSmall()
            Text(
                text = stringResource(R.string.mlkit_intro_info),
                color = AppTheme.colors.neutral900,
                style = AppTheme.typography.subtitle2l,
                textAlign = TextAlign.Center
            )
            SpacerSmall()
        }
    }
}

@Composable
private fun MlKitBottomBar(onAccept: () -> Unit, onClickReadMore: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        PrimaryButton(
            onClick = { onAccept() },
            contentPadding = PaddingValues(horizontal = 76.dp, vertical = 13.dp)
        ) {
            Text(
                text = stringResource(R.string.mlkit_intro_accept)
            )
        }
        SpacerSmall()
        SecondaryButton(
            onClick = { onClickReadMore() },
            contentPadding = PaddingValues(horizontal = PaddingDefaults.Large * 2, vertical = 13.dp)
        ) {
            Text(
                text = stringResource(R.string.mlkit_intro_read_more)
            )
        }
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun MlKitScreenScaffoldPreview() {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        TestScaffold(
            topBarTitle = "",
            navigationMode = NavigationBarMode.Close,
            bottomBar = {
                MlKitBottomBar(
                    onAccept = {},
                    onClickReadMore = { }
                )
            }
        ) {
            MlKitScreenContent(
                contentPadding = it,
                listState = listState
            )
        }
    }
}
