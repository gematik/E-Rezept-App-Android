/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.db.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList

@ProvidedTypeConverter
class TruststoreConverter(moshi: Moshi) {
    private val adapterCerts = moshi.adapter(UntrustedCertList::class.java)
    private val adapterOCSP = moshi.adapter(UntrustedOCSPList::class.java)

    @TypeConverter
    fun fromUntrustedCertList(certList: UntrustedCertList?): String? {
        return adapterCerts.toJson(certList)
    }

    @TypeConverter
    fun toUntrustedCertList(certList: String?): UntrustedCertList? {
        return certList?.let { adapterCerts.fromJson(it) }
    }

    @TypeConverter
    fun fromUntrustedOCSPList(certList: UntrustedOCSPList?): String? {
        return adapterOCSP.toJson(certList)
    }

    @TypeConverter
    fun toUntrustedOCSPList(ocspList: String?): UntrustedOCSPList? {
        return ocspList?.let { adapterOCSP.fromJson(it) }
    }
}
