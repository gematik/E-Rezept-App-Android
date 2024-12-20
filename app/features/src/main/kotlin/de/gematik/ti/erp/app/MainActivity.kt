/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import de.gematik.ti.erp.app.app.ApplicationScaffold
import de.gematik.ti.erp.app.appupdate.navigation.AppUpdateNavHost
import de.gematik.ti.erp.app.authentication.presentation.rememberBiometricAuthenticator
import de.gematik.ti.erp.app.authentication.ui.components.BiometricPrompt
import de.gematik.ti.erp.app.authentication.ui.components.ExternalAuthPrompt
import de.gematik.ti.erp.app.authentication.ui.components.HealthCardPrompt
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardwall.mini.ui.rememberAuthenticator
import de.gematik.ti.erp.app.core.AppContent
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.core.LocalBottomSheetNavigator
import de.gematik.ti.erp.app.core.LocalBottomSheetNavigatorSheetState
import de.gematik.ti.erp.app.core.LocalDi
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.LocalNavController
import de.gematik.ti.erp.app.core.LocalTimeZone
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.mainscreen.ui.ExternalAuthenticationUiHandler
import de.gematik.ti.erp.app.navigation.ErezeptNavigatorFactory.initNavigation
import de.gematik.ti.erp.app.prescription.detail.presentation.SharePrescriptionHandler
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.utils.compose.DebugOverlay
import de.gematik.ti.erp.app.utils.compose.DialogHost
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.compose.withDI
import org.kodein.di.instance

open class MainActivity : BaseActivity() {

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

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catchAllUnCaughtExceptions(this)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                intent?.let {
                    intentHandler.propagateIntent(it)
                }
            }
        }

        @Requirement(
            "O.Arch_10#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Trigger for update check"
        )
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                checkAppUpdate()
                updateInAppMessage()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val view = LocalView.current
            val isUpdateAvailable by getAppUpdateFlagUseCase.invoke().collectAsStateWithLifecycle()

            LaunchedEffect(view) { ViewCompat.setWindowInsetsAnimationCallback(view, null) }

            val (navHostController, bottomSheetNavigator, sheetState) = initNavigation()

            withDI(di) {
                CompositionLocalProvider(
                    LocalTimeZone provides TimeZone.currentSystemDefault(),
                    LocalDi provides di,
                    LocalActivity provides this,
                    LocalNavController provides navHostController,
                    LocalBottomSheetNavigator provides bottomSheetNavigator,
                    LocalBottomSheetNavigatorSheetState provides sheetState,
                    LocalBiometricAuthenticator provides rememberBiometricAuthenticator(),
                    LocalAnalytics provides analytics,
                    LocalIntentHandler provides intentHandler,
                    LocalAuthenticator provides rememberAuthenticator(intentHandler)
                ) {
                    val authenticator = LocalAuthenticator.current

                    AppContent {
                        if (isUpdateAvailable) {
                            AppUpdateNavHost(navHostController)
                        } else {
                            val profilesController = rememberProfileController()
                            val mainScreenController = rememberAppController()
                            val isScreenshotsAllowed by mainScreenController.screenshotsState

                            toggleScreenshotsAllowed(isScreenshotsAllowed)

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

                            val activeProfile by profilesController.getActiveProfileState()
                            val isSsoTokenValid = rememberSaveable(
                                activeProfile,
                                activeProfile.ssoTokenScope,
                                activeProfile.ssoTokenScope?.token
                            ) {
                                activeProfile.isSSOTokenValid()
                            }

                            Box {
                                DialogHost {
                                    Box {
                                        // show mini card wall only when we have a invalid sso token
                                        if (!isSsoTokenValid) {
                                            HealthCardPrompt(authenticator.authenticatorHealthCard)
                                            ExternalAuthPrompt(authenticator.authenticatorExternal)
                                        }
                                        BiometricPrompt(authenticator.authenticatorBiometric)

                                        ExternalAuthenticationUiHandler()

                                        ApplicationScaffold(
                                            authentication = authentication,
                                            isDemoMode = isDemoMode()
                                        )

                                        SharePrescriptionHandler(
                                            activeProfile = activeProfile,
                                            authenticationModeAndMethod = authenticationModeAndMethod
                                        )
                                    }
                                }
                            }
                        }
                        @Suppress("RestrictedApi")
                        if (BuildConfig.DEBUG && BuildKonfig.DEBUG_VISUAL_TEST_TAGS) {
                            @Suppress("RestrictedApi")
                            DebugOverlay(elementsUsedInTests)
                        }
                    }
                }
            }
        }
    }
}

@Requirement(
    "O.Data_13#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Default settings are not allow screenshots",
    codeLines = 8
)
private fun MainActivity.toggleScreenshotsAllowed(isScreenshotsAllowed: Boolean = false) {
    if (isScreenshotsAllowed) {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        this.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
