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

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallRaw
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.api.model.UntrustedVauList
import io.github.aakira.napier.Napier
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPResp
import java.io.IOException

class VauRemoteDataSource(
    private val service: VauService
) {
    // New endpoints wrappers
    suspend fun loadPkiCertificates(currentRoot: String = "GEM.RCA3"): Result<UntrustedCertList> =
        safeApiCall("Failed to GET PKI certificates") { service.getPkiCertList(currentRoot) }

    suspend fun loadVauCertificates(): Result<UntrustedVauList> =
        safeApiCallRaw("Failed to GET VAU certificates") {
            val response = service.getVauCertList()
            if (response.isSuccessful) {
                val body = response.body() ?: return@safeApiCallRaw Result.failure(IOException("Empty VAU certificate body"))
                return@safeApiCallRaw try {
                    val bytes = body.bytes()
                    // The endpoint returns a DER-encoded certificate. Parse to X509CertificateHolder
                    val holder = org.bouncycastle.cert.X509CertificateHolder(bytes)
                    Result.success(
                        UntrustedVauList(
                            responses = listOf(holder)
                        )
                    )
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            } else {
                Result.failure(ApiCallException("Error executing VAU certificates api call ${response.code()} ${response.message()}", response))
            }
        }

    // A_21216: The FD MUST provide a max 12h old OCSP response for the VAU certificate at /OCSPResponse.
    // Some backends expose this path without parameters. The response body is the OCSP response (DER or Base64-encoded DER).
    suspend fun loadOcspResponse(issuerCn: String, serialNr: String): Result<UntrustedOCSPList> =
        safeApiCallRaw("Failed to GET ocsp response") {
            val response = service.getOcspResponseRaw(issuerCn, serialNr)
            if (response.isSuccessful) {
                val body = response.body() ?: return@safeApiCallRaw Result.failure(IOException("Empty OCSP body"))
                val raw = body.bytes()

                val derBytes = toDerBytes(raw)

                val ocsp = OCSPResp(derBytes)
                // Optional: basic sanity logging to aid debugging in non-production logs
                try {
                    parseOcspResponse(derBytes)
                } catch (_: Throwable) {
                }
                Result.success(UntrustedOCSPList(responses = listOf(ocsp)))
            } else {
                Result.failure(ApiCallException("Error executing ocsp api call ${response.code()} ${response.message()}", response))
            }
        }
}

private fun isDerEncodedAsn1(bytes: ByteArray): Boolean {
    // DER-encoded ASN.1 usually starts with 0x30 (SEQUENCE)
    return bytes.isNotEmpty() && bytes[0] == 0x30.toByte()
}

private fun isLikelyBase64(bytes: ByteArray): Boolean {
    if (bytes.isEmpty()) return false
    // Quick check: printable ASCII and padding '='; skip CR/LF
    var count = 0
    for (b in bytes) {
        val c = b.toInt() and 0xFF
        if (c == '\r'.code || c == '\n'.code) continue
        val isBase64Char =
            (c in 'A'.code..'Z'.code) || (c in 'a'.code..'z'.code) || (c in '0'.code..'9'.code) || c == '+'.code || c == '/'.code || c == '='.code
        if (!isBase64Char) return false
        if (c != '\r'.code && c != '\n'.code) count++
    }
    // Base64 length should be multiple of 4 (ignoring whitespace)
    return count % 4 == 0
}

private fun toDerBytes(raw: ByteArray): ByteArray = try {
    if (!isDerEncodedAsn1(raw) && isLikelyBase64(raw)) org.bouncycastle.util.encoders.Base64.decode(raw) else raw
} catch (_: Throwable) {
    raw
}

fun parseOcspResponse(derBytes: ByteArray) {
    val ocspResp = OCSPResp(derBytes)

    Napier.d("OCSP status: ${ocspResp.status}") // 0 = successful, others = errors

    val responseObject = ocspResp.responseObject
    if (responseObject is BasicOCSPResp) {
        val basic = responseObject

        // Iterate over single responses (one per certificate usually)
        for (single in basic.responses) {
            val certId = single.certID
            val status = single.certStatus
            val thisUpdate = single.thisUpdate
            val nextUpdate = single.nextUpdate

            Napier.d("Cert serial: ${certId.serialNumber}")
            Napier.d("Status: $status")
            Napier.d("thisUpdate: $thisUpdate")
            Napier.d("nextUpdate: $nextUpdate")
        }
    } else {
        Napier.d("Unexpected OCSP response type: ${responseObject?.javaClass}")
    }
}
