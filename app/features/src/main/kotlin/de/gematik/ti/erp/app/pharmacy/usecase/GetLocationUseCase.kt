/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import de.gematik.ti.erp.app.permissions.isLocationPermissionGranted
import de.gematik.ti.erp.app.permissions.isLocationServiceEnabled
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

class GetLocationUseCase(
    private val context: Context
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): LocationResult =
        suspendCancellableCoroutine { continuation ->
            try {
                val isServiceEnabled = context.isLocationServiceEnabled()
                if (!isServiceEnabled) {
                    continuation.resume(LocationResult.ServiceDisabled, null)
                } else {
                    val isPermissionGranted = context.isLocationPermissionGranted()
                    if (!isPermissionGranted) {
                        continuation.resume(LocationResult.PermissionDenied, null)
                    } else {
                        val cancelTokenSource = CancellationTokenSource()

                        // client needs a permission check to work without crashing
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                        fusedLocationClient
                            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancelTokenSource.token)
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    continuation.resume(LocationResult.Success(location), null)
                                } else {
                                    continuation.resume(LocationResult.LocationNotFound, null)
                                }
                            }
                            .addOnFailureListener {
                                Napier.e { "Location error on suspension ${it.stackTraceToString()}" }
                                continuation.resume(LocationResult.LocationNotFound, null)
                            }
                    }
                }
            } catch (e: SecurityException) {
                Napier.e { "Location error ${e.stackTraceToString()}" }
                if (!continuation.isCancelled) {
                    continuation.cancel(e)
                }
            }
        }

    sealed interface LocationResult {
        data class Success(val location: Location) : LocationResult
        data object ServiceDisabled : LocationResult
        data object PermissionDenied : LocationResult
        data object LocationNotFound : LocationResult
    }
}
