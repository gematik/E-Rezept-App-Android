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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import io.github.aakira.napier.Napier

class VauRepository(
    private val localDataSource: VauLocalDataSource,
    private val remoteDataSource: VauRemoteDataSource,
    private val dispatchers: DispatchProvider
) {
    /**
     * Catches all exceptions originating from [block], deletes the locally saved untrusted store and
     * rethrows the exception.
     */
    suspend fun <R> withUntrusted(block: suspend (UntrustedCertList, UntrustedOCSPList) -> R) =
        withContext(dispatchers.io) {
            val (untrustedCertList, untrustedOCSPList) = localDataSource.loadUntrusted() ?: run {
                Napier.d("GET cert & ocsp from backend...")

                val certsResult = async { remoteDataSource.loadCertificates() }
                val ocspResult = async { remoteDataSource.loadOcspResponses() }

                val certs = certsResult.await().getOrThrow()

                val ocsp = ocspResult.await().getOrThrow()

                Napier.d("...GET cert & ocsp from backend was successful")

                Pair(certs, ocsp)
            }

            block(untrustedCertList, untrustedOCSPList).also {
                localDataSource.saveLists(untrustedCertList, untrustedOCSPList)
            }
        }

    suspend fun invalidate() {
        localDataSource.deleteAll()
    }
}
