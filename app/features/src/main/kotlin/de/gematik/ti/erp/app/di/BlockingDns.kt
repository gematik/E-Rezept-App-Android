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

package de.gematik.ti.erp.app.di

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * A custom [Dns] implementation used to prevent connections to known telemetry and analytics
 * domains, such as Firebase and Google Play Services logging endpoints.
 *
 * ## Purpose
 * This class is designed to block runtime connections to specific domains associated with
 * Firebase services that may log or transmit analytics, remote configuration, or diagnostic
 * information — even when such features are not explicitly used in the app.
 *
 * Blocking these domains helps to:
 * - Minimize unintended background telemetry
 * - Enforce stricter privacy controls in internal or debug builds
 * - Prevent misconfigured third-party libraries from leaking metadata
 * - Align with gematik’s data minimization and security requirements
 *
 * ## Behavior
 * - If a hostname matches any of the configured blocked domains, a [UnknownHostException]
 *   is thrown to cancel the DNS resolution.
 * - Otherwise, the request is delegated to the system default [Dns] implementation.
 *
 * ## Limitations
 * - This does not prevent the SDKs from initializing internal components.
 * - Blocking telemetry may cause some SDK features (e.g., ML Kit, Remote Config) to fallback,
 *   delay, or fail silently.
 * - This should be used with caution in production builds unless thoroughly tested.
 *
 * ## Recommended Usage
 * This implementation is typically registered in an [OkHttpClient] builder:
 *
 * ```kotlin
 * OkHttpClient.Builder()
 *     .dns(BlockingDns())
 *     .build()
 * ```
 *
 * @see okhttp3.Dns
 * @see java.net.UnknownHostException
 */
internal class BlockingDns : Dns {
    private val blockedDomainSuffixes = listOf(
        ".firebaseinstallations.googleapis.com",
        ".firebaselogging.googleapis.com",
        ".firebaseremoteconfig.googleapis.com"
    )

    override fun lookup(hostname: String): List<InetAddress> {
        if (blockedDomainSuffixes.any { hostname.endsWith(it) }) {
            throw UnknownHostException("Blocked by custom DNS: $hostname")
        }
        return Dns.SYSTEM.lookup(hostname)
    }
}
