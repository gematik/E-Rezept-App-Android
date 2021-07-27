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

package de.gematik.ti.erp.app.pharmacy.repository.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommunicationPayload(
    val version: String = "1",
    val supplyOptionsType: String,
    val name: String,
    val address: Array<String>,
    val hint: String = "",
    val phone: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommunicationPayload

        if (version != other.version) return false
        if (supplyOptionsType != other.supplyOptionsType) return false
        if (name != other.name) return false
        if (!address.contentEquals(other.address)) return false
        if (hint != other.hint) return false
        if (phone != other.phone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + supplyOptionsType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + address.contentHashCode()
        result = 31 * result + hint.hashCode()
        result = 31 * result + phone.hashCode()
        return result
    }
}
