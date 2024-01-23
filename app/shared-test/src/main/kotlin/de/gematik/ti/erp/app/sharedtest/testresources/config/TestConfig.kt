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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.sharedtest.testresources.config

object TestConfig {
    const val WeakPassword = "TrustNo1"
    const val StrongPassword = "Jaja Ding Dong!"
    const val DefaultProfileName = "Rainer Reizdarm"
    const val DefaultEGKCAN = "123123"
    const val DefaultEGKPassword = "123456"
    const val WaitTimeout1MilliSec = 100L
    const val WaitTimeout1Sec = 1_000L
    const val ScreenChangeTimeout = 3_000L
    const val WaitTimeout5Sec = 5_000L
    const val LoadPrescriptionsTimeout = 20_000L

    const val AppDefaultVirtualEgkKvnr = "X764228532"
    const val PharmacyName = "Apotheke am Flughafen - E2E-Test"
    const val PharmacyTelematikId = "3-SMC-B-Testkarte-883110000116873"

    const val PharmacyZoti = "ZoTI"
    const val PharmacyZoti01 = "ZoTI_01_TEST-ONLY"
    const val PharmacyZoti02 = "ZoTI_02_TEST-ONLY"
    const val PharmacyZoti03 = "ZoTI_03_TEST-ONLY"
    const val PharmacyZoti04 = "ZoTI_04_TEST-ONLY"
    const val PharmacyZoti05 = "ZoTI_05_TEST-ONLY"
    const val PharmacyZoti06 = "ZoTI_06_TEST-ONLY"
    const val PharmacyZoti07 = "ZoTI_07_TEST-ONLY"
    const val PharmacyZoti08 = "ZoTI_08_TEST-ONLY"
    const val PharmacyZoti09 = "ZoTI_09_TEST-ONLY"
    const val PharmacyZoti10 = "ZoTI_10_TEST-ONLY"
    const val PharmacyZoti11 = "ZoTI_11_TEST-ONLY"
    const val PharmacyZoti12 = "ZoTI_12_TEST-ONLY"
    const val PharmacyZoti13 = "ZoTI_13_TEST-ONLY"
    const val PharmacyZoti14 = "ZoTI_14_TEST-ONLY"
    const val PharmacyZoti15 = "ZoTI_15_TEST-ONLY"
    const val PharmacyZoti16 = "ZoTI_16_TEST-ONLY"
    const val PharmacyZoti17 = "ZoTI_17_TEST-ONLY"
    const val PharmacyZoti18 = "ZoTI_18_TEST-ONLY"
    const val PharmacyZoti19 = "ZoTI_19_TEST-ONLY"
    const val PharmacyZoti20 = "ZoTI_20_TEST-ONLY"

    object FD {
        const val DefaultServer = "https://erpps-test.dev.gematik.solutions"
        const val DefaultDoctor = "9a15b6f9f4b8f2e9df1db745a4091bbd"
        const val DefaultPharmacy = "886c6eda7dd5a1c6b1d112907f544d3"
    }
}

interface VirtualEgk {
    val certificate: String
    val privateKey: String
    val kvnr: String
    val name: String
}

