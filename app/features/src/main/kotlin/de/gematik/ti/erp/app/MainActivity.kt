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

package de.gematik.ti.erp.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import de.gematik.ti.erp.app.authentication.ui.ExternalAuthPrompt
import de.gematik.ti.erp.app.authentication.ui.HealthCardPrompt
import de.gematik.ti.erp.app.authentication.ui.SecureHardwarePrompt
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardwall.mini.ui.rememberAuthenticator
import de.gematik.ti.erp.app.core.AppContent
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeEnded
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeStarted
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenNavigation
import de.gematik.ti.erp.app.mainscreen.presentation.rememberMainScreenController
import de.gematik.ti.erp.app.prescription.detail.presentation.SharePrescriptionHandler
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod.Authenticated
import de.gematik.ti.erp.app.userauthentication.ui.UserAuthenticationScreen
import de.gematik.ti.erp.app.utils.compose.DebugOverlay
import de.gematik.ti.erp.app.utils.compose.DialogHost
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class MainActivity : BaseActivity() {

    @VisibleForTesting(otherwise = VisibleForTesting.NONE) // Only visible for testing, otherwise shows a warning
    val testWrapper: TestWrapper by instance()

    @RestrictTo(RestrictTo.Scope.TESTS)
    @Stable
    class ElementForTest(
        val bounds: Rect,
        val tag: String
    )

    @RestrictTo(RestrictTo.Scope.TESTS)
    val elementsUsedInTests: SnapshotStateMap<String, ElementForTest> = mutableStateMapOf()

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectCrashOnlyForDebug()
        catchAllUnCaughtExceptions()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                intent?.let {
                    when (it.action) {
                        DemoModeStarted.name -> setAsDemoMode()
                        DemoModeEnded.name -> cancelDemoMode()
                        else -> {
                            cancelDemoMode()
                            intentHandler.propagateIntent(it)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                checkAppUpdate()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val view = LocalView.current
            LaunchedEffect(view) {
                ViewCompat.setWindowInsetsAnimationCallback(view, null)
            }

            withDI(di) {
                CompositionLocalProvider(
                    LocalActivity provides this,
                    LocalAnalytics provides analytics,
                    LocalIntentHandler provides intentHandler,
                    LocalAuthenticator provides rememberAuthenticator(intentHandler)
                ) {
                    val authenticator = LocalAuthenticator.current
                    AppContent {
                        val profilesController = rememberProfileController()
                        val mainScreenController = rememberMainScreenController()
                        val screenshotsAllowed by mainScreenController.screenshotsState

                        if (screenshotsAllowed) {
                            this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        } else {
                            this.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                        val authentication by produceState<AuthenticationModeAndMethod?>(null) {
                            launch {
                                authenticationModeAndMethod.distinctUntilChangedBy { it::class }
                                    .collect {
                                        if (it is AuthenticationModeAndMethod.AuthenticationRequired) {
                                            authenticator.cancelAllAuthentications()
                                        }
                                    }
                            }
                            authenticationModeAndMethod.collect {
                                value = it
                            }
                        }

                        val bottomSheetNavigator = rememberBottomSheetNavigator()
                        val navController = rememberNavController(bottomSheetNavigator)

                        val noDrawModifier = Modifier.graphicsLayer(alpha = 0f)
                        val activeProfile by profilesController.getActiveProfileState()
                        val isSsoTokenValid = rememberSaveable(activeProfile, activeProfile.ssoTokenScope) {
                            activeProfile.isSSOTokenValid()
                        }

                        Box {
                            if (authentication !is Authenticated) {
                                Image(
                                    painterResource(R.drawable.erp_logo),
                                    null,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            DialogHost {
                                Box(
                                    if (authentication is Authenticated) Modifier else noDrawModifier
                                ) {
                                    // show mini card wall only when we have a invalid sso token
                                    if (!isSsoTokenValid) {
                                        HealthCardPrompt(authenticator.authenticatorHealthCard)
                                        ExternalAuthPrompt(authenticator.authenticatorExternal)
                                    }
                                    SecureHardwarePrompt(authenticator.authenticatorSecureElement)

                                    MainScreenNavigation(
                                        bottomSheetNavigator = bottomSheetNavigator,
                                        navController = navController
                                    )

                                    SharePrescriptionHandler(
                                        activeProfile = activeProfile,
                                        authenticationModeAndMethod = authenticationModeAndMethod
                                    )
                                }
                            }

                            DialogHost {
                                AnimatedVisibility(
                                    visible = authentication is AuthenticationModeAndMethod.AuthenticationRequired,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    UserAuthenticationScreen()
                                }
                            }
                        }
                    }
                    if (BuildConfig.DEBUG && BuildKonfig.DEBUG_VISUAL_TEST_TAGS) {
                        DebugOverlay(elementsUsedInTests)
                    }
                }
            }
        }
    }
}
