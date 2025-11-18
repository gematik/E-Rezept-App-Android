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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountryPhrases
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes.EU_NAV_REDEEM_BUTTON
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuInstructionController
import de.gematik.ti.erp.app.eurezept.ui.model.InstructionStep
import de.gematik.ti.erp.app.eurezept.ui.preview.EuInstructionsPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuInstructionsPreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton

internal class EuInstructionsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val scope = uiScope
        val showRedeemButton = navBackStackEntry.arguments?.getBoolean(EU_NAV_REDEEM_BUTTON) ?: false
        val selectedCountry by graphController.selectedCountry.collectAsStateWithLifecycle()
        val isRedemptionInProgress by graphController.isRedemptionInProgress.collectAsStateWithLifecycle()
        val controller = rememberEuInstructionController()
        val countryPhrases = controller.getPhrasesForCountry(selectedCountry)
        val isRedeemEnabled by graphController.isRedeemEnabled.collectAsStateWithLifecycle()

        val onBack by rememberUpdatedState { navController.popBackStack() }

        BackHandler { onBack() }

        fun generateEuAccessCode() {
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
        }

        // marks that the instruction screen has been viewed and is opened only if the user clicks on it.
        LaunchedEffect(Unit) {
            controller.markInstructionsViewed()
        }

        ChooseAuthenticationNavigationEventsListener(
            controller = controller,
            navController = navController,
            dialogScaffold = LocalDialog.current
        )

        controller.onBiometricAuthenticationSuccessForSubmitEvent.listen {
            generateEuAccessCode()
        }

        EuInstructionsScaffold(
            listState = listState,
            snackbarHostState = snackbarHostState,
            countryPhrases = countryPhrases,
            showBottomBar = isRedeemEnabled && showRedeemButton,
            isRedemptionInProgress = isRedemptionInProgress,
            onBack = { onBack() },
            onWebsiteClick = {
                controller.openInstructionsWebsite(context)
            },
            onNavigateToCodeScreen = { generateEuAccessCode() }
        )
    }
}

