/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.asLocalTime
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalTime

private val CommonPharmacyTimes: Map<String, LocalTime> by lazy {
    mapOf(
        "00:00:00" to LocalTime.of(0, 0),
        "00:30:00" to LocalTime.of(0, 30),
        "01:00:00" to LocalTime.of(1, 0),
        "01:30:00" to LocalTime.of(1, 30),
        "02:00:00" to LocalTime.of(2, 0),
        "02:30:00" to LocalTime.of(2, 30),
        "03:00:00" to LocalTime.of(3, 0),
        "03:30:00" to LocalTime.of(3, 30),
        "04:00:00" to LocalTime.of(4, 0),
        "04:30:00" to LocalTime.of(4, 30),
        "05:00:00" to LocalTime.of(5, 0),
        "05:30:00" to LocalTime.of(5, 30),
        "06:00:00" to LocalTime.of(6, 0),
        "06:30:00" to LocalTime.of(6, 30),
        "07:00:00" to LocalTime.of(7, 0),
        "07:30:00" to LocalTime.of(7, 30),
        "08:00:00" to LocalTime.of(8, 0),
        "08:30:00" to LocalTime.of(8, 30),
        "09:00:00" to LocalTime.of(9, 0),
        "09:30:00" to LocalTime.of(9, 30),
        "10:00:00" to LocalTime.of(10, 0),
        "10:30:00" to LocalTime.of(10, 30),
        "11:00:00" to LocalTime.of(11, 0),
        "11:30:00" to LocalTime.of(11, 30),
        "12:00:00" to LocalTime.of(12, 0),
        "12:30:00" to LocalTime.of(12, 30),
        "13:00:00" to LocalTime.of(13, 0),
        "13:30:00" to LocalTime.of(13, 30),
        "14:00:00" to LocalTime.of(14, 0),
        "14:30:00" to LocalTime.of(14, 30),
        "15:00:00" to LocalTime.of(15, 0),
        "15:30:00" to LocalTime.of(15, 30),
        "16:00:00" to LocalTime.of(16, 0),
        "16:30:00" to LocalTime.of(16, 30),
        "17:00:00" to LocalTime.of(17, 0),
        "17:30:00" to LocalTime.of(17, 30),
        "18:00:00" to LocalTime.of(18, 0),
        "18:30:00" to LocalTime.of(18, 30),
        "19:00:00" to LocalTime.of(19, 0),
        "19:30:00" to LocalTime.of(19, 30),
        "20:00:00" to LocalTime.of(20, 0),
        "20:30:00" to LocalTime.of(20, 30),
        "21:00:00" to LocalTime.of(21, 0),
        "21:30:00" to LocalTime.of(21, 30),
        "22:00:00" to LocalTime.of(22, 0),
        "22:30:00" to LocalTime.of(22, 30),
        "23:00:00" to LocalTime.of(23, 0),
        "23:30:00" to LocalTime.of(23, 30)
    )
}

fun lookupTime(tm: JsonPrimitive?): LocalTime? =
    tm?.let { CommonPharmacyTimes[it.content] ?: it.asLocalTime() }
