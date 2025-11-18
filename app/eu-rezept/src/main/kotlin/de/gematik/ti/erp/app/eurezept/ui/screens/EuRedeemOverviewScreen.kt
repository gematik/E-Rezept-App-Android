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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.button.SelectionSummaryButton
import de.gematik.ti.erp.app.button.SelectionSummaryButtonData
import de.gematik.ti.erp.app.button.selectionSummaryButtonText
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuRedeemScreenController
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedeemPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedeemPreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.ClickText
import de.gematik.ti.erp.app.utils.ClickableText
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import kotlinx.coroutines.launch

val TopBarColor = Color(0xffd6e9fb)
private const val EMOJI_SCALE_FACTOR = 0.65

internal class EuRedeemOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val scope = uiScope
        val controller = rememberEuRedeemScreenController()

        fun handleEuRedeemAction() {
            controller.handleRedeemAction(
                onStartRedemption = {
                    graphController.generateEuAccessCode(
                        onSuccessfulCodeGeneration = {
                            navController.navigate(EuRoutes.EuRedemptionCodeScreen.route)
                        },
                        onFailure = { error ->
                            snackbarHostState.showWithDismissButton(
                                context = context,
                                messageResId = error.messageResId,
                                scope = scope
                            )
                        }
                    )
                },
                onShowInstructions = {
                    scope.launch {
                        val wasRedeemInstructionNotViewed = controller.wasRedeemInstructionNotViewed()
                        navController.navigate(EuRoutes.EuInstructionsScreen.path(wasRedeemInstructionNotViewed))
                    }
                }
            )
        }

        val selectedPrescriptions by graphController.selectedPrescriptions.collectAsStateWithLifecycle()
        val selectedCountry by graphController.selectedCountry.collectAsStateWithLifecycle()
        val isRedeemEnabled by graphController.isRedeemEnabled.collectAsStateWithLifecycle()
        val isRedemptionInProgress by graphController.isRedemptionInProgress.collectAsStateWithLifecycle()

        val selectedPrescriptionNames = remember(selectedPrescriptions) {
            selectedPrescriptions.map { it.name }
        }

        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }

        ChooseAuthenticationNavigationEventsListener(
            controller = controller,
            navController = navController,
            dialogScaffold = LocalDialog.current
        )
        with(controller) {
            onBiometricAuthenticationSuccessForSubmitEvent.listen {
                handleEuRedeemAction()
            }
        }

        EuRedeemScreenScaffold(
            listState = listState,
            snackbarHostState = snackbarHostState,
            selectedPrescriptions = selectedPrescriptionNames,
            selectedCountry = selectedCountry,
            isRedeemEnabled = isRedeemEnabled,
            isRedemptionInProgress = isRedemptionInProgress,
            onBack = { onBack() },
            onCancel = {
                navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                    popUpTo(EuRoutes.subGraphName()) { inclusive = true }
                }
            },
            onSelectPrescriptions = {
                navController.navigate(EuRoutes.EuPrescriptionSelectionScreen.route)
            },
            onSelectCountry = {
                navController.navigate(EuRoutes.EuCountrySelectionScreen.route)
            },
            onInstructionsClick = {
                scope.launch {
                    val wasRedeemInstructionNotViewed = controller.wasRedeemInstructionNotViewed()
                    navController.navigate(
                        EuRoutes.EuInstructionsScreen.path(wasRedeemInstructionNotViewed)
                    )
                }
            },
            onRedeem = {
                handleEuRedeemAction()
            }
        )
    }
}

@Composable
fun EuRedeemScreenScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    listState: LazyListState,
    selectedPrescriptions: List<String>,
    selectedCountry: Country?,
    isRedeemEnabled: Boolean,
    isRedemptionInProgress: Boolean = false,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSelectPrescriptions: () -> Unit,
    onSelectCountry: () -> Unit,
    onInstructionsClick: () -> Unit,
    onRedeem: () -> Unit
) {
    Box {
        EuRedeemScaffold(
            modifier = modifier,
            listState = listState,
            onBack = onBack,
            onCancel = onCancel,
            topBarTitle = "",
            topBarColor = TopBarColor,
            snackbarHostState = snackbarHostState,
            bottomBar = {
                EuRedeemBottomBar(
                    isEnabled = isRedeemEnabled && !isRedemptionInProgress,
                    isRedeeming = isRedemptionInProgress,
                    onRedeem = onRedeem
                )
            }
        ) { paddingValues ->
            EuRedeemScreenContent(
                listState = listState,
                selectedPrescriptions = selectedPrescriptions,
                selectedCountry = selectedCountry,
                onSelectPrescriptions = onSelectPrescriptions,
                onSelectCountry = onSelectCountry,
                onInstructionsClick = onInstructionsClick,
                modifier = Modifier.padding(paddingValues)
            )
        }

        if (isRedemptionInProgress) {
            LoadingIndicator()
        }
    }
}

