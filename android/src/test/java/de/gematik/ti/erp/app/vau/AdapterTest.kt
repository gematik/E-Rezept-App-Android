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

package de.gematik.ti.erp.app.vau

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import org.junit.Assert.assertEquals
import org.junit.Test

class AdapterTest {

    @Test
    fun `base64 to x509 certificate`() {
        X509Adapter().fromJson(TestCertificates.Vau.Base64).let {
            assertEquals(
                TestCertificates.Vau.SerialNumber.toBigInteger(),
                it.serialNumber
            )
        }
    }

    @Test
    fun `parse json cert list`() {
        Moshi.Builder().add(X509Adapter()).build().adapter(UntrustedCertList::class.java)
            .fromJson(TestCertificates.Vau.JsonCertList)!!.let {
            assertEquals(0, it.addRoots.size)
            assertEquals(1, it.caCerts.size)
            assertEquals(3, it.eeCerts.size)
        }
    }

    @Test
    fun `parse ocsp response list`() {
        Moshi.Builder().add(OCSPAdapter()).build().adapter(UntrustedOCSPList::class.java)
            .fromJson(TestCertificates.OCSPList.JsonOCSPList)!!.let {
            assertEquals(3, it.responses.size)
        }
    }
}
