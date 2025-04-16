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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.asFhirLocalTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonPrimitive

private val CommonPharmacyTimes: Map<String, FhirTemporal.LocalTime> by lazy {
    mapOf(
        "00:00:00" to FhirTemporal.LocalTime(LocalTime(0, 0, 0, 0)),
        "00:30:00" to FhirTemporal.LocalTime(LocalTime(0, 30, 0, 0)),
        "01:00:00" to FhirTemporal.LocalTime(LocalTime(1, 0, 0, 0)),
        "01:30:00" to FhirTemporal.LocalTime(LocalTime(1, 30, 0, 0)),
        "02:00:00" to FhirTemporal.LocalTime(LocalTime(2, 0, 0, 0)),
        "02:30:00" to FhirTemporal.LocalTime(LocalTime(2, 30, 0, 0)),
        "03:00:00" to FhirTemporal.LocalTime(LocalTime(3, 0, 0, 0)),
        "03:30:00" to FhirTemporal.LocalTime(LocalTime(3, 30, 0, 0)),
        "04:00:00" to FhirTemporal.LocalTime(LocalTime(4, 0, 0, 0)),
        "04:30:00" to FhirTemporal.LocalTime(LocalTime(4, 30, 0, 0)),
        "05:00:00" to FhirTemporal.LocalTime(LocalTime(5, 0, 0, 0)),
        "05:30:00" to FhirTemporal.LocalTime(LocalTime(5, 30, 0, 0)),
        "06:00:00" to FhirTemporal.LocalTime(LocalTime(6, 0, 0, 0)),
        "06:30:00" to FhirTemporal.LocalTime(LocalTime(6, 30, 0, 0)),
        "07:00:00" to FhirTemporal.LocalTime(LocalTime(7, 0, 0, 0)),
        "07:30:00" to FhirTemporal.LocalTime(LocalTime(7, 30, 0, 0)),
        "08:00:00" to FhirTemporal.LocalTime(LocalTime(8, 0, 0, 0)),
        "08:30:00" to FhirTemporal.LocalTime(LocalTime(8, 30, 0, 0)),
        "09:00:00" to FhirTemporal.LocalTime(LocalTime(9, 0, 0, 0)),
        "09:30:00" to FhirTemporal.LocalTime(LocalTime(9, 30, 0, 0)),
        "10:00:00" to FhirTemporal.LocalTime(LocalTime(10, 0, 0, 0)),
        "10:30:00" to FhirTemporal.LocalTime(LocalTime(10, 30, 0, 0)),
        "11:00:00" to FhirTemporal.LocalTime(LocalTime(11, 0, 0, 0)),
        "11:30:00" to FhirTemporal.LocalTime(LocalTime(11, 30, 0, 0)),
        "12:00:00" to FhirTemporal.LocalTime(LocalTime(12, 0, 0, 0)),
        "12:30:00" to FhirTemporal.LocalTime(LocalTime(12, 30, 0, 0)),
        "13:00:00" to FhirTemporal.LocalTime(LocalTime(13, 0, 0, 0)),
        "13:30:00" to FhirTemporal.LocalTime(LocalTime(13, 30, 0, 0)),
        "14:00:00" to FhirTemporal.LocalTime(LocalTime(14, 0, 0, 0)),
        "14:30:00" to FhirTemporal.LocalTime(LocalTime(14, 30, 0, 0)),
        "15:00:00" to FhirTemporal.LocalTime(LocalTime(15, 0, 0, 0)),
        "15:30:00" to FhirTemporal.LocalTime(LocalTime(15, 30, 0, 0)),
        "16:00:00" to FhirTemporal.LocalTime(LocalTime(16, 0, 0, 0)),
        "16:30:00" to FhirTemporal.LocalTime(LocalTime(16, 30, 0, 0)),
        "17:00:00" to FhirTemporal.LocalTime(LocalTime(17, 0, 0, 0)),
        "17:30:00" to FhirTemporal.LocalTime(LocalTime(17, 30, 0, 0)),
        "18:00:00" to FhirTemporal.LocalTime(LocalTime(18, 0, 0, 0)),
        "18:30:00" to FhirTemporal.LocalTime(LocalTime(18, 30, 0, 0)),
        "19:00:00" to FhirTemporal.LocalTime(LocalTime(19, 0, 0, 0)),
        "19:30:00" to FhirTemporal.LocalTime(LocalTime(19, 30, 0, 0)),
        "20:00:00" to FhirTemporal.LocalTime(LocalTime(20, 0, 0, 0)),
        "20:30:00" to FhirTemporal.LocalTime(LocalTime(20, 30, 0, 0)),
        "21:00:00" to FhirTemporal.LocalTime(LocalTime(21, 0, 0, 0)),
        "21:30:00" to FhirTemporal.LocalTime(LocalTime(21, 30, 0, 0)),
        "22:00:00" to FhirTemporal.LocalTime(LocalTime(22, 0, 0, 0)),
        "22:30:00" to FhirTemporal.LocalTime(LocalTime(22, 30, 0, 0)),
        "23:00:00" to FhirTemporal.LocalTime(LocalTime(23, 0, 0, 0)),
        "23:30:00" to FhirTemporal.LocalTime(LocalTime(23, 30, 0, 0))
    )
}

fun lookupTime(tm: JsonPrimitive?): FhirTemporal.LocalTime? =
    tm?.let { CommonPharmacyTimes[it.content] ?: it.asFhirLocalTime() }

fun lookupTime(tm: String?): FhirTemporal.LocalTime? =
    tm?.let { CommonPharmacyTimes[it] ?: FhirTemporal.LocalTime(LocalTime.parse(it)) }
