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

package de.gematik.ti.erp.app.tracking

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import androidx.navigation.NavHostController
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.core.LocalTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import pro.piwik.sdk.Piwik
import pro.piwik.sdk.Tracker
import pro.piwik.sdk.TrackerConfig
import pro.piwik.sdk.extra.TrackHelper
import pro.piwik.sdk.tools.Checksum
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val trackerName = "Tracker"

// `gemSpec_eRp_FdV A_20187`
@Singleton
class Tracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _trackingAllowed = MutableStateFlow(false)
    val trackingAllowed: StateFlow<Boolean>
        get() = _trackingAllowed

    private val prefsName = "pro.piwik.sdk_" + Checksum.getMD5Checksum(trackerName)
    private var tracker: Tracker = initTracker()

    private fun initTracker(): Tracker {
        Timber.d("Init tracker")

        // otherwise piwik will query the advertisement id; piwik also doesn't expose this functionality in a more appropriate way
        context.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).let { prefs ->
            prefs.edit {
                putBoolean("tracker.deviceid.on", false)
                if (!prefs.contains("tracker.optout")) {
                    putBoolean("tracker.optout", true)
                }
            }
        }

        return Piwik.getInstance(context).newTracker(
            TrackerConfig(
                BuildKonfig.PIWIK_TRACKER_URI,
                BuildKonfig.PIWIK_TRACKER_ID,
                trackerName
            )
        ).apply {
            // prevents piwik from creating cache files
            offlineCacheAge = -1
            setDispatchInterval(0)

            _trackingAllowed.value = !isOptOut
        }
    }

    fun allowTracking() {
        _trackingAllowed.value = true
        tracker.isOptOut = false

        Timber.d("Tracking allowed")
    }

    fun disallowTracking() {
        tracker.preferences.edit(commit = true) {
            clear()
        }
        tracker = initTracker()
    }

    fun trackScreen(path: String) {
        TrackHelper.track().screen(path).with(tracker)
    }
}

@Composable
fun TrackNavigationChanges(navController: NavHostController) {
    val tracker = LocalTracker.current

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            try {
                tracker.trackScreen(Uri.parse(it.destination.route).buildUpon().clearQuery().build().toString())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
