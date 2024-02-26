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

@file:Suppress("LongMethod", "MagicNumber")

package de.gematik.ti.erp.app.base

import android.app.Dialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.DomainVerifier
import de.gematik.ti.erp.app.OlderSdkDomainVerifier
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.Sdk31DomainVerifier
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.appupdate.usecase.AppUpdateInfoUseCase
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.CheckVersionUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerUseCase
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.debugOverrides
import de.gematik.ti.erp.app.demomode.DefaultDemoModeObserver
import de.gematik.ti.erp.app.demomode.DemoModeObserver
import de.gematik.ti.erp.app.demomode.di.demoModeModule
import de.gematik.ti.erp.app.demomode.di.demoModeOverrides
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.timeouts.usecase.GetPauseMetricUseCase
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.SnackbarScaffold
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.Copy
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.retainedSubDI
import org.kodein.di.bindProvider
import org.kodein.di.instance

open class BaseActivity :
    SnackbarScaffold,
    DialogScaffold,
    DIAware,
    AppCompatActivity(),
    DemoModeObserver by DefaultDemoModeObserver() {

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
        bindProvider { CheckVersionUseCase(instance()) }

        // domain verification is only available on SDK 31 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bindProvider<DomainVerifier> { Sdk31DomainVerifier(instance()) }
        } else {
            bindProvider<DomainVerifier> { OlderSdkDomainVerifier() }
        }
    }

    private val checkVersionUseCase: CheckVersionUseCase by instance()

    private val getAppUpdateManagerFlagUseCase: GetAppUpdateManagerFlagUseCase by instance()

    private val appUpdateInfoUseCase: AppUpdateInfoUseCase by instance()

    private val pauseTimeoutUseCase: GetPauseMetricUseCase by instance()

    private val inactivityTimeoutObserver: InactivityTimeoutObserver by instance()

    private val changeAppUpdateFlagUseCase: ChangeAppUpdateFlagUseCase by instance()

    private val updateManagerUseCase: GetAppUpdateManagerUseCase by instance()

    val getAppUpdateFlagUseCase: GetAppUpdateFlagUseCase by instance()

    val analytics: Analytics by instance()

    val intentHandler = IntentHandler(this@BaseActivity)

    // This is needed to be declared here so that the dialog can be cancelled on-pause
    private var dialog: Dialog? = null

    private val _nfcTag = MutableSharedFlow<Tag>()

    private var pauseTimerHandler: Handler = Handler(Looper.getMainLooper())

    /**
     * A [Runnable] that makes the app require an authentication
     */
    @Requirement(
        "O.Auth_7",
        "O.Plat_12",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "The LifeCycleState of the app is monitored. If the app stopped, the authentication " +
            "process starts after a delay of 30 seconds. We opted for this delay for usability reasons, " +
            "as selecting the profile picture and external authentication requires pausing the app."
    )
    private val pauseTimerRunnable = Runnable {
        inactivityTimeoutObserver.forceRequireAuthentication()
    }

    override fun onPause() {
        super.onPause()
        // cancel the dialog when pausing since it might show up security concerns
        dialog?.remove()

        // this sets the app to require authentication after the pause-timeout
        val pauseTimeout = pauseTimeoutUseCase.invoke()
        Napier.i { "Started pause timer for ${pauseTimeout.inWholeMilliseconds}" }
        pauseTimerHandler.postDelayed(pauseTimerRunnable, pauseTimeout.inWholeMilliseconds)

        NfcAdapter.getDefaultAdapter(applicationContext)?.disableReaderMode(this)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        inactivityTimeoutObserver.resetInactivityTimer()
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
        Napier.i { "Stopped pause timer" }
        pauseTimerHandler.removeCallbacks(pauseTimerRunnable)

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

    override fun show(content: @Composable (Dialog) -> Unit) {
        dialog = Dialog(this, R.style.ThemeOverlay_MaterialAlertDialog_Rounded)
        val composableView = ComposeView(this)
        dialog?.remove()
        dialog?.setDecorView(this)
        composableView.let {
            it.setDecorView(this)
            it.setDialogContent(dialog, content)
        }
        dialog?.setViewAndShow(composableView)
    }

    override fun show(
        text: String,
        actionTextId: Int?,
        length: Int,
        onClickAction: () -> Unit?,
        @DrawableRes icon: Int,
        @ColorRes backgroundTint: Int
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), text, length)
        val theme = ContextThemeWrapper(
            applicationContext,
            R.style.ThemeOverlay_MaterialAlertDialog_Rounded
        ).theme
        snackbar.setBackgroundTint(resources.getColor(backgroundTint, theme))
        snackbar.setTextColor(resources.getColor(R.color.neutral_100, theme))
        if (actionTextId != null) {
            snackbar.setAction(actionTextId) {
                onClickAction()
            }
        }
        snackbar.setActionTextColor(resources.getColor(R.color.primary_700, theme))
        snackbar.view.updateLayoutParams<FrameLayout.LayoutParams> {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM
            setMargins(24)
        }
        snackbar.show()
    }

    private fun onTagDiscovered(tag: Tag) {
        lifecycleScope.launch {
            _nfcTag.emit(tag)
        }
    }

    val authenticationModeAndMethod: StateFlow<AuthenticationModeAndMethod>
        get() = inactivityTimeoutObserver.authenticationModeAndMethod
            .stateIn(lifecycleScope, SharingStarted.Eagerly, AuthenticationModeAndMethod.None)

    @Requirement(
        "O.Arch_10#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "If an update is required, the user is prompted to update via Google´s InAppUpdate function"
    )
    suspend fun checkAppUpdate() {
        val isUpdateRequired = when {
            !getAppUpdateManagerFlagUseCase.invoke() -> true
            else -> checkVersionUseCase.isUpdateRequired()
        }

        if (isUpdateRequired) {
            updateManagerUseCase.invoke(this@BaseActivity).let {
                appUpdateInfoUseCase.invoke(
                    activity = this@BaseActivity,
                    appUpdateManager = it,
                    onTaskSuccessful = {
                        Napier.d { "Successfully downloaded" }
                    },
                    onTaskFailed = {
                        changeAppUpdateFlagUseCase.invoke(true)
                    }
                )
            }
        }
    }

    // Flow on a non main thread to avoid ANR
    val nfcTagFlow: Flow<Tag>
        get() = _nfcTag.onStart {
            throwExceptionOnNfcNotEnabled()
        }.flowOn(Dispatchers.IO)

    companion object {
        private fun BaseActivity.isNfcNotEnabled() = !NfcAdapter.getDefaultAdapter(this).isEnabled

        fun BaseActivity.throwExceptionOnNfcNotEnabled() {
            if (isNfcNotEnabled()) {
                throw NfcNotEnabledException("NFC not switched on")
            }
        }
    }
}
