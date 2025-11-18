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

package de.gematik.ti.erp.app.appsecurity.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.appsecurity.presentation.rememberAppSecurityController
import de.gematik.ti.erp.app.appsecurity.ui.model.AppSecurityResult
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.onboarding.ui.SkipOnBoardingButton
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

private const val ANIMATION_TIME = 1000

class DeviceCheckLoadingStartScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val onSecurityCheckResult: (AppSecurityResult) -> Unit
) : Screen() {

    @Composable
    override fun Content() {
        val appSecurityController = rememberAppSecurityController()

        // used here only for skip button
        val onboardingController = rememberOnboardingController()

        var isSkipped by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (isSkipped) return@LaunchedEffect

            val result = if (BuildConfigExtension.isInternalDebug) {
                AppSecurityResult(
                    isIntegritySecure = true,
                    isDeviceSecure = true
                )
            } else {
                AppSecurityResult(
                    isIntegritySecure = appSecurityController.checkIntegrityRisk(),
                    isDeviceSecure = appSecurityController.checkDeviceSecurityRisk()
                )
            }

            if (!isSkipped) {
                onSecurityCheckResult(result)
            }
        }

        if (BuildConfigExtension.isInternalDebug) {
            SkipOnBoardingButton {
                isSkipped = true // Prevents LaunchedEffect from continuing
                onboardingController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }

        LoadingAnimation()
    }
}

@Composable
private fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(ANIMATION_TIME, easing = LinearEasing)
        ),
        label = "FloatAnimation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { rotationZ = angle },
            painter = painterResource(R.drawable.erp_logo),
            contentDescription = null
        )
    }
}
