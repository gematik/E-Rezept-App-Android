/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import org.junit.Test
import kotlin.test.assertEquals

private val testCert = """
    MIIFUTCCBDmgAwIBAgIDQNF0MA0GCSqGSIb3DQEBCwUAMIGJMQswCQYDVQQGEwJE
    RTEVMBMGA1UECgwMRC1UUlVTVCBHbWJIMUgwRgYDVQQLDD9JbnN0aXR1dGlvbiBk
    ZXMgR2VzdW5kaGVpdHN3ZXNlbnMtQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0
    dXIxGTAXBgNVBAMMEEQtVHJ1c3QuU01DQi1DQTMwHhcNMjExMDExMDM0ODU0WhcN
    MjYwODE1MDcyOTMxWjBmMQswCQYDVQQGEwJERTEgMB4GA1UECgwXQmV0cmllYnNz
    dMOkdHRlIGdlbWF0aWsxIDAeBgNVBAUTFzEwLjgwMjc2MDAzMTExMDAwMDAwNTQy
    MRMwEQYDVQQDDApnZW1hdGlrMDA2MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB
    CgKCAQEAmtDDCfvOJL82smWeqCKa1azV3SpMHOhO2P+ot6Yi+DRqANl/0HyUO+b5
    VGatK1ugqONe9f0jfwUCPKxr33V5dmtJ4F4Ywbjv5rfYhMdTR1XMbrzoOwAFhdve
    0k42dXbW2NCr8TZLz7xlcKihRphuzGbnGa+XpJriaw7g6fNmdo27Ad4tNIpezqFQ
    WduRJMDnW+89bzOdicLmyKU2k6IK9Wpd8+TjQLtoG32IAxX/+auqf9wYZW9H7mGF
    BagPxLO7D8cWaaX0K3JtRfCCE2hS7iBd6EqGCeoGz9NFg6aMDLxSOTuEgriTOI/O
    WSXVpFyAp9amm6KUmdhKegQ0iSvS0wIDAQABo4IB4jCCAd4wHwYDVR0jBBgwFoAU
    xk6YSKNeL3M/yJih5vVHqDXIhTowcgYFKyQIAwMEaTBnpCYwJDELMAkGA1UEBhMC
    REUxFTATBgNVBAoMDGdlbWF0aWsgR21iSDA9MDswOTA3MBkMF0JldHJpZWJzc3TD
    pHR0ZSBnZW1hdGlrMAkGByqCFABMBDoTDzktMi41OC4wMDAwMDA0MDBEBggrBgEF
    BQcBAQQ4MDYwNAYIKwYBBQUHMAGGKGh0dHA6Ly9ELVRSVVNULVNNQ0ItQ0EzLm9j
    c3AuZC10cnVzdC5uZXQwUQYDVR0gBEowSDA7BggqghQATASBIzAvMC0GCCsGAQUF
    BwIBFiFodHRwOi8vd3d3LmdlbWF0aWsuZGUvZ28vcG9saWNpZXMwCQYHKoIUAEwE
    TDBxBgNVHR8EajBoMGagZKBihmBsZGFwOi8vZGlyZWN0b3J5LmQtdHJ1c3QubmV0
    L0NOPUQtVHJ1c3QuU01DQi1DQTMsTz1ELVRSVVNUJTIwR21iSCxDPURFP2NlcnRp
    ZmljYXRlcmV2b2NhdGlvbmxpc3QwHQYDVR0OBBYEFO4u6BXelEMIzPzPE3Dr+mYU
    Eto/MA4GA1UdDwEB/wQEAwIEMDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUA
    A4IBAQDVUgAkYpXjjeUJbj2fWEXcgiFC0xEk0yAwmY9jK6An0fT+cRC/quTdZx81
    BR0qt77ROBJ3Sw5CH5+Ai4bjfIsmPOtIFV3qlYWgkldXhUfNHO+pLtdSnlhr7q4M
    pAoX8pyHrLyMPubJwBSeEHoY6yrW8bm1Pmo3NY/haOGEtuu6oS4hOqUD7kGHFsVp
    xYQY3gSzVzSv8B2d/pQ6zt6PU2nAYPV+JmRGBXGKPL8ncvZuQK0UsuMpNW0Q7sP6
    YDxLibjz3631dSjPs5MxIinKVxRPPSm357w8ekTs89oWshDGMuY8Oz7pu4taFHpE
    3xlzYXhnic0Bj61g6O9YFjcL43No
""".trimIndent().replace("\n", "")

class PharmacyDirectCommunicationTest {
    @Test
    fun `filter by RSA algorithm`() {
        assertEquals(1, listOf(X509CertificateHolder(Base64.decode(testCert))).filterByRSAPublicKey().size)
    }

    @Test
    fun `extract telematik id`() {
        val cert = X509CertificateHolder(Base64.decode(testCert))

        assertEquals("9-2.58.00000040", cert.extractTelematikId())
    }
}