@Composable
fun EuRedeemScreenContent(
    listState: LazyListState,
    selectedPrescriptions: List<String>,
    selectedCountry: Country?,
    onSelectPrescriptions: () -> Unit,
    onSelectCountry: () -> Unit,
    onInstructionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(SizeDefaults.zero)
    ) {
        item {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SizeDefaults.twoThirtyFiveDp),
                painter = painterResource(R.drawable.eu_redeem),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.neutral000)
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                SpacerXLarge()

                Text(
                    text = stringResource(R.string.eu_redeem_title),
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.neutral900,
                    textAlign = TextAlign.Start
                )

                SpacerTiny()

                ClickableText(
                    modifier = Modifier,
                    textStyle = AppTheme.typography.body1l,
                    text = stringResource(R.string.eu_redeem_instructions_prefix),
                    clickText = ClickText(
                        text = stringResource(R.string.eu_redeem_instructions_link),
                        onClick = onInstructionsClick
                    )
                )

                SpacerLarge()
                SelectionSummaryButton(
                    data = SelectionSummaryButtonData(
                        buttonTitleText = stringResource(R.string.eu_redeem_prescriptions_title),
                        errorTitleText = stringResource(R.string.eu_redeem_select_prescriptions),
                        errorHintText = stringResource(R.string.eu_redeem_select_prescriptions_error),
                        buttonTexts = selectedPrescriptions.map { selectionSummaryButtonText(it) }
                    ),
                    isError = false, // Always false to ignore red styling and errorHintText
                    onClick = onSelectPrescriptions
                )
                SpacerLarge()
                SelectionSummaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    data = SelectionSummaryButtonData(
                        buttonTitleText = stringResource(R.string.eu_redeem_country_title),
                        errorTitleText = stringResource(R.string.eu_redeem_select_country),
                        errorHintText = stringResource(R.string.eu_redeem_select_country_error),
                        buttonTexts = selectedCountry?.name?.let { listOf(selectionSummaryButtonText(it)) } ?: emptyList()
                    ),
                    isError = false, // Always false to ignore red styling and errorHintText
                    leadingContent = {
                        EmojiFlag(
                            flagEmoji = selectedCountry?.flagEmoji ?: "🇪🇺",
                            size = SizeDefaults.sixfoldAndQuarter
                        )
                    },
                    onClick = onSelectCountry
                )
                SpacerLarge()
            }
        }
        item {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = PaddingDefaults.XXLarge)
            )
        }
    }
}

@Composable
fun EmojiFlag(
    flagEmoji: String,
    size: Dp = SizeDefaults.fourfold,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = AppTheme.colors.neutral000,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flagEmoji,
            fontSize = (size.value * EMOJI_SCALE_FACTOR).sp,
            textAlign = TextAlign.Center,
            lineHeight = (size.value * EMOJI_SCALE_FACTOR).sp,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

@Composable
fun EuRedeemBottomBar(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    isRedeeming: Boolean = false,
    onRedeem: () -> Unit
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

        PrimaryButtonSmall(
            onClick = onRedeem,
            enabled = isEnabled && !isRedeeming,
            modifier = Modifier
                .widthIn(min = SizeDefaults.twentyfivefold)
                .heightIn(min = SizeDefaults.sixfold),
            shape = RoundedCornerShape(SizeDefaults.triple),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.primary700,
                contentColor = AppTheme.colors.neutral000,
                disabledBackgroundColor = AppTheme.colors.neutral300,
                disabledContentColor = AppTheme.colors.neutral600
            )
        ) {
            if (isRedeeming) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(SizeDefaults.double),
                        color = AppTheme.colors.neutral000,
                        strokeWidth = SizeDefaults.quarter
                    )
                    Text(
                        text = stringResource(R.string.eu_redeem_button),
                        style = MaterialTheme.typography.button
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.eu_redeem_button),
                    style = MaterialTheme.typography.button
                )
            }
        }

        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun EuRedeemScreenScaffoldPreview(
    @PreviewParameter(EuRedeemPreviewParameterProvider::class)
    previewData: EuRedeemPreviewData
) {
    val lazyListState = rememberLazyListState()

    PreviewTheme {
        EuRedeemScreenScaffold(
            listState = lazyListState,
            selectedPrescriptions = previewData.selectedPrescriptions,
            selectedCountry = previewData.selectedCountry,
            isRedeemEnabled = previewData.isRedeemEnabled,
            isRedemptionInProgress = false,
            onBack = {},
            onCancel = {},
            onSelectPrescriptions = {},
            onSelectCountry = {},
            onInstructionsClick = {},
            onRedeem = {}
        )
    }
}
