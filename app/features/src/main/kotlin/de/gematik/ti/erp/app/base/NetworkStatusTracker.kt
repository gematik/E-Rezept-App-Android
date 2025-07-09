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

package de.gematik.ti.erp.app.base

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkStatusTracker(private val connectivityManager: ConnectivityManager) {

    val networkStatus: Flow<Boolean> = callbackFlow {

        // Perform an initial network status check and emit the result immediately
        val initialStatus = isNetworkAvailable()
        trySend(initialStatus)

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true) // Network is available
            }

            override fun onUnavailable() {
                trySend(false) // Network is lost
            }

            override fun onLost(network: Network) {
                trySend(false) // Network is lost
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(hasInternetCapability) // Send the current network status
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Emit only when the network status changes and on app start

    // Helper function to check the current network status
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    // public helper function to check the current network status
    companion object {
        fun NetworkStatusTracker.isNetworkAvailable(): Boolean {
            return this.isNetworkAvailable()
        }
    }
}
