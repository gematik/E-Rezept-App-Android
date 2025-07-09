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
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkStatusTrackerTest {

    private lateinit var networkStatusTracker: NetworkStatusTracker
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        networkStatusTracker = NetworkStatusTracker(connectivityManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `network status emits true when network is available`() = runTest {
        val networkCallbackSlot = slot<ConnectivityManager.NetworkCallback>()

        // Capture the callback when `registerNetworkCallback` is called
        every {
            connectivityManager.registerDefaultNetworkCallback(capture(networkCallbackSlot))
        } answers {
            // Simulate network available
            networkCallbackSlot.captured.onAvailable(mockk())
        }

        networkStatusTracker.networkStatus.test {
            awaitItem() // initial await from ´initialStatus´
            assertEquals(true, awaitItem()) // Expect `true` because network is available
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network status emits false when network is lost`() = runTest {
        val networkCallbackSlot = slot<ConnectivityManager.NetworkCallback>()

        every {
            connectivityManager.registerDefaultNetworkCallback(capture(networkCallbackSlot))
        } answers {
            // Simulate network lost
            networkCallbackSlot.captured.onLost(mockk())
        }

        networkStatusTracker.networkStatus.test {
            assertEquals(false, awaitItem()) // Expect `false` because network is lost
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network status updates onCapabilitiesChanged`() = runTest {
        val networkCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        val networkCapabilities = mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        }

        every {
            connectivityManager.registerDefaultNetworkCallback(capture(networkCallbackSlot))
        } answers {
            // Simulate change in network capabilities
            networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), networkCapabilities)
        }

        networkStatusTracker.networkStatus.test {
            awaitItem() // initial await from ´initialStatus´
            assertEquals(true, awaitItem()) // Expect `true` because capability changed
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `callback is unregistered when flow collection stops`() = runTest {
        val networkCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        val mockNetwork = mockk<Network>()
        val mockNetworkCapabilities = mockk<NetworkCapabilities>()

        every { connectivityManager.activeNetwork } returns mockNetwork
        every { connectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        every {
            connectivityManager.registerDefaultNetworkCallback(capture(networkCallbackSlot))
        } answers {
            // Simulate successful callback registration
        }

        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

        val job = launch { networkStatusTracker.networkStatus.collect {} }

        advanceUntilIdle() // Ensures the callback gets registered before cancellation

        job.cancel()
        advanceUntilIdle() // Ensures cancellation is fully processed

        assert(networkCallbackSlot.isCaptured) { "Expected network callback to be registered" }

        verify { connectivityManager.unregisterNetworkCallback(networkCallbackSlot.captured) }
    }
}
