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

package de.gematik.ti.erp.app.analytics.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.mapper.ContentSquareScreenMapper
import de.gematik.ti.erp.app.analytics.model.TrackedEvent
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance

class Tracker(
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val contentSquareTracker: ContentSquareTracker,
    private val debugTracker: DebugTracker,
    private val isNonReleaseMode: Boolean,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @NonRestartableComposable
    @Composable
    @Suppress("ComposableNaming")
    fun computeScreenTrackingProperty(
        routeEnum: String
    ): String? {
        val mapper: ContentSquareScreenMapper by rememberInstance()
        return mapper.map(NavigationRouteNames.valueOf(routeEnum))
    }

    @Requirement(
        "O.Data_6#5",
        "O.Purp_4#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "...is recorded only after the user has given their consent."
    )
    @Requirement(
        "O.Purp_2#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Tracking of screen names for analytics purposes."
    )
    @Requirement(
        "A_24525#4",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Tracking of screen names after the user has given their consent"
    )
    @NonRestartableComposable
    @Composable
    @Suppress("ComposableNaming")
    fun trackScreen(
        screenName: String
    ) {
        LaunchedEffect(screenName) {
            withContext(dispatcher) {
                try {
                    if (isAnalyticsAllowedUseCase.invoke().first()) {
                        contentSquareTracker.track(screenName)
                        if (isNonReleaseMode) {
                            debugTracker.track(screenName)
                        }
                    } else {
                        Napier.d { "Analytics not allowed for screens" }
                    }
                } catch (e: Throwable) {
                    Napier.e { "error on tracking ${e.stackTraceToString()}" }
                }
            }
        }
    }

    suspend fun trackEvent(event: TrackedEvent) {
        withContext(dispatcher) {
            try {
                if (isAnalyticsAllowedUseCase.invoke().first()) {
                    contentSquareTracker.send(event.key to event.value)
                    if (isNonReleaseMode) {
                        debugTracker.send(event.key to event.value)
                    }
                } else {
                    Napier.d { "Analytics not allowed for events" }
                }
            } catch (e: Throwable) {
                Napier.e { "error on tracking ${e.stackTraceToString()}" }
            }
        }
    }
}
