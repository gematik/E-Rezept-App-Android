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

package de.gematik.ti.erp.app.demomode.datasource.data

import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object DemoConstants {
    internal val randomTimeToday = Clock.System.now().minus((1..20).random().minutes)
    internal val longerRandomTimeToday = Clock.System.now().minus((2..58).random().minutes)
    internal const val PHARMACY_TELEMATIK_ID = "3-03.2.1006210000.10.795"
    internal const val SYNCED_TASK_PRESET = "110.000.002.345.863"
    internal val NOW = Clock.System.now()
    internal val START_DATE = Clock.System.now().minus(5.minutes)
    internal val EXPIRY_DATE = Clock.System.now().plus(200.days)
    internal val SHORT_EXPIRY_DATE = Clock.System.now().plus(20.days)
}
