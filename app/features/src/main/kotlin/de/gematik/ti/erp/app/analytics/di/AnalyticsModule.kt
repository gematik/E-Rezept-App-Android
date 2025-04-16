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

package de.gematik.ti.erp.app.analytics.di

import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.analytics.mapper.ContentSquareScreenMapper
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenViewModel
import de.gematik.ti.erp.app.analytics.tracker.ContentSquareTracker
import de.gematik.ti.erp.app.analytics.tracker.DebugTracker
import de.gematik.ti.erp.app.analytics.tracker.DebugTrackerSession
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCase
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.GetDebugTrackingSessionUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.analytics.usecase.StartTrackerUseCase
import de.gematik.ti.erp.app.analytics.usecase.StopTrackerUseCase
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val analyticsModule = DI.Module("analyticsModule") {
    bindProvider { IsAnalyticsAllowedUseCase(instance()) }
    bindProvider { ChangeAnalyticsStateUseCase(instance()) }
    bindProvider { AnalyticsUseCase(instance()) }
    bindProvider { StartTrackerUseCase(instance()) }
    bindProvider { StopTrackerUseCase(instance()) }

    bindSingleton { Analytics(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { DebugTrackerSession() }

    bindProvider { ContentSquareScreenMapper() }
    bindProvider {
        val isNonReleaseMode = BuildConfigExtension.isNonReleaseMode
        Tracker(instance(), instance(), instance(), isNonReleaseMode)
    }
    bindProvider { ContentSquareTracker() }
    bindProvider { DebugTracker(instance()) }
    bindProvider { GetDebugTrackingSessionUseCase(instance()) }
    bindProvider { DebugTrackerScreenViewModel(instance()) }
}
