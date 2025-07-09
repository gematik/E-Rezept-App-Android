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

package de.gematik.ti.erp.app.fhir.parser

import de.gematik.ti.erp.app.utils.FhirYearMonthRegex
import de.gematik.ti.erp.app.utils.FhirYearRegex
import kotlinx.serialization.Serializable

// just support sane values
private const val YearMin = 1000
private const val YearMax = 9999
private const val MonthMin = 1
private const val MonthMax = 12

// todo: move to some other package
@Serializable
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

@Serializable
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
