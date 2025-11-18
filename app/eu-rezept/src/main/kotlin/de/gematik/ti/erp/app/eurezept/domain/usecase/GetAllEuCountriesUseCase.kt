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

package de.gematik.ti.erp.app.eurezept.domain.usecase

import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.filter

class GetAllEuCountriesUseCase(
    private val repository: EuRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): List<Country> = withContext(dispatcher) {
        repository.fetchAvailableCountries().fold(
            onSuccess = { fhirModel ->
                val countryList = fhirModel as? FhirCountryErpModelCollection
                countryList?.countries?.map { country ->
                    val code = country.code.orEmpty()
                    Country(
                        name = country.name ?: "",
                        code = code,
                        flagEmoji = countryCodeToFlag(code)
                    )
                } ?: emptyList()
            },
            onFailure = { error ->
                Napier.e { "Error fetching available eu counties Error: ${error.javaClass.simpleName} - ${error.message.orEmpty()}" }
                throw error
            }
        )
    }

    fun filterCountries(countries: List<Country>, query: String): List<Country> {
        if (query.isEmpty()) return countries

        return countries.filter { country ->
            country.name.contains(query, ignoreCase = true) ||
                country.code.contains(query, ignoreCase = true)
        }
    }

    private fun countryCodeToFlag(countryCode: String): String {
        if (countryCode.length != 2) return countryCode
        val upper = countryCode.uppercase()
        val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}
