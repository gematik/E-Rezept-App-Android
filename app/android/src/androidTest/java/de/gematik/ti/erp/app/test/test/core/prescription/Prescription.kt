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
data class Prescription(
    @SerialName("accessCode")
    val accessCode: String? = null,
    @SerialName("authoredOn")
    val authoredOn: Long? = null,
    @SerialName("authoredOnFormatted")
    val authoredOnFormatted: String? = null,
    @SerialName("coverage")
    val coverage: Coverage? = null,
    @SerialName("medication")
    val medication: Medication? = null,
    @SerialName("patient")
    val patient: Patient? = null,
    @SerialName("practitioner")
    val practitioner: Practitioner? = null,
    @SerialName("prescriptionId")
    val prescriptionId: String? = null,
    @SerialName("taskId")
    val taskId: String = ""
)
