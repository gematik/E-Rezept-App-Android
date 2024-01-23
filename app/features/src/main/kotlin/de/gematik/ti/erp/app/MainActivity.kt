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
@file:Suppress("LongMethod")

package de.gematik.ti.erp.app

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
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
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.apicheck.usecase.CheckVersionUseCase
import de.gematik.ti.erp.app.authentication.ui.ExternalAuthPrompt
import de.gematik.ti.erp.app.authentication.ui.HealthCardPrompt
import de.gematik.ti.erp.app.authentication.ui.SecureHardwarePrompt
import de.gematik.ti.erp.app.cardwall.mini.ui.rememberAuthenticator
import de.gematik.ti.erp.app.cardwall.ui.ExternalAuthenticatorListViewModel
import de.gematik.ti.erp.app.core.AppContent
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.demomode.DemoModeActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeEnded
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeStarted
import de.gematik.ti.erp.app.demomode.di.demoModeModule
import de.gematik.ti.erp.app.demomode.di.demoModeOverrides
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.SharePrescriptionHandler
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilesController
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationUseCase
import de.gematik.ti.erp.app.userauthentication.ui.UserAuthenticationScreen
import de.gematik.ti.erp.app.utils.compose.DebugOverlay
import de.gematik.ti.erp.app.utils.compose.DialogHost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.kodein.di.Copy
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.retainedSubDI
import org.kodein.di.bindProvider
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class NfcNotEnabledException : IllegalStateException()

class MainActivity : DemoModeActivity(), DIAware {
    override val di by retainedSubDI(closestDI(), copy = Copy.All) {
        // should be only done from feature module
        import(demoModeModule)
        if (isDemoMode()) demoModeOverrides()
        when {
            BuildConfig.DEBUG && BuildKonfig.INTERNAL -> {
                debugOverrides()
                fullContainerTreeOnError = true
            }
        }
        bindProvider { ExternalAuthenticatorListViewModel(instance(), instance()) }
        bindProvider { CheckVersionUseCase(instance(), instance()) }
    }

    private val checkVersionUseCase: CheckVersionUseCase by instance()

    private val auth: AuthenticationUseCase by instance()

    private val analytics: Analytics by instance()

    private val intentHandler = IntentHandler(this)

    private val _nfcTag = MutableSharedFlow<Tag>()
    val nfcTagFlow: Flow<Tag>
        get() = _nfcTag.onStart {
            if (!NfcAdapter.getDefaultAdapter(this@MainActivity).isEnabled) {
                throw NfcNotEnabledException()
            }
        }

    private val authenticationModeAndMethod: Flow<AuthenticationModeAndMethod>
        get() = auth.authenticationModeAndMethod

    // @VisibleForTesting(otherwise = VisibleForTesting.NONE) // Only visible for testing, otherwise shows a warning
    val testWrapper: TestWrapper by instance()

    // @RestrictTo(RestrictTo.Scope.TESTS)
    @Stable
    class ElementForTest(
        val bounds: Rect,
        val tag: String
    )

    // @RestrictTo(RestrictTo.Scope.TESTS)
    val elementsUsedInTests: SnapshotStateMap<String, ElementForTest> = mutableStateMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        if (!BuildConfig.DEBUG) {
            installMessageConversionExceptionHandler()
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
                    AppContent { settingsController ->
                        val profilesController = rememberProfilesController()
                        val screenShotState by settingsController.screenshotState

                        if (screenShotState.screenshotsAllowed) {
                            this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        } else {
                            this.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                        val auth by produceState<AuthenticationModeAndMethod?>(null) {
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
                        val navController = rememberNavController()
                        val noDrawModifier = Modifier.graphicsLayer(alpha = 0f)
                        val activeProfile by profilesController.getActiveProfileState()
                        val ssoTokenValid = rememberSaveable(activeProfile.ssoTokenScope) {
                            activeProfile.ssoTokenValid()
                        }

                        Box {
                            if (auth !is AuthenticationModeAndMethod.Authenticated) {
                                Image(
                                    painterResource(R.drawable.erp_logo),
                                    null,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            DialogHost {
                                Box(
                                    if (auth is AuthenticationModeAndMethod.Authenticated) Modifier else noDrawModifier
                                ) {
                                    // mini card wall
                                    if (!ssoTokenValid) {
                                        HealthCardPrompt(
                                            authenticator = authenticator.authenticatorHealthCard
                                        )
                                        ExternalAuthPrompt(
                                            authenticator = authenticator.authenticatorExternal
                                        )
                                    }

                                    SecureHardwarePrompt(
                                        authenticator = authenticator.authenticatorSecureElement
                                    )

                                    MainScreenNavigation(
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
                                    visible = auth is AuthenticationModeAndMethod.AuthenticationRequired,
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

    @Requirement(
        "O.Arch_10#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "If an update is required, the user is prompted to update via Google´s InAppUpdate function"
    )
    private suspend fun checkAppUpdate() {
        if (checkVersionUseCase.isUpdateRequired()) {
            val appUpdateManager = AppUpdateManagerFactory.create(this)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    val task = appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.defaultOptions(IMMEDIATE)
                    )

                    task.addOnCompleteListener {
                        if (task.isSuccessful && task.result != Activity.RESULT_OK) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()

        auth.resetInactivityTimer()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        lifecycleScope.launch {
            intent?.let {
                intentHandler.propagateIntent(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        NfcAdapter.getDefaultAdapter(applicationContext)?.let {
            if (it.isEnabled) {
                it.enableReaderMode(
                    this,
                    ::onTagDiscovered,
                    NfcAdapter.FLAG_READER_NFC_A
                        or NfcAdapter.FLAG_READER_NFC_B
                        or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    Bundle()
                )
            }
        }
    }

    private fun onTagDiscovered(tag: Tag) {
        lifecycleScope.launch {
            _nfcTag.emit(tag)
        }
    }

    override fun onPause() {
        super.onPause()

        NfcAdapter.getDefaultAdapter(applicationContext)?.disableReaderMode(this)
    }
}
