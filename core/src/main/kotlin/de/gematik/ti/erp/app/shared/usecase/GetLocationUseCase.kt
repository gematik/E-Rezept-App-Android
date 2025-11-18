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

package de.gematik.ti.erp.app.shared.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import de.gematik.ti.erp.app.permissions.isLocationPermissionGranted
import de.gematik.ti.erp.app.permissions.isLocationServiceEnabled
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await

class GetLocationUseCase(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(): Flow<LocationResult> = flow {
        // loading state
        emit(LocationResult.GettingLocation)

        if (!context.isLocationServiceEnabled()) {
            emit(LocationResult.ServiceDisabled)
            return@flow
        }

        if (!context.isLocationPermissionGranted()) {
            emit(LocationResult.PermissionDenied)
            return@flow
        }

        emit(requestLocation())
    }.catch { e ->
        emit(LocationResult.LocationSearchFailed(e))
    }.onCompletion {
        Napier.d { "Location search completed" } // required to ensure on completion is called
    }.flowOn(dispatcher)

    @SuppressLint("MissingPermission")
    private suspend fun requestLocation(): LocationResult {
        // client needs a permission check to work without crashing
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return try {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                LocationResult.Success(lastLocation)
            } else {
                // get new location only when we cannot find last location, since this takes time
                requestNewLocation(fusedLocationClient)
            }
        } catch (e: Exception) {
            Napier.e { "Location error: ${e.stackTraceToString()}" }
            LocationResult.LocationSearchFailed(e)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestNewLocation(
        fusedLocationClient: FusedLocationProviderClient
    ): LocationResult {
        val cancelTokenSource = CancellationTokenSource()
        return try {
            val location = fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancelTokenSource.token)
                .await()
            if (location != null) {
                LocationResult.Success(location)
            } else {
                LocationResult.LocationNotFound
            }
        } catch (e: Exception) {
            Napier.e { "Location error: ${e.stackTraceToString()}" }
            LocationResult.LocationSearchFailed(e)
        } finally {
            cancelTokenSource.cancel()
        }
    }

    sealed interface LocationResult {
        data object GettingLocation : LocationResult
        data class Success(val location: Location) : LocationResult
        data object ServiceDisabled : LocationResult
        data object PermissionDenied : LocationResult
        data object LocationNotFound : LocationResult
        data class LocationSearchFailed(val e: Throwable) : LocationResult
    }
}