@Composable
fun EuInstructionsScaffold(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    countryPhrases: CountryPhrases,
    showBottomBar: Boolean,
    isRedemptionInProgress: Boolean,
    onBack: () -> Unit,
    onWebsiteClick: () -> Unit,
    onNavigateToCodeScreen: () -> Unit
) {
    EuRedeemScaffold(
        modifier = modifier,
        listState = listState,
        snackbarHostState = snackbarHostState,
        showCloseButton = false,
        onBack = onBack,
        onCancel = onBack,
        topBarTitle = stringResource(R.string.eu_instructions_title),
        bottomBar = {
            if (showBottomBar) {
                InstructionsBottomBar(
                    isRedemptionInProgress = isRedemptionInProgress,
                    onNavigateToCodeScreen = onNavigateToCodeScreen
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            EuInstructionsContent(
                countryPhrases = countryPhrases,
                onWebsiteClick = onWebsiteClick,
                listState = listState
            )
            if (isRedemptionInProgress) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
fun EuInstructionsContent(
    listState: LazyListState,
    countryPhrases: CountryPhrases,
    onWebsiteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val instructionSteps = listOf(
        InstructionStep(
            stepNumber = 1,
            title = stringResource(R.string.eu_instructions_step_1_title),
            flagEmoji = countryPhrases.flagEmoji,
            phraseText = countryPhrases.redeemPrescriptionPhrase
        ),
        InstructionStep(
            stepNumber = 2,
            title = stringResource(R.string.eu_instructions_step_2_title)
        ),
        InstructionStep(
            stepNumber = 3,
            title = stringResource(R.string.eu_instructions_step_3_title)
        ),
        InstructionStep(
            stepNumber = 4,
            title = stringResource(R.string.eu_instructions_step_4_title),
            flagEmoji = countryPhrases.flagEmoji,
            phraseText = countryPhrases.thankYouPhrase
        ),
        InstructionStep(
            stepNumber = 5,
            title = stringResource(R.string.eu_instructions_step_5_title)
        )
    )

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PaddingDefaults.Medium)
    ) {
        item {
            Column {
                SpacerSmall()

                Text(
                    text = stringResource(R.string.eu_instructions_main_title),
                    style = MaterialTheme.typography.h5,
                    color = AppTheme.colors.neutral900,
                    fontWeight = FontWeight.Bold
                )

                SpacerTiny()

                val websiteLinkText = buildAnnotatedString {
                    append(stringResource(R.string.eu_instructions_website_info_start))

                    pushStringAnnotation(tag = "website_link", annotation = "website")
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.colors.primary700,
                            textDecoration = TextDecoration.Underline,
                            fontSize = AppTheme.typography.body2.fontSize
                        )
                    ) {
                        append(stringResource(R.string.eu_instructions_website_link_text))
                    }
                    pop()

                    append(stringResource(R.string.eu_instructions_website_info_end))
                }

                Text(
                    text = websiteLinkText,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = stringResource(R.string.a11y_eu_instructions_open_in_browser)
                        ) { onWebsiteClick() },
                    color = AppTheme.colors.neutral600
                )

                SpacerLarge()
            }
        }

        instructionSteps.forEach { step ->
            item {
                InstructionStepItem(step = step)
                SpacerLarge()
            }
        }

        item {
            Box(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
internal fun InstructionStepItem(
    step: InstructionStep,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SpacerSmall()
        Text(
            text = stringResource(R.string.eu_instructions_step_number, step.stepNumber),
            style = MaterialTheme.typography.subtitle1,
            color = AppTheme.colors.neutral900,
            fontWeight = FontWeight.Bold
        )

        SpacerSmall()

        if (step.flagEmoji != null && step.phraseText != null) {
            val flagStyle = SpanStyle(fontSize = AppTheme.typography.body2.fontSize)
            val flagAccessibilityText = stringResource(R.string.a11y_eu_instructions_flag_accessibility_text)

            val fullText = buildAnnotatedString {
                append("${step.title} ")
                withStyle(flagStyle) { append(step.flagEmoji) }
                append(" \"${step.phraseText}\"")
            }

            Text(
                text = fullText,
                style = MaterialTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                modifier = Modifier.semantics {
                    contentDescription = "${step.title} $flagAccessibilityText \"${step.phraseText}\""
                }
            )
        } else {
            Text(
                text = step.title,
                style = MaterialTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Composable
fun InstructionsBottomBar(
    modifier: Modifier = Modifier,
    isRedemptionInProgress: Boolean,
    onNavigateToCodeScreen: () -> Unit
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

        PrimaryButton(
            onClick = onNavigateToCodeScreen,
            enabled = !isRedemptionInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = SizeDefaults.twentyfivefold)
                .heightIn(min = SizeDefaults.sixfold)
                .padding(horizontal = PaddingDefaults.Medium),
            shape = RoundedCornerShape(SizeDefaults.triple),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.primary700,
                contentColor = AppTheme.colors.neutral000,
                disabledBackgroundColor = AppTheme.colors.neutral300,
                disabledContentColor = AppTheme.colors.neutral600
            )
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(R.string.eu_instructions_generate_code_button),
                style = MaterialTheme.typography.button
            )
        }

        SpacerSmall()

        Text(
            text = stringResource(R.string.eu_instructions_code_validity_info),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.neutral600,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )

        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun EuInstructionsScreenPreview(
    @PreviewParameter(EuInstructionsPreviewParameterProvider::class)
    previewData: EuInstructionsPreviewData
) {
    PreviewTheme {
        EuInstructionsScaffold(
            listState = rememberLazyListState(),
            snackbarHostState = remember { SnackbarHostState() },
            countryPhrases = previewData.countryPhrases,
            showBottomBar = true,
            isRedemptionInProgress = false,
            onBack = {},
            onWebsiteClick = {},
            onNavigateToCodeScreen = {}
        )
    }
}
