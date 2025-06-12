/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import kotlinx.coroutines.delay

enum class HealthCardAnimationState {
    START,
    ZOOM_OUT,
    POSITION_1,
    POSITION_2,
    POSITION_3
}
private data class Wobble(val radius: Dp, val color: Color, val delay: Int)

@Suppress("MagicNumber")
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

    val smartPhoneTransition = updateTransition(smartPhoneToggle, label = "SmartPhoneToggle")
    val healthCardTransition = updateTransition(healthCardToggle, label = "HealthCardToggle")

    val healthCardOffsetDuration = 1500

    val healthCardOffset by calculateHealthCardOffset(healthCardTransition, healthCardOffsetDuration)
    val healthCardScale by calculateHealthCardScale(healthCardTransition)
    val smartPhoneAlpha by calculateSmartPhoneAlpha(smartPhoneTransition)
    val smartPhoneOffset by calculateSmartPhoneOffset(smartPhoneTransition)

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

@Suppress("MagicNumber")
@Composable
private fun calculateSmartPhoneOffset(smartPhoneTransition: Transition<Boolean>): State<Dp> {
    val smartPhoneOffset = smartPhoneTransition.animateDp(
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
    return smartPhoneOffset
}

@Suppress("MagicNumber")
@Composable
private fun calculateSmartPhoneAlpha(smartPhoneTransition: Transition<Boolean>): State<Float> {
    val smartPhoneAlpha = smartPhoneTransition.animateFloat(
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
    return smartPhoneAlpha
}

@Suppress("MagicNumber")
@Composable
private fun calculateHealthCardScale(healthCardTransition: Transition<HealthCardAnimationState>): State<Float> {
    val healthCardScale = healthCardTransition.animateFloat(
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
    return healthCardScale
}

@Suppress("MagicNumber")
@Composable
private fun calculateHealthCardOffset(
    healthCardTransition: Transition<HealthCardAnimationState>,
    healthCardOffsetDuration: Int
): State<DpOffset> {
    val healthCardOffset = healthCardTransition.animateValue(
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
    return healthCardOffset
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
