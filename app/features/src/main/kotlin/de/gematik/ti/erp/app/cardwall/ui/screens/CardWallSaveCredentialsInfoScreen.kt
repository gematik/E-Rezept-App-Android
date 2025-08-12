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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.presentation.deviceStrongBoxStatus
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.presentation.SaveCredentialsController
import de.gematik.ti.erp.app.cardwall.presentation.rememberSaveCredentialsScreenController
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallSaveCredentialsInfoPreviewParameterProvider
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallSaveCredentialsInfoScreenPreviewData
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import kotlinx.coroutines.launch

@Requirement(
    "O.Resi_1#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "User information and acceptance/deny for saving credentials"
)
class CardWallSaveCredentialsInfoScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val saveCredentialsController = rememberSaveCredentialsScreenController()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val saveCredentials by sharedViewModel.saveCredentials.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val activity = LocalActivity.current
        val biometricManager = BiometricManager.from(activity)
        val useStrongBox by remember { mutableStateOf(context.deviceStrongBoxStatus()) }
        val biometricStrong = remember {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
        }
        LaunchedEffect(Unit) {
            (saveCredentials as? SaveCredentialsController.AuthResult.Initialized)?.let {
                with(saveCredentialsController) {
                    deleteKey(
                        aliasToByteArray(it.aliasOfSecureElementEntry)
                    )
                }
                sharedViewModel.setSaveCredentials(null)
            }
        }
        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }
        CardWallSaveCredentialsInfoScreenScaffold(
            onBack = { onBack() },
            onAccept = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    scope.launch {
                        when (val r = saveCredentialsController.initializeAndPrompt(useStrongBox = useStrongBox)) {
                            is SaveCredentialsController.AuthResult.Initialized -> {
                                sharedViewModel.setSaveCredentials(r)
                                navController.navigate(
                                    CardWallRoutes.CardWallReadCardScreen.path(),
                                    navOptions = navOptions {
                                        popUpTo(CardWallRoutes.CardWallSaveCredentialsScreen.route)
                                    }
                                )
                            }

                            else -> {
                                onBack()
                            }
                        }
                    }
                } else {
                    onBack()
                }
            },
            listState = listState
        ) { innerPadding ->
            CardWallSaveCredentialsInfoScreenContent(
                listState = listState,
                biometricStrong = biometricStrong,
                innerPadding = innerPadding
            )
        }
    }
}

@Composable
private fun CardWallSaveCredentialsInfoScreenContent(
    listState: LazyListState,
    biometricStrong: Boolean,
    innerPadding: PaddingValues
) {
    val contentPadding by rememberContentPadding(innerPadding)

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
                if (biometricStrong) {
                    stringResource(R.string.cdw_info_first)
                } else {
                    stringResource(R.string.cdw_info_no_strong_biometry_first)
                },
                style = AppTheme.typography.body1
            )
            SpacerSmall()
        }
        item {
            Text(
                if (biometricStrong) {
                    stringResource(R.string.cdw_info_second)
                } else {
                    stringResource(R.string.cdw_info_no_strong_biometry_second)
                },
                style = AppTheme.typography.body1
            )
            SpacerSmall()
        }
        item {
            Text(
                if (biometricStrong) {
                    stringResource(R.string.cdw_info_third)
                } else {
                    stringResource(R.string.cdw_info_no_strong_biometry_third)
                },
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
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
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

@LightDarkPreview
@Composable
fun CardWallSaveCredentialsInfoScreenPreview(
    @PreviewParameter(CardWallSaveCredentialsInfoPreviewParameterProvider::class) previewData:
        CardWallSaveCredentialsInfoScreenPreviewData
) {
    PreviewAppTheme {
        CardWallSaveCredentialsInfoScreenScaffold(
            listState = rememberLazyListState(),
            onAccept = {},
            onBack = {},
            content = { paddingValues ->
                CardWallSaveCredentialsInfoScreenContent(
                    listState = rememberLazyListState(),
                    biometricStrong = previewData.isBiometricStrong,
                    innerPadding = paddingValues
                )
            }
        )
    }
}
