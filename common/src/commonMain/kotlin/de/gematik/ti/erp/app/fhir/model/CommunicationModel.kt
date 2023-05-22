/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import kotlinx.serialization.Serializable

@Serializable
data class CommunicationPayload(
    val version: String = "1",
    val supplyOptionsType: String,
    val name: String,
    val address: List<String>,
    val hint: String = "",
    val phone: String?
)

@Serializable
data class DirectCommunicationMessage(
    val version: String = "2",
    val supplyOptionsType: String,
    val name: String,
    val address: List<String>,
    val hint: String = "",
    val text: String?,
    val phone: String?,
    val mail: String?,
    val transactionID: String,
    val taskID: String,
    val accessCode: String
)
