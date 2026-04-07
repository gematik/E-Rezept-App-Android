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

package de.gematik.ti.erp.app.analytics.di

import de.gematik.ti.erp.app.analytics.CardCommunicationAnalytics
import de.gematik.ti.erp.app.analytics.mapper.TrackingScreenMapper
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenViewModel
import de.gematik.ti.erp.app.analytics.tracker.DebugTracker
import de.gematik.ti.erp.app.analytics.tracker.LocalTrackerSession
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.analytics.usecase.GetDebugTrackingSessionUseCase
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val cardCommunicationAnalyticsModule = DI.Module("analyticsModule") {

    bindSingleton { CardCommunicationAnalytics(instance(), instance()) }
    bindSingleton { LocalTrackerSession() }

    bindProvider { TrackingScreenMapper() }
    bindProvider {
        val isNonReleaseMode = BuildConfigExtension.isInternalDebug
        Tracker(instance(), isNonReleaseMode)
    }
    bindProvider { DebugTracker(instance()) }
    bindProvider { GetDebugTrackingSessionUseCase(instance()) }
    bindProvider { DebugTrackerScreenViewModel(instance()) }
}
