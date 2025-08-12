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

import de.gematik.ti.erp.app.analytics.mapper.ContentSquareEventMapper
import de.gematik.ti.erp.app.analytics.model.TrackedParameter
import io.github.aakira.napier.Napier

const val EVENT_TRACKED = "EventTracked"

class DebugTracker(
    private val session: DebugTrackerSession
) {

    internal fun track(parameter: TrackedParameter) {
        when (parameter) {
            is TrackedParameter.Metric -> track(parameter.value.key to parameter.value.value)
            is TrackedParameter.Screen -> track(parameter.name)
        }
    }

    private fun track(screenName: String) {
        session.addScreen(screenName)
    }

    private fun track(event: Pair<ContentSquareEventMapper, Any>) {
        Napier.i { "Debug tracker send eventName=${event.first.trackingParameter} | ${event.second}" }
        session.addScreen("$EVENT_TRACKED;${event.first.trackingParameter};${event.second}")
    }
}
