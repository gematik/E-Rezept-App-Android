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

package de.gematik.ti.erp.app.test.test.core.prescription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Practitioner(
    @SerialName("bsnr")
    val bsnr: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("hba")
    val hba: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("lanr")
    val lanr: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("officeName")
    val officeName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("postal")
    val postal: String? = null,
    @SerialName("smcb")
    val smcb: String? = null,
    @SerialName("street")
    val street: String? = null,
    @SerialName("ti")
    val telematik: Telematik? = null,
    @SerialName("type")
    val type: String? = null
)