object VirtualEgk1 : VirtualEgk {
    @Suppress("MaxLineLength")
    override val certificate =
        "MIIDXTCCAwSgAwIBAgIHAs9vZEwB8jAKBggqhkjOPQQDAjCBljELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxRTBDBgNVBAsMPEVsZWt0cm9uaXNjaGUgR2VzdW5kaGVpdHNrYXJ0ZS1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEfMB0GA1UEAwwWR0VNLkVHSy1DQTEwIFRFU1QtT05MWTAeFw0yMjAxMjQwMDAwMDBaFw0yNzAxMjMyMzU5NTlaMIHgMQswCQYDVQQGEwJERTEpMCcGA1UECgwgZ2VtYXRpayBNdXN0ZXJrYXNzZTFHS1ZOT1QtVkFMSUQxEjAQBgNVBAsMCTk5OTU2Nzg5MDETMBEGA1UECwwKWDExMDU5Mjk3MTEUMBIGA1UEBAwLVsOzcm13aW5rZWwxHDAaBgNVBCoME1hlbmlhIFZlcmEgQWRlbGhlaWQxEjAQBgNVBAwMCVByb2YuIERyLjE1MDMGA1UEAwwsUHJvZi4gRHIuIFhlbmlhIFZlcmEgQS4gVsOzcm13aW5rZWxURVNULU9OTFkwWjAUBgcqhkjOPQIBBgkrJAMDAggBAQcDQgAEczsMfajcnKpGYyNeXUhODjyrX4z4j9Qzio/Ulq5COPVySk0CxYBDj+1VEd5FalhEJXC9HjVRCflQx+RkEQFbvqOB7zCB7DAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFESxTAFYVB7c2Te+5LI/Km6kXIkdMCAGA1UdIAQZMBcwCgYIKoIUAEwEgSMwCQYHKoIUAEwERjAwBgUrJAgDAwQnMCUwIzAhMB8wHTAQDA5WZXJzaWNoZXJ0ZS8tcjAJBgcqghQATAQxMB0GA1UdDgQWBBTCDfBZ8X30CZnFk7E2x8+lMM5uODA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wDgYDVR0PAQH/BAQDAgeAMAoGCCqGSM49BAMCA0cAMEQCIDDAXcyOKDYOZpoH0iYijr1yisyxHeT3ch6XZlFNXPrKAiAHepW4TOQAoqyoGG9Pgly0TO2tTB7WLKEc7B3F6lNhpA=="
    override val privateKey = "AJzshqeIuhwReqZpWbqY0PnRjTdTRzk4Zj9GpSxcUukA"
    override val kvnr = "X110592971"
    override val name = "Vórmwinkel Xenia Vera Adelheid"
}

object VirtualEgkWithPrescription : VirtualEgk {
    @Suppress("MaxLineLength")
    override val certificate =
        "MIIDLTCCAtSgAwIBAgIHAZ/zfVKUfTAKBggqhkjOPQQDAjCBljELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxRTBDBgNVBAsMPEVsZWt0cm9uaXNjaGUgR2VzdW5kaGVpdHNrYXJ0ZS1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEfMB0GA1UEAwwWR0VNLkVHSy1DQTEwIFRFU1QtT05MWTAeFw0yMjAyMDQwMDAwMDBaFw0yNzAyMDMyMzU5NTlaMIGwMQswCQYDVQQGEwJERTEpMCcGA1UECgwgZ2VtYXRpayBNdXN0ZXJrYXNzZTFHS1ZOT1QtVkFMSUQxEjAQBgNVBAsMCTk5OTU2Nzg5MDETMBEGA1UECwwKWDExMDUzNTU0MTEOMAwGA1UEBAwFS2zDtm4xDTALBgNVBCoMBEx1Y2ExDDAKBgNVBAwMA0RyLjEgMB4GA1UEAwwXRHIuIEx1Y2EgS2zDtm5URVNULU9OTFkwWjAUBgcqhkjOPQIBBgkrJAMDAggBAQcDQgAETn/MKYxsnBH9khicaXG3mFc5v4RoL0ILuJ3TreTsiFsv91OA6Yj/O4EAxm6dCpPtGgWRyVUYbOgDkaDurSUPpqOB7zCB7DAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFESxTAFYVB7c2Te+5LI/Km6kXIkdMCAGA1UdIAQZMBcwCgYIKoIUAEwEgSMwCQYHKoIUAEwERjAwBgUrJAgDAwQnMCUwIzAhMB8wHTAQDA5WZXJzaWNoZXJ0ZS8tcjAJBgcqghQATAQxMB0GA1UdDgQWBBRhIkfxtBhE+Z3fcu+OWu/3gnnYqjA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wDgYDVR0PAQH/BAQDAgeAMAoGCCqGSM49BAMCA0cAMEQCIGHDnSVg2A9NmFPhtzo4dL3CVbN94k3NrYhXLOZoCUFXAiBlE6TfW6uL91jhv8JuupHhr7X6B9YcbVizWoMxo1grFA=="
    override val privateKey = "cv2z1KGMJi+M5foz3GCz0bi5pSdBIjVTqw2cUuIsJcY="
    override val kvnr = "X110535541"
    override val name = "Klön Luca"
}
