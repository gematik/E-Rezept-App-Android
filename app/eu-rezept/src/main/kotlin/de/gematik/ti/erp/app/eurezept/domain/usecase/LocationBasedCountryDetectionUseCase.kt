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

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import kotlin.collections.find

class LocationBasedCountryDetectionUseCase(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun detectCountryFromLocation(
        location: Location,
        euCountries: List<Country>
    ): Flow<CountryDetectionResult> = flow {
        emit(performCountryDetection(location, euCountries))
    }.catch { e ->
        emit(CountryDetectionResult.CountryDetectionFailed(e))
    }.flowOn(dispatcher)

    private fun performCountryDetection(
        location: Location,
        euCountries: List<Country>
    ): CountryDetectionResult {
        return try {
            if (!Geocoder.isPresent()) {
                return CountryDetectionResult.GeocoderNotAvailable
            }

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            val countryCode = addresses?.firstOrNull()?.countryCode
            if (countryCode != null) {
                val detectedCountry = euCountries.find {
                    it.code.equals(countryCode, ignoreCase = true)
                }
                if (detectedCountry != null) {
                    CountryDetectionResult.Success(detectedCountry)
                } else {
                    CountryDetectionResult.CountryNotInEU(countryCode)
                }
            } else {
                CountryDetectionResult.CountryNotFound
            }
        } catch (e: Exception) {
            Napier.e { "Geocoding error: ${e.stackTraceToString()}" }
            CountryDetectionResult.CountryDetectionFailed(e)
        }
    }

    sealed interface CountryDetectionResult {
        data class Success(val country: Country) : CountryDetectionResult
        data object GeocoderNotAvailable : CountryDetectionResult
        data object CountryNotFound : CountryDetectionResult
        data class CountryNotInEU(val countryCode: String) : CountryDetectionResult
        data class CountryDetectionFailed(val e: Throwable) : CountryDetectionResult
    }
}
