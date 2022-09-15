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

package de.gematik.ti.erp.app.vau.api.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.OCSPResp
import org.bouncycastle.util.encoders.Base64

class OCSPAdapter {
    @FromJson
    fun fromJson(ocspRespAsBase64: String): OCSPResp {
        val bytes = Base64.decode(ocspRespAsBase64)
        return OCSPResp(bytes)
    }

    @ToJson
    fun toJson(writer: JsonWriter, ocspResp: OCSPResp) {
        writer.jsonValue(Base64.toBase64String(ocspResp.encoded!!))
    }
}

class X509Adapter {
    @FromJson
    fun fromJson(x509AsBase64: String): X509CertificateHolder {
        val x509Bytes = Base64.decode(x509AsBase64)
        return X509CertificateHolder(x509Bytes)
    }

    @ToJson
    fun toJson(writer: JsonWriter, cert: X509CertificateHolder) {
        writer.jsonValue(Base64.toBase64String(cert.encoded!!))
    }
}

class X509ArrayAdapter {
    @FromJson
    fun fromJson(x509AsBase64: Array<String>): X509CertificateHolder {
        val x509Bytes = Base64.decode(x509AsBase64[0])
        return X509CertificateHolder(x509Bytes)
    }

    @ToJson
    fun toJson(writer: JsonWriter, cert: X509CertificateHolder) {
        writer.jsonValue(Base64.toBase64String(cert.encoded!!))
    }
}
