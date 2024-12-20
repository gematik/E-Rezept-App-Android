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

package de.gematik.ti.erp.app.vau

import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.api.model.X509Serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import kotlin.test.Test

class AdapterTest {

    @Test
    fun `base64 to x509 certificate`() {
        assertEquals(
            TestCertificates.Vau.SerialNumber.toBigInteger(),
            Json.decodeFromString(X509Serializer, "\"${TestCertificates.Vau.Base64}\"").serialNumber
        )
    }

    @Test
    fun `parse json cert list`() {
        Json.decodeFromString<UntrustedCertList>(TestCertificates.Vau.JsonCertList).let {
            assertEquals(0, it.addRoots.size)
            assertEquals(1, it.caCerts.size)
            assertEquals(3, it.eeCerts.size)
        }
    }

    @Test
    fun `parse ocsp response list`() {
        assertEquals(3, Json.decodeFromString<UntrustedOCSPList>(TestCertificates.OCSP.JsonOCSPList).responses.size)
    }
}
