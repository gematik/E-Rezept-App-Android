/*
 * Copyright 2025, gematik GmbH
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

@file:Suppress("ktlint:max-line-length", "MayBeConst")

package de.gematik.ti.erp.app.vau

import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeBase64
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom

val BCProvider = BouncyCastleProvider()

val TestCryptoConfig = object : VauCryptoConfig {
    override val provider = BCProvider
    override val random = SecureRandom()
}

fun x509PEMCertificateAsBase64(data: String) =
    data.removePrefix("-----BEGIN CERTIFICATE-----")
        .removeSuffix("-----END CERTIFICATE-----").replace("\n", "").trim()

fun x509Certificate(data: String) =
    X509CertificateHolder(x509PEMCertificateAsBase64(data).decodeBase64()!!.toByteArray())

fun base64X509Certificate(certInBase64: String) =
    X509CertificateHolder(certInBase64.decodeBase64()!!.toByteArray())

object TestCertificates {

    object Vau {
        val OID = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 2) // oid = 1.2.276.0.76.4.258

        const val Base64 =
            "MIIC7jCCApWgAwIBAgIHATwrYu8gtzAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMDEwMDcwMDAwMDBaFw0yNTA4MDcwMDAwMDBaMF4xCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDEnMCUGA1UEAwweRVJQIFJlZmVyZW56ZW50d2lja2x1bmcgRkQgRW5jMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABKYLzjl704qFX+oEuUOyLV70i2Bn2K4jekh/YOxExtdADB3X/q7fX/tVr09GtDRxe3h1yov9TwuHaHYh91RlyMejggEUMIIBEDAMBgNVHRMBAf8EAjAAMCEGA1UdIAQaMBgwCgYIKoIUAEwEgSMwCgYIKoIUAEwEgUowHQYDVR0OBBYEFK5+wVL9g8tGve6b1MdHK1xs62H7MDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAOBgNVHQ8BAf8EBAMCAwgwUwYFKyQIAwMESjBIMEYwRDBCMEAwMgwwRS1SZXplcHQgdmVydHJhdWVuc3fDvHJkaWdlIEF1c2bDvGhydW5nc3VtZ2VidW5nMAoGCCqCFABMBIICMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMAoGCCqGSM49BAMCA0cAMEQCIGZ20lLY2WEAGOTmNEFBB1EeU645fE0Iy2U9ypFHMlw4AiAVEP0HYut0Z8sKUk6WVanMmKXjfxO/qgQFzjsbq954dw=="
        val X509Certificate by lazy { base64X509Certificate(Base64) }

        const val SerialNumber = "347632017809591"

        // TODO second ca is ocsp response only; production ocsp uses same ca as vau/idp
        val JsonCertList = """
            {
                "add_roots": [],
                "ca_certs": [
                    "MIIDGjCCAr+gAwIBAgIBFzAKBggqhkjOPQQDAjCBgTELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxNDAyBgNVBAsMK1plbnRyYWxlIFJvb3QtQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxGzAZBgNVBAMMEkdFTS5SQ0EzIFRFU1QtT05MWTAeFw0xNzA4MzAxMTM2MjJaFw0yNTA4MjgxMTM2MjFaMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABDFinQgzfsT1CN0QWwdm7e2JiaDYHocCiy1TWpOPyHwoPC54RULeUIBJeX199Qm1FFpgeIRP1E8cjbHGNsRbju6jggEgMIIBHDAdBgNVHQ4EFgQUKPD45qnId8xDRduartc6g6wOD6gwHwYDVR0jBBgwFoAUB5AzLXVTXn/4yDe/fskmV2jfONIwQgYIKwYBBQUHAQEENjA0MDIGCCsGAQUFBzABhiZodHRwOi8vb2NzcC5yb290LWNhLnRpLWRpZW5zdGUuZGUvb2NzcDASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB/wQEAwIBBjAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMFsGA1UdEQRUMFKgUAYDVQQKoEkMR2dlbWF0aWsgR2VzZWxsc2NoYWZ0IGbDvHIgVGVsZW1hdGlrYW53ZW5kdW5nZW4gZGVyIEdlc3VuZGhlaXRza2FydGUgbWJIMAoGCCqGSM49BAMCA0kAMEYCIQCprLtIIRx1Y4mKHlNngOVAf6D7rkYSa723oRyX7J2qwgIhAKPi9GSJyYp4gMTFeZkqvj8pcAqxNR9UKV7UYBlHrdxC"
                ],
                "ee_certs": [
                    "MIIC7jCCApWgAwIBAgIHATwrYu8gtzAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMDEwMDcwMDAwMDBaFw0yNTA4MDcwMDAwMDBaMF4xCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDEnMCUGA1UEAwweRVJQIFJlZmVyZW56ZW50d2lja2x1bmcgRkQgRW5jMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABKYLzjl704qFX+oEuUOyLV70i2Bn2K4jekh/YOxExtdADB3X/q7fX/tVr09GtDRxe3h1yov9TwuHaHYh91RlyMejggEUMIIBEDAMBgNVHRMBAf8EAjAAMCEGA1UdIAQaMBgwCgYIKoIUAEwEgSMwCgYIKoIUAEwEgUowHQYDVR0OBBYEFK5+wVL9g8tGve6b1MdHK1xs62H7MDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAOBgNVHQ8BAf8EBAMCAwgwUwYFKyQIAwMESjBIMEYwRDBCMEAwMgwwRS1SZXplcHQgdmVydHJhdWVuc3fDvHJkaWdlIEF1c2bDvGhydW5nc3VtZ2VidW5nMAoGCCqCFABMBIICMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMAoGCCqGSM49BAMCA0cAMEQCIGZ20lLY2WEAGOTmNEFBB1EeU645fE0Iy2U9ypFHMlw4AiAVEP0HYut0Z8sKUk6WVanMmKXjfxO/qgQFzjsbq954dw==",
                    "MIICsTCCAligAwIBAgIHA61I5ACUjTAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMDA4MDQwMDAwMDBaFw0yNTA4MDQyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAxMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABJZQrG1NWxIB3kz/6Z2zojlkJqN3vJXZ3EZnJ6JXTXw5ZDFZ5XjwWmtgfomv3VOV7qzI5ycUSJysMWDEu3mqRcajge0wgeowHQYDVR0OBBYEFJ8DVLAZWT+BlojTD4MT/Na+ES8YMDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAMBgNVHRMBAf8EAjAAMCEGA1UdIAQaMBgwCgYIKoIUAEwEgUswCgYIKoIUAEwEgSMwHwYDVR0jBBgwFoAUKPD45qnId8xDRduartc6g6wOD6gwLQYFKyQIAwMEJDAiMCAwHjAcMBowDAwKSURQLURpZW5zdDAKBggqghQATASCBDAOBgNVHQ8BAf8EBAMCB4AwCgYIKoZIzj0EAwIDRwAwRAIgVBPhAwyX8HAVH0O0b3+VazpBAWkQNjkEVRkv+EYX1e8CIFdn4O+nivM+XVi9xiKK4dW1R7MD334OpOPTFjeEhIVV",
                    "MIICsTCCAligAwIBAgIHAbssqQhqOzAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMTAxMTUwMDAwMDBaFw0yNjAxMTUyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAzMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABIYZnwiGAn5QYOx43Z8MwaZLD3r/bz6BTcQO5pbeum6qQzYD5dDCcriw/VNPPZCQzXQPg4StWyy5OOq9TogBEmOjge0wgeowDgYDVR0PAQH/BAQDAgeAMC0GBSskCAMDBCQwIjAgMB4wHDAaMAwMCklEUC1EaWVuc3QwCgYIKoIUAEwEggQwIQYDVR0gBBowGDAKBggqghQATASBSzAKBggqghQATASBIzAfBgNVHSMEGDAWgBQo8Pjmqch3zENF25qu1zqDrA4PqDA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wHQYDVR0OBBYEFC94M9LgW44lNgoAbkPaomnLjS8/MAwGA1UdEwEB/wQCMAAwCgYIKoZIzj0EAwIDRwAwRAIgCg4yZDWmyBirgxzawz/S8DJnRFKtYU/YGNlRc7+kBHcCIBuzba3GspqSmoP1VwMeNNKNaLsgV8vMbDJb30aqaiX1"
                ]
            }
        """.trimIndent()

        val CertList: UntrustedCertList by lazy { Json.decodeFromString(JsonCertList) }

        val ValidTimestamp: Instant = Instant.fromEpochSeconds(1615368104) // 2021-03-10T09:21:44.000Z
        val ExpiredTimestamp: Instant =
            Instant.fromEpochSeconds(1899364896) // 2030-03-10T09:21:36.812Z
    }

    object Idp1 {
        val OID = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

        const val Base64 =
            "MIICsTCCAligAwIBAgIHA61I5ACUjTAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMDA4MDQwMDAwMDBaFw0yNTA4MDQyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAxMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABJZQrG1NWxIB3kz/6Z2zojlkJqN3vJXZ3EZnJ6JXTXw5ZDFZ5XjwWmtgfomv3VOV7qzI5ycUSJysMWDEu3mqRcajge0wgeowHQYDVR0OBBYEFJ8DVLAZWT+BlojTD4MT/Na+ES8YMDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAMBgNVHRMBAf8EAjAAMCEGA1UdIAQaMBgwCgYIKoIUAEwEgUswCgYIKoIUAEwEgSMwHwYDVR0jBBgwFoAUKPD45qnId8xDRduartc6g6wOD6gwLQYFKyQIAwMEJDAiMCAwHjAcMBowDAwKSURQLURpZW5zdDAKBggqghQATASCBDAOBgNVHQ8BAf8EBAMCB4AwCgYIKoZIzj0EAwIDRwAwRAIgVBPhAwyX8HAVH0O0b3+VazpBAWkQNjkEVRkv+EYX1e8CIFdn4O+nivM+XVi9xiKK4dW1R7MD334OpOPTFjeEhIVV"
        val X509Certificate by lazy { base64X509Certificate(Base64) }

        const val SerialNumber = "1034953504625805"
    }

    object Idp2 {
        val OID = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

        const val Base64 =
            "MIICsTCCAligAwIBAgIHAbssqQhqOzAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMTAxMTUwMDAwMDBaFw0yNjAxMTUyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1QtT05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAzMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABIYZnwiGAn5QYOx43Z8MwaZLD3r/bz6BTcQO5pbeum6qQzYD5dDCcriw/VNPPZCQzXQPg4StWyy5OOq9TogBEmOjge0wgeowDgYDVR0PAQH/BAQDAgeAMC0GBSskCAMDBCQwIjAgMB4wHDAaMAwMCklEUC1EaWVuc3QwCgYIKoIUAEwEggQwIQYDVR0gBBowGDAKBggqghQATASBSzAKBggqghQATASBIzAfBgNVHSMEGDAWgBQo8Pjmqch3zENF25qu1zqDrA4PqDA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wHQYDVR0OBBYEFC94M9LgW44lNgoAbkPaomnLjS8/MAwGA1UdEwEB/wQCMAAwCgYIKoZIzj0EAwIDRwAwRAIgCg4yZDWmyBirgxzawz/S8DJnRFKtYU/YGNlRc7+kBHcCIBuzba3GspqSmoP1VwMeNNKNaLsgV8vMbDJb30aqaiX1"
        val X509Certificate by lazy { base64X509Certificate(Base64) }

        const val SerialNumber = "487275465566779"
    }

    object Idp3 {
        val OID = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

        private val data = """
            -----BEGIN CERTIFICATE-----
            MIICsTCCAligAwIBAgIHA8OQFtdAtTAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMC
            REUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtv
            bXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQD
            DBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMTAxMTMwMDAwMDBaFw0yNjAx
            MTMyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1Qt
            T05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAyMFowFAYHKoZIzj0C
            AQYJKyQDAwIIAQEHA0IABEC6Sfy6RcfusiYbG+Drx8FNZIS574ojsGDr5n+XJSu8
            mHuknfNkoMmSbytt4br0YGihOixcmBKy80UfSLdXGe6jge0wgeowDgYDVR0PAQH/
            BAQDAgeAMC0GBSskCAMDBCQwIjAgMB4wHDAaMAwMCklEUC1EaWVuc3QwCgYIKoIU
            AEwEggQwIQYDVR0gBBowGDAKBggqghQATASBSzAKBggqghQATASBIzAfBgNVHSME
            GDAWgBQo8Pjmqch3zENF25qu1zqDrA4PqDA4BggrBgEFBQcBAQQsMCowKAYIKwYB
            BQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wHQYDVR0OBBYEFLM7
            Gd6tlX+bjswtS+tVxkbTwxC0MAwGA1UdEwEB/wQCMAAwCgYIKoZIzj0EAwIDRwAw
            RAIgfKKll8KtEPLdaUWwF7ftbEvkIdz9KXhL4cKRyozGQjECIDxby8TX2iWfwVhf
            HoxmpTf+D3eCRHhmnwJWcIgm1tF0
            -----END CERTIFICATE-----
        """.trimIndent()
        val Base64 by lazy { x509PEMCertificateAsBase64(data) }
        val X509Certificate by lazy { x509Certificate(data) }

        const val SerialNumber = "1059448556044469"
    }

    object Idp4 {
        val OID = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

        private val data = """
            -----BEGIN CERTIFICATE-----
            MIICsTCCAligAwIBAgIHAbssqQhqOzAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMC
            REUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtv
            bXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQD
            DBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMTAxMTUwMDAwMDBaFw0yNjAx
            MTUyMzU5NTlaMEkxCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1nZW1hdGlrIFRFU1Qt
            T05MWSAtIE5PVC1WQUxJRDESMBAGA1UEAwwJSURQIFNpZyAzMFowFAYHKoZIzj0C
            AQYJKyQDAwIIAQEHA0IABIYZnwiGAn5QYOx43Z8MwaZLD3r/bz6BTcQO5pbeum6q
            QzYD5dDCcriw/VNPPZCQzXQPg4StWyy5OOq9TogBEmOjge0wgeowDgYDVR0PAQH/
            BAQDAgeAMC0GBSskCAMDBCQwIjAgMB4wHDAaMAwMCklEUC1EaWVuc3QwCgYIKoIU
            AEwEggQwIQYDVR0gBBowGDAKBggqghQATASBSzAKBggqghQATASBIzAfBgNVHSME
            GDAWgBQo8Pjmqch3zENF25qu1zqDrA4PqDA4BggrBgEFBQcBAQQsMCowKAYIKwYB
            BQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wHQYDVR0OBBYEFC94
            M9LgW44lNgoAbkPaomnLjS8/MAwGA1UdEwEB/wQCMAAwCgYIKoZIzj0EAwIDRwAw
            RAIgCg4yZDWmyBirgxzawz/S8DJnRFKtYU/YGNlRc7+kBHcCIBuzba3GspqSmoP1
            VwMeNNKNaLsgV8vMbDJb30aqaiX1
            -----END CERTIFICATE-----
        """.trimIndent()
        val Base64 by lazy { x509PEMCertificateAsBase64(data) }
        val X509Certificate by lazy { x509Certificate(data) }
    }

    /**
     * First response of [OCSP].
     */
    object OCSP1 {
        const val Base64 =
            "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAxWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcDrUjkAJSNgAAYDzIwMjEwNTE3MDYyMzAxWqARGA8yMDIxMDUxNzA2MjMwMVqhIzAhMB8GCSsGAQUFBzABAgQSBBDkdyImUBsO+Q8iAA2xbXu8MAkGByqGSM49BAEDRwAwRAIgW+JlwUmnZCVsME2kOyQlcqF01Lel/0nQdE6IaZmFADECIGhOH1k5Dzq42y2jCxZCzxevRc6vY1o8ky0Xy4DxLIWJoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ"
        val ProducedAt = Instant.fromEpochSeconds(1621232581) // 2021-05-17T08:23:01.000+0200
        val CertToCheckSerialNumber = "1034953504625805" // IDP 1

        object SignerCert {
            val Base64 = "MIICmjCCAkCgAwIBAgIHA602RERCazAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTEwIFRFU1QtT05MWTAeFw0yMDA1MDYwMDAwMDBaFw0yMzA1MDYyMzU5NTlaMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkwWjAUBgcqhkjOPQIBBgkrJAMDAggBAQcDQgAEGwfkaELN0cr5DfqP1bNsWZS2XiuH6reLPZLHBSLkyFp/SzTKvNDdm7nKlp6Norg1z1njhyapRraaCzRS6VreD6OBxzCBxDAfBgNVHSMEGDAWgBQo8Pjmqch3zENF25qu1zqDrA4PqDAOBgNVHQ8BAf8EBAMCBkAwFQYDVR0gBA4wDDAKBggqghQATASBIzATBgNVHSUEDDAKBggrBgEFBQcDCTAMBgNVHRMBAf8EAjAAMDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAdBgNVHQ4EFgQUHKqJlrsbnD6bAIyF2WScrHtUmeIwCgYIKoZIzj0EAwIDSAAwRQIhAIkd0/4EtDLRRnb0B8mgmvlxepYrLKX/lkVGoXy0D64OAiAkOCmXOwGJExZxxRm4diJ/GPzZI4ecAnaVqnikYAQVCQ=="
            val X509Certificate by lazy { base64X509Certificate(Base64) }
        }
    }

    /**
     * Second response of [OCSP].
     */
    object OCSP2 {
        const val Base64 =
            "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAxWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcBuyypCGo7gAAYDzIwMjEwNTE3MDYyMzAxWqARGA8yMDIxMDUxNzA2MjMwMVqhIzAhMB8GCSsGAQUFBzABAgQSBBDIsivTG9WljP4InmqVdKQmMAkGByqGSM49BAEDRwAwRAIgZMCyRhqMOaEG10KPz3mL5Yh7oX9fiIdBl8WrxLT2SewCIEvjzedVlnbt/j4e7VALo2xl8wvOcYe8gT04+PqH5vkfoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ"
        val ProducedAt = Instant.fromEpochSeconds(1621232581) // 2021-05-17T08:23:01.000+0200
        val CertToCheckSerialNumber = "487275465566779" // IDP 2
    }

    /**
     * Third response of [OCSP].
     */
    object OCSP3 {
        const val Base64 =
            "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAwWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcBPCti7yC3gAAYDzIwMjEwNTE3MDYyMzAwWqARGA8yMDIxMDUxNzA2MjMwMFqhIzAhMB8GCSsGAQUFBzABAgQSBBAWpjYsPzj/U96/S1MvypTWMAkGByqGSM49BAEDRwAwRAIgXfEC3h/1H2/aHGEyJY9L59S6NbqdkStBBk2vczj+3mwCIASMGDqPuhA7ZLBJ5HhHpwKYEQw/YPluyBMnz7j2dXtPoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ"
        val ProducedAt = Instant.fromEpochSeconds(1621232580) // 2021-05-17T08:23:00.000+0200
        val CertToCheckSerialNumber = "347632017809591" //  VAU
    }

    /**
     * +- OCSP Response --------+
     * |                        |
     * | +-------------+        |  verify with  +--------------+  verify with  +--------------+
     * | | Certificate |--------|---------------| OCSP EE Cert |---------------| OCSP CA Cert |
     * | +-------------+        |               +--------------+               +--------------+
     * |                        |
     * | +- Single Response -+  |  equals  +--------------------+
     * | | Certificate ID    |--|----------| VAU/IDP CA Cert ID |
     * | +-------------------+  |          +--------------------+
     * |                        |
     * +------------------------+
     */
    object OCSP {
        @Suppress("MaxLineLength")
        val JsonOCSPList = """
            {
                "OCSP Responses": [
                    "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAxWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcDrUjkAJSNgAAYDzIwMjEwNTE3MDYyMzAxWqARGA8yMDIxMDUxNzA2MjMwMVqhIzAhMB8GCSsGAQUFBzABAgQSBBDkdyImUBsO+Q8iAA2xbXu8MAkGByqGSM49BAEDRwAwRAIgW+JlwUmnZCVsME2kOyQlcqF01Lel/0nQdE6IaZmFADECIGhOH1k5Dzq42y2jCxZCzxevRc6vY1o8ky0Xy4DxLIWJoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ",
                    "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAxWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcBuyypCGo7gAAYDzIwMjEwNTE3MDYyMzAxWqARGA8yMDIxMDUxNzA2MjMwMVqhIzAhMB8GCSsGAQUFBzABAgQSBBDIsivTG9WljP4InmqVdKQmMAkGByqGSM49BAEDRwAwRAIgZMCyRhqMOaEG10KPz3mL5Yh7oX9fiIdBl8WrxLT2SewCIEvjzedVlnbt/j4e7VALo2xl8wvOcYe8gT04+PqH5vkfoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ",
                    "MIIEFgoBAKCCBA8wggQLBgkrBgEFBQcwAQEEggP8MIID+DCB+6FZMFcxCzAJBgNVBAYTAkRFMRowGAYDVQQKDBFnZW1hdGlrIE5PVC1WQUxJRDEsMCoGA1UEAwwjT0NTUCBTaWduZXIgS29tcC1DQTEwIGVjYyBURVNULU9OTFkYDzIwMjEwNTE3MDYyMzAwWjBoMGYwPjAHBgUrDgMCGgQUXI3/vEvbfD7eUzpI1js5mj5OUC4EFCjw+OapyHfMQ0Xbmq7XOoOsDg+oAgcBPCti7yC3gAAYDzIwMjEwNTE3MDYyMzAwWqARGA8yMDIxMDUxNzA2MjMwMFqhIzAhMB8GCSsGAQUFBzABAgQSBBAWpjYsPzj/U96/S1MvypTWMAkGByqGSM49BAEDRwAwRAIgXfEC3h/1H2/aHGEyJY9L59S6NbqdkStBBk2vczj+3mwCIASMGDqPuhA7ZLBJ5HhHpwKYEQw/YPluyBMnz7j2dXtPoIICojCCAp4wggKaMIICQKADAgECAgcDrTZEREJrMAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMB4XDTIwMDUwNjAwMDAwMFoXDTIzMDUwNjIzNTk1OVowVzELMAkGA1UEBhMCREUxGjAYBgNVBAoMEWdlbWF0aWsgTk9ULVZBTElEMSwwKgYDVQQDDCNPQ1NQIFNpZ25lciBLb21wLUNBMTAgZWNjIFRFU1QtT05MWTBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQbB+RoQs3RyvkN+o/Vs2xZlLZeK4fqt4s9kscFIuTIWn9LNMq80N2bucqWno2iuDXPWeOHJqlGtpoLNFLpWt4Po4HHMIHEMB8GA1UdIwQYMBaAFCjw+OapyHfMQ0Xbmq7XOoOsDg+oMA4GA1UdDwEB/wQEAwIGQDAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMBMGA1UdJQQMMAoGCCsGAQUFBwMJMAwGA1UdEwEB/wQCMAAwOAYIKwYBBQUHAQEELDAqMCgGCCsGAQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMB0GA1UdDgQWBBQcqomWuxucPpsAjIXZZJyse1SZ4jAKBggqhkjOPQQDAgNIADBFAiEAiR3T/gS0MtFGdvQHyaCa+XF6lisspf+WRUahfLQPrg4CICQ4KZc7AYkTFnHFGbh2In8Y/Nkjh5wCdpWqeKRgBBUJ"
                ]
            }
        """.trimIndent()

        val OCSPList: UntrustedOCSPList by lazy { Json.decodeFromString(JsonOCSPList) }
    }

    object CA10 {
        private val data = """
            -----BEGIN CERTIFICATE-----
            MIIDGjCCAr+gAwIBAgIBFzAKBggqhkjOPQQDAjCBgTELMAkGA1UEBhMCREUxHzAd
            BgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxNDAyBgNVBAsMK1plbnRyYWxl
            IFJvb3QtQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxGzAZBgNVBAMMEkdF
            TS5SQ0EzIFRFU1QtT05MWTAeFw0xNzA4MzAxMTM2MjJaFw0yNTA4MjgxMTM2MjFa
            MIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJ
            RDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3Ry
            dWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTAgVEVTVC1PTkxZMFowFAYHKoZI
            zj0CAQYJKyQDAwIIAQEHA0IABDFinQgzfsT1CN0QWwdm7e2JiaDYHocCiy1TWpOP
            yHwoPC54RULeUIBJeX199Qm1FFpgeIRP1E8cjbHGNsRbju6jggEgMIIBHDAdBgNV
            HQ4EFgQUKPD45qnId8xDRduartc6g6wOD6gwHwYDVR0jBBgwFoAUB5AzLXVTXn/4
            yDe/fskmV2jfONIwQgYIKwYBBQUHAQEENjA0MDIGCCsGAQUFBzABhiZodHRwOi8v
            b2NzcC5yb290LWNhLnRpLWRpZW5zdGUuZGUvb2NzcDASBgNVHRMBAf8ECDAGAQH/
            AgEAMA4GA1UdDwEB/wQEAwIBBjAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMFsGA1Ud
            EQRUMFKgUAYDVQQKoEkMR2dlbWF0aWsgR2VzZWxsc2NoYWZ0IGbDvHIgVGVsZW1h
            dGlrYW53ZW5kdW5nZW4gZGVyIEdlc3VuZGhlaXRza2FydGUgbWJIMAoGCCqGSM49
            BAMCA0kAMEYCIQCprLtIIRx1Y4mKHlNngOVAf6D7rkYSa723oRyX7J2qwgIhAKPi
            9GSJyYp4gMTFeZkqvj8pcAqxNR9UKV7UYBlHrdxC
            -----END CERTIFICATE-----
        """.trimIndent()
        val Base64 by lazy { x509PEMCertificateAsBase64(data) }
        val X509Certificate by lazy { x509Certificate(data) }
    }

    object CA11 {
        private val data = """
            -----BEGIN CERTIFICATE-----
            MIIDGDCCAr+gAwIBAgIBFjAKBggqhkjOPQQDAjCBgTELMAkGA1UEBhMCREUxHzAd
            BgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxNDAyBgNVBAsMK1plbnRyYWxl
            IFJvb3QtQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxGzAZBgNVBAMMEkdF
            TS5SQ0EzIFRFU1QtT05MWTAeFw0xNzA4MzAxMTM2MDhaFw0yNTA4MjgxMTM2MDda
            MIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJ
            RDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3Ry
            dWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMTEgVEVTVC1PTkxZMFowFAYHKoZI
            zj0CAQYJKyQDAwIIAQEHA0IABGTK1hKec85JygijmQ2crZYDrpMqbxX73b9BUDQ/
            b/zHoa1Liq6icJKrlCFTjJ1J7EAaAxLsGG0N/XjxWSxlBIGjggEgMIIBHDAdBgNV
            HQ4EFgQUTBRlQvR625Hnyqqo6Q4ezy2L57owHwYDVR0jBBgwFoAUB5AzLXVTXn/4
            yDe/fskmV2jfONIwQgYIKwYBBQUHAQEENjA0MDIGCCsGAQUFBzABhiZodHRwOi8v
            b2NzcC5yb290LWNhLnRpLWRpZW5zdGUuZGUvb2NzcDASBgNVHRMBAf8ECDAGAQH/
            AgEAMA4GA1UdDwEB/wQEAwIBBjAVBgNVHSAEDjAMMAoGCCqCFABMBIEjMFsGA1Ud
            EQRUMFKgUAYDVQQKoEkMR2dlbWF0aWsgR2VzZWxsc2NoYWZ0IGbDvHIgVGVsZW1h
            dGlrYW53ZW5kdW5nZW4gZGVyIEdlc3VuZGhlaXRza2FydGUgbWJIMAoGCCqGSM49
            BAMCA0cAMEQCIHNbTALWZyWkNTfmVHlADw7lmjF/mPgk4cT0iIavuddAAiBqcZFt
            l2T02k5YDqltLug2EYy+naFfl3gEI+qCS7fsAg==
            -----END CERTIFICATE-----
        """.trimIndent()
        val Base64 by lazy { x509PEMCertificateAsBase64(data) }
        val X509Certificate by lazy { x509Certificate(data) }
    }

    object RCA3 {
        private val data = """
            -----BEGIN CERTIFICATE-----
            MIICkzCCAjmgAwIBAgIBATAKBggqhkjOPQQDAjCBgTELMAkGA1UEBhMCREUxHzAd
            BgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxNDAyBgNVBAsMK1plbnRyYWxl
            IFJvb3QtQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxGzAZBgNVBAMMEkdF
            TS5SQ0EzIFRFU1QtT05MWTAeFw0xNzA4MTEwODM4NDVaFw0yNzA4MDkwODM4NDVa
            MIGBMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJ
            RDE0MDIGA1UECwwrWmVudHJhbGUgUm9vdC1DQSBkZXIgVGVsZW1hdGlraW5mcmFz
            dHJ1a3R1cjEbMBkGA1UEAwwSR0VNLlJDQTMgVEVTVC1PTkxZMFowFAYHKoZIzj0C
            AQYJKyQDAwIIAQEHA0IABG+raY8OSxIEfrDwz4K4K1HXLXbd0ZzAKtD9SUDtSexn
            fsai8lkY8rM59TLky//HB8QDkyZewRPXClwpXCrj5HOjgZ4wgZswHQYDVR0OBBYE
            FAeQMy11U15/+Mg3v37JJldo3zjSMEIGCCsGAQUFBwEBBDYwNDAyBggrBgEFBQcw
            AYYmaHR0cDovL29jc3Aucm9vdC1jYS50aS1kaWVuc3RlLmRlL29jc3AwDwYDVR0T
            AQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwFQYDVR0gBA4wDDAKBggqghQATASB
            IzAKBggqhkjOPQQDAgNIADBFAiEAo4kNteSBVR4ovNeTBhkiSXsWzdRC0tQeMfIt
            sE0s7/8CIDZ3EQxclVBV3huM8Bzl9ePbNsV+Lvnjv+Fo1om5+xJ2
            -----END CERTIFICATE-----
        """.trimIndent()
        val Base64 by lazy { x509PEMCertificateAsBase64(data) }
        val X509Certificate by lazy { x509Certificate(data) }
    }
}

object TestCrypto {
    const val CertPublicKeyX = "8634212830dad457ca05305e6687134166b9c21a65ffebf555f4e75dfb048888"
    const val CertPublicKeyY = "66e4b6843624cbda43c97ea89968bc41fd53576f82c03efa7d601b9facac2b29"

    const val Message = "Hallo Test"

    const val EccPrivateKey = "5bbba34d47502bd588ed680dfa2309ca375eb7a35ddbbd67cc7f8b6b687a1c1d"
    const val EphemeralPublicKeyX =
        "754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f"
    const val EphemeralPublicKeyY =
        "9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf"

    const val IVBytes = "257db4604af8ae0dfced37ce"
    val CipherText =
        "01 754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f 9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf 257db4604af8ae0dfced37ce 86c2b491c7a8309e750b 4e6e307219863938c204dfe85502ee0a".replace(
            " ",
            ""
        )
}
