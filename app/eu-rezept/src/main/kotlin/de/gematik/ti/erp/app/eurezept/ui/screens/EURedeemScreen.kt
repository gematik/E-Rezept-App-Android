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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.button.SelectionSummaryButton
import de.gematik.ti.erp.app.button.SelectionSummaryButtonData
import de.gematik.ti.erp.app.button.selectionSummaryButtonText
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domin.model.Country
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedeemPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuRedeemPreviewParameterProvider
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
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import kotlinx.coroutines.launch

internal class EuRedeemScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()
        val selectedPrescriptions by graphController.selectedPrescriptions.collectAsStateWithLifecycle()
        val selectedCountry by graphController.selectedCountry.collectAsStateWithLifecycle()
        val isRedeemEnabled by graphController.isRedeemEnabled.collectAsStateWithLifecycle()

        val selectedPrescriptionNames = remember(selectedPrescriptions) {
            selectedPrescriptions.map { it.name }
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        EuRedeemScreenScaffold(
            listState = lazyListState,
            selectedPrescriptions = selectedPrescriptionNames,
            selectedCountry = selectedCountry,
            snackbarHostState = snackbarHostState,
            isRedeemEnabled = isRedeemEnabled,
            onBack = { navController.popBackStack() },
            onCancel = {
                navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route) {
                    popUpTo(EuRoutes.EuConsentScreen.route) { inclusive = true }
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
                    snackbarHostState.showSnackbar(
                        message = "Eu Instructions Screen coming soon!",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            },
            onRedeem = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Eu Instructions Screen coming soon!",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            }
        )
    }
}

@Composable
fun EuRedeemScreenScaffold(
    listState: LazyListState,
    selectedPrescriptions: List<String>,
    selectedCountry: Country?,
    isRedeemEnabled: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSelectPrescriptions: () -> Unit,
    onSelectCountry: () -> Unit,
    onInstructionsClick: () -> Unit,
    onRedeem: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    modifier: Modifier = Modifier
) {
    EuRedeemScaffold(
        modifier = modifier,
        listState = listState,
        onBack = onBack,
        onCancel = onCancel,
        topBarTitle = "",
        topBarColor = Color(0xffd6e9fb),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            EuRedeemBottomBar(
                isEnabled = isRedeemEnabled,
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
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.neutral000),

        contentPadding = PaddingValues(
            SizeDefaults.zero
        ),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.zero)
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.eu_redeem),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp),
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

                val instructionsText = buildAnnotatedString {
                    append(stringResource(R.string.eu_redeem_instructions_prefix) + " ")

                    pushStringAnnotation(tag = "instructions_link", annotation = "instructions")
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.colors.primary700,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(stringResource(R.string.eu_redeem_instructions_link))
                    }
                    pop()

                    append(".")
                }

                Text(
                    text = instructionsText,
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onInstructionsClick()
                        },
                    color = AppTheme.colors.neutral600
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
            fontSize = (size.value * 0.65).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EuRedeemBottomBar(
    isEnabled: Boolean,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
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
            enabled = isEnabled,
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
            Text(
                text = stringResource(R.string.eu_redeem_button), // "Einlösen"
                style = MaterialTheme.typography.button
            )
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
            onBack = {},
            onCancel = {},
            onSelectPrescriptions = {},
            onSelectCountry = {},
            onInstructionsClick = {},
            onRedeem = {}
        )
    }
}
