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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.idp.extension.issuerCommonName
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.api.model.UntrustedVauList
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class VauRepository(
    private val trustStoreLocalDataSource: TrustStoreLocalDataSource,
    private val remoteDataSource: VauRemoteDataSource,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO,
    private val config: TruststoreConfig
) {

    suspend fun <R> withUntrusted(
        block: suspend (pkiCerts: UntrustedCertList, ocspList: UntrustedOCSPList) -> R
    ): R = withContext(dispatchers) {
        val cached = loadUntrusted()

        if (cached == null) {
            Napier.d("GET PKI, VAU certs & OCSP from backend...")

            val (pkiCerts, ocspList) = loadFreshUntrustedFromBackend()

            Napier.d("...GET PKI, VAU certs & OCSP from backend was successful")

            block(pkiCerts, ocspList)
        } else {
            val (untrustedCertList, untrustedOCSPList) = cached
            block(untrustedCertList, untrustedOCSPList)
        }
    }

    private suspend fun loadUntrusted(): Pair<UntrustedCertList, UntrustedOCSPList>? =
        trustStoreLocalDataSource.loadUntrusted().firstOrNull()?.let { trustStoreErpModel ->
            Pair(
                Json.decodeFromString(UntrustedCertList.serializer(), trustStoreErpModel.certListJson),
                Json.decodeFromString(UntrustedOCSPList.serializer(), trustStoreErpModel.ocspListJson)
            )
        }

    private suspend fun loadFreshUntrustedFromBackend(): Pair<UntrustedCertList, UntrustedOCSPList> =
        coroutineScope {
            val pkiDeferred = async {
                remoteDataSource.loadPkiCertificates(config.trustAnchorName).getOrThrow()
            }
            val vauDeferred = async {
                remoteDataSource.loadVauCertificates().getOrThrow()
            }

            val ocspDeferred = async {
                val vauCerts = vauDeferred.await()
                val eeCert = vauCerts.responses.firstOrNull()
                    ?: error("No VAU EE certificate available")

                val issuerCn = eeCert.issuer.issuerCommonName()
                require(issuerCn.isNotEmpty()) {
                    "Issuer CN could not be determined from VAU certificate"
                }

                val serialNr = eeCert.serialNumber.toString()
                remoteDataSource
                    .loadOcspResponse(issuerCn, serialNr)
                    .getOrThrow()
            }

            val pkiCerts = pkiDeferred.await()
            val vauCerts = vauDeferred.await()
            val ocspList = ocspDeferred.await()

            val allCerts = pkiCerts.copy(eeCerts = vauCerts.responses)
            Pair(allCerts, ocspList)
        }

    private suspend fun loadVauAndOcsp(): Pair<UntrustedVauList, UntrustedOCSPList> =
        coroutineScope {
            val vauDeferred = async {
                remoteDataSource.loadVauCertificates().getOrThrow()
            }
            val ocspDeferred = async {
                val vauCerts = vauDeferred.await()
                val eeCert = vauCerts.responses.firstOrNull()
                    ?: error("No VAU EE certificate available")

                val issuerCn = eeCert.issuer.issuerCommonName()
                require(issuerCn.isNotEmpty()) {
                    "Issuer CN could not be determined from VAU certificate"
                }

                val serialNr = eeCert.serialNumber.toString()
                remoteDataSource
                    .loadOcspResponse(issuerCn, serialNr)
                    .getOrThrow()
            }

            val vauCerts = vauDeferred.await()
            val ocspList = ocspDeferred.await()

            vauCerts to ocspList
        }

    @Requirement(
        "A_25061#1",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "When the trust store is not valid we invalidate all the data sources and cannot call the fachdienst.",
        codeLines = 2
    )
    suspend fun invalidate() {
        trustStoreLocalDataSource.deleteAll()
    }

    suspend fun saveLists(certList: UntrustedCertList, ocspList: UntrustedOCSPList) {
        val certListJson = Json.encodeToString(UntrustedCertList.serializer(), certList)
        val ocspListJson = Json.encodeToString(UntrustedOCSPList.serializer(), ocspList)
        trustStoreLocalDataSource.saveCertificateAndOcspLists(certListJson, ocspListJson)
    }
}
