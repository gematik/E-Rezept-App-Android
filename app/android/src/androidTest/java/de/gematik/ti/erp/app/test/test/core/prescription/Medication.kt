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
data class Medication(
    @SerialName("amount")
    val amount: Int? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("dosage")
    val dosage: String? = null,
    @SerialName("freeText")
    val freeText: String? = null,
    @SerialName("ingredient")
    val ingredient: String? = null,
    @SerialName("ingredientStrength")
    val ingredientStrength: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("packageQuantity")
    val packageQuantity: Int? = null,
    @SerialName("pzn")
    val pzn: String? = null,
    @SerialName("standardSize")
    val standardSize: String? = null,
    @SerialName("substitutionAllowed")
    val substitutionAllowed: Boolean? = null,
    @SerialName("supplyForm")
    val supplyForm: String? = null,
    @SerialName("type")
    val type: String? = null
)
