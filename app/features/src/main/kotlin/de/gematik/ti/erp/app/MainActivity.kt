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

package de.gematik.ti.erp.app

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.gematik.ti.erp.app.app.ApplicationScaffold
import de.gematik.ti.erp.app.appupdate.navigation.AppUpdateNavHost
import de.gematik.ti.erp.app.authentication.presentation.rememberBiometricAuthenticator
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.AppContent
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.core.LocalBottomSheetNavigator
import de.gematik.ti.erp.app.core.LocalBottomSheetNavigatorSheetState
import de.gematik.ti.erp.app.core.LocalCardCommunicationAnalytics
import de.gematik.ti.erp.app.core.LocalDi
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.LocalNavController
import de.gematik.ti.erp.app.core.LocalTimeZone
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.mainscreen.ui.ExternalAuthenticationUiHandler
import de.gematik.ti.erp.app.navigation.ErezeptNavigatorFactory.initNavigation
import de.gematik.ti.erp.app.prescription.share.presentation.SharePrescriptionHandler
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.compose.withDI
import org.kodein.di.instance

open class MainActivity : BaseActivity() {

    @VisibleForTesting(otherwise = VisibleForTesting.NONE) // Only visible for testing, otherwise shows a warning
    val testWrapper: TestWrapper by instance()

    @OptIn(ExperimentalComposeUiApi::class)
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

        registerDialogVisibilityProvider() // registration needs to happen before setContent

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
                    LocalCardCommunicationAnalytics provides cardCommunicationAnalytics,
                    LocalIntentHandler provides intentHandler
                ) {
                    AppContent {
                        if (isUpdateAvailable) {
                            AppUpdateNavHost(navHostController)
                        } else {
                            val profilesController = rememberProfileController()
                            val mainScreenController = rememberAppController()
                            val isScreenshotsAllowed by mainScreenController.screenshotsState
                            val authentication by authenticationModeAndMethod.collectAsStateWithLifecycle()

                            toggleScreenshotsAllowed(isScreenshotsAllowed)

                            val activeProfile by profilesController.getActiveProfileState()

                            Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                                ApplicationScaffold(
                                    authentication = authentication,
                                    isDemoMode = isDemoMode()
                                )
                                ExternalAuthenticationUiHandler()
                                SharePrescriptionHandler(
                                    activeProfile = activeProfile,
                                    authenticationModeAndMethod = authenticationModeAndMethod
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun MainActivity.isAppInForeground(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false
    val packageName = packageName
    return appProcesses.any {
        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            it.processName == packageName
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
