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

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.StartTrackerUseCase
import de.gematik.ti.erp.app.analytics.usecase.StopTrackerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class AnalyticsSettingsController(
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val startTrackerUseCase: StartTrackerUseCase,
    private val stopTrackerUseCase: StopTrackerUseCase,
    private val scope: CoroutineScope
) {
    @Requirement(
        "O.Purp_5#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Set allow/disallow analytics state."
    )
    fun changeAnalyticsAllowedState(state: Boolean) {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(state)
            when {
                state -> startTrackerUseCase()
                else -> stopTrackerUseCase()
            }
        }
    }
}

@Composable
fun rememberAnalyticsSettingsController(): AnalyticsSettingsController {
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val startTrackerUseCase by rememberInstance<StartTrackerUseCase>()
    val stopTrackerUseCase by rememberInstance<StopTrackerUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        AnalyticsSettingsController(
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            startTrackerUseCase = startTrackerUseCase,
            stopTrackerUseCase = stopTrackerUseCase,
            scope = scope
        )
    }
}
