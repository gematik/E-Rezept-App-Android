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

package de.gematik.ti.erp.app.demo.usecase

import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.gematik.ti.erp.app.di.ApplicationDemoPreferences
import de.gematik.ti.erp.app.prescription.repository.PrescriptionDemoDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val DEMOMODE_HAS_BEEN_SEEN = "DEMOMODE_HAS_BEEN_SEEN"

@Singleton
class DemoUseCase @Inject constructor(
    @ApplicationDemoPreferences
    private val appDemoPrefs: SharedPreferences,
    @Named("cardWallDemoSecurePrefs")
    private val secDemoPrefs: SharedPreferences,
    private val prescriptionDemoDataUseCase: PrescriptionDemoDataSource,
) : LifecycleObserver {

    val isDemoModeActive
        get() = demoModeActive.value

    private val _demoModeActive = MutableStateFlow(false)
    val demoModeActive: StateFlow<Boolean>
        get() = _demoModeActive

    var authTokenReceived = MutableStateFlow(false)

    fun activateDemoMode() {
        demoModeHasBeenSeen = true
        _demoModeActive.value = true
    }

    fun deactivateDemoMode() {
        _demoModeActive.value = false

        authTokenReceived.value = false

        prescriptionDemoDataUseCase.reset()
        clearPrefs()
    }

    private var _demoModeHasBeenSeen: Boolean =
        appDemoPrefs.getBoolean(DEMOMODE_HAS_BEEN_SEEN, false)

    var demoModeHasBeenSeen: Boolean
        get() = _demoModeHasBeenSeen
        set(value) {
            if (value != _demoModeHasBeenSeen) {
                appDemoPrefs.edit().putBoolean(DEMOMODE_HAS_BEEN_SEEN, value).apply()
                _demoModeHasBeenSeen = value
            }
        }

    private fun clearPrefs() {
        appDemoPrefs.edit().clear().apply()
        secDemoPrefs.edit().clear().apply()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreateApp() {
        deactivateDemoMode()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyApp() {
        deactivateDemoMode()
    }
}
