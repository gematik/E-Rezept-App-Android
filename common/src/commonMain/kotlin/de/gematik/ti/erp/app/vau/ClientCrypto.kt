/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.vau

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.util.Locale
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/*
VAU steps:

A.1 receive http response from fhir client
A.1.1 get long-term cert from vau or keystore
A.1.2 validate cert with vau ocsp
A.2 encrypt http req and a random AES key with ECIES as payload for http request to vau
A.3 send vau http request

B.1 get response from vau
B.2 decrypt vau response payload with AES key from A.2
B.3 pass http response to fhir client

C.1 goto A.1
*/

private val defaultContentType = "application/octet-stream".toMediaTypeOrNull()
private const val byteSpace: Byte = 32

/**
 * Trusted execution environment channel specifications according to `gemSpec_Krypt 7`.
 */
class VauChannelSpec constructor(
    /**
     * Version byte. E.g. `'1'.toByte()`.
     */
    val version: Byte,
    /**
     * Request id size in bytes.
     */
    val requestIdSize: Int,
    /**
     * Symmetrical decryption key size in bytes.
     */
    val decryptionKeySize: Int,

    val specEcies: VauEciesSpec,
    val specAesGcm: VauAesGcmSpec
) {
    /**
     * Raw request data holding the previously used request id (hex encoded) and the decryption key.
     * The payload is the actual encrypted inner request to the VAU.
     */
    class RawRequestData(
        val requestIdHex: ByteArray,
        val decryptionKey: SecretKey,
        val payload: ByteArray
    )

    /**
     * Encrypts a byte array as the inner request.
     */
    fun encryptRawVauRequest(
        innerHttp: ByteArray,

        bearer: ByteArray,
        publicKey: ECPublicKey,

        cryptoConfig: VauCryptoConfig = defaultCryptoConfig
    ): RawRequestData {
        val decryptionKey = KeyGenerator.getInstance("AES", cryptoConfig.provider).apply {
            init(this@VauChannelSpec.decryptionKeySize * 8)
        }.generateKey()

        val requestId = ByteArray(this.requestIdSize).apply {
            SecureRandom().nextBytes(this)
        }

        return encryptRawVauRequest(
            innerHttp = innerHttp,
            bearer = bearer,
            publicKey = publicKey,
            requestId = requestId,
            decryptionKey = decryptionKey,
            cryptoConfig = cryptoConfig
        )
    }

    /**
     * Encrypt raw request data as the inner request.
     */
    fun encryptRawVauRequest(
        innerHttp: ByteArray,

        bearer: ByteArray,
        publicKey: ECPublicKey,

        requestId: ByteArray,
        decryptionKey: SecretKey,

        cryptoConfig: VauCryptoConfig = defaultCryptoConfig
    ): RawRequestData {
        val symmetricalKeyHex = decryptionKey.encoded!!.toLowerCaseHex()
        val requestIdHex = requestId.toLowerCaseHex()
        val composedInnerHttp =
            composeInnerHttp(innerHttp, this.version, bearer, requestIdHex, symmetricalKeyHex)

        return RawRequestData(
            requestIdHex = requestIdHex,
            decryptionKey = decryptionKey,
            payload = Ecies.encrypt(publicKey, specEcies, composedInnerHttp, cryptoConfig)
        )
    }

    private fun composeInnerHttp(
        innerHttp: ByteArray,
        version: Byte,
        bearer: ByteArray,
        requestId: ByteArray,
        symmetricalKey: ByteArray
    ) =
        ByteArray(5 + bearer.size + requestId.size + symmetricalKey.size + innerHttp.size).apply {
            this[0] = version
            this[1] = byteSpace
            bearer.copyInto(this, 2)
            this[2 + bearer.size] = byteSpace
            requestId.copyInto(this, 3 + bearer.size)
            this[3 + bearer.size + requestId.size] = byteSpace
            symmetricalKey.copyInto(this, 4 + bearer.size + requestId.size)
            this[4 + bearer.size + requestId.size + symmetricalKey.size] = byteSpace
            innerHttp.copyInto(this, 5 + bearer.size + requestId.size + symmetricalKey.size)
        }

    /**
     * Decrypt raw response data.
     */
    fun decryptRawVauResponse(
        encryptedInnerHttp: ByteArray,
        decryptionKey: SecretKey,
        cryptoConfig: VauCryptoConfig = defaultCryptoConfig
    ): ByteArray =
        AesGcm.decrypt(decryptionKey, specAesGcm, encryptedInnerHttp, cryptoConfig)

    /**
     * Returns the minimum response size in bytes assuming the `request id` to be hex encoded.
     * This includes the space separating the `header` from the actual encrypted body.
     */
    val minResponseSize: Int = 3 + requestIdSize * 2

    /**
     * Encrypts an okhttp [Request] and wraps it within a new outer request.
     * The outer request points to the location `$baseUrl/$userpseudonym`.
     * The bearer token is extracted from the authorization header of the [innerRequest]
     * and is required to be prefixed with `Bearer`; i.e. `Authorization = Bearer ab12cd34d42fs324`.
     *
     * @return the encrypted request and [RawRequestData] of the actual encryption process.
     */
    fun encryptHttpRequest(
        innerRequest: Request,
        userpseudonym: String,
        publicKey: ECPublicKey,
        baseUrl: HttpUrl,

        cryptoConfig: VauCryptoConfig = defaultCryptoConfig
    ): Pair<Request, RawRequestData> {
        val bearer = requireNotNull(
            innerRequest.header("Authorization")
                ?.takeIf { it.startsWith("Bearer") }
        )
            .removePrefix("Bearer")
            .trim()

        val payload = innerRequest.toRawVauInnerHttpRequest(baseUrl)

        val encryptedRawRequest = encryptRawVauRequest(
            innerHttp = payload,
            bearer = bearer.encodeToByteArray(),
            publicKey = publicKey,
            cryptoConfig = cryptoConfig
        )

        val body = encryptedRawRequest.payload.toRequestBody(defaultContentType)

        return Pair(
            Request.Builder()
                .url(requireNotNull(baseUrl.resolve("VAU/$userpseudonym")))
                .post(body)
                .header("Content-Length", body.contentLength().toString())
                .build(),
            encryptedRawRequest
        )
    }

    /**
     * Decrypts a response from the VAU containing the encrypted inner response as payload.
     * The [previousInnerRequest] is only required to match the decrypted response with its previous request.
     *
     * Additional checks include the minimum length of the decrypted inner response and
     * that the request id matches with its request.
     *
     * @return the decrypted inner response with the user pseudonym.
     */
    fun decryptHttpResponse(
        outerResponse: Response,
        previousInnerRequest: Request,

        rawRequestData: RawRequestData,

        cryptoConfig: VauCryptoConfig = defaultCryptoConfig
    ): Pair<Response, String?> {
        require(outerResponse.isSuccessful)
        val body = requireNotNull(outerResponse.body) { "VAU response body empty" }
        require(body.contentType() == defaultContentType) { "VAU response body has wrong content type" }

        val userpseudonym = outerResponse.header("Userpseudonym")

        val p = decryptRawVauResponse(
            encryptedInnerHttp = body.bytes(),
            decryptionKey = rawRequestData.decryptionKey,
            cryptoConfig = cryptoConfig
        )

        require(p.size >= this.minResponseSize)

        require(p[0] == this.version)
        require(
            p.copyOfRange(2, this.minResponseSize - 1)
                .contentEquals(rawRequestData.requestIdHex)
        ) { "VAU response contains wrong request id" }

        val innerResponse = p.copyOfRange(this.minResponseSize, p.size)

        return Pair(innerResponse.toVauInnerHttpResponse(previousInnerRequest), userpseudonym)
    }

    companion object {
        @JvmField
        val V1 = VauChannelSpec(
            version = '1'.code.toByte(),
            requestIdSize = 16,
            decryptionKeySize = 16,
            specEcies = VauEciesSpec.V1,
            specAesGcm = VauAesGcmSpec.V1
        )
    }
}

