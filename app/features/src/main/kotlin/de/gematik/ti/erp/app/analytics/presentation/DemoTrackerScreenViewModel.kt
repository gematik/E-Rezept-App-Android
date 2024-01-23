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

package de.gematik.ti.erp.app.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import de.gematik.ti.erp.app.analytics.usecase.GetDemoTrackingSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DemoTrackerScreenViewModel(
    private val getDemoTrackingSessionUseCase: GetDemoTrackingSessionUseCase
) : ViewModel() {

    private val mutableSession = MutableStateFlow<List<String>>(emptyList())
    init {
        viewModelScope.launch {
            mutableSession.value = getDemoTrackingSessionUseCase.invoke()
        }
    }

    val session: StateFlow<List<String>> = derived { get(mutableSession) }
}
