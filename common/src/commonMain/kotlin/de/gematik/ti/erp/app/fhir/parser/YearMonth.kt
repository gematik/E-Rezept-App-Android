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

package de.gematik.ti.erp.app.fhir.parser

import de.gematik.ti.erp.app.utils.FhirYearMonthRegex
import de.gematik.ti.erp.app.utils.FhirYearRegex

// just support sane values
private const val YearMin = 1000
private const val YearMax = 9999
private const val MonthMin = 1
private const val MonthMax = 12

data class YearMonth(val year: Int, val monthNumber: Int) {
    init {
        require(year in YearMin..YearMax)
        require(monthNumber in MonthMin..MonthMax)
    }

    override fun toString(): String = "%d-%02d".format(year, monthNumber)

    companion object {
        fun parse(value: String) =
            requireNotNull(
                FhirYearMonthRegex.matchEntire(value)?.let {
                    val year = requireNotNull(it.groups["year"]) { "`$value` missing field `year`" }
                    val month = requireNotNull(it.groups["month"]) { "`$value` missing field `month`" }
                    YearMonth(
                        year = year.value.toInt(),
                        monthNumber = month.value.toInt()
                    )
                }
            ) { "`$value` doesn't match the pattern `YYYY-MM`" }
    }
}

data class Year(val year: Int) {
    init {
        require(year in YearMin..YearMax)
    }

    override fun toString(): String = "%d".format(year)

    companion object {
        fun parse(value: String) =
            requireNotNull(
                FhirYearRegex.matchEntire(value)?.let {
                    val year = requireNotNull(it.groups["year"]) { "`$value` missing field `year`" }
                    Year(
                        year = year.value.toInt()
                    )
                }
            ) { "`$value` doesn't match the pattern `YYYY`" }
    }
}
