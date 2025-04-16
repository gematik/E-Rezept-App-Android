/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import de.gematik.ti.erp.app.analytics.tracker.EVENT_TRACKED
import de.gematik.ti.erp.app.analytics.usecase.GetDebugTrackingSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DebugTrackerScreenEvent {
    class ScreenEvent(val value: String) : DebugTrackerScreenEvent
    class DynamicEvent(val key: String, val value: String) : DebugTrackerScreenEvent
}

@Suppress("MagicNumber")
class DebugTrackerScreenViewModel(
    private val getDebugTrackingSessionUseCase: GetDebugTrackingSessionUseCase
) : ViewModel() {

    private val _session = MutableStateFlow<List<DebugTrackerScreenEvent>>(emptyList())

    init {
        viewModelScope.launch {
            _session.value = getDebugTrackingSessionUseCase.invoke().map { trackedValue ->
                if (trackedValue.contains(EVENT_TRACKED)) {
                    val splits = trackedValue.split(";")
                    if (splits.size == 3) {
                        DebugTrackerScreenEvent.DynamicEvent(splits[1], splits[2])
                    } else {
                        DebugTrackerScreenEvent.DynamicEvent("unknown", "unknown")
                    }
                } else {
                    DebugTrackerScreenEvent.ScreenEvent(trackedValue)
                }
            }
        }
    }

    val session: StateFlow<List<DebugTrackerScreenEvent>> = derived { get(_session) }
}
