/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class VauRepository @Inject constructor(
    private val localDataSource: VauLocalDataSource,
    private val remoteDataSource: VauRemoteDataSource,
    private val dispatchProvider: DispatchProvider
) {
    /**
     * Catches all exceptions originating from [block], deletes the locally saved untrusted store and
     * rethrows the exception.
     */
    suspend fun <R> withUntrusted(block: suspend (UntrustedCertList, UntrustedOCSPList) -> R) =
        withContext(dispatchProvider.io()) {
            val (untrustedCertList, untrustedOCSPList) = localDataSource.loadUntrusted() ?: run {
                Timber.d("GET cert & ocsp from backend...")

                val certsResult = async { remoteDataSource.loadCertificates() }
                val ocspResult = async { remoteDataSource.loadOcspResponses() }

                val certs = certsResult.await().getOrThrow()

                val ocsp = ocspResult.await().getOrThrow()

                Timber.d("...GET cert & ocsp from backend was successful")

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
