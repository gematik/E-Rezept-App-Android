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

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.AltPairingProvider
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberAltPairingProvider
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import kotlinx.coroutines.launch

@Requirement(
    "O.Biom_1",
    "O.Biom_8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Authentication via biometrics is only possible in combination with " +
        "successful authentication via eGK + PIN."
)
class CardWallSaveCredentialsInfoScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val altPairingProvider = rememberAltPairingProvider()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val altPairing by graphController.altPairing.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) {
            (altPairing as? AltPairingProvider.AuthResult.Initialized)?.let {
                altPairingProvider.cleanup(it.aliasOfSecureElementEntry)
                graphController.setAltPairing(null)
            }
        }
        CardWallSaveCredentialsInfoScreenScaffold(
            onBack = {
                navController.popBackStack()
            },
            onAccept = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    scope.launch {
                        when (val r = altPairingProvider.initializeAndPrompt()) {
                            is AltPairingProvider.AuthResult.Initialized -> {
                                graphController.setAltPairing(r)
                                navController.navigate(
                                    CardWallRoutes.CardWallReadCardScreen.path(),
                                    navOptions = navOptions {
                                        popUpTo(CardWallRoutes.CardWallSaveCredentialsScreen.route)
                                    }
                                )
                            }
                            else -> {
                                navController.popBackStack()
                            }
                        }
                    }
                } else {
                    navController.popBackStack()
                }
            },
            listState = listState
        ) { innerPadding ->
            CardWallSaveCredentialsInfoScreenContent(
                listState = listState,
                innerPadding = innerPadding
            )
        }
    }
}

@Composable
private fun CardWallSaveCredentialsInfoScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues
) {
    val contentPadding by remember(innerPadding) {
        derivedStateOf {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
    }
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            Text(
                stringResource(R.string.cdw_info_header),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.cdw_info_first),
                style = AppTheme.typography.body1
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.cdw_info_second),
                style = AppTheme.typography.body1
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.cdw_info_third),
                style = AppTheme.typography.body1
            )
            SpacerXXLarge()
        }
    }
}

@Composable
fun CardWallSaveCredentialsInfoScreenScaffold(
    listState: LazyListState,
    onAccept: () -> Unit,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.systemBarsPadding(),
        topBarTitle = stringResource(R.string.cdw_info_title),
        topBarColor = MaterialTheme.colors.background,
        listState = listState,
        navigationMode = NavigationBarMode.Close,
        bottomBar = {
            CardWallSaveCredentialsInfoScreenBottomBar(
                onNext = onAccept
            )
        },
        onBack = onBack,
        content = content
    )
}

@Composable
private fun CardWallSaveCredentialsInfoScreenBottomBar(
    onNext: () -> Unit
) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = SizeDefaults.half
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.CardWall.SecurityAcceptance.AcceptButton)
                    .padding(
                        horizontal = SizeDefaults.ninefold,
                        vertical = PaddingDefaults.ShortMedium
                    )
            ) {
                Text(
                    stringResource(R.string.cdw_info_accept)
                )
            }
        }
    }
}
