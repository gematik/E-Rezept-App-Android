/*
 * Copyright (c) 2024 gematik GmbH
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

import io.mockk.mockk
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ClientCryptoTest {
    // util tests
    @Test
    fun `byte array encoded to lower case hex`() {
        assertContentEquals(
            "Hello test".toByteArray(),
            "Hello test".toByteArray().toLowerCaseHex().decodeToString().decodeHex().toByteArray()
        )

        assertContentEquals(
            byteArrayOf(-20, 10, 120, 0, -127),
            byteArrayOf(-20, 10, 120, 0, -127).toLowerCaseHex().decodeToString().decodeHex().toByteArray()
        )
    }

    // http conversion tests

    @Test
    fun `from raw http - normal`() {
        val http200 = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/fhir+json;charset=utf-8\r\n" +
            "X-Header: abc\r\n" +
            "Y-Header: 12345\r\n" +
            "\r\n" +
            "Some Content"

        http200.toVauInnerHttpResponse(mockk()).let {
            assertEquals(Protocol.HTTP_1_1, it.protocol)
            assertEquals(200, it.code)
            assertEquals("abc", it.headers["X-Header"])
            assertEquals("12345", it.headers["Y-Header"])
            assertEquals("application/fhir+json;charset=utf-8", it.body!!.contentType().toString())
            assertEquals("Some Content", it.body!!.string())
        }

        val http400 = "HTTP/1.1 400 NOT FOUND\r\n" +
            "Content-Type: application/fhir+json;charset=utf-8\r\n" +
            "X-Header: abc\r\n" +
            "Y-Header: 12345\r\n" +
            "\r\n" +
            "Some Content"

        http400.toVauInnerHttpResponse(mockk()).let {
            assertEquals(Protocol.HTTP_1_1, it.protocol)
            assertEquals(400, it.code)
            assertEquals("abc", it.headers["X-Header"])
            assertEquals("12345", it.headers["Y-Header"])
            assertEquals("application/fhir+json;charset=utf-8", it.body!!.contentType().toString())
            assertEquals("Some Content", it.body!!.string())
        }
    }

    @Test(expected = Exception::class)
    fun `from raw http - with invalid response line should fail`() {
        val http = "HTTP/1.1 200\r\n" +
            "Content-Type: application/fhir+json;charset=utf-8\r\n" +
            "X-Header: abc\r\n" +
            "Y-Header: 12345\r\n" +
            "\r\n" +
            "Some Content"
        http.toVauInnerHttpResponse(mockk())
    }

    @Test
    fun `from raw http - with missing content`() {
        val http = "HTTP/1.1 200 OK\r\n" +
            "X-Header: abc\r\n" +
            "Y-Header: 12345\r\n" +
            "\r\n"

        http.toVauInnerHttpResponse(mockk()).let {
            assertEquals(Protocol.HTTP_1_1, it.protocol)
            assertEquals(200, it.code)
            assertEquals("abc", it.headers["X-Header"])
            assertEquals("12345", it.headers["Y-Header"])
            assertEquals(0L, it.body?.contentLength())
        }
    }

    @Test(expected = Exception::class)
    fun `from raw http - with missing double CR LF at end should fail`() {
        val http = "HTTP/1.1 200\r\n" +
            "X-Header: abc\r\n" +
            "Y-Header: 12345\r\n"
        http.toVauInnerHttpResponse(mockk())
    }

    @Test
    fun `to raw http - normal`() {
        val expectedHttp = "GET /Task HTTP/1.1\r\n" +
            "Host: abc.def\r\n" +
            "Abc: 123\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n"

        val r = Request.Builder()
            .url("http://abc.def/Task")
            .get()
            .header("Abc", "123")
            .build()
            .toRawVauInnerHttpRequest("http://abc.def/".toHttpUrl()).decodeToString()

        assertEquals(expectedHttp, r)
    }

    @Test
    fun `to raw http - with content`() {
        Request.Builder()
            .url("http://abc.def/Task/hello")
            .post("Something".toRequestBody("application/json".toMediaType()))
            .build()
            .toRawVauInnerHttpRequest("http://abc.def/".toHttpUrl()).decodeToString().let {
                it.split("\r\n").let {
                    assertEquals(5, it.size)
                    assertArrayEquals(
                        arrayOf(
                            "POST /Task/hello HTTP/1.1",
                            "Host: abc.def",
                            "Content-Length: 9",
                            "",
                            "Something"
                        ),
                        it.toTypedArray()
                    )
                }
            }
    }

    @Test
    fun `to raw http - with prefixed base uri`() {
        Request.Builder()
            .url("http://abc.def/123/Task/hello")
            .post("Something".toRequestBody("application/json".toMediaType()))
            .build()
            .toRawVauInnerHttpRequest("http://abc.def/123/".toHttpUrl()).decodeToString().let {
                it.split("\r\n").let {
                    assertEquals(5, it.size)
                    assertArrayEquals(
                        arrayOf(
                            "POST /Task/hello HTTP/1.1",
                            "Host: abc.def",
                            "Content-Length: 9",
                            "",
                            "Something"
                        ),
                        it.toTypedArray()
                    )
                }
            }
    }

    @Test(expected = Exception::class)
    fun `to raw http - with prefixed base uri missing trailing slash should throw exception`() {
        Request.Builder()
            .url("http://abc.def/123/Task/hello")
            .post("Something".toRequestBody("application/json".toMediaType()))
            .build()
            .toRawVauInnerHttpRequest("http://abc.def/123".toHttpUrl())
    }

    // encryption and decryption

    @Test
    fun `raw encryption and decryption`() {
        val vauKeyPair = KeyPairGenerator.getInstance("EC", BCProvider)
            .apply { initialize(ECGenParameterSpec("brainpoolP256r1")) }
            .generateKeyPair()

        //
        // client
        //

        val innerHttp = """
                    Test 123
        """.trimIndent()

        val bearer = "0123456789"

        val encryptionData = VauChannelSpec.V1.encryptRawVauRequest(
            innerHttp = innerHttp.toByteArray(),
            bearer = bearer.toByteArray(),
            publicKey = vauKeyPair.public as ECPublicKey,
            cryptoConfig = TestCryptoConfig
        )

        //
        // server
        //

        // decrypt the request with the VAU private key
        val clearTextReq =
            Ecies.decrypt(vauKeyPair, VauEciesSpec.V1, encryptionData.payload, cryptoConfig = TestCryptoConfig)
                .decodeToString()

        assertTrue(clearTextReq.matches("""1 $bearer [a-f0-9]{32} [a-f0-9]{32} $innerHttp""".toRegex()))

        // extract both keys
        val keys = clearTextReq.split(" ")

        val reqId = keys[2].toByteArray()
        val symKey = keys[3].decodeHex().toByteArray()

        assertArrayEquals(encryptionData.requestIdHex, reqId)
        assertArrayEquals(encryptionData.decryptionKey.encoded, symKey)

        val responseInnerHttp = """
                Abc 321
        """.trimIndent()

        val vauRawResp = AesGcm.encrypt(
            SecretKeySpec(symKey, "AES"),
            VauAesGcmSpec.V1,
            responseInnerHttp.toByteArray(),
            cryptoConfig = TestCryptoConfig
        )

        val clearTextResp = VauChannelSpec.V1.decryptRawVauResponse(
            encryptedInnerHttp = vauRawResp,
            decryptionKey = encryptionData.decryptionKey,
            cryptoConfig = TestCryptoConfig
        )
        assertArrayEquals(responseInnerHttp.toByteArray(), clearTextResp)
    }

    @Test
    fun `request encryption and response decryption`() {
        val vauKeyPair = KeyPairGenerator.getInstance("EC", BCProvider)
            .apply { initialize(ECGenParameterSpec("brainpoolP256r1")) }
            .generateKeyPair()

        //
        // client
        //

        val innerHttpRequest = Request.Builder()
            .post("123ABC".toRequestBody("application/octet-stream".toMediaType()))
            .url("https://vau.xyz/Task/p?something=123")
            .addHeader("Authorization", "Bearer 0123456789")
            .build()

        val (outerRequest, rawInnerRequest) = VauChannelSpec.V1.encryptHttpRequest(
            innerHttpRequest,
            userpseudonym = "0",
            publicKey = vauKeyPair.public as ECPublicKey,
            baseUrl = "https://vau.xyz/".toHttpUrl(),
            cryptoConfig = TestCryptoConfig
        )

        outerRequest.let {
            assertEquals("https://vau.xyz/VAU/0", it.url.toString())
            assertEquals("POST", it.method)
        }

        // extract the payload of the VAU request
        val innerEncryptedPayload = Buffer().apply {
            outerRequest.body!!.writeTo(this)
        }.inputStream().readBytes()

        //
        // server
        //

        val innerDecryptedPayload = Ecies.decrypt(vauKeyPair, VauEciesSpec.V1, innerEncryptedPayload, TestCryptoConfig)
        val split = innerDecryptedPayload.toString(Charsets.UTF_8).split("\r\n")

        split.let {
            assertEquals(6, it.size)
            assertTrue(
                it[0].matches(
                    """1 0123456789 [a-f0-9]{32} [a-f0-9]{32} POST /Task/p\?something=123 HTTP/1.1""".toRegex()
                )
            )
            assertEquals("Host: vau.xyz", it[1])
            assertEquals("Authorization: Bearer 0123456789", it[2])
            assertEquals("Content-Length: 6", it[3])
            assertEquals("", it[4])
            assertEquals("123ABC", it[5])
        }

        // extract both keys
        val keys = split[0].split(" ")

        val reqId = keys[2]
        val symKey = keys[3].decodeHex().toByteArray()

        val responseBody = "1 $reqId HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/fhir+json;charset=utf-8\r\n" +
            "Content-Location: https://prescriptionserver.telematik/Bundle/f5ba6eaf-9052-42f6-ac4e-fadceed7293b\r\n" +
            "\r\n" +
            "Some Content"

        val encryptedResponseFromServer = AesGcm.encrypt(
            SecretKeySpec(symKey, "AES"),
            VauAesGcmSpec.V1,
            responseBody.toByteArray(),
            TestCryptoConfig
        )

        val response = Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .message("")
            .request(innerHttpRequest)
            .code(200)
            .header("Userpseudonym", "270810c79748768a9b0aefbf52c8d72be7ad5e0d2d328d9bb70dbf58623fc7ae")
            .body(encryptedResponseFromServer.toResponseBody("application/octet-stream".toMediaType()))
            .build()

        //
        // client
        //

        VauChannelSpec.V1.decryptHttpResponse(
            response,
            outerRequest,
            rawInnerRequest,
            cryptoConfig = TestCryptoConfig
        )
            .let {
                assertEquals("270810c79748768a9b0aefbf52c8d72be7ad5e0d2d328d9bb70dbf58623fc7ae", it.second)
                assertEquals(Protocol.HTTP_1_1, it.first.protocol)
                assertEquals(200, it.first.code)
                assertEquals(
                    "https://prescriptionserver.telematik/Bundle/f5ba6eaf-9052-42f6-ac4e-fadceed7293b",
                    it.first.headers["Content-Location"]
                )
                assertEquals("application/fhir+json;charset=utf-8", it.first.body!!.contentType().toString())
                assertEquals("Some Content", it.first.body!!.string())
            }
    }
}
