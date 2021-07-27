/*
 * Copyright (c) 2021 gematik GmbH
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

import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.Tag
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.VerbatimTtsAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.model.CardWall
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallNavigation
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalFragmentNavController
import de.gematik.ti.erp.app.demo.ui.DemoBanner
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintLargeImage
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer40
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.SpacerMaxWidth
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

private val framePadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CardWallScreen(viewModel: CardWallViewModel = viewModel()) {
    val navController = rememberNavController()
    val frNavController = LocalFragmentNavController.current

    val state by viewModel.state().collectAsState(viewModel.defaultState)

    val startDestination = when {
        !state.hardwareRequirementsFulfilled -> CardWallNavigation.IntroMissingCapabilities.route
        state.isIntroSeenByUser && state.isCardAccessNumberValid -> CardWallNavigation.PersonalIdentificationNumber.route
        state.isIntroSeenByUser -> CardWallNavigation.CardAccessNumber.route
        else -> CardWallNavigation.Intro.route
    }

    val context = LocalContext.current
    val biometricMode = remember {
        val biometricManager = BiometricManager.from(context)
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    }

    val navigationMode by navController.navigationModeState(startDestination = startDestination)

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(CardWallNavigation.Intro.route) {
            Box(
                modifier = Modifier
                    .background(AppTheme.colors.primary100)
                    .fillMaxSize()
            ) {
                NavigationAnimation(navigationMode) {
                    CardWallIntro(
                        cardHelper = { navController.navigate(CardWallNavigation.HealthCardHelper.route) },
                        state.demoMode
                    ) {
                        navController.navigate(CardWallNavigation.CardAccessNumber.route)
                    }
                }
            }
        }
        composable(CardWallNavigation.IntroMissingCapabilities.route) {
            NavigationAnimation(navigationMode) {
                CardWallMissingCapabilities()
            }
        }
        composable(CardWallNavigation.CardAccessNumber.route) {
            viewModel.onIntroSeenByUser()

            NavigationAnimation(navigationMode) {
                CardAccessNumber(
                    cardHelper = { navController.navigate(CardWallNavigation.HealthCardHelper.route) },
                    navigationMode,
                    state.cardAccessNumber,
                    onCanChange = { viewModel.onCardAccessNumberChange(it) },
                    demoMode = state.demoMode
                ) {
                    navController.navigate(CardWallNavigation.PersonalIdentificationNumber.route)
                }
            }
        }
        composable(CardWallNavigation.PersonalIdentificationNumber.route) {
            NavigationAnimation(navigationMode) {
                PersonalIdentificationNumber(
                    navigationMode,
                    state.personalIdentificationNumber,
                    demoMode = state.demoMode,
                    onPinChange = { viewModel.onPersonalIdentificationChange(it) }
                ) {
                    val deviceSupportsBiometric = when (biometricMode) {
                        BiometricManager.BIOMETRIC_SUCCESS,
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                            true
                        else ->
                            false
                    }
                    val deviceSupportsStrongbox = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
                    } else {
                        false
                    }
                    if (deviceSupportsBiometric &&
                        deviceSupportsStrongbox &&
                        state.selectedAuthenticationMethod == CardWall.AuthenticationMethod.None
                    ) {
                        navController.navigate(CardWallNavigation.AuthenticationSelection.route)
                    } else {
                        viewModel.onSelectAuthenticationMethod(CardWall.AuthenticationMethod.HealthCard)
                        navController.navigate(CardWallNavigation.Authentication.route)
                    }
                }
            }
        }

        composable(CardWallNavigation.AuthenticationSelection.route) {
            NavigationAnimation(navigationMode) {
                AuthenticationSelection(
                    state.demoMode,
                    biometricMode = biometricMode,
                    selectedAuthMode = state.selectedAuthenticationMethod,
                    onSelectAuthMode = { viewModel.onSelectAuthenticationMethod(it) }
                ) {
                    navController.navigate(
                        CardWallNavigation.Authentication.route
                    )
                }
            }
        }

        composable(CardWallNavigation.Authentication.route) {
            NavigationAnimation(navigationMode) {
                Authentication(
                    viewModel, state.demoMode,
                    authenticationMethod = state.selectedAuthenticationMethod,
                    cardAccessNumber = state.cardAccessNumber,
                    personalIdentificationNumber = state.personalIdentificationNumber,
                    onNext = {
                        navController.navigate(CardWallNavigation.Happy.route)
                    },
                    onRetryCan = {
                        navController.navigate(CardWallNavigation.CardAccessNumber.route) {
                            popUpTo(CardWallNavigation.CardAccessNumber.route) { inclusive = true }
                        }
                    },
                    onRetryPin = {
                        navController.navigate(CardWallNavigation.PersonalIdentificationNumber.route) {
                            popUpTo(CardWallNavigation.PersonalIdentificationNumber.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
        composable(CardWallNavigation.Happy.route) {
            NavigationAnimation(navigationMode) {
                Outro(demoMode = state.demoMode) {
                    frNavController.popBackStack()
                }
            }
        }
        composable(CardWallNavigation.HealthCardHelper.route) {
            NavigationAnimation(NavigationMode.Open) {
                HealthCardHelperScreen(viewModel, state.demoMode)
            }
        }
    }
}

@Composable
private fun CardAccessNumber(
    cardHelper: () -> Unit,
    navMode: NavigationMode,
    can: String,
    demoMode: Boolean,
    onCanChange: (String) -> Unit,
    next: (String) -> Unit
) {

    CardWallScaffold(
        backMode = when (navMode) {
            NavigationMode.Forward,
            NavigationMode.Back -> NavigationBarMode.Back
            NavigationMode.Open -> NavigationBarMode.Close
        },
        title = stringResource(R.string.cdw_top_bar_title),
        nextEnabled = can.length == 6,
        onNext = { next(can) },
        demoMode = demoMode
    ) {
        Text(
            stringResource(R.string.cdw_can_headline),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(PaddingDefaults.Medium)
        )

        Image(
            painterResource(R.drawable.card_wall_card_can),
            null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        Column {
            val textValue = TextFieldValue(
                annotatedString = buildAnnotatedString {
                    pushTtsAnnotation(VerbatimTtsAnnotation(can))
                    append(can)
                    pop()
                },
                selection = TextRange(can.length)
            )

            val interactionSource = MutableInteractionSource()
            var isFocussed by remember { mutableStateOf(false) }
            val canRegex = """^\d{0,6}$""".toRegex()

            BasicTextField(
                value = textValue,
                onValueChange = {
                    if (it.text.matches(canRegex)) {
                        onCanChange(it.text)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (can.length == 6) {
                            next(can)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, bottom = 8.dp, end = 24.dp)
                    .focusable(true, interactionSource)
                    .onFocusEvent {
                        isFocussed = it.isFocused
                    }
                    .testId("cdw_edt_can_input")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        val shape = RoundedCornerShape(8.dp)
                        val backgroundColor = AppTheme.colors.neutral200
                        val borderModifier = Modifier.border(
                            BorderStroke(1.dp, color = AppTheme.colors.primary700),
                            shape
                        )

                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp, 48.dp)
                                    .shadow(1.dp, shape)
                                    .then(if (can.length == it && isFocussed) borderModifier else Modifier)
                                    .background(
                                        color = backgroundColor, shape,
                                    )
                                    .graphicsLayer {
                                        clip = false
                                    }
                            ) {
                                Text(
                                    text = can.getOrNull(it)?.toString() ?: " ",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clearAndSetSemantics { }
                                )
                            }
                        }
                    }
                }
            }

            Text(
                stringResource(R.string.cdw_can_caption),
                style = AppTheme.typography.captionl,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            Spacer40()
            if (demoMode) {
                DemoInputHint(
                    stringResource(R.string.cdw_can_demo_info),
                    Modifier
                        .padding(horizontal = PaddingDefaults.Medium)
                        .fillMaxWidth()
                )
            } else {
                HintCard(
                    image = {
                        HintLargeImage(
                            painterResource(R.drawable.pharmacist_2),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.cdw_can_info_hint_header)) },
                    body = { Text(stringResource(R.string.cdw_can_info_hint_info)) },
                    action = {
                        HealthCardHelperButton(cardHelper)
                    },
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                )
            }
            SpacerMedium()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PersonalIdentificationNumber(
    navMode: NavigationMode,
    pin: String,
    demoMode: Boolean,
    onPinChange: (String) -> Unit,
    next: (String) -> Unit
) {

    CardWallScaffold(
        backMode = when (navMode) {
            NavigationMode.Forward,
            NavigationMode.Back -> NavigationBarMode.Back
            NavigationMode.Open -> NavigationBarMode.Close
        },
        title = stringResource(R.string.cdw_top_bar_title),
        nextEnabled = pin.length in 6..8,
        onNext = { next(pin) },
        demoMode = demoMode
    ) {

        Text(
            stringResource(R.string.cdw_pin_title),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(PaddingDefaults.Medium)
        )

        Column {
            val textValue = TextFieldValue(
                annotatedString = buildAnnotatedString {
                    pushTtsAnnotation(VerbatimTtsAnnotation(pin))
                    append(pin)
                    pop()
                },
                selection = TextRange(pin.length)
            )

            val interactionSource = MutableInteractionSource()
            var isFocussed by remember { mutableStateOf(false) }
            val pinRegex = """^\d{0,8}$""".toRegex()

            BasicTextField(
                value = textValue,
                onValueChange = {
                    if (it.text.matches(pinRegex)) {
                        onPinChange(it.text)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (pin.length in 6..8) {
                            next(pin)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Small,
                        end = PaddingDefaults.Medium
                    )
                    .focusable(true, interactionSource)
                    .onFocusEvent {
                        isFocussed = it.isFocused
                    }
                    .testId("cdw_edt_pin_input")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val shape = RoundedCornerShape(8.dp)
                    val borderModifier = Modifier.border(
                        BorderStroke(1.dp, color = AppTheme.colors.primary700),
                        shape
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .shadow(1.dp, shape)
                            .then(if (isFocussed && pin.length < 8) borderModifier else Modifier)
                            .background(
                                color = AppTheme.colors.neutral200,
                                shape = shape
                            )
                    ) {
                        var pinVisible by remember { mutableStateOf(false) }
                        val transformedPin = if (pinVisible) {
                            pin
                        } else {
                            pin.asIterable().joinToString(separator = "") { "\u2B24" }
                        }

                        Spacer(modifier = Modifier.size(48.dp))

                        Text(
                            text = transformedPin,
                            style = MaterialTheme.typography.h6.copy(letterSpacing = 0.7.em),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .clearAndSetSemantics { }
                        )

                        IconToggleButton(
                            checked = pinVisible,
                            onCheckedChange = { pinVisible = it },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            when (pinVisible) {
                                true -> Icon(
                                    Icons.Rounded.Visibility,
                                    null,
                                    tint = AppTheme.colors.neutral600
                                )
                                false -> Icon(
                                    Icons.Rounded.VisibilityOff,
                                    null,
                                    tint = AppTheme.colors.neutral600
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.cdw_pin_caption),
                        style = AppTheme.typography.captionl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer40()
            if (demoMode) {
                DemoInputHint(
                    stringResource(R.string.cdw_pin_demo_info),
                    Modifier
                        .padding(horizontal = PaddingDefaults.Medium)
                        .fillMaxWidth()
                )
            } else {
                HintCard(
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.pharmacist_circle),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.cdw_pin_info_hint_header)) },
                    body = { Text(stringResource(R.string.cdw_pin_info_hint_info)) },
                    action = {
                        HintTextLearnMoreButton()
                    },
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                )
            }
            SpacerMedium()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticationSelection(
    demoMode: Boolean,
    biometricMode: Int,
    selectedAuthMode: CardWall.AuthenticationMethod,
    onSelectAuthMode: (CardWall.AuthenticationMethod) -> Unit,
    next: () -> Unit
) {
    CardWallScaffold(
        title = stringResource(R.string.cdw_top_bar_title),
        nextEnabled = selectedAuthMode != CardWall.AuthenticationMethod.None,
        onNext = {
            next()
        },
        demoMode = demoMode
    ) {

        Text(
            stringResource(R.string.cdw_save_access_title),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp)
        )

        val biometricText = when (biometricMode) {
            BiometricManager.BIOMETRIC_SUCCESS -> Pair(
                stringResource(R.string.cdw_save_with_biometry_title),
                stringResource(R.string.cdw_save_with_biometry_info)
            )
            else -> Pair(
                stringResource(R.string.cdw_biometric_not_possible_title),
                stringResource(R.string.cdw_biometric_not_possible_info)
            )
        }
        if (biometricMode == BiometricManager.BIOMETRIC_SUCCESS) {
            SelectableCard(
                modifier = Modifier.testId("cdw_btn_option_alternative"),
                enabled = true,
                selected = selectedAuthMode == CardWall.AuthenticationMethod.Alternative,
                Icons.Rounded.Fingerprint,
                biometricText.first,
                biometricText.second
            ) {
                onSelectAuthMode(CardWall.AuthenticationMethod.Alternative)
            }
        } else {
            BiometricInfoCard(biometricText.first, biometricText.second)
        }

        SelectableCard(
            modifier = Modifier.testId("cdw_btn_option_healthcard"),
            enabled = true,
            selected = selectedAuthMode == CardWall.AuthenticationMethod.HealthCard,
            Icons.Rounded.Lock,
            stringResource(R.string.cdw_not_save_with_biometry_title),
            stringResource(R.string.cdw_not_save_with_biometry_info)
        ) {
            onSelectAuthMode(CardWall.AuthenticationMethod.HealthCard)
        }
        Spacer8()
    }
}

@Composable
private fun SelectableCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    image: ImageVector,
    header: String,
    info: String,
    onCardSelected: () -> Unit = {},
) {

    val checkIcon = if (selected) {
        Icons.Rounded.CheckCircle
    } else {
        Icons.Rounded.RadioButtonUnchecked
    }

    val checkIconTint = if (selected) {
        AppTheme.colors.primary600
    } else {
        AppTheme.colors.neutral400
    }

    val elevation = if (selected) {
        8.dp
    } else {
        2.dp
    }

    var cardBackGroundColor = AppTheme.colors.neutral000
    var iconBackGroundColor = AppTheme.colors.primary100
    var iconColor = AppTheme.colors.primary600
    var textcolor = MaterialTheme.colors.onBackground

    if (!enabled) {
        cardBackGroundColor = AppTheme.colors.neutral050
        iconBackGroundColor = AppTheme.colors.neutral200
        iconColor = AppTheme.colors.neutral400
        textcolor = AppTheme.colors.neutral600
    }

    Card(
        border = BorderStroke(0.5.dp, AppTheme.colors.neutral300),
        backgroundColor = cardBackGroundColor,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = elevation
    ) {
        Column(
            modifier = modifier
                .clickable(
                    enabled = enabled,
                    onClick = onCardSelected
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(Modifier.weight(1f)) {
                    SpacerMaxWidth()
                }
                Box(
                    Modifier
                        .weight(1f)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(32.dp),
                        color = iconBackGroundColor
                    ) {
                        Icon(
                            image,
                            null,
                            tint = iconColor,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    Icon(
                        checkIcon,
                        null,
                        tint = checkIconTint,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(header, style = MaterialTheme.typography.subtitle1, color = textcolor)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    info,
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center,
                    color = textcolor
                )
            }
        }
    }
}

@Composable
private fun BiometricInfoCard(
    header: String,
    info: String,
) {
    Card(
        border = BorderStroke(0.5.dp, AppTheme.colors.neutral300),
        backgroundColor = AppTheme.colors.neutral050,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(32.dp),
                        color = AppTheme.colors.neutral200
                    ) {
                        Icon(
                            Icons.Rounded.Fingerprint,
                            null,
                            tint = AppTheme.colors.neutral400,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Text(
                header,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.neutral600
            )
            Spacer4()
            Text(
                info,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Composable
private fun DemoInputHint(text: String, modifier: Modifier) {
    HintCard(
        modifier = modifier,
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.primary100,
            contentColor = AppTheme.colors.primary900,
            border = BorderStroke(0.5.dp, AppTheme.colors.primary300)
        ),
        image = {
            Icon(
                Icons.Rounded.ModelTraining, null,
                modifier = Modifier
                    .padding(it)
                    .requiredSize(40.dp)
            )
        },
        title = null,
        body = { Text(text) }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class, ExperimentalCoroutinesApi::class)
@Composable
private fun Authentication(
    viewModel: CardWallViewModel,
    demoMode: Boolean,
    authenticationMethod: CardWall.AuthenticationMethod,
    cardAccessNumber: String,
    personalIdentificationNumber: String,
    onNext: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
) {
    val activity = LocalActivity.current as MainActivity

    var showEnableNfcDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val toggleAuth = remember { MutableSharedFlow<Boolean>() }
    val nfcTagFlow = remember { MutableSharedFlow<Tag>(replay = 1) }
    val state by produceState(initialValue = AuthenticationState.None) {
        toggleAuth.transformLatest {
            if (it) {
                emitAll(
                    viewModel.doAuthentication(
                        can = cardAccessNumber,
                        pin = personalIdentificationNumber,
                        method = authenticationMethod,
                        nfcTagFlow.onCompletion { cause ->
                            if (cause !is CancellationException) {
                                nfcTagFlow.resetReplayCache()
                            }
                        }
                    )
                )
            } else {
                nfcTagFlow.resetReplayCache()
                value = AuthenticationState.None
            }
        }.catch {
            Timber.e(it)
            // if this happens we can't recover from here
            emit(AuthenticationState.HealthCardCommunicationInterrupted)
            delay(1000)
            cancel()
        }.onCompletion { cause ->
            if (cause is CancellationException) {
                value = AuthenticationState.None
            }
        }.collect {
            value = it
        }
    }

    LaunchedEffect(Unit) {
        activity.nfcTagFlow.retry().collect {
            toggleAuth.emit(true)
            nfcTagFlow.emit(it)
        }
    }

    val cardCommunicationBottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            when (it) {
                ModalBottomSheetValue.Hidden -> {
                    when (state) {
                        AuthenticationState.AuthenticationFlowInitialized,
                        AuthenticationState.HealthCardCommunicationChannelReady,
                        AuthenticationState.HealthCardCommunicationTrustedChannelEstablished,
                        AuthenticationState.HealthCardCommunicationCertificateLoaded,
                        AuthenticationState.HealthCardCommunicationFinished,
                        AuthenticationState.IDPCommunicationFinished -> false
                        else -> true
                    }
                }
                ModalBottomSheetValue.Expanded -> false
                else -> true
            }
        }
    )

    // prevent the bottom sheet from 'disappearing' while clicking outside
    LaunchedEffect(cardCommunicationBottomSheetState.targetValue) {
        when (state) {
            AuthenticationState.AuthenticationFlowInitialized,
            AuthenticationState.HealthCardCommunicationChannelReady,
            AuthenticationState.HealthCardCommunicationTrustedChannelEstablished,
            AuthenticationState.HealthCardCommunicationCertificateLoaded,
            AuthenticationState.HealthCardCommunicationFinished,
            AuthenticationState.IDPCommunicationFinished,
            AuthenticationState.HealthCardCommunicationInterrupted -> if (cardCommunicationBottomSheetState.targetValue == ModalBottomSheetValue.Hidden) {
                cardCommunicationBottomSheetState.snapTo(ModalBottomSheetValue.HalfExpanded)
            }
            else -> {
                /* noop */
            }
        }
    }

    LaunchedEffect(state) {
        when {
            state.isInProgress() -> if (!cardCommunicationBottomSheetState.isVisible) {
                // TODO remove try catch if https://issuetracker.google.com/issues/181593642 is fixed
                try {
                    cardCommunicationBottomSheetState.show()
                } catch (_: CancellationException) {
                    launch {
                        cardCommunicationBottomSheetState.show()
                    }
                }
            }
            state.isFailure() || state.isReady() -> if (cardCommunicationBottomSheetState.isVisible) {
                cardCommunicationBottomSheetState.hide()
            }
            state.isFinal() -> {
                onNext()
            }
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            CardWallAuthenticationBottomSheet(state) {
                coroutineScope.launch { toggleAuth.emit(false) }
            }
        },
        sheetState = cardCommunicationBottomSheetState,
    ) {
        CardWallScaffold(
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = true,
            onNext = {
                if (viewModel.isNFCEnabled()) {
                    coroutineScope.launch { toggleAuth.emit(true) }
                } else {
                    showEnableNfcDialog = true
                }
            },
            nextText = stringResource(R.string.cdw_auth_btn_txt),
            demoMode = demoMode
        ) {
            Box {
                Column(modifier = Modifier.padding(framePadding)) {
                    Text(
                        stringResource(R.string.cdw_nfc_intro_headline),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.testId("cdw_txt_auth_title")
                    )
                    Spacer8()
                    Text(
                        stringResource(R.string.cdw_nfc_intro_body),
                        style = MaterialTheme.typography.body1
                    )
                    Spacer16()
                    Surface(modifier = Modifier.fillMaxSize()) {
                        InstructionVideo()
                    }
                }
                if (state.isInProgress()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    val nextText = when (state) {
        AuthenticationState.HealthCardCardAccessNumberWrong -> stringResource(R.string.cdw_auth_retry_pin_can)
        AuthenticationState.HealthCardPin2RetriesLeft,
        AuthenticationState.HealthCardPin1RetryLeft -> stringResource(R.string.cdw_auth_retry_pin_can)
        else -> stringResource(R.string.cdw_auth_retry)
    }

    val retryText = when (state) {
        AuthenticationState.IDPCommunicationFailed -> Pair(
            stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step1_info_on_error).toAnnotatedString()
        )
        AuthenticationState.HealthCardCardAccessNumberWrong -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
        )
        AuthenticationState.HealthCardPin2RetriesLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error).toAnnotatedString(),
            pinRetriesLeft(2)
        )
        AuthenticationState.HealthCardPin1RetryLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error).toAnnotatedString(),
            pinRetriesLeft(1)
        )
        AuthenticationState.HealthCardBlocked -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_card_blocked).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step2_info_on_card_blocked).toAnnotatedString()
        )
        else -> null
    }

    if (showEnableNfcDialog) {
        val header = stringResource(R.string.cdw_enable_nfc_header)
        val info = stringResource(R.string.cdw_enable_nfc_info)
        val enableNfcButtonText = stringResource(R.string.cdw_enable_nfc_btn_text)
        val cancelText = stringResource(R.string.cancel)

        CommonAlertDialog(
            header = header,
            info = info,
            cancelText = cancelText,
            actionText = enableNfcButtonText,
            onCancel = { showEnableNfcDialog = false },
            onClickAction = {
                activity.startActivity(Intent("android.settings.NFC_SETTINGS"))
                showEnableNfcDialog = false
            }
        )
    }

    retryText?.let {
        ErrorDialog(
            header = it.first,
            info = it.second,
            retryButtonText = nextText,
            onCancel = {
                coroutineScope.launch { toggleAuth.emit(false) }
            },
            onRetry = {
                when (state) {
                    AuthenticationState.HealthCardCardAccessNumberWrong -> onRetryCan()
                    AuthenticationState.HealthCardPin2RetriesLeft,
                    AuthenticationState.HealthCardPin1RetryLeft -> onRetryPin()
                    else -> if (viewModel.isNFCEnabled()) {
                        coroutineScope.launch { toggleAuth.emit(true) }
                    }
                }
            }
        )
    }
}

