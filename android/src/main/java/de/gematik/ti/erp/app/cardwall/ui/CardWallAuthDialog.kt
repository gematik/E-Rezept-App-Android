/*
 * Copyright (c) 2022 gematik GmbH
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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.NfcNotEnabledException
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.analytics.Analytics.AuthenticationProblem
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.handleIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.retryWhen
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException

private enum class HealthCardAnimationState {
    START,
    ZOOM_OUT,
    POSITION_1,
    POSITION_2,
    POSITION_3
}

@Stable
class CardWallAuthenticationDialogState {
    val toggleAuth = MutableSharedFlow<ToggleAuth>()

    suspend fun show() {
        toggleAuth.emit(ToggleAuth.ToggleByUser(true))
    }
}

@Composable
fun rememberCardWallAuthenticationDialogState(): CardWallAuthenticationDialogState {
    return remember { CardWallAuthenticationDialogState() }
}

@Suppress("LongMethod", "ComplexMethod")
@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun CardWallAuthenticationDialog(
    dialogState: CardWallAuthenticationDialogState = rememberCardWallAuthenticationDialogState(),
    viewModel: CardWallViewModel,
    authenticationData: CardWallAuthenticationData,
    profileId: ProfileIdentifier,
    troubleShootingEnabled: Boolean = false,
    allowUserCancellation: Boolean = true,
    onFinal: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
    onUnlockEgk: () -> Unit,
    onClickTroubleshooting: (() -> Unit)? = null,
    onStateChange: ((AuthenticationState) -> Unit)? = null
) {
    val activity = LocalActivity.current as MainActivity
    val coroutineScope = rememberCoroutineScope()
    val toggleAuth = dialogState.toggleAuth

    var showEnableNfcDialog by remember { mutableStateOf(false) }
    var errorCount by remember(troubleShootingEnabled) { mutableStateOf(0) }

    val tracker = LocalAnalytics.current

    var cancelEnabled by remember { mutableStateOf(true) }
    val state by produceState<AuthenticationState>(initialValue = AuthenticationState.None) {
        toggleAuth.transformLatest {
            emit(AuthenticationState.None)
            cancelEnabled = true
            when (it) {
                is ToggleAuth.ToggleByUser -> {
                    if (it.value) {
                        emitAll(
                            viewModel.doAuthentication(
                                profileId = profileId,
                                authenticationData = authenticationData,
                                activity
                                    .nfcTagFlow
                                    .catch {
                                        if (it is NfcNotEnabledException) {
                                            showEnableNfcDialog = true
                                        }
                                    }
                            )
                        )
                    } else {
                        value = AuthenticationState.None
                    }
                }
                is ToggleAuth.ToggleByHealthCard -> {
                    val collectedOnce = AtomicBoolean(false)
                    val tagFlow = flow {
                        if (collectedOnce.get()) {
                            activity.nfcTagFlow.collect {
                                emit(it)
                            }
                        } else {
                            collectedOnce.set(true)
                            emit(it.tag)
                        }
                    }
                    emitAll(
                        viewModel.doAuthentication(
                            profileId = profileId,
                            authenticationData = authenticationData,
                            tagFlow
                        )
                    )
                }
            }
        }.catch {
            Napier.e("Something unforeseen happened", it)
            // if this happens we can't recover from here
            emit(AuthenticationState.HealthCardCommunicationInterrupted)
            delay(1000)
        }.onCompletion { cause ->
            if (cause is CancellationException) {
                value = AuthenticationState.None
            }
        }.collect {
            errorCount += if (it == AuthenticationState.HealthCardCommunicationInterrupted) 1 else 0
            value = it

            tracker.trackAuth(it)
        }
    }

    LaunchedEffect(Unit) {
        activity.nfcTagFlow
            .retryWhen { cause, _ ->
                cause !is NfcNotEnabledException
            }
            .catch { cause ->
                if (cause is NfcNotEnabledException) {
                    showEnableNfcDialog = true
                }
            }
            .filter {
                // only let interrupted communications through
                !(state.isFailure() && state != AuthenticationState.HealthCardCommunicationInterrupted)
            }
            .collect {
                toggleAuth.emit(ToggleAuth.ToggleByHealthCard(it))
            }
    }

    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        onStateChange?.run { onStateChange(state) }

        when {
            state.isInProgress() -> showAuthDialog = true
            state.isReady() -> showAuthDialog = false
            state.isFinal() -> {
                tracker.trackIdentifiedWithIDP()
                onFinal()
            }
        }
    }

    if (showAuthDialog) {
        AuthenticationDialog(
            state = state,
            showTroubleshooting = troubleShootingEnabled && errorCount > 2 && !state.isInProgress(),
            onCancelEnabled = allowUserCancellation && cancelEnabled,
            onCancel = {
                coroutineScope.launch {
                    cancelEnabled = false
                    toggleAuth.emit(ToggleAuth.ToggleByUser(false))
                }
            },
            onClickTroubleshooting = onClickTroubleshooting
        )
    }

    val nextText = when (state) {
        AuthenticationState.HealthCardCardAccessNumberWrong -> stringResource(R.string.cdw_auth_retry_pin_can)
        AuthenticationState.HealthCardPin2RetriesLeft,
        AuthenticationState.HealthCardPin1RetryLeft -> stringResource(R.string.cdw_auth_retry_pin_can)
        AuthenticationState.HealthCardBlocked -> stringResource(R.string.cdw_auth_retry_unlock_egk)
        else -> stringResource(R.string.cdw_auth_retry)
    }

    val retryText = when (val s = state) {
        AuthenticationState.IDPCommunicationFailed -> Pair(
            stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
            stringResource(R.string.cdw_idp_error_time_and_connection).toAnnotatedString()
        )
        AuthenticationState.IDPCommunicationInvalidCertificate -> Pair(
            stringResource(R.string.cdw_nfc_error_title_invalid_certificate).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_error_body_invalid_certificate).toAnnotatedString()
        )
        AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate -> Pair(
            stringResource(R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate).toAnnotatedString()
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
            stringResource(R.string.cdw_header_on_card_blocked).toAnnotatedString(),
            stringResource(R.string.cdw_info_on_card_blocked).toAnnotatedString()
        )
        is AuthenticationState.InsuranceIdentifierAlreadyExists -> {
            Pair(
                stringResource(R.string.cdw_nfc_error_assign_title).toAnnotatedString(),
                if (s.inActiveProfile) {
                    stringResource(R.string.cdw_nfc_error_assign_subtitle, s.insuranceIdentifier).toAnnotatedString()
                } else {
                    stringResource(R.string.cdw_nfc_error_assign_other_subtitle, s.profileName).toAnnotatedString()
                }
            )
        }
        else -> null
    }

    if (showEnableNfcDialog) {
        EnableNfcDialog {
            showEnableNfcDialog = false
        }
    }

    retryText?.let {
        ErrorDialog(
            header = it.first,
            info = it.second,
            retryButtonText = nextText,
            onCancel = {
                coroutineScope.launch { toggleAuth.emit(ToggleAuth.ToggleByUser(false)) }
            },
            onRetry = {
                when (state) {
                    AuthenticationState.HealthCardCardAccessNumberWrong -> onRetryCan()
                    AuthenticationState.HealthCardPin2RetriesLeft,
                    AuthenticationState.HealthCardPin1RetryLeft -> onRetryPin()
                    AuthenticationState.HealthCardBlocked -> onUnlockEgk()
                    else -> if (viewModel.isNFCEnabled()) {
                        coroutineScope.launch {
                            toggleAuth.emit(ToggleAuth.ToggleByUser(true))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun EnableNfcDialog(onCancel: () -> Unit) {
    val context = LocalContext.current
    val header = stringResource(R.string.cdw_enable_nfc_header)
    val info = stringResource(R.string.cdw_enable_nfc_info)
    val enableNfcButtonText = stringResource(R.string.cdw_enable_nfc_btn_text)
    val cancelText = stringResource(R.string.cancel)

    CommonAlertDialog(
        header = header,
        info = info,
        cancelText = cancelText,
        actionText = enableNfcButtonText,
        onCancel = onCancel,
        onClickAction = {
            context.handleIntent(Intent("android.settings.NFC_SETTINGS"))
        }
    )
}

@Composable
fun ErrorDialog(
    header: AnnotatedString,
    info: AnnotatedString,
    retryButtonText: String,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) =
    CommonAlertDialog(
        header = header,
        info = info,
        onCancel = onCancel,
        onClickAction = onRetry,
        cancelText = stringResource(R.string.cancel),
        actionText = retryButtonText
    )

fun String.toAnnotatedString() =
    buildAnnotatedString { append(this@toAnnotatedString) }

@Composable
fun pinRetriesLeft(count: Int) =
    annotatedPluralsResource(
        R.plurals.cdw_nfc_intro_step2_info_on_pin_error,
        count,
        buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(count.toString()) } }
    )

@ExperimentalAnimationApi
@Composable
private fun AuthenticationDialog(
    state: AuthenticationState,
    showTroubleshooting: Boolean,
    onCancelEnabled: Boolean,
    onCancel: () -> Unit,
    onClickTroubleshooting: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            Modifier
                .testTag(TestTag.CardWall.Nfc.CardReadingDialog)
                .semantics(false) { }
                .fillMaxSize()
                .background(SolidColor(Color.Black), alpha = 0.5f)
                .systemBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(PaddingDefaults.Medium),
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(28.dp),
                elevation = 8.dp
            ) {
                val screen = remember(state) {
                    when (state) {
                        AuthenticationState.AuthenticationFlowInitialized -> 0
                        AuthenticationState.HealthCardCommunicationChannelReady,
                        AuthenticationState.HealthCardCommunicationTrustedChannelEstablished,
                        AuthenticationState.HealthCardCommunicationCertificateLoaded,
                        AuthenticationState.HealthCardCommunicationFinished,
                        AuthenticationState.IDPCommunicationFinished,
                        AuthenticationState.AuthenticationFlowFinished -> 1
                        AuthenticationState.HealthCardCommunicationInterrupted -> 2
                        else -> 1
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentSize()
                        .testTag("cdw_auth_nfc_bottom_sheet"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        enabled = onCancelEnabled,
                        onClick = onCancel
                    ) {
                        Text(stringResource(R.string.cdw_nfc_dlg_cancel).uppercase(Locale.getDefault()))
                    }
                    CardAnimationBox(screen)

                    // how to hold your card
                    val rotatingScanCardAssistance = rotatingScanCardAssistance()

                    var info by remember { mutableStateOf(rotatingScanCardAssistance.first()) }

                    LaunchedEffect(state) {
                        if (state == AuthenticationState.AuthenticationFlowInitialized) {
                            var i = 0
                            while (true) {
                                info = rotatingScanCardAssistance[i]

                                i = if (i < rotatingScanCardAssistance.size - 1) {
                                    i + 1
                                } else {
                                    0
                                }

                                delay(5000)
                            }
                        }
                    }

                    info = when (state) {
                        AuthenticationState.HealthCardCommunicationChannelReady -> Pair(
                            stringResource(R.string.cdw_nfc_found_headline),
                            stringResource(R.string.cdw_nfc_found_info)
                        )
                        AuthenticationState.HealthCardCommunicationTrustedChannelEstablished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_trusted_channel_established),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )
                        AuthenticationState.HealthCardCommunicationFinished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_certificate_loaded),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )
                        AuthenticationState.IDPCommunicationFinished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_pin_verified),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )
                        AuthenticationState.AuthenticationFlowFinished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_challenge_signed),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )
                        AuthenticationState.HealthCardCommunicationInterrupted -> Pair(
                            stringResource(R.string.cdw_nfc_tag_lost_headline),
                            stringResource(R.string.cdw_nfc_tag_lost_info)
                        )
                        else -> info
                    }

                    if (showTroubleshooting) {
                        Troubleshooting(
                            onClick = { onClickTroubleshooting?.run { onClickTroubleshooting() } }
                        )
                    } else {
                        Text(
                            info.first,
                            style = AppTheme.typography.subtitle1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            info.second,
                            style = AppTheme.typography.body2,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Troubleshooting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.cdw_enter_troubleshooting_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.cdw_enter_troubleshooting_subtitle),
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        SpacerMedium()
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
        ) {
            Icon(Icons.Outlined.Lightbulb, null)
            SpacerTiny()
            Text(stringResource(R.string.cdw_enter_troubleshooting_action))
        }
    }
}

private data class Wobble(val radius: Dp, val color: Color, val delay: Int)

@Suppress("LongMethod")
@Composable
fun SearchingCardAnimation() {
    val wobbleColorL = Wobble(72.dp, AppTheme.colors.primary100.copy(alpha = 0.7f), 600)
    val wobbleColorM = Wobble(56.dp, AppTheme.colors.primary200.copy(alpha = 0.3f), 300)
    val wobbleColorS = Wobble(40.dp, AppTheme.colors.primary300.copy(alpha = 0.2f), 0)

    val wobble = listOf(wobbleColorL, wobbleColorM, wobbleColorS)

    val wobbleTransition = rememberInfiniteTransition()
    val slowInSlowOut = CubicBezierEasing(0.3f, 0.0f, 0.7f, 1.0f)

    val smartPhone = painterResource(R.drawable.ic_phone_transparent)
    val healthCard = painterResource(R.drawable.ic_healthcard)

    var smartPhoneToggle by remember { mutableStateOf(false) }
    var healthCardToggle by remember { mutableStateOf(HealthCardAnimationState.START) }

    val smartPhoneTransition = updateTransition(smartPhoneToggle)
    val healthCardTransition = updateTransition(healthCardToggle)

    val healthCardOffsetDuration = 1500

    val healthCardOffset by healthCardTransition.animateValue(
        DpOffset.VectorConverter,
        transitionSpec = {
            tween(
                healthCardOffsetDuration - 10,
                0
            )
        },
        label = "healthCardOffset"
    ) { state ->
        when (state) {
            HealthCardAnimationState.START -> DpOffset(0.dp, 0.dp)
            HealthCardAnimationState.ZOOM_OUT -> DpOffset(-(30.dp), 0.dp)
            HealthCardAnimationState.POSITION_1 -> DpOffset(30.dp, 0.dp)
            HealthCardAnimationState.POSITION_2 -> DpOffset(-(20.dp), 30.dp)
            HealthCardAnimationState.POSITION_3 -> DpOffset(-(30.dp), 0.dp)
        }
    }

    val healthCardScale by healthCardTransition.animateFloat(
        transitionSpec = {
            tween(
                1000,
                0
            )
        },
        label = "healthCardScale"
    ) { state ->
        when (state) {
            HealthCardAnimationState.START -> 1.0f
            else -> 0.7f
        }
    }
    val smartPhoneAlpha by smartPhoneTransition.animateFloat(
        transitionSpec = {
            tween(
                1300,
                1500
            )
        },
        label = "smartPhoneAlpha"
    ) { state ->
        when (state) {
            true -> 1.0f
            false -> 0.0f
        }
    }

    val smartPhoneOffset by smartPhoneTransition.animateDp(
        transitionSpec = {
            tween(
                1300,
                1500
            )
        },
        label = "smartPhoneOffset"
    ) { state ->
        when (state) {
            true -> 0.dp
            false -> 50.dp
        }
    }

    SideEffect {
        smartPhoneToggle = true
    }

    LaunchedEffect(Unit) {
        delay(3000)
        healthCardToggle = HealthCardAnimationState.ZOOM_OUT
        while (true) {
            delay(healthCardOffsetDuration.toLong())
            healthCardToggle = HealthCardAnimationState.POSITION_1
            delay(healthCardOffsetDuration.toLong())
            healthCardToggle = HealthCardAnimationState.POSITION_2
            delay(healthCardOffsetDuration.toLong())
            healthCardToggle = HealthCardAnimationState.POSITION_3
        }
    }

    val wobbleAnimations =
        wobble.map {
            Triple(
                it,
                wobbleTransition.animateFloat(
                    1.0f,
                    1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 2500
                            1.0f at 0
                            1.0f at it.delay with slowInSlowOut
                            1.1f at 1000 + it.delay
                            1.0f at 2500
                        },
                        repeatMode = RepeatMode.Restart
                    )
                ),
                wobbleTransition.animateFloat(
                    1.0f,
                    0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1000,
                            delayMillis = it.delay,
                            slowInSlowOut
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            )
        }

    Box {
        Box(
            modifier = Modifier
                .drawBehind {
                    wobbleAnimations.forEach { (wobble, animScale, animAlpha) ->
                        drawCircle(
                            color = wobble.color,
                            radius = wobble.radius.toPx() * animScale.value,
                            alpha = animAlpha.value
                        )
                    }
                }
        ) {
            Image(
                healthCard,
                null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = healthCardScale
                        scaleY = healthCardScale
                    }
                    .offset(healthCardOffset.x, healthCardOffset.y)
                    .align(Alignment.Center)
            )

            Image(
                smartPhone,
                null,
                alpha = smartPhoneAlpha,
                modifier = Modifier
                    .size(80.dp)
                    .align(
                        Alignment.Center
                    )
                    .offset(y = smartPhoneOffset)
            )
        }
    }
}

@Composable
fun ReadingCardAnimation() {
    Box {
        Image(
            painterResource(R.drawable.ic_healthcard_spinner),
            null,
            modifier = Modifier
                .align(
                    Alignment.CenterEnd
                )
        )
        CircularProgressIndicator(
            color = AppTheme.colors.neutral400,
            strokeWidth = 2.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(4.dp)
                .size(24.dp)
        )
    }
}

@Composable
fun TagLostCard() {
    Image(
        painterResource(R.drawable.ic_healthcard_tag_lost),
        null
    )
}

private fun Analytics.trackAuth(state: AuthenticationState) {
    if (trackingAllowed.value) {
        when (state) {
            AuthenticationState.HealthCardBlocked ->
                trackAuthenticationProblem(AuthenticationProblem.CardBlocked)
            AuthenticationState.HealthCardCardAccessNumberWrong ->
                trackAuthenticationProblem(AuthenticationProblem.CardAccessNumberWrong)
            AuthenticationState.HealthCardCommunicationInterrupted ->
                trackAuthenticationProblem(AuthenticationProblem.CardCommunicationInterrupted)
            AuthenticationState.HealthCardPin1RetryLeft,
            AuthenticationState.HealthCardPin2RetriesLeft ->
                trackAuthenticationProblem(AuthenticationProblem.CardPinWrong)
            AuthenticationState.IDPCommunicationFailed ->
                trackAuthenticationProblem(AuthenticationProblem.IDPCommunicationFailed)
            AuthenticationState.IDPCommunicationInvalidCertificate ->
                trackAuthenticationProblem(AuthenticationProblem.IDPCommunicationInvalidCertificate)
            AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                trackAuthenticationProblem(AuthenticationProblem.IDPCommunicationInvalidOCSPOfCard)
            AuthenticationState.SecureElementCryptographyFailed ->
                trackAuthenticationProblem(AuthenticationProblem.SecureElementCryptographyFailed)
            AuthenticationState.UserNotAuthenticated ->
                trackAuthenticationProblem(AuthenticationProblem.UserNotAuthenticated)
            else -> {}
        }
    }
}

@Composable
fun CardAnimationBox(screen: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minHeight = 150.dp)
            .fillMaxWidth()
    ) {
        when (screen) {
            0 -> SearchingCardAnimation()
            1 -> ReadingCardAnimation()
            2 -> TagLostCard()
        }
    }
}

@Composable
fun rotatingScanCardAssistance() = listOf(
    Pair(
        stringResource(R.string.cdw_nfc_search1_headline),
        stringResource(R.string.cdw_nfc_search1_info)
    ),
    Pair(
        stringResource(R.string.cdw_nfc_search2_headline),
        stringResource(R.string.cdw_nfc_search2_info)
    ),
    Pair(
        stringResource(R.string.cdw_nfc_search3_headline),
        stringResource(R.string.cdw_nfc_search3_info)
    )
)