// http

/**
 * Create a raw http request according to rfc2616 from a okhttp [Request].
 *
 * This request path will be transformed according to the following:
 *
 * [baseUrl] path: `.../VAU/`
 * [this] path: `.../VAU/Task/123`
 *
 * resulting path: `/Task/123`
 *
 * Throws an exception if [baseUrl] doesn't contain a trailing `/` or [this] doesn't contain the [baseUrl].
 */
fun Request.toRawVauInnerHttpRequest(
    baseUrl: HttpUrl,
    protocol: Protocol = Protocol.HTTP_1_1
): ByteArray =
    this.let { req ->
        require(baseUrl.querySize == 0)
        require(baseUrl.fragment == null)
        require(baseUrl.pathSegments.last() == "") // trailing `/`

        val urlEncoded = req.url.toString()
        val baseUrlEncoded = baseUrl.toString()
        require(urlEncoded.startsWith(baseUrlEncoded))

        val urlWithoutBase = "/" + urlEncoded.removePrefix(baseUrlEncoded)

        Buffer().apply {
            // request line
            writeUtf8("${req.method} $urlWithoutBase ${protocol.toString().uppercase(Locale.getDefault())}\r\n")
            // host
            writeUtf8("Host: ${url.host}\r\n")
            // other headers
            req.headers.forEach { h ->
                writeUtf8("${h.first}: ${h.second}\r\n")
            }
            writeUtf8("Content-Length: ${req.body?.contentLength() ?: 0 }\r\n")
            // body separation
            writeUtf8("\r\n")
            // body if present
            req.body?.writeTo(this)
        }.readByteArray()
    }

private fun Response.Builder.parseResponseLine(l: String): Response.Builder =
    l.split(" ", limit = 3).let {
        require(it.size == 3) { "Invalid status line!" }

        this.protocol(Protocol.get(it[0].lowercase())).code(it[1].toInt()).message(it[2])
    }

/**
 * Creates an okhttp [Response] from a raw http request according to rfc2616.
 */
fun String.toVauInnerHttpResponse(req: Request): Response =
    this.split("\r\n\r\n", limit = 2).let { rawHttp ->
        val rawHeader = rawHttp.first().split("\r\n").iterator()

        Response.Builder().apply {
            require(rawHeader.hasNext()) { "Response is empty!" }
            // status line
            this.parseResponseLine(rawHeader.next())

            // might be empty
            val headers = Headers.Builder().apply {
                rawHeader.forEachRemaining { headerLine ->
                    add(headerLine)
                }
            }.build()
            this.headers(headers)

            headers["Content-Type"]?.takeIf { rawHttp.size == 2 }?.let {
                this.body(rawHttp[1].toResponseBody(it.toMediaType()))
            } ?: this.body("".toResponseBody().apply { close() })

            this.request(req)
        }.build()
    }

fun ByteArray.toVauInnerHttpResponse(req: Request): Response =
    this.decodeToString().toVauInnerHttpResponse(req)