@Composable
private fun ErrorDialog(
    header: AnnotatedString,
    info: AnnotatedString,
    retryButtonText: String,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(header)
        },
        onDismissRequest = onCancel,
        text = {
            Text(info)
        },
        buttons = {
            Row(Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
                }
                TextButton(onClick = onRetry) {
                    Text(retryButtonText.uppercase(Locale.getDefault()))
                }
            }
        },
    )
}

private fun String.toAnnotatedString() =
    buildAnnotatedString { append(this@toAnnotatedString) }

@Composable
private fun pinRetriesLeft(count: Int) =
    annotatedPluralsResource(
        R.plurals.cdw_nfc_intro_step2_info_on_pin_error,
        count,
        buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(count.toString()) } }
    )

// clapping hands screen at the end
@Composable
private fun Outro(demoMode: Boolean, onNext: () -> Unit) {
    BackHandler(enabled = true) { onNext() }

    Scaffold(
        bottomBar = {
            CardWallBottomBar(onNext, true, stringResource(R.string.cdw_happy_back_to_mainscreen))
        }
    ) {
        Column {
            if (demoMode) {
                DemoBanner {}
            }
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(it)
                        .padding(framePadding)
                        .semantics(true) {
                            focused = true
                        }
                ) {

                    Image(
                        painterResource(R.drawable.clapping_hands),
                        null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        stringResource(R.string.cdw_happy_headline),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testId("cdw_txt_outro_title")
                    )
                    Spacer8()
                    Text(
                        stringResource(R.string.cdw_happy_body),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CardWallScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    nextText: String = stringResource(R.string.cdw_next),
    demoMode: Boolean,
    backMode: NavigationBarMode = NavigationBarMode.Back,
    content: @Composable ColumnScope.() -> Unit
) {
    val activity = LocalActivity.current

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                mode = backMode,
                headline = title,
                onClick = if (onBack == null) {
                    { activity.onBackPressed() }
                } else {
                    onBack
                }
            )
        },
        bottomBar = {
            CardWallBottomBar(onNext, nextEnabled, nextText)
        }
    ) {
        Column {
            if (demoMode) {
                DemoBanner {}
            }
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .semantics(true) {
                            focused = true
                        }
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CardWallBottomBar(
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextText: String = stringResource(R.string.cdw_next)
) {
    BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNext, enabled = nextEnabled) {
            Text(nextText.uppercase(Locale.getDefault()), modifier = Modifier.testId("cdw_btn_next"))
        }
        Spacer8()
    }
}
