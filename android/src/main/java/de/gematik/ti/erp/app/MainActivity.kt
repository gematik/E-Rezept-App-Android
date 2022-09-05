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

package de.gematik.ti.erp.app

import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgkViewModel
import de.gematik.ti.erp.app.cardwall.mini.ui.ExternalAuthPrompt
import de.gematik.ti.erp.app.cardwall.mini.ui.HealthCardPrompt
import de.gematik.ti.erp.app.cardwall.mini.ui.MiniCardWallViewModel
import de.gematik.ti.erp.app.cardwall.mini.ui.rememberAuthenticator
import de.gematik.ti.erp.app.cardwall.ui.CardWallNfcPositionViewModel
import de.gematik.ti.erp.app.cardwall.ui.CardWallViewModel
import de.gematik.ti.erp.app.cardwall.ui.ExternalAuthenticatorListViewModel
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.core.MainContent
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.di.ApplicationPreferencesTag
import de.gematik.ti.erp.app.mainscreen.ui.MainScreen
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.mainscreen.ui.RedeemStateViewModel
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardOrderViewModel
import de.gematik.ti.erp.app.pharmacy.ui.PharmacySearchViewModel
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsViewModel
import de.gematik.ti.erp.app.prescription.ui.PrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.ScanPrescriptionViewModel
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.profiles.ui.ProfileViewModel
import de.gematik.ti.erp.app.redeem.ui.RedeemViewModel
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.pharmacy.repository.model.OftenUsedPharmaciesViewModel
import de.gematik.ti.erp.app.prescription.detail.ui.SharePrescriptionHandler
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.rememberProfileHandler
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationUseCase
import de.gematik.ti.erp.app.userauthentication.ui.UserAuthenticationScreen
import de.gematik.ti.erp.app.userauthentication.ui.UserAuthenticationViewModel
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
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberViewModel
import org.kodein.di.compose.withDI
import org.kodein.di.instance

const val SCREENSHOTS_ALLOWED = "SCREENSHOTS_ALLOWED"

class NfcNotEnabledException : IllegalStateException()

class MainActivity : AppCompatActivity(), DIAware {
    override val di by retainedSubDI(closestDI(), copy = Copy.None) {
        if (BuildKonfig.INTERNAL) {
            fullContainerTreeOnError = true
        }

        bindProvider { UnlockEgkViewModel(instance(), instance()) }
        bindProvider { MiniCardWallViewModel(instance(), instance(), instance(), instance()) }
        bindProvider { CardWallNfcPositionViewModel(instance()) }
        bindProvider { CardWallViewModel(instance(), instance(), instance()) }
        bindProvider { ExternalAuthenticatorListViewModel(instance(), instance()) }
        bindProvider { RedeemStateViewModel(instance(), instance()) }
        bindProvider { HealthCardOrderViewModel(instance()) }
        bindProvider { PrescriptionDetailsViewModel(instance(), instance()) }
        bindProvider { PrescriptionViewModel(instance(), instance(), instance()) }
        bindProvider {
            ScanPrescriptionViewModel(
                prescriptionUseCase = instance(),
                profilesUseCase = instance(),
                scanner = instance(),
                processor = instance(),
                validator = instance(),
                dispatchers = instance()
            )
        }
        bindProvider { ProfileViewModel(instance()) }
        bindProvider { ProfileSettingsViewModel(instance(), instance()) }
        bindProvider { RedeemViewModel(instance(), instance(), instance()) }
        bindProvider { UserAuthenticationViewModel(instance()) }
        bindProvider { PharmacySearchViewModel(instance(), instance(), instance(), instance()) }
        bindProvider { OftenUsedPharmaciesViewModel(instance()) }

        bindSingleton {
            SettingsViewModel(
                settingsUseCase = instance(),
                profilesUseCase = instance(),
                profilesWithPairedDevicesUseCase = instance(),
                analytics = instance(),
                appPrefs = instance(ApplicationPreferencesTag),
                dispatchers = instance()
            )
        }
        bindSingleton { MainViewModel(instance(), instance()) }
        bindSingleton { MainScreenViewModel(instance(), instance()) }
    }

    private val auth: AuthenticationUseCase by instance()

    private val analytics: Analytics by instance()

    private val appPrefs: SharedPreferences by instance(ApplicationPreferencesTag)

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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Stable
    class Element(
        val bounds: Rect,
        val tag: String
    )

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    val elements: SnapshotStateMap<String, Element> = mutableStateMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            intent?.let {
                intentHandler.propagateIntent(it)
            }
        }

        if (!BuildConfig.DEBUG) {
            installMessageConversionExceptionHandler()
        }

        if (BuildKonfig.INTERNAL) {
            appPrefs.edit {
                putBoolean(SCREENSHOTS_ALLOWED, true)
            }
        }

        switchScreenshotMode()
        appPrefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == SCREENSHOTS_ALLOWED) {
                switchScreenshotMode()
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

                    MainContent { mainViewModel ->
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
                        val noDrawModifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = 0f)

                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    HealthCardPrompt(
                                        authenticator = authenticator.authenticatorHealthCard
                                    )
                                    ExternalAuthPrompt(
                                        authenticator = authenticator.authenticatorExternal
                                    )

                                    val settingsViewModel by rememberViewModel<SettingsViewModel>()
                                    val profileSettingsViewModel by rememberViewModel<ProfileSettingsViewModel>()

                                    CompositionLocalProvider(
                                        LocalProfileHandler provides rememberProfileHandler()
                                    ) {
                                        MainScreen(
                                            navController = navController,
                                            mainViewModel = mainViewModel,
                                            settingsViewModel = settingsViewModel,
                                            profileSettingsViewModel = profileSettingsViewModel
                                        )

                                        SharePrescriptionHandler(authenticationModeAndMethod)
                                    }
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
                        DebugOverlay(elements)
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

    private fun switchScreenshotMode() {
        // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
        if (appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false)) {
            this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            this.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
