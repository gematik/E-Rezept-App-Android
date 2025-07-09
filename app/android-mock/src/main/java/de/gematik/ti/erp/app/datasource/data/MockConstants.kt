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

package de.gematik.ti.erp.app.datasource.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

object MockConstants {
    internal val fixedTime = Instant.parse("2021-11-25T15:20:00Z")
    internal val longerRandomTimeToday = Instant.parse("2021-11-25T15:20:00Z")
    internal const val SYNCED_TASK_PRESET = "110.000.002.345.863"
    internal val NOW = Clock.System.now()
    internal val EXPIRY_DATE = Clock.System.now().plus(200.days)
    internal val SHORT_EXPIRY_DATE = Clock.System.now().plus(20.days)
    internal const val MOCK_COMMUNICATION_ID_01 = "CID-123-001"
}
