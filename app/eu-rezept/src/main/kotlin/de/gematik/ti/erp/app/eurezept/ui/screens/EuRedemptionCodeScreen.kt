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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuRedemptionCodeController
import de.gematik.ti.erp.app.eurezept.ui.component.TogglableRedemptionCodeCard
import de.gematik.ti.erp.app.eurezept.ui.model.NavigationActions
import de.gematik.ti.erp.app.eurezept.ui.model.RedemptionCodeActions
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedemptionCodePreviewParameter
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedemptionCodePreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.loading.LoadingIndicator
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
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.PrimaryIconButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.uistate.UiState

internal class EuRedemptionCodeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val scope = uiScope
        val selectedCountry by graphController.selectedCountry.collectAsStateWithLifecycle()

        val controller = rememberEuRedemptionCodeController(selectedCountryCode = selectedCountry?.code ?: "")
        val uiState by graphController.euRedemptionCode.collectAsStateWithLifecycle()
        val isRedemptionInProgress by graphController.isRedemptionInProgress.collectAsStateWithLifecycle()
        val isQrCodeVisible by controller.isQrCodeVisible.collectAsStateWithLifecycle()
        val countrySpecificLabels by controller.countrySpecificLabels.collectAsStateWithLifecycle()

        fun generateEuAccessCode() {
            graphController.generateEuAccessCode(
                onFailure = { error ->
                    snackbarHostState.showWithDismissButton(
                        context = context,
                        messageResId = error.messageResId,
                        scope = scope
                    )
                }
            )
        }

        controller.onBiometricAuthenticationSuccessForSubmitEvent.listen { generateEuAccessCode() }

        val onBack by rememberUpdatedState {
            graphController.setEuRedemptionCode(uiState.data)
            navController.popBackStack()
        }
        BackHandler { onBack() }

        ChooseAuthenticationNavigationEventsListener(
            controller = controller,
            navController = navController,
            dialogScaffold = LocalDialog.current
        )

        val navigationActions = NavigationActions(
            onBack = { onBack() },
            onCancel = {
                navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                    popUpTo(EuRoutes.subGraphName()) { inclusive = true }
                }
            },
            navigateToPhotoReceipt = {
                navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                    popUpTo(EuRoutes.subGraphName()) { inclusive = true }
                }
            },
            onRetry = { generateEuAccessCode() }
        )

        val redemptionActions = RedemptionCodeActions(
            onPlayInsuranceAudio = {
                controller.onPlayInsuranceNumberAudio(uiState.data)
            },
            onPlayCodeAudio = {
                controller.onPlayCodeAudio(uiState.data)
            },
            onRenewCode = {
                generateEuAccessCode()
            },
            onToggleQrCode = controller::toggleQrCodeView
        )

        Box {
            EuRedemptionCodeScreenScaffold(
                listState = listState,
                snackbarHostState = snackbarHostState,
                uiState = uiState,
                isQrCodeVisible = isQrCodeVisible,
                countrySpecificLabels = countrySpecificLabels,
                navigationActions = navigationActions,
                actions = redemptionActions,
                isRedemptionInProgress = isRedemptionInProgress
            )
            if (isRedemptionInProgress) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
private fun EuRedemptionCodeScreenScaffold(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    isRedemptionInProgress: Boolean,
    uiState: UiState<EuRedemptionDetails>,
    isQrCodeVisible: Boolean,
    countrySpecificLabels: CountrySpecificLabels,
    navigationActions: NavigationActions,
    actions: RedemptionCodeActions
) {
    EuRedeemScaffold(
        modifier = modifier,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onBack = navigationActions.onBack,
        onCancel = navigationActions.onCancel,
        topBarTitle = stringResource(R.string.eu_redemption_title),
        bottomBar = {
            RedemptionBottomBar(
                uiState = uiState,
                isRedemptionInProgress = isRedemptionInProgress,
                onRetry = navigationActions.onRetry,
                onTakeReceiptPhoto = navigationActions.navigateToPhotoReceipt
            )
        }
    ) { paddingValues ->
        EuRedemptionCodeScreenContent(
            listState = listState,
            paddingValues = paddingValues,
            uiState = uiState,
            isQrCodeVisible = isQrCodeVisible,
            countrySpecificLabels = countrySpecificLabels,
            actions = actions
        )
    }
}

@Composable
private fun EuRedemptionCodeScreenContent(
    listState: LazyListState,
    paddingValues: PaddingValues,
    uiState: UiState<EuRedemptionDetails>,
    isQrCodeVisible: Boolean,
    countrySpecificLabels: CountrySpecificLabels,
    actions: RedemptionCodeActions
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium)
    ) {
        item {
            Column {
                SpacerMedium()

                Text(
                    text = stringResource(R.string.eu_redemption_step_2),
                    style = AppTheme.typography.subtitle1,
                    color = AppTheme.colors.neutral900,
                    fontWeight = FontWeight.Bold
                )

                SpacerSmall()

                Text(
                    text = stringResource(R.string.eu_redemption_instruction),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral600
                )

                SpacerXXLarge()
            }
        }
        item {
            UiStateMachine(
                state = uiState,
                onLoading = {
                    LoadingCard()
                },
                onError = {
                    ErrorCard()
                },
                onContent = {
                    TogglableRedemptionCodeCard(
                        redemptionData = it,
                        isQrCodeVisible = isQrCodeVisible,
                        countrySpecificLabels = countrySpecificLabels,
                        actions = actions
                    )
                }
            )
        }
    }
}

