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

package de.gematik.ti.erp.app

import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.di.NavigationObservable
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationMode
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationModeAndMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

const val SCREENSHOTS_ALLOWED = "SCREENSHOTS_ALLOWED"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: AuthenticationMode

    @Inject
    lateinit var navigationObservable: NavigationObservable

    @Inject
    @ApplicationPreferences
    lateinit var appPrefs: SharedPreferences

    private val _nfcTag = MutableSharedFlow<Tag>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val nfcTagFlow: Flow<Tag>
        get() = _nfcTag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_main)

        if (BuildConfig.DEBUG) {
            appPrefs.edit {
                putBoolean(SCREENSHOTS_ALLOWED, true)
            }
        }

        switchScreenshotMode()
        appPrefs.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == SCREENSHOTS_ALLOWED) {
                switchScreenshotMode()
            }
        }
        lifecycleScope.launchWhenStarted {
            navigationObservable.navigationEventLane.collect {
                it.invoke(findNavController(R.id.nav_host_fragment))
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
                    NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    Bundle()
                )
            }
        }

        lifecycleScope.launchWhenStarted {
            auth.authenticationModeAndMethod.collect {
                val navCtr = findNavController(R.id.nav_host_fragment)

                if (it is AuthenticationModeAndMethod.AuthenticationRequired) {
                    if (navCtr.currentDestination?.id != R.id.userAuthenticationFragment) {
                        navCtr.navigate(
                            NavGraphDirections.actionGlobalUserAuthenticationFragment()
                        )
                    }
                } else {
                    if (navCtr.currentDestination?.id == R.id.userAuthenticationFragment) {
                        navCtr.popBackStack()
                    }
                }
            }
        }
    }

    private fun onTagDiscovered(tag: Tag) {
        _nfcTag.tryEmit(tag)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