@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SizeDefaults.oneHalf),
        elevation = SizeDefaults.threeSeventyFifth
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.XXLargePlus),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SpacerMedium()
            Box(
                modifier = Modifier.size(SizeDefaults.twentyfivefold),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(SizeDefaults.sixfold),
                        color = AppTheme.colors.primary700,
                        strokeWidth = SizeDefaults.half
                    )

                    SpacerLarge()

                    Text(
                        text = stringResource(R.string.eu_redemption_generating_code),
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral700,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SpacerMedium()
        }
    }
}

@Composable
private fun ErrorCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SizeDefaults.oneHalf),
        elevation = SizeDefaults.threeSeventyFifth,
        backgroundColor = AppTheme.colors.neutral050
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.XXLargePlus),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SpacerMedium()

            Box(
                modifier = Modifier.size(SizeDefaults.twentyfivefold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.eu_redemption_error_message),
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.neutral700,
                    textAlign = TextAlign.Center
                )
            }

            SpacerMedium()
        }
    }
}

@Composable
private fun RedemptionBottomBar(
    modifier: Modifier = Modifier,
    uiState: UiState<EuRedemptionDetails>,
    isRedemptionInProgress: Boolean,
    onRetry: () -> Unit,
    onTakeReceiptPhoto: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .padding(horizontal = PaddingDefaults.Medium)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerMedium()

        UiStateMachine(
            state = uiState,
            onLoading = {
                PrimaryButton(
                    onClick = { },
                    enabled = !isRedemptionInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = SizeDefaults.twentyfivefold)
                        .heightIn(min = SizeDefaults.sixfold)
                        .padding(horizontal = PaddingDefaults.Medium)
                ) {
                    Text(stringResource(R.string.eu_redemption_navigate_to_home_screen))
                }
            },
            onError = {
                PrimaryIconButton(
                    onClick = onRetry,
                    imageVector = Icons.Default.Refresh,
                    text = stringResource(R.string.eu_redemption_retry_button),
                    enabled = !isRedemptionInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = SizeDefaults.twentyfivefold)
                        .heightIn(min = SizeDefaults.sixfold)
                        .padding(horizontal = PaddingDefaults.Medium)
                )
            },
            onContent = {
                PrimaryButton(
                    onClick = onTakeReceiptPhoto,
                    enabled = !isRedemptionInProgress || !it.isExpired,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = SizeDefaults.twentyfivefold)
                        .heightIn(min = SizeDefaults.sixfold)
                        .padding(horizontal = PaddingDefaults.Medium)
                ) {
                    Text(stringResource(R.string.eu_redemption_navigate_to_home_screen))
                }
            }
        )
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun EuRedemptionCodeScreenScaffoldPreview(
    @PreviewParameter(EuRedemptionCodePreviewParameterProvider::class) previewData: EuRedemptionCodePreviewParameter
) {
    PreviewTheme {
        val navigationActions = NavigationActions(
            onBack = {},
            onCancel = {},
            onRetry = {},
            navigateToPhotoReceipt = {}
        )

        val actions = RedemptionCodeActions(
            onRenewCode = {},
            onPlayInsuranceAudio = {},
            onPlayCodeAudio = {},
            onToggleQrCode = {}
        )

        EuRedemptionCodeScreenScaffold(
            listState = rememberLazyListState(),
            snackbarHostState = remember { SnackbarHostState() },
            uiState = previewData.uiState,
            isQrCodeVisible = previewData.isQrCodeVisible,
            countrySpecificLabels = CountrySpecificLabels(
                codeLabel = stringResource(R.string.eu_redemption_code_label),
                insuranceNumberLabel = stringResource(R.string.eu_redemption_insurance_number_label)
            ),
            navigationActions = navigationActions,
            isRedemptionInProgress = false,
            actions = actions
        )
    }
}
